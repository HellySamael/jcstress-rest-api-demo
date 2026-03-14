package com.example.demo.counter.distributated;

import com.example.demo.counter.core.PizzaCounter;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ✅ SafeDbCounter — thread-safe DB-backed counter, 100% SQL solution.
 *
 * Uses PostgreSQL via {@link DataSourceFactory}.
 *
 * Two construction modes:
 *   - SafeDbCounter()           — JCStress mode: creates a UUID-suffixed table per
 *                                  instance so workers never share state.
 *   - SafeDbCounter("pizza_votes") — Server mode: uses a fixed shared table.
 *
 * Concurrency strategy — purely at the SQL level, no Java locks:
 *   1. resetVotes() pre-seeds all known pizza rows at 0 before actors run.
 *   2. SELECT FOR UPDATE — acquires an exclusive pessimistic row lock.
 *      Any concurrent transaction touching the same row BLOCKS until COMMIT.
 *   3. UPDATE SET votes = votes + 1 — atomic increment under the lock.
 *   4. SELECT votes — read back our value (lock still held).
 *   5. COMMIT — release the lock.
 */
public final class SafeDbCounter implements PizzaCounter {

    private static final HikariDataSource DATA_SOURCE = DataSourceFactory.SERVER_DATA_SOURCE;

    /**
     * Pizza names matching the 01-init.sql seed script.
     * Pre-seeded by resetVotes() before every JCStress iteration so that
     * SELECT FOR UPDATE always finds an existing row to lock.
     */
    static final List<String> KNOWN_PIZZAS = List.of(
            "item1", "item2",
            "margherita", "pepperoni", "funghi", "quattro");

    private final String table;

    /**
     * JCStress constructor — creates a fresh UUID-suffixed table.
     * Each @State instance gets its own isolated table; no cross-contamination.
     */
    public SafeDbCounter() {
        this.table = "pizza_votes_" + UUID.randomUUID().toString().replace("-", "_");
        init();
    }

    /**
     * Server constructor — uses a fixed, pre-existing table.
     * The table must already exist (created by 01-init.sql or initTable()).
     */
    public SafeDbCounter(String table) {
        this.table = table;
    }

    /** Creates the per-instance UUID table. Called only by the JCStress constructor. */
    private void init() {
        try (Connection conn = DATA_SOURCE.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE " + table
                    + " (pizza VARCHAR(255) PRIMARY KEY, votes INT NOT NULL DEFAULT 0)");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to init table " + table, e);
        }
    }

    /**
     * Resets counters to 0 and pre-seeds all known pizza rows.
     * Called by @Before in the JCStress test before every iteration.
     *
     * Pre-seeding is critical: SELECT FOR UPDATE can only lock an existing row.
     * By inserting all rows here (before actors run), vote() never needs to
     * INSERT in the hot path — only SELECT FOR UPDATE + UPDATE.
     */
    @Override
    public void resetVotes() {
        try (Connection conn = DATA_SOURCE.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM " + table);
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO " + table + " (pizza, votes) VALUES (?, 0)")) {
                for (String pizza : KNOWN_PIZZAS) {
                    insert.setString(1, pizza);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Atomically increments the vote counter and returns the new value.
     *
     * 100% SQL — no Java synchronization, no try/catch concurrency tricks.
     *
     * Step 1 — ensure the row exists (auto-commit, single statement):
     *   INSERT INTO table (pizza, votes) VALUES (?, 0) ON CONFLICT (pizza) DO NOTHING
     *   PostgreSQL executes this atomically: exactly one transaction wins the
     *   INSERT; all others silently skip (DO NOTHING). No duplicate-key error,
     *   no Java-side error handling required.
     *
     * Step 2 — pessimistic lock + atomic increment (explicit transaction):
     *   SELECT FOR UPDATE — acquires an exclusive row lock.
     *   Any concurrent transaction touching the same row BLOCKS until COMMIT.
     *   UPDATE votes = votes + 1 — atomic increment under the lock.
     *   SELECT votes — read back our value (lock still held).
     *   COMMIT — release the lock.
     *
     * The two-step split is intentional:
     *   - Step 1 runs in auto-commit so its lock scope is minimal (no long txn).
     *   - Step 2 is a short explicit transaction; the row is guaranteed to exist
     *     at this point so SELECT FOR UPDATE always finds it.
     */
    @Override
    public int vote(String pizza) {
        try (Connection conn = DATA_SOURCE.getConnection()) {

            // ── Step 1: ensure row exists — ON CONFLICT DO NOTHING is atomic in PG ──
            // Runs in auto-commit (its own micro-transaction).
            // If the row is already there, the INSERT is a no-op — no error thrown.
            conn.setAutoCommit(true);
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO " + table + " (pizza, votes) VALUES (?, 0) "
                    + "ON CONFLICT (pizza) DO NOTHING")) {
                ins.setString(1, pizza);
                ins.executeUpdate();
            }

            // ── Step 2: pessimistic lock + atomic increment ───────────────────────
            conn.setAutoCommit(false);
            try {
                // Acquire exclusive row lock — concurrent transactions BLOCK here.
                try (PreparedStatement lockStmt = conn.prepareStatement(
                        "SELECT votes FROM " + table + " WHERE pizza = ? FOR UPDATE")) {
                    lockStmt.setString(1, pizza);
                    lockStmt.executeQuery().close();
                }

                // Atomic increment under the lock.
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE " + table + " SET votes = votes + 1 WHERE pizza = ?")) {
                    update.setString(1, pizza);
                    update.executeUpdate();
                }

                // Read back the value we just wrote — lock still held until commit.
                try (PreparedStatement select = conn.prepareStatement(
                        "SELECT votes FROM " + table + " WHERE pizza = ?")) {
                    select.setString(1, pizza);
                    try (ResultSet rs = select.executeQuery()) {
                        int newValue = rs.next() ? rs.getInt(1) : 0;
                        conn.commit();
                        return newValue;
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Integer> getVotes() {
        try (Connection conn = DATA_SOURCE.getConnection();
             PreparedStatement select = conn.prepareStatement(
                     "SELECT pizza, votes FROM " + table)) {
            Map<String, Integer> result = new java.util.HashMap<>();
            try (ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("pizza"), rs.getInt("votes"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getVotesFor(String pizza) {
        try (Connection conn = DATA_SOURCE.getConnection();
             PreparedStatement select = conn.prepareStatement(
                     "SELECT votes FROM " + table + " WHERE pizza = ?")) {
            select.setString(1, pizza);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("votes");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public static void shutdown() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }
}

package com.example.demo.counter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

/**
 * ✅ SafeDbCounter — thread-safe DB-backed counter.
 *
 * The race condition in RacyDbCounter comes from splitting the read and the write
 * into separate statements with no transaction (SELECT → sleep → MERGE).
 * The fix: wrap the increment in an explicit transaction so H2's row-level
 * write lock is held from the UPDATE until the commit:
 *
 *   BEGIN
 *     INSERT INTO table (pizza, votes)
 *       SELECT ?, 0 WHERE NOT EXISTS (SELECT 1 FROM table WHERE pizza = ?)
 *                                              — insert row at 0 only if absent
 *     UPDATE table SET votes = votes + 1 WHERE pizza = ?  — acquire write lock
 *     SELECT votes FROM table WHERE pizza = ?             — read OUR value
 *   COMMIT                                               — release lock
 *
 * MERGE is intentionally avoided: MERGE KEY(pizza) VALUES (?,0) resets an
 * existing row to 0, which would make every vote return 1.
 *
 * No other thread can interleave between the UPDATE and the COMMIT.
 * Each instance uses its own uniquely-named table (UUID suffix) so that concurrent
 * JCStress workers never share state — same isolation strategy as RacyDbCounter.
 */
public final class SafeDbCounter implements PizzaCounter {

    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:jcstress;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(16);
        config.setConnectionTimeout(30000);
        config.setPoolName("safe-hikari-pool");
        DATA_SOURCE = new HikariDataSource(config);
    }

    // Per-instance table name: isolates each JCStress @State instance completely
    private final String table;

    public SafeDbCounter() {
        // H2 identifiers cannot contain hyphens — replace with underscores
        this.table = "pizza_votes_" + UUID.randomUUID().toString().replace("-", "_");
        init();
    }

    /**
     * Creates the per-instance table (fresh, empty). Called once per @State instance.
     */
    public void init() {
        try (Connection conn = DATA_SOURCE.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE " + table
                    + " (pizza VARCHAR(255) PRIMARY KEY, votes INT NOT NULL DEFAULT 0)");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize DB schema for table " + table, e);
        }
    }

    /**
     * Resets (empties) the table — called by @Before in the JCStress test so that
     * every iteration starts from a clean state.
     */
    @Override
    public void resetVotes() {
        try (Connection conn = DATA_SOURCE.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM " + table);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Atomically increments the vote counter and returns the new value.
     *
     * Runs inside a single explicit transaction (autoCommit=false) on the same connection:
     *   1. INSERT ... WHERE NOT EXISTS — insert row at 0 only if absent (never overwrites)
     *   2. UPDATE ... SET votes = votes + 1 — H2 acquires a row-level write lock
     *   3. SELECT votes ...               — reads the value WE just wrote (lock still held)
     *   4. commit()                       — releases the lock
     *
     * No other thread can read or write the same row between steps 2 and 4.
     * MERGE is intentionally avoided: MERGE KEY(pizza) VALUES (?,0) would reset
     * an existing row back to 0 on every call.
     */
    @Override
    public int vote(String pizza) {
        try (Connection conn = DATA_SOURCE.getConnection()) {
            // Disable auto-commit so both statements run in the same transaction,
            // preventing any other thread from reading/writing between them.
            conn.setAutoCommit(false);
            try {
                // Insert the row at 0 only if it does not already exist.
                // INSERT ... SELECT ... WHERE NOT EXISTS never touches an existing row,
                // unlike MERGE which would overwrite votes back to 0.
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO " + table + " (pizza, votes) "
                        + "SELECT ?, 0 WHERE NOT EXISTS (SELECT 1 FROM " + table + " WHERE pizza = ?)")) {
                    insert.setString(1, pizza);
                    insert.setString(2, pizza);
                    insert.executeUpdate();
                }

                // Atomic increment: H2 holds a row-level write lock for the duration
                // of this transaction. No other thread can read or write this row
                // until we call commit() below.
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE " + table + " SET votes = votes + 1 WHERE pizza = ?")) {
                    update.setString(1, pizza);
                    update.executeUpdate();
                }

                // Read the new value while still inside the transaction and holding
                // the write lock — guaranteed to be the value we just wrote.
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


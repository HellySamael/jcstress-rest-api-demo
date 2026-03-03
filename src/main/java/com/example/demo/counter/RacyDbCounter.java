package com.example.demo.counter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Deliberately racy DB-backed counter: performs SELECT then UPDATE (read-modify-write)
 * without any transaction/locking so concurrent actors can lose updates.
 * This is intended for JCStress demonstration of race conditions on DB usage.
 *
 * Each instance uses its own uniquely-named table so that concurrent JCStress
 * workers do not contaminate each other's state across iterations.
 */
public final class RacyDbCounter implements PizzaCounter {

    private static final HikariDataSource DATA_SOURCE;
    private static final Random RAND = new Random();

    static {
        HikariConfig config = new HikariConfig();
        // Named in-memory DB shared across connections within the same JVM
        config.setJdbcUrl("jdbc:h2:mem:jcstress;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(16);
        config.setConnectionTimeout(30000);
        config.setPoolName("racy-hikari-pool");
        DATA_SOURCE = new HikariDataSource(config);
    }

    // Per-instance table name: isolates each JCStress @State instance completely
    private final String table;

    public RacyDbCounter() {
        // Replace hyphens: H2 identifiers cannot contain hyphens
        this.table = "pizza_votes_" + UUID.randomUUID().toString().replace("-", "_");
        init();
    }

    /**
     * Creates the per-instance table (fresh, empty). Called once per @State instance.
     */
    public void init() {
        try (Connection conn = DATA_SOURCE.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE " + table +
                    " (pizza VARCHAR(255) PRIMARY KEY, votes INT)");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize DB schema for table " + table, e);
        }
    }

    /**
     * Cleans all rows — called by @Before in the JCStress test to reset state
     * between iterations without recreating the table.
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

    @Override
    public int vote(String pizza) {
        try (Connection conn = DATA_SOURCE.getConnection()) {
            // --- Step 1: read current value (non-atomic, this is the bug!) ---
            int current = 0;
            try (PreparedStatement select = conn.prepareStatement(
                    "SELECT votes FROM " + table + " WHERE pizza = ?")) {
                select.setString(1, pizza);
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        current = rs.getInt("votes");
                    }
                }
            }

            // --- Step 2: simulate processing delay to widen the race window ---
            try {
                Thread.sleep(1 + RAND.nextInt(5));
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            int next = current + 1;

            // --- Step 3: write back (another thread may have written a different value already!) ---
            try (PreparedStatement upsert = conn.prepareStatement(
                    "MERGE INTO " + table + " (pizza, votes) KEY(pizza) VALUES (?, ?)")) {
                upsert.setString(1, pizza);
                upsert.setInt(2, next);
                upsert.executeUpdate();
            }

            return next;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Integer> getVotes() {
        try (Connection conn = DATA_SOURCE.getConnection();
             PreparedStatement select = conn.prepareStatement(
                     "SELECT pizza, votes FROM " + table)) {
            ResultSet rs = select.executeQuery();
            Map<String, Integer> result = new java.util.HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("pizza"), rs.getInt("votes"));
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


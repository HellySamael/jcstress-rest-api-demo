package com.example.demo.counter.distributated;

import com.example.demo.counter.core.PizzaCounter;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AppDbCounter implements PizzaCounter {

    private static final HikariDataSource DATA_SOURCE = DataSourceFactory.SERVER_DATA_SOURCE;

    private static final List<String> KNOWN_PIZZAS = List.of(
            "item1", "item2",
            "margherita", "pepperoni", "funghi", "quattro");

    private final String table;

    public AppDbCounter() {
        this.table = "pizza_votes_" + UUID.randomUUID().toString().replace("-", "_");
        init();
    }

    public AppDbCounter(String table) {
        this.table = table;
        init();
    }

    private void init() {
        try (Connection conn = DATA_SOURCE.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + table
                    + " (pizza VARCHAR(255) PRIMARY KEY, votes INT NOT NULL DEFAULT 0)");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to init table " + table, e);
        }
    }

    @Override
    public int vote(String pizza) {
        try (Connection conn = DATA_SOURCE.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO " + table + " (pizza, votes) VALUES (?, 0) ON CONFLICT (pizza) DO NOTHING")) {
                insert.setString(1, pizza);
                insert.executeUpdate();
            }

            int current;
            try (PreparedStatement select = conn.prepareStatement(
                    "SELECT votes FROM " + table + " WHERE pizza = ?")) {
                select.setString(1, pizza);
                try (ResultSet rs = select.executeQuery()) {
                    current = rs.next() ? rs.getInt(1) : 0;
                }
            }

            int next = current + 1;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE " + table + " SET votes = ? WHERE pizza = ?")) {
                update.setInt(1, next);
                update.setString(2, pizza);
                update.executeUpdate();
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
            Map<String, Integer> result = new HashMap<>();
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

    @Override
    public void resetVotes() {
        try (Connection conn = DATA_SOURCE.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM " + table);
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO " + table + " (pizza, votes) VALUES (?, 0) ON CONFLICT (pizza) DO NOTHING")) {
                for (String pizza : KNOWN_PIZZAS) {
                    insert.setString(1, pizza);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void shutdown() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }
}

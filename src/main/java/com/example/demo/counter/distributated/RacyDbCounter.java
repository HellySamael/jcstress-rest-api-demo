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


public final class RacyDbCounter implements PizzaCounter {

    private static final HikariDataSource DATA_SOURCE = DataSourceFactory.SERVER_DATA_SOURCE;

    
    static final List<String> KNOWN_PIZZAS = List.of(
            "item1", "item2",
            "margherita", "pepperoni", "funghi", "quattro");

    private final String table;

    
    public RacyDbCounter() {
        this.table = "pizza_votes_" + UUID.randomUUID().toString().replace("-", "_");
        init();
    }

    
    public RacyDbCounter(String table) {
        this.table = table;
    }

    
    private void init() {
        try (Connection conn = DATA_SOURCE.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE " + table
                    + " (pizza VARCHAR(255) PRIMARY KEY, votes INT NOT NULL DEFAULT 0)");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to init table " + table, e);
        }
    }

    
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
        }
    }

    
    @Override
    public int vote(String pizza) {
        try (Connection conn = DATA_SOURCE.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO " + table + " (pizza, votes) VALUES (?, 0) ")) {
                ins.setString(1, pizza);
                ins.executeUpdate();
            }
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement lockStmt = conn.prepareStatement(
                        "SELECT votes FROM " + table + " WHERE pizza = ? FOR UPDATE")) {
                    lockStmt.setString(1, pizza);
                    lockStmt.executeQuery().close();
                }
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE " + table + " SET votes = votes + 1 WHERE pizza = ?")) {
                    update.setString(1, pizza);
                    update.executeUpdate();
                }
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
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
        }
        return -1;
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
        }
        return null;
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
        }
        return 0;
    }

    public static void shutdown() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }
}

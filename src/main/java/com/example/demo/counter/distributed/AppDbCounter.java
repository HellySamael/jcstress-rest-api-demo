package com.example.demo.counter.distributed;

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


public final class AppDbCounter implements PizzaCounter {

    private static final HikariDataSource DATA_SOURCE = DataSourceFactory.SERVER_DATA_SOURCE;

    static final List<String> KNOWN_PIZZAS = List.of(
            "item1", "item2",
            "margherita", "pepperoni", "funghi", "quattro");

    private final String table;

    public AppDbCounter() {
        this.table = "pizza_votes_" + UUID.randomUUID().toString().replace("-", "_");
        init();
    }


    public AppDbCounter(String table) {
        this.table = table;
    }

    @Override
    public int vote(String pizza) {
        try (Connection conn = DATA_SOURCE.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO " + table + " (pizza, votes) VALUES (?, 0) " +
                            "ON CONFLICT (pizza) DO NOTHING"
            )) {
                ins.setString(1, pizza);
                ins.executeUpdate();
            }

            int current = 0;
            try (PreparedStatement read = conn.prepareStatement(
                    "SELECT votes FROM " + table + " WHERE pizza = ?")) {
                read.setString(1, pizza);
                try (ResultSet rs = read.executeQuery()) {
                    if (rs.next()) {
                        current = rs.getInt(1);
                    }
                }
            }

            int next = current + 1;
            try (PreparedStatement updateReturning = conn.prepareStatement(
                    "UPDATE " + table + " SET votes = ?  WHERE pizza = ? RETURNING votes")) {
                updateReturning.setInt(1, next);
                updateReturning.setString(2, pizza);
                try (ResultSet rs = updateReturning.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : next;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

}

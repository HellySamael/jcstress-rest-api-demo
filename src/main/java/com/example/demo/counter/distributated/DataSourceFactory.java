package com.example.demo.counter.distributated;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public final class DataSourceFactory {

    private DataSourceFactory() {}

    
    public static final HikariDataSource SERVER_DATA_SOURCE = build();

    private static HikariDataSource build() {
        HikariConfig cfg = new HikariConfig();

        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null && !dbUrl.isBlank()) {
            cfg.setJdbcUrl(dbUrl);
            cfg.setUsername(System.getenv().getOrDefault("DB_USER", "postgres"));
            cfg.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "postgres"));
        } else {
            String host     = System.getenv().getOrDefault("DB_HOST",     "localhost");
            String port     = System.getenv().getOrDefault("DB_PORT",     "5432");
            String dbName   = System.getenv().getOrDefault("DB_NAME",     "pizzavotes");
            String user     = System.getenv().getOrDefault("DB_USER",     "postgres");
            String password = System.getenv().getOrDefault("DB_PASSWORD", "postgres");

            cfg.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName);
            cfg.setUsername(user);
            cfg.setPassword(password);
        }

        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(30_000);
        cfg.setIdleTimeout(600_000);
        cfg.setMaxLifetime(1_800_000);
        cfg.setPoolName("pizzavotes-pool");

        return new HikariDataSource(cfg);
    }
}

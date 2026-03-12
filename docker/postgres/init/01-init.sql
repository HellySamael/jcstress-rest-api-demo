-- =============================================================
-- Pizza Votes — PostgreSQL initialisation script
-- Executed automatically by the postgres container on first start
-- (mounted in /docker-entrypoint-initdb.d)
-- =============================================================

-- The database 'pizzavotes' is already created by POSTGRES_DB env var.
-- This script creates the schema and seeds initial data.

\connect pizzavotes

-- ──────────────────────────────────────────────
-- Table: pizza_votes
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pizza_votes (
    pizza  VARCHAR(255) PRIMARY KEY,
    votes  INT          NOT NULL DEFAULT 0
);

-- ──────────────────────────────────────────────
-- Seed: pre-insert all known pizzas at 0 votes
-- ON CONFLICT DO NOTHING makes this idempotent
-- ──────────────────────────────────────────────
INSERT INTO pizza_votes (pizza, votes) VALUES
    ('margherita',        0),
    ('pepperoni',         0),
    ('funghi',         0),
    ('quattro',  0)
ON CONFLICT (pizza) DO NOTHING;


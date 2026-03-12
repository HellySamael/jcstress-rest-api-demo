-- =============================================================
-- cleandb.sql — Nettoyage de la table pizza_votes
-- Vide tous les votes et re-seed les pizzas à 0
--
-- Usage (psql local) :
--   psql -h localhost -U postgres -d pizzavotes -f cleandb.sql
--
-- Usage (via Docker) :
--   docker exec -i pizzavotes-db psql -U postgres -d pizzavotes -f - < cleandb.sql
-- =============================================================

\connect pizzavotes

-- Vide tous les votes
DELETE FROM pizza_votes;

-- Re-seed les pizzas initiales à 0
INSERT INTO pizza_votes (pizza, votes) VALUES
    ('margherita',  0),
    ('pepperoni',   0),
    ('funghi',      0),
    ('quattro',     0)
ON CONFLICT (pizza) DO UPDATE SET votes = 0;

-- Affiche l'état après nettoyage
SELECT pizza, votes FROM pizza_votes ORDER BY pizza;


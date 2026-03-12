docker exec -i pizzavotes-db psql -U postgres -d pizzavotes -f - < cleandb.sql

-- Solution B (PostgreSQL installé en local)
-- Usage : psql -U postgres -v mdp="MotDePasseDev" -f scripts/init-db.sql
SELECT 'CREATE ROLE devbrik LOGIN'
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'devbrik')\gexec

ALTER ROLE devbrik WITH LOGIN PASSWORD :'mdp';

SELECT 'CREATE DATABASE devbrik OWNER devbrik'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'devbrik')\gexec

SELECT 'CREATE DATABASE devbrik_test OWNER devbrik'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'devbrik_test')\gexec
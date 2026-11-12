#!/usr/bin/env bash
# Usage : ./restaurer.sh ../sauvegardes/devbrik-AAAA-MM-JJ-HHMM.sql.gz
set -euo pipefail
[ $# -eq 1 ] || { echo "Usage: $0 fichier.sql.gz"; exit 1; }
cd "$(dirname "$0")"
set -a; . ./.env; set +a
read -r -p "Ecraser la base $DB_NAME ? (oui/non) " r; [ "$r" = "oui" ] || exit 1
docker compose exec -T postgres psql -U "$DB_USER" -d postgres -c "DROP DATABASE IF EXISTS $DB_NAME;" -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"
gunzip -c "$1" | docker compose exec -T postgres psql -U "$DB_USER" -d "$DB_NAME"
echo "Restauration terminee."
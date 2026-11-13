#!/usr/bin/env bash
# Sauvegarde quotidienne PostgreSQL, 7 jours conservés (dossier 18.4) (cron : 0 3 * * * /opt/devbrik/deploy/sauvegarde.sh)
set -euo pipefail
cd "$(dirname "$0")"
set -a; . ./.env; set +a
mkdir -p ../sauvegardes
F="../sauvegardes/devbrik-$(date +%F-%H%M).sql.gz"
docker compose exec -T postgres pg_dump -U "$DB_USER" "$DB_NAME" | gzip > "$F"
find ../sauvegardes -name '*.sql.gz' -mtime +7 -delete
echo "Sauvegarde : $F"
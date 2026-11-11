# Deploiement Datacloud (dossier section 18)

1. Serveur Ubuntu : `apt update && apt install -y docker.io docker-compose-v2 git ufw`
2. `git clone <depot> /opt/devbrik && cd /opt/devbrik/deploy`
3. `cp .env.example .env` puis remplir TOUS les secrets (`nano .env`, `chmod 600 .env`)
4. Swap si RAM <= 2 Go : `fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile`
5. `sudo ./pare-feu.sh`
6. `docker compose up -d --build` ; verifier `docker compose ps` (seul nginx publie 80/443)
7. Test : `curl -i http://IP/api/briques` (200) et ouverture du site dans le navigateur
8. HTTPS : obtenir un certificat (certbot), le monter dans nginx sous `/etc/nginx/certs/`, mettre `NGINX_CONF=https.conf`, `docker compose up -d --build nginx`
9. Cron sauvegarde : `crontab -e` -> `0 3 * * * /opt/devbrik/deploy/sauvegarde.sh`
10. **Tester une restauration** (`restaurer.sh`) une fois avant la soumission.
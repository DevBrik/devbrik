# Déploiement sur Datacloud (dossier, section 18)

Serveur cloud Datacloud : Ubuntu Server 24.04 LTS, 2 cœurs, 4 Go, 60 Go, Madrid. Conteneurs : `nginx` (seul à publier 80 et 443), `backend`, `postgres`.

## 1. Préparer le serveur

```bash
ssh -i ~/.ssh/id_ed25519 ubuntu@<IP>
sudo apt update && sudo apt upgrade -y
sudo apt install -y docker.io docker-compose-v2 git ufw
sudo usermod -aG docker $USER   # puis se reconnecter
```

Mémoire : si la compilation sur le serveur échoue faute de mémoire, ajouter un fichier d'échange :
```bash
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## 2. Récupérer le projet et les secrets

```bash
sudo git clone <dépôt> /opt/devbrik && sudo chown -R $USER /opt/devbrik
cd /opt/devbrik/deploy
cp .env.example .env && chmod 600 .env && nano .env
```

| Variable | Valeur |
| --- | --- |
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Base de production (mot de passe différent du développement) |
| `JWT_SECRET` | `openssl rand -base64 48` (32 caractères au moins) |
| `ADMIN_PSEUDO`, `ADMIN_MOT_DE_PASSE` | Premier administrateur (pseudo valide et non réservé, par exemple `admin.demo`) |
| `DEMO_MOT_DE_PASSE` | Comptes `fleury` et `bousso` ; différent de `ADMIN_MOT_DE_PASSE` (S12) |
| `NGINX_CONF` | `http.conf` avant le certificat, `https.conf` après |

`deploy/.env` est ignoré par Git : il ne quitte jamais le serveur.

## 3. Pare-feu et démarrage

```bash
sudo ./pare-feu.sh                 # 22 (clé SSH), 80 et 443 seulement (S11)
docker compose up -d --build
docker compose ps                  # nginx publie 80/443 ; backend et postgres n'ont aucun port publié
docker compose logs -f backend     # « Successfully applied 3 migrations », administrateur créé
curl -i http://<IP>/api/categories # 200 et 8 catégories
```

## 4. Domaine et HTTPS

1. Enregistrement DNS `A` vers l'IP du serveur.
2. Certificat Let's Encrypt (le conteneur nginx arrêté pour libérer le port 80) :
   ```bash
   docker compose stop nginx
   sudo apt install -y certbot && sudo certbot certonly --standalone -d <domaine>
   ```
3. Copier les certificats dans `deploy/certs/` (monté en lecture seule sur `/etc/nginx/certs`, ignoré par Git) :
   ```bash
   mkdir -p /opt/devbrik/deploy/certs
   sudo cp -L /etc/letsencrypt/live/<domaine>/fullchain.pem /etc/letsencrypt/live/<domaine>/privkey.pem /opt/devbrik/deploy/certs/
   sudo chmod 644 /opt/devbrik/deploy/certs/fullchain.pem && sudo chmod 640 /opt/devbrik/deploy/certs/privkey.pem
   ```
   puis mettre `NGINX_CONF=https.conf` dans `.env` et lancer `docker compose up -d --build nginx`.
4. Vérifier les en-têtes : `curl -I https://<domaine>/` doit montrer `Strict-Transport-Security`, `Content-Security-Policy`, `X-Content-Type-Options`, `Referrer-Policy`.
5. Renouvellement : `sudo certbot renew --pre-hook "docker compose -f /opt/devbrik/deploy/docker-compose.yml stop nginx" --deploy-hook "cp -L /etc/letsencrypt/live/<domaine>/*.pem /opt/devbrik/deploy/certs/" --post-hook "docker compose -f /opt/devbrik/deploy/docker-compose.yml start nginx"` dans une tâche cron mensuelle.

## 5. Contenu de démonstration

Depuis n'importe quel poste disposant de Node.js 18 ou plus (le serveur n'en a pas besoin) :

```bash
DEVBRIK_URL=https://<domaine> DEMO_MOT_DE_PASSE='<secret>' node scripts/publier-briques-equipe.mjs
# après avoir exécuté les tests de chaque brique (README) :
DEVBRIK_URL=https://<domaine> DEMO_MOT_DE_PASSE='<secret>' node scripts/publier-briques-equipe.mjs --tester
```
Le script publie les dix briques (une sur deux par chaque compte de démonstration) puis, avec `--tester`, enregistre « testé OK » depuis l'autre compte, avec l'environnement réel des tests. Il peut être relancé : les briques déjà publiées sont ignorées.

## 6. Sauvegarde et restauration

```bash
crontab -e
0 3 * * * /opt/devbrik/deploy/sauvegarde.sh >> /opt/devbrik/sauvegardes/journal.txt 2>&1
```
`sauvegarde.sh` écrit `sauvegardes/devbrik-AAAA-MM-JJ-HHMM.sql.gz` et supprime les fichiers de plus de 7 jours, en plus de la sauvegarde hebdomadaire Datacloud. **Tester une restauration une fois avant la soumission** avec `restaurer.sh` (liste de contrôle, section 20).

## 7. Mise à jour

```bash
cd /opt/devbrik && git pull
cd deploy && docker compose up -d --build
```
Les migrations Flyway s'appliquent au démarrage du backend ; une migration fusionnée ne change plus jamais.

## 8. Après le concours

Changer le mot de passe de l'administrateur de démonstration (S12). La facturation étant horaire, le serveur peut être supprimé après les résultats du 23 novembre.

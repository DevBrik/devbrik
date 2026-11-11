# DevBrik

Bibliotheque communautaire de briques de code reutilisables (Datacloud Senegal Contest).

## Structure
| Dossier | Contenu |
|---|---|
| `backend/` | Spring Boot 4.1.1, Java 21, PostgreSQL, Flyway |
| `frontend/` | Angular 21 + Material |
| `deploy/` | Docker Compose (nginx / backend / postgres), scripts d'exploitation |
| `docs/` | Dossier de reference, API, deploiement, IA, licences, maquettes |
| `scripts/` | Controles (matrice RG, pre-soumission) |
| `briques-equipe/` | Briques de demonstration de l'equipe |

## Lancer en local
1. Base : `docker compose -f deploy/docker-compose.dev.yml up -d`
2. Backend : `cd backend; .\mvnw spring-boot:run` (profil dev) ; tests : `.\mvnw test`
3. Frontend : `cd frontend; npm ci; npm start`

## Deploiement
Voir `docs/deploiement.md`. Les secrets vont dans `deploy/.env` (non versionne) a partir de `deploy/.env.example`.

## Avant de soumettre
`.\scripts\controle-avant-soumission.ps1`
# DevBrik

**Les bonnes briques. Pour les projets d'ici.**

DevBrik est la bibliothèque de briques de code locales, partagées, testées et notées par les développeurs africains : validation des numéros de téléphone, montants en FCFA, fuseau Africa/Dakar, signatures de webhooks, réseaux instables… Chaque brique est publiée avec son code, un mode d'emploi, une licence permissive et des **preuves de fonctionnement** : notes, avis et signal « testé » daté et lié à la version du code. Les développeurs postent aussi des **demandes** auxquelles la communauté répond, et une **réputation** récompense ceux qui publient et ceux qui aident.

**Démo en ligne : https://devbrik.duckdns.org** (serveur cloud Datacloud, Madrid · Docker Compose, HTTPS Let's Encrypt).

*Concours CADEV 2026 · thème « Construire la plateforme d'échange africaine, pour les devs, par les devs » · équipe « Les Lions et les Lionnes du code ».*

## Équipe

| Membre | Rôle | Module |
| --- | --- | --- |
| Amadou Fleury BA | Lead technique | Socle, sécurité, déploiement, maquettes |
| Papa Birane DIAO | Briques | Catalogue, recherche, publication |
| Mame Diarra Bousso DIEME | Confiance et modération | Avis, tests, signalements, statistiques |
| Ndiémé NDIAYE | Échange | Demandes, réponses, profils, réputation |
| Magatte TALL | Comptes et conformité | Comptes, pages légales |

## Fonctionnalités

| Niveau | Contenu | État |
| --- | --- | --- |
| P0 | Comptes (pseudo, jeton 12 h) ; publier, modifier, supprimer une brique ; liste avec recherche, filtres, tris ; fiche avec « Copier le code » ; catégories et langages ; administrateur ; déploiement HTTPS | Fait |
| P1 | Notes et avis ; « testé » OK / KO avec environnement ; signalements et modération ; demandes, réponses, acceptation ; profil public ; gestion du compte (profil, mot de passe, export, suppression) ; statistiques ; 10 briques réelles | Fait (briques à publier sur le serveur : `scripts/publier-briques-equipe.mjs`) |
| P2 | Classement des contributeurs | Fait |

## Stack

Angular 21 + Angular Material · Spring Boot 4.1.1 (Java 21) · PostgreSQL 18 + Flyway · Log4j2 · Docker Compose · Nginx · serveur cloud Datacloud (Madrid). Aucun service externe.

## Structure du dépôt

| Dossier | Contenu |
| --- | --- |
| `backend/` | API Spring Boot, paquet `sn.devbrik.backend` (common, erreur, config, auth, brique, avis, demande, developpeur), migrations Flyway, 429 tests |
| `frontend/` | Application Angular : `core/`, `layout/`, `shared/` (composants `db-*`), `features/` (briques, avis, demandes, auth) ; chaque composant en `.ts` + `.html` + `.scss` |
| `briques-equipe/` | Les 10 briques de l'équipe (B1 à B10) avec leurs tests et leur fiche |
| `deploy/` | Dockerfiles, `docker-compose.yml`, Nginx (en-têtes de sécurité, limitation de débit), pare-feu, sauvegarde, restauration |
| `docs/` | Dossier de référence v1.1, charte v1.1, maquettes, API, architecture, tests, déploiement, démonstration, licences, IA, collection Postman |
| `scripts/` | Base locale, publication des briques, contrôles avant soumission |

## Démarrer en local

Prérequis : Java 21, Node.js 22, PostgreSQL 18 (ou Docker).

1. **Base de données**
   - avec Docker : `docker compose -f deploy/docker-compose.dev.yml up -d`
   - ou PostgreSQL installé : `psql -U postgres -v mdp="devbrik_dev" -f scripts/init-db.sql` (crée `devbrik` et `devbrik_test`)
2. **Backend** (port 8080)
   ```bash
   cd backend
   cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties  # puis l'adapter
   ./mvnw spring-boot:run        # Windows : .\mvnw spring-boot:run
   ```
   Au premier démarrage, Flyway crée les tables, 8 catégories et 11 langages ; l'administrateur local est créé à partir de `application-local.properties` (ignoré par Git).
3. **Frontend** (port 4200, `/api` redirigé vers 8080)
   ```bash
   cd frontend
   npm ci
   npm start
   ```
   Ouvrir http://localhost:4200.
4. **Contenu de démonstration** (facultatif) : définir `DEMO_MOT_DE_PASSE` pour le backend (crée `fleury` et `bousso`), puis
   `DEMO_MOT_DE_PASSE=… node scripts/publier-briques-equipe.mjs --tester`.

## Tests

```bash
cd backend && ./mvnw test                          # 429 tests, base devbrik_test
cd frontend && npm test -- --watch=false           # 107 tests
cd frontend && npm run build                       # compilation de production
```
Détails, matrice RG1 à RG19 et tests des briques : `docs/tests.md`.

## Déploiement

`docs/deploiement.md` : serveur Ubuntu 24.04, `deploy/.env` (à partir de `deploy/.env.example`, jamais versionné), `docker compose up -d --build`, HTTPS Let's Encrypt, sauvegarde quotidienne conservée 7 jours.

## Documentation

| Document | Contenu |
| --- | --- |
| `docs/DevBrik-dossier-de-reference-v1.1.md` | Périmètre, règles RG1 à RG19, modèle de données, contrat d'API, découpage : **fait foi** |
| `docs/DevBrik-design-system-v1.1.md` et `docs/maquettes/` | Charte graphique et maquette finale (PDF + un PNG par écran) |
| `docs/api.md` | Référence de l'API et catalogue des erreurs |
| `docs/architecture.md` | Modules backend et frontend, composants partagés |
| `docs/tests.md` | Stratégie, matrice des règles, commandes |
| `docs/guide-demo.md` | Parcours de démonstration de 3 minutes |
| `docs/demo-comptes.md` | Comptes de démonstration et premier administrateur |
| `docs/LICENCES.md` | Licences de toutes les dépendances et ressources |
| `docs/IA.md` | Déclaration de l'usage de l'IA |
| `docs/postman/` | Collection Postman (47 requêtes, 8 dossiers) |

## Limites connues

- Contraste : le texte blanc sur orange et le texte orange sur fond clair mesurent 3,56:1 (sous AA pour du texte normal) ; décision assumée du 4 novembre, corrigeable en une ligne (`--db-orange-texte` et `--db-orange-bouton` à `#C2410C`). Voir la charte, 3.2.
- Recherche insensible à la casse mais pas aux accents (RG16).
- Les textes des Conditions et de la Confidentialité sont à faire relire (l'équipe n'est pas juriste).
- Points non vérifiés : section 19 du dossier de référence.

## Licence

Code de la plateforme : propriété de l'équipe, développé pendant le concours. Briques de l'équipe : MIT. Dépendances : voir `docs/LICENCES.md`.

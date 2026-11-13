# Licences des composants

*Règlement CADEV, article 6 : composants sous licence permissive (notamment MIT, Apache 2.0, BSD) ; GPL, AGPL et LGPL rendent le projet irrecevable. Dossier de référence, section 8 : chaque dépendance, directe ou indirecte, est déclarée ici.*

Dernière vérification : 12 novembre 2026.

## 1. Backend (Maven, `backend/pom.xml`)

| Composant | Version | Licence | Remarque |
| --- | --- | --- | --- |
| Spring Boot (starters webmvc, data-jpa, validation, security, oauth2-resource-server, flyway, log4j2) | 4.1.1 | Apache 2.0 | Logback exclu de chaque starter |
| Spring Framework, Spring Security, Spring Data JPA | gérées par Spring Boot | Apache 2.0 | |
| Hibernate ORM | 7.x (géré par Spring Boot 4) | Apache 2.0 | Relicencié en Apache 2.0 à partir de la 7.0 ; les versions 6.x et antérieures étaient en LGPL 2.1 : ne pas rétrograder |
| Hibernate Validator | 9.x | Apache 2.0 | |
| Jakarta Persistence, Validation, Annotation (API) | | EPL 2.0 / Apache 2.0 | API standard, sans réciprocité sur notre code |
| Flyway (cœur et `flyway-database-postgresql`) | gérée par Spring Boot | Apache 2.0 | |
| Pilote JDBC PostgreSQL | géré par Spring Boot | BSD 2-Clause | |
| Nimbus JOSE + JWT (via oauth2-resource-server) | | Apache 2.0 | Signature du jeton |
| Apache Log4j 2 | | Apache 2.0 | |
| Jackson | | Apache 2.0 | |
| JUnit 5, AssertJ, Mockito (tests uniquement) | | EPL 2.0 / Apache 2.0 / MIT | Non distribués dans l'application |

**Contrôle** : `./mvnw dependency:tree` puis vérification des licences des artefacts listés (ou `./mvnw license:aggregate-third-party-report` si le plugin est ajouté avec l'accord du membre 1).

## 2. Frontend (npm, `frontend/package.json`)

Dépendances livrées dans l'application (`dependencies`, avec leurs dépendances indirectes) :

| Composant | Licence |
| --- | --- |
| Angular (core, common, compiler, forms, router, platform-browser, cdk, material) | MIT |
| RxJS | Apache 2.0 |
| tslib | 0BSD |
| `entities` (dépendance indirecte) | BSD 2-Clause |
| `material-icons` (police Material Icons, fichiers locaux) | Apache 2.0 |
| `@fontsource/inter` (police Inter, fichiers locaux) | SIL Open Font License 1.1 (police) ; paquet MIT |

Outils de développement (`devDependencies`, non livrés) : Angular CLI et build (MIT), TypeScript (Apache 2.0), Vitest (MIT), jsdom (MIT) et leurs dépendances indirectes, toutes sous MIT, ISC, Apache 2.0, BSD, 0BSD, BlueOak 1.0.0, CC0 ou CC-BY (fichiers de données, par exemple `caniuse-lite`). Aucune GPL, AGPL ni LGPL (`npm ls --all` et le champ `license` du `package-lock.json` vérifiés le 12 novembre 2026).

> **Police Inter (OFL 1.1)** : licence libre propre aux polices, permissive pour l'usage et l'intégration dans une application ; la réciprocité ne porte que sur une police *modifiée et redistribuée*, ce que nous ne faisons pas. Elle n'est ni GPL, ni AGPL, ni LGPL. À signaler si le jury demande une liste stricte « MIT / Apache / BSD » ; repli possible sans changement de code : « Segoe UI », Arial (charte 4).

La dépendance npm `devicon` a été retirée : seules les neuf icônes utilisées sont copiées dans `frontend/public/langages/` (voir 3).

## 3. Ressources graphiques

| Ressource | Emplacement | Licence | Remarque |
| --- | --- | --- | --- |
| Icônes de langages Devicon (Java, TypeScript, JavaScript, Python, PHP, C#, Kotlin, Dart, Go) | `frontend/public/langages/*.svg` | MIT (projet Devicon) | La licence couvre les fichiers d'icônes, pas les marques des langages ; le nom du langage est toujours affiché en texte (dossier 19, V16) |
| Material Icons | police locale | Apache 2.0 | SQL et Shell : icônes `storage` et `terminal` |
| Logo et monogramme DevBrik | `frontend/public/assets/`, `favicon.ico` | Création de l'équipe | Généré avec un outil d'IA puis corrigé : déclaré dans `docs/IA.md` |
| Maquettes | `docs/maquettes/` | Création de l'équipe | Aucune photo de personne, aucun logo de partenaire |

## 4. Briques de l'équipe (`briques-equipe/`)

Les dix briques sont publiées sous licence **MIT** et n'ont **aucune dépendance d'exécution**. Seule B4 utilise Jest (MIT) pour ses tests. Les autres tests utilisent la bibliothèque standard (`node:test`, `unittest`, `javac`/`kotlinc`, `psql`).

## 5. Déploiement

| Composant | Licence | Remarque |
| --- | --- | --- |
| Image `postgres:18` (PostgreSQL) | PostgreSQL License (permissive, type BSD) | |
| Image `nginx:1.27-alpine` | BSD 2-Clause | |
| Image `node:22` (étape de build seulement) | MIT | Non présente dans l'image finale |
| Image Eclipse Temurin (JRE 21) | GPL v2 avec *Classpath Exception* | Environnement d'exécution, pas un composant intégré au code ; l'exception de liaison n'impose aucune licence à l'application |
| Docker Engine, Docker Compose | Apache 2.0 | Outils du serveur |

## 6. Procédure pour un nouvel ajout

1. Demander l'accord du membre 1 (dossier 9.4, règle 5).
2. Vérifier la licence du paquet **et de ses dépendances indirectes** (`npm ls --all`, `./mvnw dependency:tree`).
3. Ajouter une ligne ici dans la même pull request.

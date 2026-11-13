# DevBrik : stratégie et état des tests

## 1. Lancer les tests

| Partie | Commande | Prérequis |
| --- | --- | --- |
| Backend | `cd backend && ./mvnw test` | PostgreSQL local avec la base `devbrik_test` (utilisateur `devbrik`, mot de passe `devbrik_dev` ou variable `DB_PASSWORD`) |
| Frontend (unitaires) | `cd frontend && npm ci && npm test -- --watch=false` | Node.js 22 |
| Frontend (compilation) | `cd frontend && npm run build` | |
| Briques de l'équipe | voir le README de chaque dossier de `briques-equipe/` | Java 17+, Node.js 22, Python 3, PostgreSQL, Kotlin |
| API (parcours) | Collection `docs/postman/DevBrik.postman_collection.json`, dossiers 1 à 8 dans l'ordre | Backend démarré, comptes de démonstration |

La CI GitHub (`.github/workflows/ci.yml`) exécute les tests du backend sur PostgreSQL et la compilation de production du frontend à chaque pull request.

## 2. Backend : matrice RG1 à RG19

429 méthodes de test (unitaires, intégration sur `devbrik_test`, API HTTP). Chaque règle apparaît dans au moins un nom de test (dossier 15) :

| Règle | Occurrences dans les tests | Exemples de fichiers |
| --- | :-: | --- |
| RG1 pseudo | 17 | `ValidationDtoTest`, `UtilisateurServiceTest`, `SchemaInitialTest` |
| RG2 mot de passe, jeton | 28 | `AuthServiceTest`, `JwtServiceTest`, `ApiComptesHttpIntegrationTest` |
| RG3 longueurs | 14 | `BriqueServiceTest`, `AvisRequeteTest`, `SignalementRequeteTest` |
| RG4 licence, droits | 5 | `BriqueServiceTest` |
| RG5 titre unique | 3 | `BriqueServiceTest` |
| RG6 version du code | 6 | `BriqueServiceTest`, `ScenarioChangementCodeApiTest` |
| RG7 visibilité | 17 | `BriqueAdminApiTest`, `AvisApiTest` |
| RG8 avis | 16 | `AvisServiceTest`, `AvisApiTest` |
| RG9 tests version actuelle | 19 | `TestBriqueServiceTest`, `ScenarioChangementCodeApiTest` |
| RG10 dernière vérification | 3 | `StatsBriqueServiceIntegrationTest` |
| RG11 signalements | 30 | `SignalementServiceTest`, `SignalementApiTest`, `MigrationV4Test` |
| RG12 acceptation | 23 | `ReponseServiceTest`, `ReponseRepositoryTest`, `DemandeApiTest` |
| RG13 lien vers une brique | 14 | `DemandeServiceLienBriqueTest` |
| RG14 réputation | 11 | `ReputationServiceTest`, `DeveloppeurApiTest` |
| RG15 quotas | 31 | `QuotaServiceTest`, `QuotaApiTest` |
| RG16 pagination, recherche | 37 | `BriqueRechercheTest`, `DemandeSpecificationsTest` |
| RG17 suppression en cascade | 15 | `CompteServiceTest`, `CompteIntegrationTest` |
| RG18 texte brut | 1 (+ frontend) | `BriqueServiceTest` ; voir 3 |
| RG19 droits | 33 | `SecuriteApiTest`, `BriqueAdminApiTest`, `DemandeApiTest` |

Validation du schéma (dossier 10.6) : `SchemaInitialTest` (9 tables + `flyway_schema_history`, 8 catégories, 11 langages, contraintes) et `MigrationV4Test`.

## 3. Frontend

- 107 tests unitaires (Vitest) : coquille (`app.spec.ts`), libellés partagés, pagination, champ de note, module avis (actions, avis, dialogues, M11, M12, service, erreurs).
- Compilation de production sans erreur ni avertissement.
- **RG18 / S1** : aucun `innerHTML` ni `bypassSecurityTrust` (`grep -rn "innerHTML\|bypassSecurityTrust" frontend/src` ne renvoie rien) ; un code contenant `<script>` s'affiche tel quel dans `db-zone-code` (vérifié à l'écran).
- **Mise en page** : toutes les pages vérifiées à 1280 px et à 360 px (aucun défilement horizontal de la page ; le code défile dans son bloc).

## 4. Parcours de démonstration (dossier 5)

À rejouer sur la version déployée avant la soumission (voir `docs/guide-demo.md`) : recherche « téléphone » + filtre Java, fiche et copie du code, test et avis, publication, demande / réponse / acceptation, signalement et masquage par l'administrateur.

## 5. Sécurité (dossier 15 et 17)

| Contrôle | Vérification |
| --- | --- |
| Texte brut | Contenu avec `<script>` affiché tel quel (frontend) |
| 401 sur jeton expiré | `JwtServiceTest`, `SecuriteApiTest` ; intercepteur frontend |
| 429 sur quota | `QuotaApiTest` |
| Secrets | `application-local.properties` et `deploy/.env` ignorés par Git ; aucun mot de passe dans les journaux (`AuthServiceTest`) |
| En-têtes Nginx | `curl -I https://<domaine>/` : CSP, X-Content-Type-Options, Referrer-Policy, HSTS |

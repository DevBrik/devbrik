# DevBrik : référence de l'API

*Contrat de la section 11 du dossier de référence v1.1, avenant du 4 novembre inclus. Ce fichier décrit ce que le backend implémente et ce que le frontend consomme. Toute évolution est d'abord inscrite dans le dossier, puis ici.*

## 1. Conventions

| Sujet | Règle |
| --- | --- |
| Préfixe | Toutes les routes commencent par `/api` |
| Format | JSON UTF-8, champs en camelCase, dates ISO 8601 UTC (`2026-11-05T14:30:00Z`) |
| Authentification | En-tête `Authorization: Bearer <jeton>` ; jeton signé (HS256), valable 12 h, contient l'identifiant (`sub`) et le rôle |
| Jeton invalide ou expiré | 401 `NON_AUTHENTIFIE`, **même sur une route publique** ; le frontend supprime le jeton et réessaie sans jeton (`jeton.interceptor.ts`) |
| Routes publiques | Toutes les lectures (GET) de briques, catégories, langages, demandes, développeurs ; inscription et connexion |
| Pagination | `page` à partir de 0, `taille` 20 par défaut, 50 au plus ; réponse `PageReponse` |
| Texte | Brut uniquement (RG3, RG18), rogné (trim) par le serveur avant validation |
| Champs inconnus | Ignorés en entrée ; `null` explicite en sortie pour un champ absent |

### PageReponse
```json
{ "contenu": [], "page": 0, "taille": 20, "totalElements": 134, "totalPages": 7 }
```

### Erreur (toutes les erreurs, y compris 401 et 403 de la sécurité)
```json
{
  "statut": 400,
  "code": "REQUETE_INVALIDE",
  "message": "Le titre doit contenir entre 5 et 120 caractères.",
  "details": [{ "champ": "titre", "message": "Entre 5 et 120 caractères" }],
  "horodatage": "2026-11-05T14:30:00Z"
}
```
`details` n'est présent que pour les erreurs de validation (400).

### ReferenceUtilisateur
```json
{ "id": 12, "pseudo": "awa.dev", "nomAffiche": "Awa Diop" }
```

## 2. Authentification et compte (membre 5)

| Méthode | Route | Accès | Corps | Réponse |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/inscription` | Public | `{ pseudo, motDePasse, nomAffiche? }` | 201 `{ jeton, utilisateur }` |
| POST | `/api/auth/connexion` | Public | `{ pseudo, motDePasse }` | 200 `{ jeton, utilisateur }` |
| GET | `/api/moi` | Connecté | | 200 `Utilisateur` |
| PUT | `/api/moi` | Connecté | `{ nomAffiche?, pays?, bio?, lienProfil? }` | 200 `Utilisateur` |
| PUT | `/api/moi/mot-de-passe` | Connecté | `{ ancienMotDePasse, nouveauMotDePasse }` | 204 |
| GET | `/api/moi/export` | Connecté | | 200 : profil, briques, avis, tests, demandes, réponses, signalements |
| DELETE | `/api/moi` | Connecté | `{ motDePasse }` | 204 (cascade RG17) |

**Utilisateur** : `{ id, pseudo, nomAffiche, role, pays, bio, lienProfil, dateCreation }` ; jamais de mot de passe.

Règles : RG1 (pseudo), RG2 (mot de passe 8 à 72, BCrypt, jeton 12 h), RG17. Connexion refusée : 401 `IDENTIFIANTS_INVALIDES` sans dire si le pseudo existe. Un administrateur reçoit 403 `SUPPRESSION_ADMIN_INTERDITE` sur `DELETE /api/moi`.

## 3. Briques (membre 2)

| Méthode | Route | Accès | Détail |
| --- | --- | --- | --- |
| GET | `/api/categories` | Public | 200 liste `{ id, slug, nom, description }` |
| GET | `/api/langages` | Public | 200 liste `{ id, slug, nom }` |
| GET | `/api/briques` | Public | `q`, `categorie` (slug), `langage` (slug), `auteur` (id), `tri` (`recentes` par défaut, `mieux-notees`, `plus-testees`), `page`, `taille` ; `BriqueResume` PUBLIEES uniquement |
| GET | `/api/briques/{id}` | Public | `BriqueDetail` ; MASQUEE : auteur et administrateurs seulement, sinon 404 (RG7) |
| POST | `/api/briques` | Développeur | 201 `BriqueDetail` |
| PUT | `/api/briques/{id}` | Auteur | 200 `BriqueDetail` ; `versionCode` + 1 si le code change (RG6) |
| DELETE | `/api/briques/{id}` | Auteur ou admin | 204 |
| GET | `/api/moi/briques` | Connecté | Mes briques, masquées comprises ; mêmes paramètres que la liste publique plus `statut` (`PUBLIEE` ou `MASQUEE`, absent = toutes) |
| PUT | `/api/admin/briques/{id}/statut` | Admin | `{ statut: "PUBLIEE" \| "MASQUEE" }` → 200 `BriqueDetail` |
| GET | `/api/admin/briques` | Admin | `statut` (`MASQUEE` par défaut), `page`, `taille` → page de `BriqueResume` |

**Corps de création et de modification**
```json
{
  "titre": "Validation des numéros sénégalais",
  "resume": "Valide et normalise un numéro de téléphone sénégalais au format E.164.",
  "code": "…",
  "modeEmploi": "Étape 1\nÉtape 2",
  "prerequis": "Java 21",
  "licence": "MIT",
  "idCategorie": 3,
  "idLangage": 1,
  "droitsConfirmes": true
}
```
Licences : `MIT`, `APACHE_2_0`, `BSD_2_CLAUSE`, `BSD_3_CLAUSE`, `ISC`, `CC0_1_0`. `droitsConfirmes` absent ou faux : 400 `DROITS_NON_CONFIRMES`. Titre déjà utilisé par le même auteur (casse ignorée) : 409 `TITRE_DEJA_UTILISE`. Quota : 20 briques par 24 h.

**BriqueResume** : `{ id, titre, resume, licence, statut, versionCode, dateCreation, dateModification, categorie: {slug, nom}, langage: {slug, nom}, auteur, noteMoyenne, nombreAvis, testsOk, testsKo, derniereVerification }`
**BriqueDetail** : `BriqueResume` + `code`, `modeEmploi`, `prerequis`, `idCategorie`, `idLangage`.
`noteMoyenne` (une décimale) et `derniereVerification` valent `null` sans avis ou sans test de la version actuelle ; `testsOk` et `testsKo` ne comptent que la version actuelle (RG9, RG10).

**Recherche (RG16)** : `q` de 2 à 100 caractères, sur le titre et le résumé, insensible à la casse mais pas aux accents ; `%`, `_` et le caractère d'échappement sont traités comme du texte.

## 4. Avis, tests, signalements, administration (membre 3)

| Méthode | Route | Accès | Corps | Réponse |
| --- | --- | --- | --- | --- |
| GET | `/api/briques/{id}/avis` | Public | | 200 page d'`Avis`, récents d'abord |
| PUT | `/api/briques/{id}/avis` | Développeur | `{ note, commentaire? }` | 201 créé, 200 mis à jour |
| DELETE | `/api/avis/{idAvis}` | Auteur ou admin | | 204 |
| GET | `/api/briques/{id}/tests` | Public | | 200 page de `Test` (version actuelle) |
| PUT | `/api/briques/{id}/test` | Développeur | `{ resultat: "OK" \| "KO", environnement? }` | 201 ou 200 |
| DELETE | `/api/briques/{id}/test` | Développeur | | 204 (son propre test) |
| GET | `/api/briques/{id}/moi` | Connecté | | 200 `{ monAvis, monTest }` (chacun peut être `null`) |
| POST | `/api/briques/{id}/signalements` | Développeur | `{ motif, details? }` | 201 |
| GET | `/api/admin/signalements` | Admin | `statut` (`OUVERT` par défaut), `page`, `taille` | 200 page de `Signalement` |
| PUT | `/api/admin/signalements/{id}/traitement` | Admin | `{ action: "MASQUER_BRIQUE" \| "CONSERVER" }` | 200 `Signalement` |
| GET | `/api/admin/statistiques` | Admin | | 200 `Statistiques` |

- **Avis** : `{ id, note, commentaire, auteur, dateCreation, dateModification }`
- **Test** : `{ id, resultat, environnement, versionCode, auteur, dateTest }`
- **Signalement** : `{ id, motif, details, statut, actionTraitement, dateCreation, dateTraitement, brique: { id, titre, statut, auteur }, auteur }` (`brique.auteur` : auteur de la brique ; `auteur` : qui a signalé)
- **Statistiques** : `{ utilisateurs, briquesPubliees, briquesMasquees, avis, tests, demandesOuvertes, demandesResolues, signalementsOuverts }`

Motifs : `CODE_DANGEREUX`, `DROITS_AUTEUR`, `SPAM`, `INEXACT`, `INAPPROPRIE` (migration V4), `AUTRE`. Avis ou test sur sa propre brique : 403 `AUTO_EVALUATION_INTERDITE`. Deuxième signalement : 409 `SIGNALEMENT_DEJA_ENVOYE`. Quotas par 24 h : 50 avis, 50 tests, 10 signalements. `MASQUER_BRIQUE` appelle `BriqueService.masquer` puis passe le signalement à `TRAITE`.

## 5. Demandes, réponses, développeurs (membre 4)

| Méthode | Route | Accès | Corps | Réponse |
| --- | --- | --- | --- | --- |
| GET | `/api/demandes` | Public | `statut`, `categorie`, `langage`, `q`, `page`, `taille` | 200 page de `DemandeResume`, récentes d'abord |
| GET | `/api/demandes/{id}` | Public | | 200 `DemandeDetail` |
| POST | `/api/demandes` | Développeur | `{ titre, description, idCategorie?, idLangage? }` | 201 `DemandeDetail` |
| DELETE | `/api/demandes/{id}` | Auteur ou admin | | 204 |
| POST | `/api/demandes/{id}/reponses` | Développeur | `{ contenu, idBrique? }` | 201 `Reponse` |
| DELETE | `/api/reponses/{id}` | Auteur ou admin | | 204 |
| PUT | `/api/reponses/{id}/acceptation` | Auteur de la demande | | 200 `DemandeDetail` (RESOLUE) |
| DELETE | `/api/reponses/{id}/acceptation` | Auteur de la demande | | 200 `DemandeDetail` (OUVERTE) |
| GET | `/api/developpeurs/{id}` | Public | | 200 `ProfilPublic` |
| GET | `/api/developpeurs/classement` | Public | `page`, `taille` | 200 page d'éléments de classement, réputation décroissante |

- **DemandeResume** : `{ id, titre, extrait, statut, dateCreation, categorie, langage, auteur, nombreReponses }` (`extrait` : 160 premiers caractères, espaces réduits, « … » si coupé)
- **DemandeDetail** : `DemandeResume` + `description`, `reponses`
- **Reponse** : `{ id, contenu, acceptee, dateCreation, auteur, brique: { id, titre } | null }` (`null` si le lien n'est plus valide, RG13)
- **ProfilPublic** : `{ id, pseudo, nomAffiche, pays, bio, lienProfil, dateCreation, reputation, nombreBriques, testsOkRecus, nombreReponsesAcceptees }`
- **Élément du classement** : `{ developpeur, pays, reputation, nombreBriques, testsOkRecus, nombreReponsesAcceptees }`

Réputation (RG14, calculée à la demande) : 10 × briques publiées + 5 × tests OK reçus (version actuelle) + 3 × réponses acceptées. Accepter sa propre réponse : 403 `ACCEPTATION_PROPRE_REPONSE`. Une autre réponse est déjà acceptée : 409 `REPONSE_DEJA_ACCEPTEE`. Brique liée inexistante ou non publiée : 400 `BRIQUE_LIEE_INVALIDE`. Quotas : 10 demandes et 50 réponses par 24 h.

## 6. Catalogue des erreurs

| Code | Statut | Quand | Affichage frontend |
| --- | :-: | --- | --- |
| REQUETE_INVALIDE | 400 | Validation d'un champ (`details`), paramètre mal formé | Sous le champ concerné |
| DROITS_NON_CONFIRMES | 400 | `droitsConfirmes` absent ou faux | Sous la case (M03) |
| BRIQUE_LIEE_INVALIDE | 400 | Brique liée inexistante ou non publiée | Sous « Lier une brique » (M07) |
| MOT_DE_PASSE_INCORRECT | 400 | Ancien mot de passe ou confirmation erroné | Sous « Ancien mot de passe » (M10) |
| PSEUDO_RESERVE | 400 | Pseudo réservé (RG1) | Sous le pseudo (M04) |
| NON_AUTHENTIFIE | 401 | Jeton absent, invalide ou expiré | Retour à la connexion |
| IDENTIFIANTS_INVALIDES | 401 | Connexion refusée | « Pseudo ou mot de passe incorrect. » |
| ACCES_REFUSE | 403 | Réservé à l'auteur ou à l'administrateur | Notification / état 403 |
| AUTO_EVALUATION_INTERDITE | 403 | Avis ou test sur sa propre brique | Boutons désactivés + notification |
| ACCEPTATION_PROPRE_REPONSE | 403 | Accepter sa propre réponse | Notification |
| SUPPRESSION_ADMIN_INTERDITE | 403 | Un administrateur supprime son compte | Bouton désactivé + notification |
| RESSOURCE_INTROUVABLE | 404 | Identifiant inconnu, brique masquée vue par un tiers | État 404 |
| PSEUDO_DEJA_UTILISE | 409 | Pseudo pris (casse ignorée) | Sous le pseudo |
| TITRE_DEJA_UTILISE | 409 | Titre déjà utilisé par le même auteur | Sous le titre |
| SIGNALEMENT_DEJA_ENVOYE | 409 | Deuxième signalement | Notification |
| REPONSE_DEJA_ACCEPTEE | 409 | Une autre réponse est déjà acceptée | Notification + rechargement |
| LIMITE_QUOTIDIENNE | 429 | Quota dépassé (RG15) | Notification avec le message |
| TROP_DE_REQUETES | 429 | Limitation de débit de Nginx sur `/api/auth/` (10 par minute et par adresse IP) : renvoyée par Nginx, pas par le backend | Notification avec le message |
| ERREUR_INTERNE | 500 | Erreur inattendue | Notification générique |

## 7. Tester l'API

- Collection Postman : `docs/postman/DevBrik.postman_collection.json` (variables `baseUrl`, `pseudo`, `motDePasse`, `adminPseudo`, `adminMotDePasse` ; le jeton est capturé automatiquement après la connexion).
- En ligne de commande :
```bash
curl -s http://localhost:8080/api/briques?q=telephone\&langage=java | jq '.totalElements'
JETON=$(curl -s -X POST http://localhost:8080/api/auth/connexion -H 'Content-Type: application/json' \
  -d '{"pseudo":"awa.dev","motDePasse":"…"}' | jq -r .jeton)
curl -s http://localhost:8080/api/moi -H "Authorization: Bearer $JETON"
```

# DevBrik : architecture

## 1. Vue d'ensemble

```
Navigateur ──HTTPS──> Nginx ──┬── fichiers Angular compilés (frontend)
                              └── /api ──> Spring Boot (backend) ──> PostgreSQL
```

| Couche | Technologie | Rôle |
| --- | --- | --- |
| Frontend | Angular 21, Angular Material 21, SCSS | Application monopage, routes chargées à la demande, aucun service externe |
| Backend | Spring Boot 4.1.1, Java 21 | API REST `/api`, jeton signé 12 h, validation, règles RG1 à RG19 |
| Base | PostgreSQL 18, Flyway | Schéma versionné (V1, V2, V4), `ddl-auto=validate` |
| Serveur | Docker Compose sur un serveur cloud Datacloud (Madrid) | Nginx (HTTPS, en-têtes de sécurité, limitation de débit), backend, PostgreSQL non exposé |

## 2. Backend (`backend/`, paquet `sn.devbrik.backend`)

| Paquet | Contenu | Propriétaire |
| --- | --- | --- |
| `common` | `ApiException`, `CodeErreur`, `PageReponse`, `IdentiteUtilisateur`, `ReferenceUtilisateur`, `QuotaService` | Membre 1 |
| `erreur` | `GestionnaireErreurs` : toutes les erreurs au format de l'API | Membre 1 |
| `config` | `SecurityConfig`, `JwtConfig`, `JwtService`, `JwtFiltre`, `ReponseErreurSecurite` | Membre 1 |
| `auth` | Utilisateur, inscription, connexion, profil, mot de passe, export, suppression, `AdminInitialiseur`, `DemoInitialiseur` | Membre 5 |
| `brique` | Brique, catégories, langages, recherche, publication, modération du statut | Membre 2 |
| `avis` | Avis, tests, signalements, statistiques, `StatsBriqueService` | Membre 3 |
| `demande` | Demandes, réponses, acceptation | Membre 4 |
| `developpeur` | Profil public, réputation, classement | Membre 4 |

Règles entre modules (dossier 9.4) : aucune relation JPA entre modules (identifiants `Long`), lecture d'un autre module par son service public (`UtilisateurService`, `BriqueService`, `StatsBriqueService`, `DemandeService`, `QuotaService`) ou par SQL en lecture seule, erreurs métier uniquement par `ApiException`.

Migrations : `V1__schema_initial.sql` (9 tables), `V2__referentiels.sql` (8 catégories, 11 langages), `V4__signalement_motif_inapproprie.sql`. `spring.flyway.out-of-order=true`.

## 3. Frontend (`frontend/src/app/`)

```
core/            socle (membre 1)
  api/           modèles du contrat (modeles.ts), erreurs, ReferentielService
  auth/          AuthService (jeton + utilisateur), gardes, intercepteur de jeton
  libelles.ts    licences, tris, pastille « Testé », initiales, pays
  config.ts      adresse de contact, date des textes légaux
layout/          coquille : en-tête, menu du compte, pied de page, barre mobile
shared/          composants partagés db-* (12.6 du dossier)
features/
  briques/       M01 liste, M02 fiche, M03 formulaire, M05 mes briques   (membre 2)
  avis/          blocs de M02 (actions, avis, dialogues), M11, M12       (membre 3)
  demandes/      M06, M07, M08, M09 profil public, classement            (membre 4)
  auth/          M04 connexion et inscription, M10 mon compte, M13       (membre 5)
```

**Chaque composant est en trois fichiers** : `*.component.ts` (logique), `*.component.html` (gabarit), `*.component.scss` (styles). Les styles communs (boutons `.btn`, champs `.champ`, grilles, pages) sont dans `src/styles/_communs.scss` et `src/styles/_utilitaires.scss`, chargés une fois par `src/styles.scss`. Les couleurs passent toujours par les variables `--db-*` de la charte ; `#EA580C` n'est jamais écrit dans un composant.

### Composants partagés

| Sélecteur | Rôle |
| --- | --- |
| `db-logo` | Monogramme + mot-symbole « DevBrik » en texte |
| `db-brique-carte` | Carte de brique ; actions `aucune`, `auteur` (Voir, Modifier, Supprimer) ou `republier` (M11), toujours visibles |
| `db-langage-badge` | Icône Devicon locale (décorative) + nom du langage |
| `db-pastille` | Statuts : Testé OK, Testé KO, Avis partagés, Non testée, Publiée, Masquée, Ouverte, Acceptée, Résolue, catégorie, motif |
| `db-note` | « ★ 4,8 sur 5 (12 avis) » ou « Pas encore d'avis » |
| `db-pagination` | Précédent, numéros avec « … », Suivant ; « Page 1 sur 10 » sur mobile |
| `db-barre-filtres` | Recherche (300 ms, 2 à 100 caractères), catégorie, langage, tri facultatif |
| `db-tuile` | Chiffre orange + libellé |
| `db-zone-code` | Code en texte brut, numéros de ligne, « Copier le code » |
| `db-champ-code` | Zone de code éditable (monospace, Tabulation, compteur) |
| `db-champ-lignes` | Saisie par lignes en pastilles (mode d'emploi, prérequis) |
| `db-champ-note` | Étoiles 1 à 5 au clavier, avec texte |
| `db-champ-mot-de-passe` | Champ avec bouton œil |
| `db-etat-page` | États vide, chargement, erreur réseau, 401, 403, 404 |
| `db-squelette-cartes` | Squelettes de chargement aux formes des cartes |
| `db-dialogue-confirmation` + `DialogueService` | Confirmations (variante avec mot de passe) |
| `NotificationService` | `mat-snack-bar` annoncé aux lecteurs d'écran |

### Comportements transverses

- **Jeton** : `AuthService` garde le jeton et l'utilisateur dans `localStorage` (protégé par `try/catch`). L'intercepteur ajoute l'en-tête, supprime le jeton sur 401 et réessaie sans jeton ; si la route reste protégée, retour à `/compte/connexion?retour=…`.
- **Gardes** : `connexionGuard` (pages connectées), `adminGuard` (`/admin`). Les droits restent vérifiés par le serveur (RG19).
- **Adresse** : filtres et page des listes conservés dans l'adresse (`?q=&categorie=&langage=&tri=&page=`).
- **Accessibilité** : lien « Aller au contenu », titre unique par écran, annonce du titre et focus sur le contenu à chaque navigation, focus visible 3 px, onglets au clavier (flèches), dialogues à focus piégé, aucune action au survol, `prefers-reduced-motion`.
- **Texte brut** : aucun `innerHTML`, aucun `bypassSecurityTrust*` ; `white-space: pre-wrap` pour les sauts de ligne.
- **Locale** : `fr` (dates « 21 octobre 2026 », virgule décimale).

## 4. Déploiement (`deploy/`)

`docker-compose.yml` : `postgres` (volume, aucun port publié), `backend` (image Temurin JRE 21, utilisateur non root), `nginx` (frontend compilé dans l'image, proxy `/api`, ports 80 et 443). Voir `docs/deploiement.md`.

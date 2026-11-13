# Usage de l'intelligence artificielle

*Règlement CADEV, article 6 : l'usage d'outils d'IA est libre mais obligatoirement déclaré (outils, parties du projet concernées). Il n'entraîne ni bonus ni pénalité ; le jury évalue la maîtrise de la solution par l'équipe, y compris des parties produites avec l'IA. Dossier de référence, section 8 : tout usage (code, textes, logo, briques) est déclaré ici.*

## 1. Synthèse

| Partie | Outil | Usage | Vérification humaine attendue |
| --- | --- | --- | --- |
| Dossier de référence, charte, prompts | Claude (Anthropic) | Rédaction assistée, relue et arbitrée par le membre 1 | Décisions prises en équipe |
| Logo et monogramme DevBrik | Outil de génération d'images | Proposition générée, puis corrigée à la main (charte 10) | Membre 1 |
| Socle backend, sécurité, déploiement | Claude | Code, tests, scripts de déploiement, audit du backend contre le dossier | Tests Maven exécutés (429 / 429) par le membre 1 |
| Module comptes (backend) | Claude | Entité, services, contrôleurs, tests (étapes 1 à 5 du prompt membre 5) | Relu et exécuté par le membre 5 avant la pull request |
| Module avis (backend et frontend) | Claude | Services, tests, blocs de M02, M11, M12 | Membre 3 |
| Modules briques et demandes (backend) | Claude | Selon les prompts de la section 21 du dossier | Membres 2 et 4 |
| **Achèvement du frontend** (12 novembre) | Claude | Voir 2 | Voir 4 |
| **Briques de l'équipe B1 à B3, B5 à B10** (12 novembre) | Claude | Voir 3 | Voir 4 |
| **Documentation** (12 novembre) | Claude | README, `docs/api.md`, `architecture.md`, `tests.md`, `deploiement.md`, `guide-demo.md`, `LICENCES.md`, ce fichier, collection Postman | Relecture par le membre 1 |

Rien n'est repris d'AgriLion dans le frontend ni dans les briques. Pour le backend, les parties génériques reprises d'AgriLion (dossier 9.5 : gestionnaire d'erreurs, configuration de sécurité, Flyway, Log4j2) sont à confirmer ici par le membre 1 avec la liste des fichiers.

## 2. Frontend achevé avec Claude (12 novembre 2026)

- Coquille : en-tête (Briques · Demandes · Classement, menu du compte), pied de page, barre de navigation mobile, annonces de navigation.
- Composants partagés `db-*` de la section 12.6 (carte de brique, pastilles, note, pagination, barre de filtres, tuiles, zone et champ de code, saisie par lignes, mot de passe, états, dialogue de confirmation).
- Écrans M01, M02, M03, M05 (membre 2), M06, M07, M08, M09 et classement (membre 4), M04, M10, M13 (membre 5), M14 (membre 1).
- Séparation de **tous** les composants en trois fichiers (`.ts`, `.html`, `.scss`), y compris ceux du module avis écrits par le membre 3 ; remplacement de ses gabarits locaux (pagination, dialogue de confirmation, carte de brique masquée, champ de note) par les composants partagés.
- Textes provisoires des Conditions et de la Confidentialité (à faire relire, dossier 19, V10).

## 3. Briques de l'équipe avec Claude (12 novembre 2026)

| Brique | Ce qui a été produit | État |
| --- | --- | --- |
| B1 téléphone (Java) | Déplacée depuis l'ancien dossier B3, tests réécrits sans dépendance | 22 vérifications passent |
| B2 montant FCFA (TypeScript) | Nouvelle brique ; l'ancienne B2 « paiement Orange Money » a été retirée (dossier 2.6) | 5 tests passent |
| B3 montant en lettres (Java) | Nouvelle brique | 36 vérifications passent |
| B4 date Dakar (JavaScript) | Écrite par le membre 2 ; README ajouté | 29 tests Jest passent |
| B5 signature HMAC (Python) | Nouvelle brique, vecteurs RFC 4231 | 5 tests passent |
| B6 réessai exponentiel (TypeScript) | Nouvelle brique, horloge simulée | 6 tests passent |
| B7 coordonnées GPS (SQL) | Remplace « régions du Sénégal » faute de source vérifiée (dossier 16) | Tests PostgreSQL passent |
| B8 file hors ligne (JavaScript) | Nouvelle brique | 6 tests passent |
| B9 texte sans accents (Java) | Nouvelle brique | 11 vérifications passent |
| B10 masquage téléphone (Kotlin) | Nouvelle brique | 11 vérifications passent (Kotlin 2.1) |

Les sources des règles métier sont indiquées dans chaque README ; celles marquées « à confirmer » (préfixes ARTP pour B1, référence grammaticale pour B3) doivent être vérifiées par écrit par l'auteur avant publication (dossier 19, V8).

## 4. Engagement de l'équipe

Chaque membre relit et sait expliquer les parties produites avec l'IA dans son module (critère « maîtrise par l'équipe de la solution livrée », annexe 1 du règlement). Inscrire ci-dessous la relecture :

| Membre | Parties relues | Date | Signature |
| --- | --- | --- | --- |
| 1 | Socle, coquille, composants partagés, déploiement, documentation | | |
| 2 | M01, M02, M03, M05 ; B2, B3, B4 | | |
| 3 | Blocs de M02, M11, M12 ; B5, B6 | | |
| 4 | M06 à M09, classement ; B7, B8 | | |
| 5 | M04, M10, M13 ; B9, B10 ; textes légaux | | |

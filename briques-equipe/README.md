# Briques de l'équipe (dossier, section 16)

Dix briques réelles, **vérifiables sans compte externe**, sous licence **MIT**, sans dépendance d'exécution. Chaque dossier contient le code, ses tests et un `README.md` qui sert de fiche (résumé, mode d'emploi, prérequis, limites, source).

| ID | Brique | Catégorie | Langage | Auteur | Tests |
| --- | --- | --- | --- | --- | --- |
| B1 | Validation et normalisation des numéros de téléphone sénégalais | Validation de données | Java | Amadou Fleury BA | `javac` + classe de test (22 vérifications) |
| B2 | Formatage d'un montant en FCFA | Formats, dates et devises | TypeScript | Papa Birane DIAO | `node --test` (5) |
| B3 | Montant en lettres en français | Formats, dates et devises | Java | Papa Birane DIAO | `javac` + classe de test (36) |
| B4 | Date et heure en Africa/Dakar | Formats, dates et devises | JavaScript | Papa Birane DIAO | Jest (29) |
| B5 | Vérification d'une signature de rappel (webhook) HMAC-SHA256 | Sécurité et authentification | Python | Mame Diarra Bousso DIEME | `unittest`, vecteurs RFC 4231 (5) |
| B6 | Réessai avec attente exponentielle | Réseaux instables et hors ligne | TypeScript | Mame Diarra Bousso DIEME | `node --test`, horloge simulée (6) |
| B7 | Validation de coordonnées GPS et distance en SQL | Données géographiques | SQL | Ndiémé NDIAYE | `psql` (11 assertions) |
| B8 | File d'envoi hors ligne avec reprise | Réseaux instables et hors ligne | JavaScript | Ndiémé NDIAYE | `node --test` (6) |
| B9 | Normalisation de texte sans accents pour la recherche | Outils divers | Java | Magatte TALL | `javac` + classe de test (11) |
| B10 | Masquage d'un numéro de téléphone dans les journaux | Sécurité et authentification | Kotlin | Magatte TALL | `kotlinc` + test (11) |

Tous les tests passent (vérification du 12 novembre 2026).

## Changements par rapport au premier dépôt

- **B2 « paiement Orange Money » retirée** : le dossier (2.6, 16) interdit toute brique d'intégration de paiement sans documentation officielle et moyen de test. Remplacée par le formatage FCFA prévu.
- **Ancienne B3 (téléphone) devenue B1**, conformément au tableau de la section 16.
- **B7** : la liste des régions et départements n'a pas de source officielle ni de licence vérifiée (dossier 19, V9). Elle est remplacée, comme le prévoit la section 16, par une brique vérifiable de la même catégorie.

## Publier sur la plateforme

`briques.json` décrit chaque brique (titre, catégorie, langage, fichier publié, environnement de test). Le script lit le code et la fiche (README) et publie par l'API :

```bash
DEVBRIK_URL=http://localhost:8080 DEMO_MOT_DE_PASSE='…' node scripts/publier-briques-equipe.mjs --simulation   # contrôle sans rien envoyer
DEVBRIK_URL=http://localhost:8080 DEMO_MOT_DE_PASSE='…' node scripts/publier-briques-equipe.mjs              # publication
DEVBRIK_URL=http://localhost:8080 DEMO_MOT_DE_PASSE='…' node scripts/publier-briques-equipe.mjs --tester     # + « testé OK » par l'autre compte
```

N'utilisez `--tester` qu'après avoir exécuté les tests de chaque brique : le signal « testé » doit refléter un vrai test, et l'auteur ne peut pas tester sa propre brique (RG9).

## Avant publication

- Sources à confirmer par écrit (dossier 19, V8) : préfixes mobiles ARTP (B1), référence grammaticale précise (B3).
- Chaque brique écrite ou relue avec une IA est déclarée dans `docs/IA.md`.

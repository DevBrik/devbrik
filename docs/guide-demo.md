# Guide de démonstration (3 minutes, dossier section 5)

## Avant de commencer

- Backend démarré avec `ADMIN_PSEUDO`, `ADMIN_MOT_DE_PASSE` et `DEMO_MOT_DE_PASSE` : comptes `admin.demo`, `awa.dev` (Awa Diop), `moussa.dev` (Moussa Ndiaye) — voir `docs/demo-comptes.md`.
- Les dix briques de l'équipe publiées et testées (`scripts/publier-briques-equipe.mjs`, voir `docs/deploiement.md`, 5).
- Deux navigateurs (ou une fenêtre privée) : l'un pour Awa, l'autre pour Moussa puis l'administrateur.

## Déroulé

| Temps | Action | Ce que le jury voit |
| --- | --- | --- |
| 0:00 | Accueil `/briques`, taper « téléphone », filtre Langage = Java | Recherche après 300 ms, compteur, adresse conservée (`?q=téléphone&langage=java`) |
| 0:20 | Ouvrir « Validation et normalisation des numéros de téléphone sénégalais » | Code en texte brut, mode d'emploi numéroté, prérequis, licence MIT, version, tuiles Évaluation et Tests, dernière vérification |
| 0:35 | **Copier le code** | Notification « Code copié » |
| 0:45 | Se connecter (Moussa) depuis la fiche | Retour automatique sur la fiche |
| 0:55 | **Tester cette brique** : OK, environnement « Java 21, Windows 11 » ; puis **Donner mon avis** : 5 étoiles + commentaire | Tests OK et note mis à jour, avis affiché |
| 1:20 | **+ Publier une brique** : formulaire, licence, case des droits | Brique publiée, redirection vers sa fiche, présente dans la liste |
| 1:50 | Demandes → **+ Nouvelle demande** (Awa) : « Je cherche une brique pour… » | Demande ouverte |
| 2:05 | Moussa répond en liant une brique (champ « Lier une brique », suggestions) | Réponse avec « Consulter la brique → » |
| 2:20 | Awa clique **Accepter cette réponse** | Pastille « Résolue », réputation de Moussa + 3 dans le classement |
| 2:35 | Moussa **signale** une brique ; l'administrateur ouvre **Administration → Signalements**, **Examiner**, **Masquer la brique** | La brique disparaît de la liste publique ; elle apparaît dans « Briques masquées » avec **Republier** |
| 2:55 | Montrer le tableau de bord des statistiques et la vue mobile (barre basse) | Six tuiles ; navigation mobile |

## Points à mentionner

- Le signal « testé » ne compte que la **version actuelle** du code : modifier le code fait passer la version à n + 1 et remet les tests à zéro.
- DevBrik **n'exécute jamais** le code : il est stocké et affiché en texte brut (un `<script>` s'affiche tel quel).
- Licences permissives uniquement, confirmation des droits obligatoire, export et suppression du compte, hébergement à Madrid.
- Écart de contraste assumé et documenté (orange `#EA580C`, charte 3.2) : ne pas annoncer une conformité AA complète.

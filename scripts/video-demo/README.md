# Vidéo de démonstration automatique

`enregistrer-demo.mjs` pilote un navigateur sur DevBrik **en ligne** (https://devbrik.duckdns.org) et filme le parcours, avec un curseur visible, des légendes, et des écrans d'ouverture et de fin aux logos DevBrik (environ 3 min, 1280×720).

## Parcours filmé

1. Écran d'ouverture (fond blanc couvert de logos DevBrik qui glissent)
2. Visiteur : accueil, recherche « sans accents », fiche d'une brique, **Copier le code**
3. **fleury** (Fleury Ba) se connecte depuis la fiche, ouvre **Tester cette brique** et **Donner mon avis** sur la brique d'un autre membre
4. **bousso** (Bousso Diémé) : Mes briques, formulaire de publication, demandes, classement
5. **admin.demo** : statistiques et signalements
6. Aperçu mobile dans un téléphone Android standard 360 × 800
7. Écran de fin (adresse du site, stack, membres de l'équipe)

Par défaut, les fenêtres de test et d'avis sont remplies puis **annulées** : rien n'est enregistré sur le site. Avec `DEMO_ECRITURE=1`, le test « OK » et l'avis 5 étoiles sont réellement enregistrés : ne le faites que si le test de la brique a vraiment été exécuté.

## Lancer l'enregistrement (PowerShell)

```powershell
cd E:\GitHub\DevBrik\scripts\video-demo
npm install                      # une seule fois
npx playwright install chromium  # une seule fois
$env:DEMO_MDP_FLEURY = "<mot de passe de fleury>"
$env:DEMO_MDP_BOUSSO = "<mot de passe de bousso>"
$env:DEMO_MDP_ADMIN  = "<mot de passe de admin.demo>"
node enregistrer-demo.mjs
```

La vidéo est créée dans ce dossier : `devbrik-demo.webm`. Les mots de passe restent dans la session PowerShell : ils ne sont écrits ni dans le script ni dans Git.

## Options

| Variable | Rôle | Défaut |
|---|---|---|
| `DEMO_URL` | Adresse du site | `https://devbrik.duckdns.org` |
| `DEMO_RECHERCHE` | Mot recherché à l'accueil | `sans accents` |
| `DEMO_ENVIRONNEMENT` | Environnement saisi dans le test | `Java 21, Windows 11` |
| `DEMO_ECRITURE=1` | Enregistrer réellement le test et l'avis | désactivé |

## En cas de souci

- Une étape qui échoue affiche `⚠` et le script continue : la vidéo reste utilisable.
- MP4 : `ffmpeg -i devbrik-demo.webm -c:v libx264 -crf 23 -preset veryfast devbrik-demo.mp4`, ou exportez depuis Clipchamp.

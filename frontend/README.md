# DevBrik : frontend

Angular 21 + Angular Material 21, composants autonomes, signaux, routes chargées à la demande. Charte : `docs/DevBrik-design-system-v1.1.md`.

## Commandes

| Commande | Effet |
| --- | --- |
| `npm ci` | Installe les dépendances (versions du `package-lock.json`) |
| `npm start` | Serveur de développement sur http://localhost:4200, `/api` redirigé vers http://localhost:8080 (`proxy.conf.json`) |
| `npm test -- --watch=false` | Tests unitaires (Vitest) |
| `npm run build` | Compilation de production dans `dist/frontend/browser` (servie par Nginx) |

## Organisation

```
src/
  styles.scss              thème Material 3 + variables --db-* (charte 3 et 11)
  styles/_communs.scss     boutons .btn, champs .champ, pastilles
  styles/_utilitaires.scss pages, cartes, grilles, squelettes, .sr-only
  app/
    core/       api (modèles, erreurs, référentiels), auth (service, gardes, intercepteur), libellés, config
    layout/     en-tête, menu du compte, pied de page, barre mobile
    shared/     composants db-* réutilisables (voir docs/architecture.md)
    features/
      briques/  M01 liste, M02 fiche, M03 publier / modifier, M05 mes briques
      avis/     actions d'évaluation et avis de M02, dialogues, M11 signalements, M12 statistiques
      demandes/ M06 liste, M07 détail, M08 nouvelle demande, M09 profil, classement
      auth/     M04 connexion / inscription, M10 mon compte, M13 pages statiques
public/
  langages/   icônes Devicon (MIT)
  assets/     monogramme DevBrik
```

## Règles de code

- **Un composant = trois fichiers** : `nom.component.ts`, `nom.component.html`, `nom.component.scss` (`templateUrl`, `styleUrl`).
- Couleurs uniquement par les variables `--db-*` ; jamais `#EA580C` en dur (dossier 12.5).
- Contenu utilisateur en texte brut : jamais `innerHTML` ni `bypassSecurityTrust*` ; `white-space: pre-wrap` pour les sauts de ligne.
- Aucune action visible seulement au survol ; libellés visibles ; erreurs sous les champs, focus sur le premier champ invalide.
- Erreurs d'API : `core/api/erreurs-formulaire.ts` (`lireErreur`, `appliquerDetails`, `erreurSurChamp`, `messageChamp`).
- Aucune dépendance nouvelle sans l'accord du membre 1 ; licence ajoutée dans `docs/LICENCES.md`.
- Aucun `console.log`.

## Routes

| Route | Écran | Accès |
| --- | --- | --- |
| `/briques` | M01 liste | Public |
| `/briques/nouvelle` | M03 publier | Connecté |
| `/briques/mes-briques` | M05 mes briques | Connecté |
| `/briques/:id` | M02 fiche | Public |
| `/briques/:id/modifier` | M03 modifier | Auteur |
| `/demandes`, `/demandes/nouvelle`, `/demandes/:id` | M06, M08, M07 | Public, connecté, public |
| `/developpeurs/classement`, `/developpeurs/:id` | Classement, M09 | Public |
| `/compte/connexion`, `/compte/inscription`, `/compte/profil` | M04, M04, M10 | Public, public, connecté |
| `/compte/mon-profil` | Redirige vers son profil public | Connecté |
| `/infos/conditions`, `/infos/confidentialite`, `/infos/a-propos` | M13 | Public |
| `/admin/signalements`, `/admin/statistiques` | M11, M12 | Administrateur |
| `/acces-refuse`, `**` | M14 403, 404 | Public |

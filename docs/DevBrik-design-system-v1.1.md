# DevBrik : charte graphique et design system

*Version 1.1 du 4 novembre 2026 · remplace la version 1.0 · la **maquette finale** (Maquette_DevBrik_Finale.pdf) et ce document font foi pour l'interface*

Direction retenue : **B, « Terminal solaire »**. Cible : ordinateur d'abord, adapté à partir de 360 px. Thème clair uniquement.

## 0. Ce qui change par rapport à la version 1.0

| Sujet | Version 1.0 | Version 1.1 (maquette finale) |
| --- | --- | --- |
| Texte sur boutons orange | Sombre `#0F172A` obligatoire | **Blanc**, comme sur la maquette (écart accepté, voir 3.2) |
| Texte orange sur fond clair | Interdit | Autorisé pour titres, chiffres, lien actif et liens (écart accepté, voir 3.2) |
| Tailles de texte | 16 px minimum partout | 16 px pour le texte courant, les libellés de champs et les boutons ; 12 à 14 px pour les métadonnées, filtres et pagination (voir 4) |
| Logos de tiers | Interdits | Autorisés **uniquement** pour les icônes de langages (Devicon, licence MIT) ; aucun logo de partenaire, aucune photo de personne |
| Navigation | Classement absent en P1 | Classement présent dans la navigation (livré par le membre 4) |
| Annuler | Bouton secondaire | **Rouge**, comme sur la maquette |
| Actions « au survol » | Non précisé | **Interdites** : toute action est visible en permanence (clavier et tactile) |
| Mobile | Menu replié | Barre de navigation basse à 4 entrées (voir 7) |

Le fichier `logo.svg`, le monogramme DB et le zip d'icônes font partie des **assets** livrés avec la maquette.

## 1. Intention

DevBrik est une bibliothèque communautaire de briques de code réutilisables. L'identité « Terminal solaire » associe la précision d'un outil de développement à une interface lumineuse. Le bleu profond structure la navigation et les actions principales ; l'orange signale les actions importantes et les chiffres clés ; le bleu-vert apporte une nuance.

La brique est traitée par des modules géométriques simples (monogramme DB : le D est une brique, le B deux briques). Le logo ne reprend pas la silhouette d'une marque de jouets. L'interface reste sobre : pas de photos de personnes, pas de logos de partenaires, pas d'illustration décorative nécessaire à la compréhension.

## 2. Principes

1. Clarté avant décoration : une action primaire dominante par zone.
2. Code lisible : affiché comme texte brut, jamais interprété (RG18).
3. État explicite : un statut associe toujours un libellé, une icône et une couleur.
4. Confiance vérifiable : note, tests OK et KO et date de dernière vérification sont distincts.
5. Responsive réel : les mêmes contenus sont réorganisés, non supprimés, à 360 px.
6. Accessibilité : libellés visibles, focus visible, ordre clavier logique, cibles tactiles de 44 × 44 px au moins, **aucune action cachée derrière un survol**.
7. Pas de données inventées présentées comme réelles : les exemples de la maquette sont fictifs.

## 3. Couleurs

### 3.1 Palette

| Variable CSS | Valeur | Usage |
| --- | --- | --- |
| `--db-primaire` | `#1E3A8A` | Navigation, boutons primaires, liens, logo |
| `--db-orange` | `#EA580C` | **Fonds** : boutons d'accent, pastilles, puces |
| `--db-orange-texte` | `#EA580C` | **Texte** orange : lien actif, chiffres, titres de section. Variable séparée pour pouvoir passer à `#C2410C` (voir 3.2) |
| `--db-orange-bouton` | `#EA580C` | Fond des boutons orange. Même remarque |
| `--db-sarcelle` | `#0F766E` | Nuance secondaire, succès légers |
| `--db-encre` | `#0F172A` | Texte principal, fond du code et du pied de page |
| `--db-fond` | `#F8FAFC` | Fond de page |
| `--db-surface` | `#FFFFFF` | Cartes et champs |
| `--db-succes` | `#166534` | Testé OK, valeurs positives |
| `--db-erreur` | `#B91C1C` | Testé KO, suppression, annulation, erreurs |
| `--db-bordure` | `#64748B` | Contour des champs et des contrôles (3:1 au moins) |
| `--db-focus` | `#1D4ED8` | Anneau de focus, 3 px |

### 3.2 Contrastes mesurés

| Paire | Ratio | Verdict |
| --- | --- | --- |
| `#0F172A` sur blanc | 17,85:1 | AA |
| `#475569` sur blanc (texte secondaire) | 7,58:1 | AA |
| `#64748B` sur blanc (texte discret) | 4,76:1 | AA |
| Blanc sur `#1E3A8A` (bouton primaire) | 10,36:1 | AA |
| Blanc sur `#B91C1C` (rouge) | 6,47:1 | AA |
| Blanc sur `#166534` | 7,13:1 | AA |
| `#F8FAFC` sur `#0F172A` (code) | 17,06:1 | AA |
| `#0F172A` sur `#EA580C` | 5,02:1 | AA |
| **Blanc sur `#EA580C`** (boutons orange de la maquette) | **3,56:1** | **Écart assumé** : AA exige 4,5:1 pour du texte normal (3:1 seulement pour du texte large, 24 px ou 18,66 px en gras) |
| **`#EA580C` sur blanc** (lien actif, chiffres, titres orange) | **3,56:1** | **Écart assumé**, même raison |
| `#EA580C` sur `#0F172A` (copyright du pied de page) | 5,02:1 | AA |
| Blanc sur `#C2410C` | 5,18:1 | AA |

**Décision du chef de projet (4 novembre 2026)** : la maquette est conservée. Le texte blanc sur orange et le texte orange sur blanc ne respectent pas AA pour du texte normal. Atténuations obligatoires :
- libellés des boutons orange en graisse **600 au moins** et **16 px au moins** ;
- chiffres et titres orange en **grande taille** (24 px ou plus, ou 18,66 px en gras), ce qui les met au seuil de 3:1 ;
- l'orange n'est **jamais** le seul porteur d'une information (libellé et icône l'accompagnent) ;
- les trois variables `--db-orange`, `--db-orange-texte` et `--db-orange-bouton` sont **séparées** : si un audit ou le jury l'exige, passer `--db-orange-texte` et `--db-orange-bouton` à `#C2410C` corrige tous les écrans en une ligne, sans toucher aux composants.

Cet écart est listé dans le dossier de référence (section 19) comme **limite connue**. Ne pas le présenter comme conforme AA.

### 3.3 Règles d'usage

- Un statut n'est jamais indiqué par la couleur seule : libellé et icône sont obligatoires (voir 6.4).
- `#CBD5E1` est une bordure décorative (cartes), pas un contour de contrôle. Les champs utilisent `--db-bordure`.
- Les fonds pastel portent un texte foncé de la même teinte.
- Recalculer les ratios si l'opacité ou le fond réel changent.

## 4. Typographie

- Interface : **Inter**, variable, auto-hébergée (`@fontsource/inter`), licence SIL OFL 1.1. Repli : « Segoe UI », Arial, sans-serif.
- Mot-symbole « DevBrik » : texte réel, graisse forte (voir la maquette), jamais une image.
- Code uniquement : `ui-monospace`, `SFMono-Regular`, `Consolas`, `"Liberation Mono"`, monospace.
- Icônes : Material Icons (Apache 2.0), fichiers locaux (`material-icons`) ; icônes de langages : Devicon (MIT), fichiers locaux du zip d'assets. **Aucun chargement depuis Google Fonts ni un CDN.**

| Style | Taille | Graisse | Usage |
| --- | --- | --- | --- |
| H1 | 32 px | 700 | Titre de page |
| H2 | 24 px | 700 | Titre de section, chiffres des tuiles |
| H3 | 20 px | 600 | Titre de carte |
| Corps | 16 px | 400 | Texte courant, champs, boutons, libellés de champs (600) |
| Métadonnées | 14 px | 400 | Auteur, licence, dates, version |
| Filtres et pagination | 13 à 14 px | 500 à 600 | Listes déroulantes, boutons de page |
| Plancher | 12 px | 400 | Dates et mentions secondaires. **Rien en dessous** |
| Code | 14 à 15 px | 400 | Bloc de code, interligne 1,6 |

Le texte qui porte à lui seul une action ou une information (libellé de bouton, message d'erreur, titre) reste à 16 px au moins.

## 5. Espacements et grille

- Échelle de base : multiples de 4 px. Conteneur desktop : 1200 px au plus.
- Grille : 12 colonnes (desktop), 8 (tablette), 4 (mobile, marge de 16 px).
- Barre supérieure : 64 px desktop. Hauteur minimale des champs et boutons : 48 px. Zone tactile : 44 × 44 px au moins.
- Cartes : fond blanc, rayon 16 px, ombre très légère.
- Code : défilement horizontal autorisé, jamais de débordement de la page.

## 6. Composants

### 6.1 En-tête et pied de page

- **En-tête** : logo à gauche (monogramme + « DevBrik »), navigation centrale **Briques · Demandes · Classement** (lien actif en `--db-orange-texte`, graisse 600), à droite :
  - visiteur : bouton primaire « Se connecter » et bouton contour « S'inscrire » ;
  - connecté : pastille ronde avec les initiales, nom affiché, chevron, qui ouvre un menu (`mat-menu`) : **Mon profil**, **Mes briques**, **Mon compte**, **Administration** (administrateur seulement), **Se déconnecter**.
- **Pied de page** (fond `--db-encre`) : logo, phrase de présentation, colonnes EXPLORER (Découvrir les briques, Demandes, Classement), COMMUNAUTÉ (Publier une brique, Créer une demande, Mon profil), INFORMATIONS (À propos, Conditions d'utilisation, Confidentialité), puis le copyright en orange. Un lien qui exige une connexion renvoie un visiteur vers la connexion, puis revient sur la destination.

### 6.2 Boutons

| Type | Style | Usage |
| --- | --- | --- |
| Primaire | Fond `--db-primaire`, texte blanc | Action principale d'une zone (« Publier la brique », « Se connecter », « Voir la brique ») |
| Accent | Fond `--db-orange-bouton`, texte blanc (voir 3.2) | « Donner mon avis », « Modifier », « Voir la demande », « Accepter cette réponse », « Envoyer ma réponse », « Exporter mes données » |
| Contour | Bord `--db-primaire`, fond transparent | « S'inscrire », onglets inactifs, « Supprimer » secondaire dans un dialogue |
| Destructif et annulation | Fond `--db-erreur`, texte blanc | « Supprimer », « Signaler », « Masquer la brique », « Annuler », « Retirer l'acceptation » |
| Désactivé | Fond bleu pâle, texte blanc | « Précédent » en première page ; jamais cliquable |

Hauteur de 48 px ; pleine largeur sur mobile. États : repos, survol, appuyé, focus (anneau `--db-focus`), désactivé, chargement (indicateur plus texte « … en cours »).

### 6.3 Champs

- `mat-form-field` en apparence **outline**, coins arrondis, libellé visible au-dessus ; astérisque orange pour les champs obligatoires.
- Mot de passe : bouton œil (nom accessible « Afficher le mot de passe »), règles visibles sous le champ.
- Zone de texte : compteur de caractères (« 120 / 300 ») quand une limite existe.
- **Saisie par lignes** (Mode d'emploi, Prérequis) : champ « Entrer pour ajouter » ; chaque Entrée ajoute une ligne, affichée en pastille numérotée (mode d'emploi) ou simple (prérequis) ; une pastille se supprime au clavier (Retour arrière) ou par un bouton.
- Case à cocher et boutons radio : **état coché toujours visible** (coche ou point plein) ; bordure `--db-bordure` ; la case « droits » est obligatoire.
- Erreur : texte rouge sous le champ, en français, qui dit comment corriger ; focus sur le premier champ invalide.

### 6.4 Pastilles et statuts

| Pastille | Icône | Fond | Texte |
| --- | --- | --- | --- |
| Testé OK | coche | vert pâle | `--db-succes` |
| Testé KO | point d'exclamation | rose pâle | `--db-erreur` |
| Publiée | radio plein | bleu pâle | `--db-primaire` |
| Masquée | point plein | gris-bleu pâle | `#475569` |
| Ouverte | cercle vide | bleu pâle | `--db-primaire` |
| Acceptée / Résolue | coche | orange pâle | `--db-orange-texte` |
| Catégorie | aucune | blanc, contour bleu | `--db-primaire` |

Note : étoile et valeur écrite (« 4,8 sur 5 »), **jamais** une rangée d'étoiles seule. Dans les avis, les étoiles sont accompagnées d'un texte masqué visuellement mais lu par les lecteurs d'écran (« 5 sur 5 »).

Règle d'affichage « Testé » d'une brique (version actuelle seulement, RG9) : `testsOk > testsKo` : **Testé OK** ; `testsKo > testsOk` : **Testé KO** ; égalité non nulle : **Avis partagés** (neutre) ; aucun test : **Non testée** (neutre).

### 6.5 Carte de brique

Contenu, dans cet ordre : badge de langage (icône + nom en majuscules) et pastille « Testé » ; pastille de catégorie ; titre (H3) ; résumé ; ligne « Catégorie · Licence » ; ligne « Auteur · ★ note (n avis) » ; « Version n » ; bouton primaire « Voir la brique » à droite. Un champ absent reste absent (ni note ni date inventées). Aucune action n'apparaît au survol.

Sur la page **Mes briques**, la carte affiche **en permanence** la pastille de statut (Publiée ou Masquée) et trois boutons : **Voir**, **Modifier** (accent), **Supprimer** (destructif).

### 6.6 Badge de langage

Icône Devicon (`aria-hidden`, 24 px) **plus** le nom du langage en texte. SQL et Shell n'ont pas de logo : icônes Material `database` et `terminal`. Si le fichier manque, le nom seul s'affiche. L'icône ne porte jamais l'information seule.

### 6.7 Bloc de code

- Fond `--db-encre`, texte `#F8FAFC`, monospace, numéros de ligne, en-tête « </> Code source » et bouton « Copier le code » (retour « Code copié » par `mat-snack-bar`, annoncé aux lecteurs d'écran).
- Affichage en **texte brut** ; défilement horizontal ; aucun bouton d'exécution.
- En saisie (formulaire de publication) : même apparence, mais **zone éditable** avec compteur (limite 30 000 caractères).

### 6.8 Tuiles de chiffres

Carte à contour fin, valeur en H2 `--db-orange-texte` (24 px ou plus), libellé en 16 px. Utilisées pour : Évaluation et Tests (fiche), Mes briques, profil, statistiques, classement.

### 6.9 Boîtes de dialogue

`mat-dialog`, nomment l'objet concerné, bouton d'annulation visible, focus piégé, fermeture par Échap :
1. **Donner votre avis** : note 1 à 5 par étoiles cliquables (aussi au clavier, flèches), commentaire facultatif (2 000), boutons « Envoyer mon avis » et « Annuler ».
2. **Enregistrer un test** : résultat OK ou KO (boutons radio avec pastille), champ « Environnement (facultatif) » de 120 caractères, boutons « Enregistrer » et « Annuler ».
3. **Signaler ce contenu** : motifs en boutons radio (Contenu inapproprié, Code dangereux, Violation de droits d'auteur, Informations incorrectes, Spam ou publicité, Autre), description facultative (1 000), boutons « Enregistrer » et « Annuler ».
4. **Supprimer cette brique ?** : « Cette action est irréversible », boutons « Supprimer » (rouge) et « Annuler » (contour rouge).
5. **Supprimer mon compte ?** : demande le mot de passe, rappelle la suppression en cascade (RG17), boutons « Supprimer mon compte » et « Annuler ».

### 6.10 Pagination

Boutons « ← Précédent », numéros avec points de suspension (1 2 3 4 … 10), « Suivant → » ; page courante pleine (bleu), autres en contour. Réponse API : `page` à partir de 0, `taille` 20 par défaut, 50 au plus. Sur mobile : « Précédent », « Page 1 sur 10 », « Suivant ».

### 6.11 Notifications

`mat-snack-bar` (région `aria-live="polite"`) pour les confirmations (« Brique publiée », « Code copié ») et les erreurs récupérables. Jamais la couleur seule : texte obligatoire.

## 7. Mobile (360 px)

Pas de maquette dédiée : on applique les règles ci-dessous à la maquette de bureau.

- **Barre haute** : logo à gauche ; à droite, « Se connecter » (visiteur) ou la pastille d'initiales (connecté).
- **Barre de navigation basse**, fixe, 4 entrées avec icône et libellé : **Briques**, **Demandes**, **Classement**, **Compte** (« Compte » mène à la connexion pour un visiteur, ouvre le menu du compte sinon). Entrée active en `--db-orange-texte` et graisse 600. Cibles de 44 px au moins.
- Cartes en **une colonne** ; tuiles en une colonne (deux pour les statistiques) ; recherche et filtres **empilés** en pleine largeur ; boutons d'action en pleine largeur.
- Pagination réduite (6.10). Tableau du classement transformé en liste de cartes (rang, pseudo, points, détail en dessous).
- Pages de connexion et d'inscription : le panneau décoratif de droite est masqué.
- Boîtes de dialogue en pleine largeur ; code avec défilement horizontal ; aucun texte à moins de 12 px.
- Le pied de page passe en une colonne ; le bouton « Publier une brique » reste dans la page.

## 8. États transverses

| État | Présentation | Comportement |
| --- | --- | --- |
| Chargement | Squelette gris aux formes de la carte, ou indicateur avec texte (« Publication en cours… ») | Conserver le contexte, pas de saut de mise en page |
| Vide | Icône simple orange, titre, explication, action utile | Aucune zone blanche inexpliquée |
| Erreur réseau | Icône Wi-Fi barré, « Connexion impossible », « Vérifiez votre connexion Internet puis réessayez. », bouton « Réessayer » | Ne pas effacer les saisies |
| 401 | Icône personne, « Connexion requise », bouton **« Se connecter »** | Conserver la destination de retour |
| 403 | Icône d'interdiction, « Accès refusé », « Vous n'avez pas l'autorisation d'effectuer cette action. » | Aucune action inaccessible proposée (cette consigne n'est **pas** affichée) |
| 404 | Icône 404, « Page introuvable », « La ressource demandée n'existe pas ou n'est plus disponible. », bouton « Retour à l'accueil » | Une brique masquée vue par un tiers affiche cet état |
| Succès | Notification textuelle | Mettre à jour l'écran |
| Validation | Erreur près du champ, focus sur le premier champ invalide | |

Icône de l'état vide : icône Material **`inventory_2`** (boîte vide), orange. L'icône de brique à plots de la première maquette est abandonnée pour ne pas évoquer un jeu de construction de marque.

## 9. Accessibilité et clavier

- Focus visible sur tout élément interactif : anneau `--db-focus` de 3 px.
- Tab et Maj+Tab ; Entrée et Espace ; flèches dans les groupes de boutons radio, les étoiles et les menus ; Échap ferme menus et dialogues.
- Libellés visibles ; erreurs et aides associées au champ (`aria-describedby`).
- Icônes décoratives masquées aux lecteurs d'écran ; boutons icône avec nom accessible.
- Les états ne reposent jamais sur la couleur seule.
- Respecter `prefers-reduced-motion`.
- Les limites connues (3.2) sont déclarées, pas cachées.

## 10. Logo et assets

- Logo : monogramme **DB** blanc dans un carré arrondi `#1E3A8A` (le D est une brique, le B deux briques), puis le mot « DevBrik » en texte. En-tête : monogramme 40 px + mot-symbole. Favicon : monogramme seul.
- Fichiers fournis dans le zip d'assets : `logo.svg`, `favicon.svg` (si présents), icônes de langages (Devicon), icônes utiles. Chaque fichier utilisé est déclaré dans `docs/LICENCES.md`. Le logo a été généré avec un outil d'IA puis corrigé : le déclarer dans `docs/IA.md`.

## 11. Thème Angular Material (point de départ)

Voir `frontend/src/styles.scss` : thème Material 3, `primary` azure, `tertiary` orange, puis surcharges des variables `--mat-sys-*` et `--db-*` de la section 3. **Valider les mixins et noms de palettes contre la version d'Angular Material installée** (21.2.x) ; éviter les surcharges internes profondes.

```scss
@use '@angular/material' as mat;
@import '@fontsource/inter/400.css';
@import '@fontsource/inter/500.css';
@import '@fontsource/inter/600.css';
@import '@fontsource/inter/700.css';
@import 'material-icons/iconfont/material-icons.css';

html {
  color-scheme: light;
  @include mat.theme((
    color: (primary: mat.$azure-palette, tertiary: mat.$orange-palette, theme-type: light),
    typography: Inter,
    density: 0,
  ));

  --mat-sys-primary: #1E3A8A;
  --mat-sys-on-primary: #FFFFFF;
  --mat-sys-tertiary: #EA580C;
  --mat-sys-on-tertiary: #FFFFFF;   // version 1.1 : texte blanc sur orange (écart assumé, 3.2)
  --mat-sys-error: #B91C1C;

  --db-primaire: #1E3A8A;
  --db-orange: #EA580C;
  --db-orange-texte: #EA580C;
  --db-orange-bouton: #EA580C;
  --db-sarcelle: #0F766E;
  --db-encre: #0F172A;
  --db-fond: #F8FAFC;
  --db-surface: #FFFFFF;
  --db-succes: #166534;
  --db-erreur: #B91C1C;
  --db-bordure: #64748B;
  --db-focus: #1D4ED8;
}

body {
  margin: 0;
  background: var(--db-fond);
  color: var(--db-encre);
  font-family: 'Inter', 'Segoe UI', Arial, sans-serif;
  font-size: 16px;
  line-height: 1.5;
}

:focus-visible { outline: 3px solid var(--db-focus); outline-offset: 2px; }
.mat-mdc-form-field { width: 100%; }
.mat-mdc-raised-button, .mat-mdc-outlined-button, .mat-mdc-button { min-height: 48px; }
pre, code { font-family: ui-monospace, 'SFMono-Regular', Consolas, 'Liberation Mono', monospace; }
pre { overflow-x: auto; }
@media (prefers-reduced-motion: reduce) { * { animation: none !important; transition: none !important; } }
```

## 12. Correspondance avec l'API

Les composants consomment les routes et les champs de la **section 11 du dossier de référence** (version 1.1) : pagination (`page` à partir de 0, `taille` 20 par défaut, 50 au plus), statuts et codes d'erreur du catalogue 11.7. Les données de la maquette sont des exemples : la maquette ne constitue pas une réponse d'API.

## 13. Vérifications avant livraison

- [ ] Inter et icônes servis en local, licences conservées (`docs/LICENCES.md`).
- [ ] Aucune requête vers Google Fonts ou un CDN (onglet Réseau du navigateur).
- [ ] Les trois variables orange sont bien séparées et utilisées partout.
- [ ] Focus visible et parcours clavier testés sur les 15 écrans.
- [ ] Aucune action visible seulement au survol.
- [ ] Contrôles mobiles de 44 × 44 px ; aucun texte à moins de 12 px.
- [ ] Code rendu en texte brut, jamais exécuté.
- [ ] États chargement, vide, erreur, 401, 403, 404 testés.
- [ ] Responsive vérifié à 1280 px et à 360 px.
- [ ] Limite connue de contraste (3.2) présente dans la section 19 du dossier.

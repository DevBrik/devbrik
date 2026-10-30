# DevBrik : dossier de référence du projet

*Concours CADEV · équipe « Les Lions et les Lionnes du code » · version 1.0 du 28 octobre 2026 · à lire en entier avant de coder*

## 0. En bref

|  |  |
| --- | --- |
| **Projet** | DevBrik : la bibliothèque de briques de code locales, partagées, testées et notées par les développeurs africains |
| **Thème du concours** | « Construire la plateforme d'échange africaine, pour les devs, par les devs » |
| **Livrables** | Dépôt Git + démo en ligne hébergée sur Datacloud |
| **Limite de soumission** | Dimanche 15 novembre 2026, 17h00 (heure de Dakar). Objectif interne : jeudi 12 novembre |
| **Stack** | Angular 21 + Angular Material, Spring Boot 4.1.1 (Java 21), PostgreSQL 18, Flyway, Docker, Nginx |
| **Équipe** | 5 membres, un propriétaire par module (section 13) |

**Ce dossier fait foi** pour le périmètre (section 3), les règles de gestion (section 7), le modèle de données (section 10), le contrat d'API (section 11) et les propriétaires de dossiers (section 13). Aucun membre ne modifie ces parties seul : on en parle à l'équipe, puis on met ce dossier à jour.

**Ce qui n'est pas vérifié** et ne doit pas être présenté comme acquis : voir la section 19.

## 1. Contexte et objectifs

- **Thème officiel** (page « Vue d'ensemble » de la plateforme Systalink) : « Construire la plateforme d'échange africaine, pour les devs, par les devs. Livrez un projet utilisable et hébergé en ligne. »
- **Critères d'évaluation** (d'après le résumé du règlement utilisé par l'équipe, à relire dans l'article sur l'évaluation) : qualité technique 30 %, pertinence 25 %, UX et ergonomie 20 %, innovation 15 %, vote du public 10 %.
- **Obligations** : déploiement sur Datacloud avec au moins un produit Systalink ; déclaration des composants et de leurs licences (MIT, Apache 2.0 et BSD acceptées ; GPL, AGPL et LGPL exclues) ; déclaration de l'usage de l'IA ; projet développé pendant le concours ; vote des pairs obligatoire pour les 5 membres.
- **Calendrier officiel** (article 3, heure de Dakar) : soumission jusqu'au 15 novembre 17h ; vote des pairs du 15 novembre 18h au 16 novembre 23h59 ; vote du public du 17 au 20 novembre ; jury les 21 et 22 novembre ; résultats le 23 novembre.

**Objectif** : livrer d'ici le 12 novembre une plateforme **utilisable, fiable et déployée**, qui répond au thème à la lettre, avec **au moins 10 vraies briques** publiées et testées, pour que la démonstration ne montre jamais une plateforme vide.

## 2. Le produit

### 2.1 Le problème

Au Sénégal et dans beaucoup de pays d'Afrique, chaque développeur réécrit de zéro les mêmes briques locales : validation des numéros de téléphone, formats de montants en FCFA, montants en lettres, fuseau Africa/Dakar, vérification de signatures de rappels (webhooks) de paiement, gestion des réseaux instables. Ces solutions existent dans des dépôts dispersés, souvent non testées, non datées et sans licence claire.

### 2.2 La solution

Une **brique** est une unité de code autonome publiée avec : un titre et un résumé, une catégorie et un langage, le code, un mode d'emploi (installation, exemple d'appel, limites connues), une **licence permissive**, et des **preuves de fonctionnement** : notes, commentaires et signal « testé » **daté et lié à la version du code**.

Les développeurs peuvent aussi poster une **demande** (« je cherche une brique pour X ») à laquelle la communauté répond, avec un lien vers une brique. Une **réputation** simple récompense ceux qui publient et ceux qui aident.

### 2.3 Le public

Développeurs et étudiants en développement africains, en priorité francophones, qui construisent des applications destinées à des utilisateurs locaux.

### 2.4 Positionnement : « pourquoi pas GitHub ? »

Nous ne remplaçons ni GitHub ni Stack Overflow : DevBrik est un **catalogue ciblé et vérifié**.

| Ce que DevBrik apporte | Comment |
| --- | --- |
| Un contexte local | Catégories pensées pour les besoins d'ici (paiement mobile, formats FCFA, réseaux instables) |
| Une fiche structurée et comparable | Mêmes champs pour toutes les briques : prérequis, mode d'emploi, licence, version |
| Une confiance mesurable | Signal « testé » (OK ou KO) avec l'environnement, **valable uniquement pour la version actuelle du code**, et date de dernière vérification |
| Un échange | Demandes et réponses qui renvoient vers des briques ; réputation |
| Une licence claire | Seules des licences permissives sont acceptées ; l'auteur confirme ses droits |

### 2.5 Adéquation au thème

| Mot du thème | DevBrik |
| --- | --- |
| Plateforme d'échange | Les développeurs publient, cherchent, notent, testent et se répondent |
| Africaine | Besoins et contexte locaux ; profils avec pays |
| Pour les devs, par les devs | Le contenu est publié, testé et noté par des développeurs |
| Utilisable, hébergé en ligne | Démo en ligne sur Datacloud, avec du contenu réel |

### 2.6 Ce que nous ne promettons pas

- Aucune brique ne prétend « se connecter à Wave ou Orange Money » tant que l'équipe n'a pas la documentation officielle **et** un moyen de tester. Les premières briques sont des utilitaires vérifiables sans compte externe (section 16).
- Aucun lien avec un partenaire (par exemple Paxity) n'est affirmé avant vérification.
- DevBrik n'exécute **jamais** le code publié : il le stocke et l'affiche.

## 3. Périmètre

| Niveau | Contenu |
| --- | --- |
| **P0 : indispensable** | Comptes (pseudo et mot de passe, jeton) ; publier, modifier et supprimer une brique ; liste avec recherche, filtres et tris ; fiche d'une brique avec bouton « Copier le code » ; catégories et langages ; compte administrateur ; déploiement sur Datacloud avec HTTPS |
| **P1 : attendu pour la soumission** | Notes et commentaires ; « testé » OK ou KO avec environnement ; signalement et modération ; demandes et réponses avec acceptation ; profil public ; gestion du compte (profil, mot de passe, export, suppression) ; statistiques administrateur ; 10 briques réelles publiées |
| **P2 : seulement si tout le reste est fini** | Classement des contributeurs ; coloration syntaxique ; PWA installable ; notifications ; recherche insensible aux accents |
| **Hors périmètre** | Exécution ou test automatique du code publié ; paiement ; messagerie privée ; fichiers multiples par brique ; Markdown ou HTML dans les contenus ; envoi d'e-mails ; connexion par GitHub ; API publique ; application mobile native |

## 4. Rôles et droits

| Action | Visiteur | Développeur | Administrateur |
| --- | :-: | :-: | :-: |
| Consulter briques, demandes et profils | Oui | Oui | Oui |
| S'inscrire, se connecter | Oui | non applicable | non applicable |
| Publier une brique ; modifier ou supprimer les siennes | Non | Oui | Oui |
| Noter, commenter, tester, signaler la brique d'un autre | Non | Oui | Oui |
| Poster une demande, répondre, accepter une réponse à sa demande | Non | Oui | Oui |
| Masquer, republier ou supprimer n'importe quelle brique | Non | Non | Oui |
| Supprimer un avis, une demande ou une réponse quelconque | Non | Non | Oui |
| Traiter les signalements, voir les statistiques | Non | Non | Oui |

## 5. Parcours de démonstration (3 minutes)

1. Un visiteur arrive sur l'accueil, tape « téléphone » dans la recherche et filtre sur le langage Java.
2. Il ouvre une fiche : code, mode d'emploi, licence, note moyenne, nombre de « testé OK » pour la version actuelle, date de dernière vérification. Il clique sur **Copier le code** (notification de confirmation).
3. Il se connecte, déclare « testé : ça marche » avec son environnement, puis laisse une note et un commentaire.
4. Il publie une brique (formulaire avec licence et confirmation des droits), qui apparaît dans la liste.
5. Il poste une demande « je cherche une brique pour X » ; un second compte répond en liant une brique ; l'auteur accepte la réponse, la demande passe à **Résolue**, la réputation du répondant augmente.
6. L'administrateur ouvre un signalement et masque une brique ; elle disparaît de la liste publique.

**Comptes de démonstration à préparer** : deux comptes développeurs et un administrateur.

## 6. Histoires utilisateur

| ID | Rôle | Je veux… | Niveau |
| --- | --- | --- | --- |
| U1 | Visiteur | Parcourir les briques et ouvrir une fiche | P0 |
| U2 | Visiteur | Rechercher par mot-clé et filtrer par catégorie et langage | P0 |
| U3 | Visiteur | Trier par récentes, mieux notées, plus testées | P0 |
| U4 | Visiteur | Copier le code d'une brique en un clic | P0 |
| U5 | Visiteur | Créer un compte avec un pseudo | P0 |
| U6 | Utilisateur | Me connecter et me déconnecter | P0 |
| U7 | Développeur | Publier une brique avec licence et confirmation des droits | P0 |
| U8 | Développeur | Modifier ou supprimer mes briques | P0 |
| U9 | Développeur | Voir la liste de mes briques | P0 |
| U10 | Développeur | Noter et commenter la brique d'un autre | P1 |
| U11 | Développeur | Dire « testé : ça marche » ou « ça ne marche pas » avec mon environnement | P1 |
| U12 | Développeur | Signaler une brique problématique | P1 |
| U13 | Développeur | Poster une demande de brique | P1 |
| U14 | Développeur | Répondre à une demande en liant une brique | P1 |
| U15 | Développeur | Accepter la meilleure réponse à ma demande | P1 |
| U16 | Visiteur | Consulter les demandes ouvertes et résolues | P1 |
| U17 | Visiteur | Voir le profil public d'un développeur | P1 |
| U18 | Développeur | Modifier mon profil | P1 |
| U19 | Développeur | Changer mon mot de passe | P1 |
| U20 | Développeur | Exporter mes données | P1 |
| U21 | Développeur | Supprimer mon compte | P1 |
| U22 | Administrateur | Traiter les signalements (masquer ou conserver) | P1 |
| U23 | Administrateur | Masquer, republier ou supprimer une brique ; supprimer un avis, une demande ou une réponse | P1 |
| U24 | Administrateur | Voir des statistiques | P1 |
| U25 | Visiteur | Voir le classement des contributeurs | P2 |

## 7. Règles de gestion

| ID | Règle |
| --- | --- |
| RG1 | **Pseudo** : 3 à 30 caractères (lettres, chiffres, point, tiret, tiret bas), commence par une lettre ou un chiffre, unique sans tenir compte de la casse. Pseudos réservés refusés : admin, administrator, administrateur, root, support, moderateur, devbrik, equipe, systalink, datacloud |
| RG2 | **Mot de passe** : 8 à 72 caractères, haché avec BCrypt, jamais renvoyé ni journalisé. Le jeton est valable 12 heures |
| RG3 | **Contenus** : tout texte saisi est du texte brut. Longueurs : titre de brique 5 à 120, résumé 10 à 300, code 1 à 30 000, mode d'emploi 10 à 10 000, prérequis 500 au plus, commentaire d'avis 2 000 au plus, titre de demande 10 à 150, description de demande 20 à 5 000, réponse 5 à 5 000, détails d'un signalement 1 000 au plus |
| RG4 | **Licence** : choisie parmi MIT, APACHE\_2\_0, BSD\_2\_CLAUSE, BSD\_3\_CLAUSE, ISC, CC0\_1\_0. L'auteur doit confirmer qu'il a le droit de publier le code (droitsConfirmes vrai), sinon refus |
| RG5 | **Titre unique par auteur**, sans tenir compte de la casse |
| RG6 | **Version du code** : démarre à 1 et augmente de 1 à chaque modification du champ code. Modifier les autres champs ne la change pas |
| RG7 | **Visibilité** : une brique PUBLIEE est visible de tous ; une brique MASQUEE n'est visible que de son auteur et des administrateurs (404 pour les autres) et n'apparaît dans aucune liste publique ni recherche |
| RG8 | **Avis** : un par utilisateur et par brique (créé ou mis à jour par le même appel), note de 1 à 5, interdit sur sa propre brique. Note moyenne arrondie à une décimale, absente (null) sans avis |
| RG9 | **Test** : un par utilisateur et par brique, résultat OK ou KO, environnement facultatif (120 caractères), interdit sur sa propre brique. Le test enregistre la version du code au moment du vote ; **seuls les tests de la version actuelle sont comptés**. Un utilisateur peut changer son test |
| RG10 | **Dernière vérification** : date du test le plus récent de la version actuelle ; absente (null) s'il n'y en a aucun |
| RG11 | **Signalement** : un par utilisateur et par brique. Motifs : CODE\_DANGEREUX, DROITS\_AUTEUR, SPAM, INEXACT, AUTRE. Seul un administrateur le traite, avec deux actions : MASQUER\_BRIQUE ou CONSERVER |
| RG12 | **Demande** : statut OUVERTE ou RESOLUE ; RESOLUE si, et seulement si, une réponse est acceptée. Une seule réponse acceptée par demande ; seul l'auteur de la demande accepte, jamais sa propre réponse. Retirer l'acceptation rouvre la demande |
| RG13 | **Réponse** : peut renvoyer vers une brique PUBLIEE existante. Si la brique est supprimée ou masquée, le lien n'est plus renvoyé |
| RG14 | **Réputation** (calculée à la demande, jamais stockée) : 10 points par brique publiée + 5 points par test OK reçu (version actuelle) sur ses briques + 3 points par réponse acceptée |
| RG15 | **Quotas** par période glissante de 24 h et par utilisateur : 20 briques, 50 avis, 50 tests, 10 demandes, 50 réponses, 10 signalements. Dépassement : 429 LIMITE\_QUOTIDIENNE |
| RG16 | **Pagination** : page à partir de 0, taille par défaut 20, maximum 50. **Recherche** (paramètre q) : 2 à 100 caractères, insensible à la casse mais pas aux accents ; les caractères % \_ et le caractère d'échappement sont traités comme du texte |
| RG17 | **Suppression d'un compte** : supprime aussi ses briques, avis, tests, demandes, réponses et signalements (suppression en cascade dans la base). Un administrateur ne peut pas supprimer son compte |
| RG18 | **Le serveur n'exécute jamais le code d'une brique.** L'interface l'affiche dans un bloc de texte, jamais en HTML |
| RG19 | **Droits** : modifier une brique = son auteur seulement ; supprimer une brique = son auteur ou un administrateur ; supprimer un avis, une demande ou une réponse = son auteur ou un administrateur |

## 8. Exigences non fonctionnelles

| Sujet | Exigence |
| --- | --- |
| **Contenu utilisateur** | Stocké et affiché comme du texte. Angular échappe par défaut : interdiction d'utiliser innerHTML ou bypassSecurityTrust, interdiction d'ajouter du Markdown ou du HTML. Aucune exécution côté serveur |
| **Authentification** | BCrypt, jeton signé de 12 h, rôle dans le jeton. Un jeton invalide ou expiré donne 401, même sur une route publique : le frontend le supprime et réessaie sans jeton |
| **Autorisations** | Vérifiées côté serveur pour chaque action (auteur ou administrateur). Un test automatique par règle de la section 7 |
| **Anti-abus** | Quotas (RG15) ; tailles maximales (RG3) ; limitation du débit des routes d'authentification par Nginx |
| **En-têtes de sécurité** | Nginx : Content-Security-Policy restrictive (ressources de la même origine seulement), X-Content-Type-Options, Referrer-Policy, HSTS en HTTPS |
| **Secrets** | Variables d'environnement uniquement, jamais dans Git |
| **Journaux** | Jamais de mot de passe, de jeton ni de code de brique |
| **Dépendances** | MIT, Apache 2.0 ou BSD uniquement, y compris indirectes. Aucune dépendance nouvelle sans l'accord du membre 1. Chaque ajout est inscrit dans docs/LICENCES.md |
| **Données personnelles** | Pseudo obligatoire ; nom affiché, pays, bio et lien de profil facultatifs. Export et suppression du compte. Hébergement à Madrid : à écrire dans la page Confidentialité |
| **Performance** | Pagination partout ; index en base ; liste affichée en moins d'une seconde avec les données de démonstration ; chargement différé des routes |
| **Accessibilité** | Contraste AA, navigation au clavier, focus visible, libellés de champs, notifications annoncées aux lecteurs d'écran, texte courant de 16 px au moins |
| **Affichage** | Ordinateur d'abord (1280 px), correct jusqu'à 360 px |
| **Qualité** | Compilation sans erreur, tests des services et des autorisations, aucun console.log restant |
| **Conditions d'utilisation** | Page courte : l'auteur garantit avoir le droit de publier, reste soumis à la licence qu'il choisit et autorise la plateforme à stocker et afficher son contenu ; modération sur signalement. À faire relire : l'équipe n'est pas juriste, et le contenu publié par les utilisateurs doit rester utilisable si la plateforme change d'exploitant |
| **IA** | Tout usage d'outil d'IA (code, textes, logo, briques) est déclaré dans docs/IA.md |

## 9. Stack et architecture

### 9.1 Vue d'ensemble

```
Navigateur --HTTPS--> Nginx --+-- fichiers Angular (frontend)
                              +-- /api --> Spring Boot --> PostgreSQL
```

| Couche | Choix | Remarque |
| --- | --- | --- |
| Frontend | Angular 21, Angular Material, SCSS | Ordinateur d'abord ; routes à chargement différé ; PWA facultative (P2) |
| Backend | Spring Boot 4.1.1, Java 21 | Starters webmvc, data-jpa, validation, security, oauth2-resource-server (jeton), flyway, log4j2 |
| Base de données | PostgreSQL 18 | Migrations Flyway, ddl-auto=validate : une entité qui ne correspond pas aux tables empêche le démarrage |
| Journaux | Log4j2 (Apache 2.0) | Logback est exclu : chaque starter Spring exclut spring-boot-starter-logging |
| Déploiement | Docker Compose sur un Serveur cloud Datacloud (Ubuntu 24.04, Madrid) | Nginx, HTTPS, sauvegarde hebdomadaire |
| Services externes | **Aucun** | Pas d'API d'IA, de météo ni de SMS : pas de quota, pas de clé, pas de panne externe |

### 9.2 Structure du dépôt

```
devbrik/
  backend/          Spring Boot, paquet racine sn.devbrik.backend
  frontend/         Angular + Angular Material
  briques-equipe/   briques écrites par l'équipe, avec leurs tests (section 16)
  deploy/           Dockerfile, nginx.conf, docker-compose.yml, sauvegarde
  docs/             dossier de référence, api.md, LICENCES.md, IA.md, maquettes
  README.md
```

### 9.3 Paquets backend et propriétaires

| Paquet (sous sn.devbrik.backend) | Contenu | Propriétaire |
| --- | --- | --- |
| common | ApiException, PageReponse, IdentiteUtilisateur (lit l'identifiant et le rôle dans le jeton), ReferenceUtilisateur (pseudo et nom affiché) | Membre 1 |
| erreur | Gestionnaire d'erreurs global | Membre 1 |
| config | SecurityConfig, JwtConfig, JwtService, ReponseErreurSecurite | Membre 1 |
| auth | Entité Utilisateur, inscription, connexion, profil, compte (mot de passe, export, suppression), AdminInitialiseur | Membre 5 |
| brique | Brique, Categorie, Langage, lecture, modération, administration des briques | Membre 2 |
| avis | Avis, TestBrique, SignalementBrique, statistiques administrateur | Membre 3 |
| demande | Demande, Reponse | Membre 4 |
| developpeur | Profil public, classement, calcul de la réputation | Membre 4 |

Chaque paquet contient ses sous-paquets controller, service, repository, entity et dto.

### 9.4 Règles entre modules

1. **Aucune relation JPA entre modules** : on manipule des identifiants Long. Les clés étrangères existent en base.
2. Pour lire une donnée d'un autre module : utiliser le **service public de son propriétaire** (liste en section 13.4) ou une requête SQL en lecture seule.
3. Un module n'écrit **jamais** dans la table d'un autre module, sauf par le service public de son propriétaire.
4. Toutes les erreurs métier sont levées avec ApiException (statut, code du catalogue de la section 11.7, message en français). Le gestionnaire global les transforme en réponse. **Aucun module n'ajoute son propre gestionnaire d'erreurs.**
5. Aucune dépendance Maven ou npm sans l'accord du membre 1.

### 9.5 Ce qui est repris d'AgriLion

| À reprendre (copier, adapter, citer dans docs/IA.md) | À ne pas reprendre |
| --- | --- |
| Gestionnaire d'erreurs et ApiException ; configuration de sécurité (jeton, 401 et 403 au format de l'API) ; classes d'inscription et de connexion (passer du téléphone au pseudo) ; AdminInitialiseur ; lot Compte ; configuration Flyway, Log4j2 et pom.xml ; composants de notification et de dialogue ; structure de la collection Postman ; procédures Git, licences et IA | Cultures, fiches, météo, prix, diagnostic ; NumeroTelephone ; maquettes et visuels d'AgriLion (logo, photos) ; dépendances au service d'IA et à Open-Meteo |

### 9.6 Environnement local

| Élément | Valeur |
| --- | --- |
| PostgreSQL | Version 18, base devbrik, utilisateur devbrik, mot de passe devbrik\_dev (**local uniquement**) |
| Base de test | devbrik\_test, même utilisateur, créée vide (Flyway y crée les tables) |
| Backend | Spring Boot Dashboard, port 8080 |
| Frontend | npm start, port 4200, proxy /api vers 8080 |
| Secrets locaux | Fichier application-local.properties (ignoré par Git), jamais dans le dépôt |

## 10. Modèle de données

### 10.1 Entités

| Entité | Attributs |
| --- | --- |
| UTILISATEUR | id\_utilisateur, pseudo, nom\_affiche, mot\_de\_passe, role, pays, bio, lien\_profil, date\_creation |
| CATEGORIE | id\_categorie, slug, nom, description |
| LANGAGE | id\_langage, slug, nom |
| BRIQUE | id\_brique, titre, resume, code, mode\_emploi, prerequis, licence, statut, version\_code, date\_creation, date\_modification |
| AVIS | id\_avis, note, commentaire, date\_creation, date\_modification |
| TEST\_BRIQUE | id\_test, resultat, environnement, version\_code, date\_test |
| SIGNALEMENT\_BRIQUE | id\_signalement, motif, details, statut, action\_traitement, date\_creation, date\_traitement |
| DEMANDE | id\_demande, titre, description, statut, date\_creation |
| REPONSE | id\_reponse, contenu, acceptee, date\_creation |

### 10.2 Associations et cardinalités

| # | Association | Entité 1 | Card. | Entité 2 | Card. |
| --- | --- | --- | :-: | --- | :-: |
| 1 | PUBLIER | UTILISATEUR | (0,n) | BRIQUE | (1,1) |
| 2 | CLASSER | CATEGORIE | (0,n) | BRIQUE | (1,1) |
| 3 | ECRIRE\_EN | LANGAGE | (0,n) | BRIQUE | (1,1) |
| 4 | RECEVOIR\_AVIS | BRIQUE | (0,n) | AVIS | (1,1) |
| 5 | REDIGER\_AVIS | UTILISATEUR | (0,n) | AVIS | (1,1) |
| 6 | RECEVOIR\_TEST | BRIQUE | (0,n) | TEST\_BRIQUE | (1,1) |
| 7 | EFFECTUER\_TEST | UTILISATEUR | (0,n) | TEST\_BRIQUE | (1,1) |
| 8 | ETRE\_SIGNALEE | BRIQUE | (0,n) | SIGNALEMENT\_BRIQUE | (1,1) |
| 9 | SIGNALER | UTILISATEUR | (0,n) | SIGNALEMENT\_BRIQUE | (1,1) |
| 10 | POSER | UTILISATEUR | (0,n) | DEMANDE | (1,1) |
| 11 | CIBLER\_CATEGORIE | CATEGORIE | (0,n) | DEMANDE | (0,1) |
| 12 | CIBLER\_LANGAGE | LANGAGE | (0,n) | DEMANDE | (0,1) |
| 13 | RECEVOIR\_REPONSE | DEMANDE | (0,n) | REPONSE | (1,1) |
| 14 | REPONDRE | UTILISATEUR | (0,n) | REPONSE | (1,1) |
| 15 | ILLUSTRER | BRIQUE | (0,n) | REPONSE | (0,1) |

### 10.3 Script V1 : schéma (backend/src/main/resources/db/migration/V1\_\_schema\_initial.sql)

```sql
CREATE TABLE utilisateur (
    id_utilisateur  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pseudo          VARCHAR(30)  NOT NULL,
    nom_affiche     VARCHAR(80),
    mot_de_passe    VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'DEVELOPPEUR',
    pays            VARCHAR(60),
    bio             VARCHAR(500),
    lien_profil     VARCHAR(255),
    date_creation   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_utilisateur_role CHECK (role IN ('DEVELOPPEUR', 'ADMIN')),
    CONSTRAINT ck_utilisateur_pseudo CHECK (pseudo ~ '^[A-Za-z0-9][A-Za-z0-9._-]{2,29}$')
);
CREATE UNIQUE INDEX uk_utilisateur_pseudo ON utilisateur (lower(pseudo));

CREATE TABLE categorie (
    id_categorie  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    slug          VARCHAR(50) NOT NULL,
    nom           VARCHAR(80) NOT NULL,
    description   VARCHAR(255),
    CONSTRAINT uk_categorie_slug UNIQUE (slug),
    CONSTRAINT uk_categorie_nom UNIQUE (nom)
);

CREATE TABLE langage (
    id_langage  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    slug        VARCHAR(30) NOT NULL,
    nom         VARCHAR(50) NOT NULL,
    CONSTRAINT uk_langage_slug UNIQUE (slug),
    CONSTRAINT uk_langage_nom UNIQUE (nom)
);

CREATE TABLE brique (
    id_brique          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    titre              VARCHAR(120) NOT NULL,
    resume             VARCHAR(300) NOT NULL,
    code               TEXT         NOT NULL,
    mode_emploi        TEXT         NOT NULL,
    prerequis          VARCHAR(500),
    licence            VARCHAR(20)  NOT NULL,
    statut             VARCHAR(10)  NOT NULL DEFAULT 'PUBLIEE',
    version_code       INTEGER      NOT NULL DEFAULT 1,
    date_creation      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    date_modification  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    id_categorie       BIGINT       NOT NULL,
    id_langage         BIGINT       NOT NULL,
    id_auteur          BIGINT       NOT NULL,
    CONSTRAINT fk_brique_categorie FOREIGN KEY (id_categorie) REFERENCES categorie (id_categorie),
    CONSTRAINT fk_brique_langage   FOREIGN KEY (id_langage)   REFERENCES langage (id_langage),
    CONSTRAINT fk_brique_auteur    FOREIGN KEY (id_auteur)    REFERENCES utilisateur (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT ck_brique_licence CHECK (licence IN ('MIT', 'APACHE_2_0', 'BSD_2_CLAUSE', 'BSD_3_CLAUSE', 'ISC', 'CC0_1_0')),
    CONSTRAINT ck_brique_statut CHECK (statut IN ('PUBLIEE', 'MASQUEE')),
    CONSTRAINT ck_brique_code CHECK (char_length(code) BETWEEN 1 AND 30000),
    CONSTRAINT ck_brique_mode_emploi CHECK (char_length(mode_emploi) BETWEEN 10 AND 10000),
    CONSTRAINT ck_brique_version CHECK (version_code >= 1)
);
CREATE UNIQUE INDEX uk_brique_auteur_titre ON brique (id_auteur, lower(titre));
CREATE INDEX idx_brique_statut_date ON brique (statut, date_creation DESC);
CREATE INDEX idx_brique_categorie ON brique (id_categorie);
CREATE INDEX idx_brique_langage ON brique (id_langage);
CREATE INDEX idx_brique_auteur ON brique (id_auteur);

CREATE TABLE avis (
    id_avis            BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    note               INTEGER     NOT NULL,
    commentaire        VARCHAR(2000),
    date_creation      TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_modification  TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_brique          BIGINT      NOT NULL,
    id_auteur          BIGINT      NOT NULL,
    CONSTRAINT fk_avis_brique FOREIGN KEY (id_brique) REFERENCES brique (id_brique) ON DELETE CASCADE,
    CONSTRAINT fk_avis_auteur FOREIGN KEY (id_auteur) REFERENCES utilisateur (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT ck_avis_note CHECK (note BETWEEN 1 AND 5),
    CONSTRAINT uk_avis_brique_auteur UNIQUE (id_brique, id_auteur)
);
CREATE INDEX idx_avis_brique ON avis (id_brique);

CREATE TABLE test_brique (
    id_test        BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    resultat       VARCHAR(2)  NOT NULL,
    environnement  VARCHAR(120),
    version_code   INTEGER     NOT NULL,
    date_test      TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_brique      BIGINT      NOT NULL,
    id_auteur      BIGINT      NOT NULL,
    CONSTRAINT fk_test_brique FOREIGN KEY (id_brique) REFERENCES brique (id_brique) ON DELETE CASCADE,
    CONSTRAINT fk_test_auteur FOREIGN KEY (id_auteur) REFERENCES utilisateur (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT ck_test_resultat CHECK (resultat IN ('OK', 'KO')),
    CONSTRAINT uk_test_brique_auteur UNIQUE (id_brique, id_auteur)
);
CREATE INDEX idx_test_brique ON test_brique (id_brique);

CREATE TABLE signalement_brique (
    id_signalement      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    motif               VARCHAR(20)  NOT NULL,
    details             VARCHAR(1000),
    statut              VARCHAR(10)  NOT NULL DEFAULT 'OUVERT',
    action_traitement   VARCHAR(20),
    date_creation       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    date_traitement     TIMESTAMPTZ,
    id_brique           BIGINT       NOT NULL,
    id_auteur           BIGINT       NOT NULL,
    CONSTRAINT fk_signalement_brique FOREIGN KEY (id_brique) REFERENCES brique (id_brique) ON DELETE CASCADE,
    CONSTRAINT fk_signalement_auteur FOREIGN KEY (id_auteur) REFERENCES utilisateur (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT ck_signalement_motif CHECK (motif IN ('CODE_DANGEREUX', 'DROITS_AUTEUR', 'SPAM', 'INEXACT', 'AUTRE')),
    CONSTRAINT ck_signalement_statut CHECK (statut IN ('OUVERT', 'TRAITE')),
    CONSTRAINT ck_signalement_action CHECK (action_traitement IS NULL OR action_traitement IN ('MASQUER_BRIQUE', 'CONSERVER')),
    CONSTRAINT uk_signalement_brique_auteur UNIQUE (id_brique, id_auteur)
);
CREATE INDEX idx_signalement_statut ON signalement_brique (statut, date_creation DESC);

CREATE TABLE demande (
    id_demande     BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    titre          VARCHAR(150) NOT NULL,
    description    TEXT         NOT NULL,
    statut         VARCHAR(10)  NOT NULL DEFAULT 'OUVERTE',
    date_creation  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    id_categorie   BIGINT,
    id_langage     BIGINT,
    id_auteur      BIGINT       NOT NULL,
    CONSTRAINT fk_demande_categorie FOREIGN KEY (id_categorie) REFERENCES categorie (id_categorie),
    CONSTRAINT fk_demande_langage   FOREIGN KEY (id_langage)   REFERENCES langage (id_langage),
    CONSTRAINT fk_demande_auteur    FOREIGN KEY (id_auteur)    REFERENCES utilisateur (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT ck_demande_statut CHECK (statut IN ('OUVERTE', 'RESOLUE')),
    CONSTRAINT ck_demande_description CHECK (char_length(description) BETWEEN 20 AND 5000)
);
CREATE INDEX idx_demande_statut_date ON demande (statut, date_creation DESC);

CREATE TABLE reponse (
    id_reponse     BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    contenu        TEXT        NOT NULL,
    acceptee       BOOLEAN     NOT NULL DEFAULT FALSE,
    date_creation  TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_demande     BIGINT      NOT NULL,
    id_brique      BIGINT,
    id_auteur      BIGINT      NOT NULL,
    CONSTRAINT fk_reponse_demande FOREIGN KEY (id_demande) REFERENCES demande (id_demande) ON DELETE CASCADE,
    CONSTRAINT fk_reponse_brique  FOREIGN KEY (id_brique)  REFERENCES brique (id_brique) ON DELETE SET NULL,
    CONSTRAINT fk_reponse_auteur  FOREIGN KEY (id_auteur)  REFERENCES utilisateur (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT ck_reponse_contenu CHECK (char_length(contenu) BETWEEN 5 AND 5000)
);
CREATE INDEX idx_reponse_demande ON reponse (id_demande);
CREATE UNIQUE INDEX uk_reponse_acceptee ON reponse (id_demande) WHERE acceptee;
```

Ce script n'a pas été exécuté par son auteur : **le test de la section 10.6 le valide**.

### 10.4 Script V2 : référentiels (V2\_\_referentiels.sql)

```sql
INSERT INTO categorie (slug, nom, description) VALUES
 ('paiement-mobile', 'Paiement mobile', 'Utilitaires et intégrations liés aux paiements mobiles'),
 ('sms-notifications', 'SMS et notifications', 'Envoi de messages et notifications'),
 ('validation-donnees', 'Validation de données', 'Numéros, identifiants et formats locaux'),
 ('formats-devises', 'Formats, dates et devises', 'Montants en FCFA, dates et fuseaux horaires'),
 ('donnees-geographiques', 'Données géographiques', 'Régions, départements et communes'),
 ('securite-authentification', 'Sécurité et authentification', 'Signatures, jetons et bonnes pratiques'),
 ('reseau-instable', 'Réseaux instables et hors ligne', 'Réessais, reprises et compression'),
 ('outils', 'Outils divers', 'Autres utilitaires')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO langage (slug, nom) VALUES
 ('java', 'Java'), ('typescript', 'TypeScript'), ('javascript', 'JavaScript'),
 ('python', 'Python'), ('php', 'PHP'), ('sql', 'SQL'), ('csharp', 'C#'),
 ('kotlin', 'Kotlin'), ('dart', 'Dart'), ('go', 'Go'), ('shell', 'Shell')
ON CONFLICT (slug) DO NOTHING;
```

### 10.5 Correspondance JPA

- Clés : Long avec GenerationType.IDENTITY. Dates : Instant. Statuts, rôle, licence, motif, résultat : enum avec EnumType.STRING.
- note et version\_code sont des INTEGER (type Integer en Java) ; acceptee est un boolean.
- Colonnes TEXT : @Column(columnDefinition = TEXT) ; si la validation Hibernate se plaint au démarrage, corriger l'entité, pas la base.
- Les noms de colonnes sont ceux du SQL : @Column(name = ...) ou stratégie de nommage vérifiée au démarrage.

### 10.6 Migrations

| Numéro | Contenu | Propriétaire |
| --- | --- | --- |
| V1 | Schéma (script ci-dessus) | Membre 1 |
| V2 | Référentiels | Membre 1 |
| V3, V4, V5, V6 | Réservés aux membres 2, 3, 4, 5 en cas de besoin réel. **Demander un numéro au membre 1 avant de créer un fichier** | Membres 2 à 5 |

**Une fois V1 fusionnée, elle ne change plus.** Toute évolution du schéma passe par une nouvelle migration, annoncée à l'équipe. Le réglage spring.flyway.out-of-order=true est activé.

**Test de validation de V1 et V2** (membre 1, avant la fusion) : sur une base vide, démarrer le backend ; la console doit indiquer deux migrations appliquées ; la base doit contenir **9 tables plus flyway\_schema\_history**, 8 catégories et 11 langages. Puis tester les contraintes : pseudo invalide, doublon de pseudo en changeant la casse, note à 6, deuxième avis du même utilisateur sur la même brique, deux réponses acceptées pour une même demande, licence inconnue.


## 11. Contrat d'API

*Le contrat fait foi. Le backend l'implémente, le frontend le consomme, la collection Postman le teste. Toute modification est annoncée à l'équipe et reportée ici et dans docs/api.md.*

### 11.1 Conventions

| Sujet | Règle |
| --- | --- |
| Préfixe | Toutes les routes commencent par `/api` |
| Format | JSON en UTF-8, champs en camelCase, dates en ISO 8601 UTC (`2026-11-05T14:30:00Z`) |
| Authentification | En-tête `Authorization: Bearer <jeton>`. Jeton signé de 12 h contenant l'identifiant (sub) et le rôle |
| Jeton invalide ou expiré | 401 `NON_AUTHENTIFIE`, **même sur une route publique** ; le frontend supprime le jeton et réessaie sans jeton |
| Routes publiques (sans jeton) | Toutes les lectures (GET) de briques, catégories, langages, demandes, profils ; inscription et connexion |
| Pagination | Paramètres `page` (à partir de 0) et `taille` (défaut 20, max 50) ; réponse `PageReponse` (11.2) |
| Identifiants | Entiers (Long) dans les chemins et les corps |
| Champs inconnus | Ignorés en entrée ; valeurs `null` explicites en sortie pour les champs absents |
| Texte | Tout texte est brut (RG3, RG18). Les chaînes sont rognées (trim) côté serveur avant validation |

### 11.2 Formats communs

**PageReponse**
```json
{ "contenu": [ ], "page": 0, "taille": 20, "totalElements": 134, "totalPages": 7 }
```

**Erreur** (toutes les erreurs, y compris 401 et 403 de la sécurité)
```json
{
  "statut": 400,
  "code": "REQUETE_INVALIDE",
  "message": "Le titre doit contenir entre 5 et 120 caractères.",
  "details": [ { "champ": "titre", "message": "Entre 5 et 120 caractères" } ],
  "horodatage": "2026-11-05T14:30:00Z"
}
```
`details` n'est présent que pour les erreurs de validation (400). Les messages sont en français.

**ReferenceUtilisateur**
```json
{ "id": 12, "pseudo": "awa.dev", "nomAffiche": "Awa Diop" }
```

### 11.3 Authentification et compte (propriétaire : membre 5)

| Méthode | Route | Accès | Corps | Réponse |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/inscription` | Public | `{ pseudo, motDePasse, nomAffiche? }` | 201 `{ jeton, utilisateur }` |
| POST | `/api/auth/connexion` | Public | `{ pseudo, motDePasse }` | 200 `{ jeton, utilisateur }` |
| GET | `/api/moi` | Connecté | | 200 `Utilisateur` |
| PUT | `/api/moi` | Connecté | `{ nomAffiche?, pays?, bio?, lienProfil? }` | 200 `Utilisateur` |
| PUT | `/api/moi/mot-de-passe` | Connecté | `{ ancienMotDePasse, nouveauMotDePasse }` | 204 |
| GET | `/api/moi/export` | Connecté | | 200 JSON : profil, briques, avis, tests, demandes, réponses, signalements |
| DELETE | `/api/moi` | Connecté | `{ motDePasse }` | 204 (RG17) |

**Utilisateur** : `{ id, pseudo, nomAffiche, role, pays, bio, lienProfil, dateCreation }` (jamais de mot de passe).
Règles : RG1, RG2. La connexion échoue avec 401 `IDENTIFIANTS_INVALIDES` sans dire si le pseudo existe. Un administrateur reçoit 403 `SUPPRESSION_ADMIN_INTERDITE` sur `DELETE /api/moi`.

### 11.4 Briques (propriétaire : membre 2)

| Méthode | Route | Accès | Détail |
| --- | --- | --- | --- |
| GET | `/api/categories` | Public | 200 liste `{ id, slug, nom, description }` |
| GET | `/api/langages` | Public | 200 liste `{ id, slug, nom }` |
| GET | `/api/briques` | Public | Paramètres : `q`, `categorie` (slug), `langage` (slug), `auteur` (id), `tri` (`recentes` par défaut, `mieux-notees`, `plus-testees`), `page`, `taille`. Retourne des `BriqueResume` PUBLIEES uniquement |
| GET | `/api/briques/{id}` | Public | `BriqueDetail`. MASQUEE : visible de l'auteur et des administrateurs, sinon 404 (RG7) |
| POST | `/api/briques` | Développeur | 201 `BriqueDetail` |
| PUT | `/api/briques/{id}` | Auteur | 200 `BriqueDetail` ; la version du code augmente si le code change (RG6) |
| DELETE | `/api/briques/{id}` | Auteur ou admin | 204 |
| GET | `/api/moi/briques` | Connecté | Page de `BriqueResume`, y compris MASQUEES |
| PUT | `/api/admin/briques/{id}/statut` | Admin | `{ statut: "PUBLIEE" \| "MASQUEE" }` → 200 `BriqueDetail` |

**Corps de création et de modification**
```json
{
  "titre": "Validation des numéros sénégalais",
  "resume": "Valide et normalise un numéro de téléphone sénégalais au format E.164.",
  "code": "…",
  "modeEmploi": "…",
  "prerequis": "Java 21",
  "licence": "MIT",
  "idCategorie": 3,
  "idLangage": 1,
  "droitsConfirmes": true
}
```
Règles : RG3, RG4, RG5, RG6, RG15 (20 briques par 24 h), RG16 (recherche). `droitsConfirmes` doit être `true`, sinon 400 `DROITS_NON_CONFIRMES`.

**BriqueResume** : `{ id, titre, resume, licence, statut, versionCode, dateCreation, dateModification, categorie: {slug, nom}, langage: {slug, nom}, auteur: ReferenceUtilisateur, noteMoyenne, nombreAvis, testsOk, testsKo, derniereVerification }`
**BriqueDetail** : tous les champs de `BriqueResume` plus `code`, `modeEmploi`, `prerequis`, `idCategorie`, `idLangage`.
`noteMoyenne` (une décimale) et `derniereVerification` valent `null` sans avis ou sans test de la version actuelle ; `testsOk` et `testsKo` ne comptent que la version actuelle (RG9, RG10).

### 11.5 Avis, tests, signalements, administration (propriétaire : membre 3)

| Méthode | Route | Accès | Corps | Réponse |
| --- | --- | --- | --- | --- |
| GET | `/api/briques/{id}/avis` | Public | | 200 page d'`Avis` (récents d'abord) |
| PUT | `/api/briques/{id}/avis` | Développeur | `{ note, commentaire? }` | 201 si créé, 200 si mis à jour |
| DELETE | `/api/avis/{idAvis}` | Auteur ou admin | | 204 |
| GET | `/api/briques/{id}/tests` | Public | | 200 page de `Test` de la version actuelle |
| PUT | `/api/briques/{id}/test` | Développeur | `{ resultat: "OK" \| "KO", environnement? }` | 201 ou 200 |
| DELETE | `/api/briques/{id}/test` | Développeur | | 204 (retire son propre test) |
| GET | `/api/briques/{id}/moi` | Connecté | | 200 `{ monAvis, monTest }` (chacun peut être `null`) |
| POST | `/api/briques/{id}/signalements` | Développeur | `{ motif, details? }` | 201 |
| GET | `/api/admin/signalements` | Admin | `statut` (`OUVERT` par défaut), `page`, `taille` | 200 page de `Signalement` |
| PUT | `/api/admin/signalements/{id}/traitement` | Admin | `{ action: "MASQUER_BRIQUE" \| "CONSERVER" }` | 200 `Signalement` |
| GET | `/api/admin/statistiques` | Admin | | 200 `Statistiques` |

- **Avis** : `{ id, note, commentaire, auteur: ReferenceUtilisateur, dateCreation, dateModification }`
- **Test** : `{ id, resultat, environnement, versionCode, auteur: ReferenceUtilisateur, dateTest }`
- **Signalement** : `{ id, motif, details, statut, actionTraitement, dateCreation, dateTraitement, brique: {id, titre, statut}, auteur: ReferenceUtilisateur }`
- **Statistiques** : `{ utilisateurs, briquesPubliees, briquesMasquees, avis, tests, demandesOuvertes, demandesResolues, signalementsOuverts }`

Règles : RG8, RG9, RG10, RG11, RG15. Un avis ou un test sur sa propre brique donne 403 `AUTO_EVALUATION_INTERDITE`. Un avis, un test ou un signalement sur une brique MASQUEE visible de l'appelant est refusé comme sur une brique inexistante (404) pour un non-auteur ; pour l'auteur, 403 `AUTO_EVALUATION_INTERDITE`. Le traitement `MASQUER_BRIQUE` appelle `BriqueService.masquer` (13.4) puis passe le signalement à `TRAITE`.

### 11.6 Demandes, réponses, développeurs (propriétaire : membre 4)

| Méthode | Route | Accès | Corps | Réponse |
| --- | --- | --- | --- | --- |
| GET | `/api/demandes` | Public | Paramètres `statut`, `categorie`, `langage`, `q`, `page`, `taille` | 200 page de `DemandeResume` (récentes d'abord) |
| GET | `/api/demandes/{id}` | Public | | 200 `DemandeDetail` avec ses réponses |
| POST | `/api/demandes` | Développeur | `{ titre, description, idCategorie?, idLangage? }` | 201 `DemandeDetail` |
| DELETE | `/api/demandes/{id}` | Auteur ou admin | | 204 |
| POST | `/api/demandes/{id}/reponses` | Développeur | `{ contenu, idBrique? }` | 201 `Reponse` |
| DELETE | `/api/reponses/{id}` | Auteur ou admin | | 204 |
| PUT | `/api/reponses/{id}/acceptation` | Auteur de la demande | | 200 `DemandeDetail` (statut RESOLUE) |
| DELETE | `/api/reponses/{id}/acceptation` | Auteur de la demande | | 200 `DemandeDetail` (statut OUVERTE) |
| GET | `/api/developpeurs/{id}` | Public | | 200 `ProfilPublic` |
| GET | `/api/developpeurs/classement` | Public (P2) | `page`, `taille` | 200 page de `{ developpeur: ReferenceUtilisateur, reputation }` |

- **DemandeResume** : `{ id, titre, statut, dateCreation, categorie, langage, auteur: ReferenceUtilisateur, nombreReponses }`
- **DemandeDetail** : `DemandeResume` plus `description` et `reponses: Reponse[]`
- **Reponse** : `{ id, contenu, acceptee, dateCreation, auteur: ReferenceUtilisateur, brique: { id, titre } | null }` (`brique` vaut `null` si le lien n'est plus valide, RG13)
- **ProfilPublic** : `{ id, pseudo, nomAffiche, pays, bio, lienProfil, dateCreation, reputation, nombreBriques, nombreReponsesAcceptees }`

Règles : RG12, RG13, RG14, RG15. Accepter sa propre réponse : 403 `ACCEPTATION_PROPRE_REPONSE`. Accepter quand une autre réponse l'est déjà : 409 `REPONSE_DEJA_ACCEPTEE`. Le passage de la demande à RESOLUE (ou OUVERTE) et de la réponse à `acceptee` se fait dans une seule transaction.

### 11.7 Catalogue des erreurs

| Code | Statut | Quand |
| --- | :-: | --- |
| REQUETE_INVALIDE | 400 | Validation d'un champ (avec `details`), paramètre mal formé |
| DROITS_NON_CONFIRMES | 400 | `droitsConfirmes` absent ou faux (RG4) |
| BRIQUE_LIEE_INVALIDE | 400 | Réponse liée à une brique inexistante ou non publiée (RG13) |
| MOT_DE_PASSE_INCORRECT | 400 | Ancien mot de passe ou confirmation de suppression erroné |
| PSEUDO_RESERVE | 400 | Pseudo de la liste réservée (RG1) |
| NON_AUTHENTIFIE | 401 | Jeton absent, invalide ou expiré sur une route protégée, ou jeton invalide sur une route publique |
| IDENTIFIANTS_INVALIDES | 401 | Connexion refusée |
| ACCES_REFUSE | 403 | Action réservée à l'auteur ou à l'administrateur (RG19) |
| AUTO_EVALUATION_INTERDITE | 403 | Avis ou test sur sa propre brique (RG8, RG9) |
| ACCEPTATION_PROPRE_REPONSE | 403 | Accepter sa propre réponse (RG12) |
| SUPPRESSION_ADMIN_INTERDITE | 403 | Un administrateur supprime son compte (RG17) |
| RESSOURCE_INTROUVABLE | 404 | Identifiant inconnu, ou brique MASQUEE vue par un non-autorisé (RG7) |
| PSEUDO_DEJA_UTILISE | 409 | Pseudo pris, casse ignorée |
| TITRE_DEJA_UTILISE | 409 | Titre déjà utilisé par le même auteur (RG5) |
| SIGNALEMENT_DEJA_ENVOYE | 409 | Deuxième signalement du même utilisateur sur la même brique (RG11) |
| REPONSE_DEJA_ACCEPTEE | 409 | Une autre réponse est déjà acceptée (RG12) |
| LIMITE_QUOTIDIENNE | 429 | Quota dépassé (RG15) |
| ERREUR_INTERNE | 500 | Erreur inattendue ; message générique, rien de sensible |

Un nouveau code est ajouté **ici d'abord** (accord du membre 1), puis utilisé.

---

## 12. Maquettes et design (tâche du lead technique, membre 1)

Le membre 1 ne reçoit **pas de fonctionnalité** : il dessine les maquettes que les membres 2 à 5 implémentent, en plus du socle et de la revue. Les maquettes sont le contrat visuel au même titre que le contrat d'API.

### 12.1 Livrables

| Livrable | Contenu | Échéance |
| --- | --- | --- |
| Design system | Couleurs (contraste AA vérifié), typographie (16 px au moins pour le texte courant), espacements, boutons, champs, cartes, pastilles de statut, notifications, boîtes de dialogue, états vide / chargement / erreur | Samedi 31 octobre |
| Lot A : parcours P0 | M01, M02, M03, M04, M05 (écran large 1280 px) | Dimanche 1er novembre |
| Lot B : parcours P1 | M06 à M12 | Mardi 3 novembre |
| Lot C : mobile et états | Version 360 px de M01, M02, M04, M06 ; M13 et M14 | Mercredi 4 novembre |

Chaque maquette est exportée en image (PNG) dans `docs/maquettes/` avec le nom de l'écran (`M02-fiche-brique.png`) et accompagnée d'une courte note : données affichées, actions, route d'API utilisée, états à prévoir. Outil libre au choix (Figma gratuit, Penpot, Excalidraw) ; **aucune image ni icône sous licence incompatible** (déclarer dans docs/LICENCES.md ; Angular Material Icons sont sous Apache 2.0).

### 12.2 Liste des écrans

| ID | Écran | Contenu et actions | API | Implémente |
| --- | --- | --- | --- | --- |
| M01 | Accueil et liste des briques | Barre de recherche, filtres catégorie et langage, tri, cartes de briques (titre, résumé, langage, catégorie, note, « testé OK »), pagination | 11.4 | Membre 2 |
| M02 | Fiche d'une brique | Code dans un bloc de texte avec **Copier le code**, mode d'emploi, prérequis, licence, version, note moyenne, tests OK et KO de la version actuelle, dernière vérification, avis, boutons Noter, Tester, Signaler | 11.4, 11.5 | Membre 2 (fiche), membre 3 (avis, tests, signalement) |
| M03 | Publier ou modifier une brique | Formulaire avec compteurs de caractères, choix de licence, case de confirmation des droits | 11.4 | Membre 2 |
| M04 | Inscription et connexion | Deux formulaires, messages d'erreur clairs, règles du pseudo et du mot de passe visibles | 11.3 | Membre 5 |
| M05 | Mes briques | Liste avec statut (publiée, masquée), modifier, supprimer | 11.4 | Membre 2 |
| M06 | Liste des demandes | Onglets Ouvertes et Résolues, filtres, bouton Nouvelle demande | 11.6 | Membre 4 |
| M07 | Détail d'une demande | Description, réponses avec lien vers la brique, bouton Accepter pour l'auteur, formulaire de réponse | 11.6 | Membre 4 |
| M08 | Nouvelle demande | Formulaire (titre, description, catégorie et langage facultatifs) | 11.6 | Membre 4 |
| M09 | Profil public | Pseudo, pays, bio, lien, réputation avec détail du calcul, briques publiées | 11.6, 11.4 | Membre 4 |
| M10 | Mon compte | Profil, mot de passe, export, suppression avec confirmation | 11.3 | Membre 5 |
| M11 | Administration : signalements | Liste des signalements ouverts, aperçu de la brique, boutons Masquer et Conserver ; gestion des briques masquées | 11.5, 11.4 | Membre 3 |
| M12 | Administration : statistiques | Compteurs de `Statistiques` | 11.5 | Membre 3 |
| M13 | Pages statiques | Conditions d'utilisation, Confidentialité (hébergement à Madrid), À propos | aucune | Membre 5 |
| M14 | États transverses | Liste vide, chargement, erreur réseau, 401 / 403 / 404, notification de confirmation | aucune | Membre 1 |

### 12.3 Principes de design

- Ordinateur d'abord (1280 px), correct jusqu'à 360 px ; navigation haute avec : Briques, Demandes, (Classement en P2), connexion ou menu du compte.
- Une identité simple et constante : une couleur principale, une couleur d'accent, une police lisible pour le texte et une police à chasse fixe **uniquement** pour le code.
- Le code est toujours affiché dans un bloc de texte brut (RG18). Pas de rendu HTML ni Markdown.
- Chaque état d'un écran est dessiné : vide, chargement, erreur, succès.
- Focus visible, ordre de tabulation logique, libellés de champs, contraste AA.
- Le nom, le logo et les couleurs de DevBrik sont créés par l'équipe ; si un outil d'IA les génère, c'est déclaré dans docs/IA.md.

---

## 13. Découpage par membre

### 13.1 Principes

1. **Un propriétaire par module**, backend **et** frontend. Chacun livre son module de bout en bout : API, écrans, tests.
2. Le membre 1 est le **lead technique** : socle, sécurité, déploiement, revue des pull requests, intégration, **et maquettes** (section 12). Il ne prend aucune fonctionnalité métier.
3. Le backend d'un module est livrable sans les maquettes (le contrat d'API suffit) ; le frontend démarre à la livraison de la maquette correspondante.
4. On ne modifie pas un dossier dont on n'est pas propriétaire sans prévenir son propriétaire (13.6).

### 13.2 Tableau récapitulatif

| Membre | Rôle | Backend | Frontend | Briques de l'équipe (section 16) |
| --- | --- | --- | --- | --- |
| **1** | Lead technique, architecture, déploiement, **maquettes** | common, erreur, config, migrations V1 et V2, QuotaService | Coquille (en-tête, pied de page, routes à chargement différé), thème Material, intercepteurs, gardes, composants partagés (notification, dialogue, états), M14 | B1 (relecture globale) |
| **2** | Briques | brique (catégories, langages, CRUD, recherche, modération) | M01, M02 (hors avis et tests), M03, M05 | B2, B3, B4 |
| **3** | Confiance et modération | avis (avis, tests, signalements, statistiques) | Blocs avis, test et signalement de M02 ; M11, M12 | B5, B6 |
| **4** | Échange | demande (demandes, réponses), developpeur (profil, réputation, classement) | M06, M07, M08, M09 | B7, B8 |
| **5** | Comptes et conformité | auth (inscription, connexion, profil, compte, AdminInitialiseur) | M04, M10, M13 | B9, B10 |

### 13.3 Détail par membre

**Membre 1 : lead technique (Fleury)**
- Backend : dépôt et squelette Spring Boot, `ApiException`, gestionnaire d'erreurs global, `PageReponse`, `IdentiteUtilisateur`, `ReferenceUtilisateur`, sécurité (jeton 12 h, 401 et 403 au format de l'API), Log4j2, Flyway V1 et V2 avec le test de la section 10.6, `QuotaService` (13.4).
- Frontend : projet Angular 21 avec Angular Material, thème issu du design system, intercepteur de jeton (suppression du jeton sur 401), garde de connexion, garde administrateur, composants partagés, M14.
- Déploiement : Dockerfile, `docker-compose.yml`, Nginx (en-têtes de sécurité, limitation du débit de `/api/auth`), HTTPS, sauvegarde hebdomadaire, serveur Datacloud (section 18).
- Documents : docs/api.md, docs/LICENCES.md, docs/IA.md (structure), collection Postman.
- **Maquettes** : section 12.
- Revue de toutes les pull requests et intégration quotidienne.

**Membre 2 : briques**
- Backend : entités `Brique`, `Categorie`, `Langage`, lecture, recherche avec les trois tris (RG16), création, modification avec version du code (RG6), suppression, administration du statut (RG7, RG19), `BriqueService` public (13.4), quota RG15 des briques.
- Les tris `mieux-notees` et `plus-testees` s'appuient sur `StatsBriqueService` du membre 3 ou sur une requête SQL **en lecture seule** (règle 2 de 9.4).
- Frontend : M01, M02 (en-tête de fiche, code, copie, mode d'emploi), M03, M05.
- Tests : un test par règle RG3 à RG7 et RG19 concernant les briques ; test de la recherche avec les caractères `%` et `_`.

**Membre 3 : avis, tests, signalements, statistiques**
- Backend : entités `Avis`, `TestBrique`, `SignalementBrique` ; création ou mise à jour d'un avis et d'un test par le même appel ; version du code lue via `BriqueService` ; `StatsBriqueService` public ; traitement des signalements ; statistiques administrateur par requêtes SQL en lecture seule ; quotas RG15 (avis, tests, signalements).
- Frontend : blocs de M02 (notation, « testé » avec environnement, signalement), M11, M12.
- Tests : RG8, RG9, RG10, RG11 ; scénario « le code change, les tests de l'ancienne version ne comptent plus ».

**Membre 4 : demandes, réponses, développeurs**
- Backend : entités `Demande`, `Reponse` ; acceptation et retrait atomiques (RG12) ; lien vers une brique publiée (RG13) ; `ReputationService` (RG14, calcul à la demande) ; profil public ; classement (P2) ; quotas RG15 (demandes, réponses).
- Frontend : M06, M07, M08, M09.
- Tests : RG12 (y compris la contrainte d'unicité de l'acceptation), RG13, RG14.

**Membre 5 : comptes et conformité**
- Backend : entité `Utilisateur`, inscription et connexion (adaptées d'AgriLion : du téléphone au pseudo), profil, mot de passe, export, suppression (RG17), `AdminInitialiseur` (identifiants du premier administrateur lus dans les variables d'environnement), `UtilisateurService` public (13.4).
- Frontend : M04, M10, M13 (textes des Conditions et de la Confidentialité à faire relire, section 19).
- Tests : RG1, RG2, RG17, RG19 concernant les comptes.
- Données de démonstration : script ou procédure qui crée les deux comptes développeurs et l'administrateur (section 5).

### 13.4 Services publics entre modules

Un module qui lit une donnée d'un autre module passe **uniquement** par ces services (règle 2 de 9.4).

| Service (propriétaire) | Méthodes publiques | Utilisé par |
| --- | --- | --- |
| `UtilisateurService` (5) | `references(Collection<Long> ids) : Map<Long, ReferenceUtilisateur>` ; `existe(Long id)` | 2, 3, 4 |
| `BriqueService` (2) | `resume(Long id) : Optional<BriqueResumeInterne>` (id, idAuteur, statut, versionCode, titre) ; `references(Collection<Long> ids)` (id, titre, publiée ou non) ; `masquer(Long id)` ; `compterPubliees(Long idAuteur)` | 3, 4 |
| `StatsBriqueService` (3) | `statistiques(Collection<Long> ids) : Map<Long, StatsBrique>` (noteMoyenne, nombreAvis, testsOk, testsKo, derniereVerification) ; `compterTestsOkRecus(Long idAuteur)` (version actuelle) | 2, 4 |
| `DemandeService` (4) | `compterReponsesAcceptees(Long idAuteur)` | 4 (réputation) |
| `QuotaService` (1) | `verifier(TypeQuota type, long actionsDansLes24h)` : lève `ApiException` 429 `LIMITE_QUOTIDIENNE` | 2, 3, 4 |

Chaque module compte ses propres actions sur 24 h glissantes avec sa propre table, puis appelle `QuotaService.verifier`. Un changement de signature est annoncé avant la fusion.

### 13.5 Organisation du frontend

```
frontend/src/app/
  core/          intercepteurs, gardes, services d'API de base      (membre 1)
  shared/        composants partagés, thème                        (membre 1)
  features/
    briques/     M01, M02, M03, M05                                (membre 2)
    avis/        blocs de M02, M11, M12                            (membre 3)
    demandes/    M06, M07, M08, M09                                (membre 4)
    auth/        M04, M10, M13                                     (membre 5)
```
Chaque `feature` est chargée à la demande. Les interfaces TypeScript (`Brique`, `Avis`, etc.) reproduisent le contrat de la section 11 ; le propriétaire du module les écrit, les autres les importent.

### 13.6 Règles de travail en commun

- **Branches** : `main` est protégée (aucun commit direct). Chaque tâche : `feature/m<numéro>-<sujet>`, par exemple `feature/m2-recherche`. Une pull request relue par le membre 1 avant la fusion (le membre 1 est relu par le membre 2).
- **Commits** : messages en français, à l'impératif (« Ajoute la recherche par catégorie »).
- **Définition de « terminé »** : compile sans erreur ; tests du module passent ; aucun `console.log` ; contrat d'API respecté ; fichiers et licences à jour ; pull request relue.
- **Point quotidien** : 10 minutes, mêmes questions : ce qui est fait, ce qui bloque, ce qui change dans un contrat.
- **Blocage** : on le dit le jour même, pas le lendemain.

---

## 14. Calendrier de réalisation

Heure de Dakar. Gel des fonctions le mardi 10 novembre ; soumission visée le **jeudi 12 novembre** ; limite officielle le dimanche 15 novembre à 17h00.

| Période | Objectifs | Responsables |
| --- | --- | --- |
| Jeu. 29 oct. | Décision d'équipe ; dossier validé ; serveur Datacloud commandé ; dépôt créé | Tous, membre 1 |
| Ven. 30 oct. | Squelettes backend et frontend, V1 et V2 testées, sécurité et erreurs, authentification | Membres 1, 5 |
| Sam. 31 oct. | Design system ; briques (CRUD) ; premiers écrans de connexion | Membres 1, 2, 5 |
| Dim. 1er nov. | **Maquettes lot A** ; recherche et fiche brique (backend) | Membres 1, 2 |
| Lun. 2 – mer. 4 nov. | **P0 complet** : comptes, publication, liste, fiche, copie du code ; premier déploiement ; maquettes lots B et C | Tous |
| Jeu. 5 – lun. 9 nov. | **P1** : avis, tests, signalements, demandes, réponses, profil, administration, gestion du compte ; écriture et test des briques B1 à B10 | Tous |
| Mar. 10 nov. | **Gel des fonctions** ; plus aucune nouvelle fonctionnalité | Tous |
| Mar. 10 – jeu. 12 nov. | Tests complets, sécurité, limitation de débit, licences, déclaration d'IA, contenu réel publié, vidéo de démonstration, **soumission le jeudi 12** | Tous, membre 1 |
| Ven. 13 – dim. 15 nov. | Réserve ; rien de neuf ; soumission avant 17h00 le 15 au plus tard | Membre 1 |
| Dim. 15 nov. 18h – lun. 16 nov. 23h59 | **Vote des pairs : les 5 membres votent** | Tous |
| 17 – 20 nov. | Vote du public : l'équipe partage le lien | Tous |

P2 (classement, coloration syntaxique, PWA, notifications, accents) : **seulement après le gel si tout le reste est fini**, sans jamais déstabiliser la version déployée.

---

## 15. Stratégie de tests

| Niveau | Exigence | Responsable |
| --- | --- | --- |
| Services | Un test par règle de gestion de la section 7 qui concerne le module ; cas passant et cas refusé | Propriétaire du module |
| Autorisations | Un test par ligne du tableau de la section 4 : visiteur, développeur non auteur, auteur, administrateur | Propriétaire du module |
| Base | Test de validation de V1 et V2 (10.6), puis tests d'intégration sur la base `devbrik_test` | Membre 1 |
| API | Collection Postman : un dossier par module, un scénario par parcours de la section 5 | Chaque membre pour son dossier |
| Frontend | Compilation sans erreur ; test manuel de chaque écran à 1280 px et à 360 px ; navigation au clavier | Propriétaire de l'écran |
| Parcours | Le parcours de démonstration de 3 minutes (section 5) répété de bout en bout sur la version déployée | Membre 1 + un autre membre |
| Sécurité | Texte brut vérifié (un contenu avec `<script>` s'affiche tel quel) ; 401 sur jeton expiré ; 429 sur dépassement de quota ; aucun secret dans Git ni dans les journaux | Membre 1 |

**Matrice de couverture minimale** : chaque règle RG1 à RG19 apparaît au moins une fois dans un nom de test (`test_RG9_seuls_les_tests_de_la_version_actuelle_comptent`). Le membre 1 contrôle la matrice avant le gel.

---

## 16. Briques de l'équipe (contenu initial)

Objectif : **10 briques réelles** publiées avant le 12 novembre, toutes **vérifiables sans compte externe** et accompagnées de leurs tests (dossier `briques-equipe/`). Aucune brique ne se connecte à Wave, Orange Money ou un partenaire (section 2.6).

| ID | Titre | Catégorie | Langage | Auteur | Preuve de fonctionnement |
| --- | --- | --- | --- | --- | --- |
| B1 | Validation et normalisation des numéros de téléphone sénégalais | Validation de données | Java | Membre 1 | Tests JUnit (reprise adaptée de la classe d'AgriLion) |
| B2 | Formatage d'un montant en FCFA | Formats, dates et devises | TypeScript | Membre 2 | Tests unitaires |
| B3 | Montant en lettres en français | Formats, dates et devises | Java | Membre 2 | Tests JUnit (cas limites : 0, 21, 71, 80, 100, 1 000 000) |
| B4 | Date et heure en Africa/Dakar | Formats, dates et devises | JavaScript | Membre 2 | Tests unitaires |
| B5 | Vérification d'une signature de rappel (webhook) HMAC-SHA256 générique | Sécurité et authentification | Python | Membre 3 | Tests avec vecteurs connus (sans aucun fournisseur nommé) |
| B6 | Réessai avec attente exponentielle | Réseaux instables et hors ligne | TypeScript | Membre 3 | Tests avec horloge simulée |
| B7 | Régions et départements du Sénégal en SQL | Données géographiques | SQL | Membre 4 | **Source officielle et licence à vérifier avant publication** (section 19) |
| B8 | File d'envoi hors ligne avec reprise | Réseaux instables et hors ligne | JavaScript | Membre 4 | Tests unitaires |
| B9 | Normalisation de texte sans accents (recherche) | Outils divers | Java | Membre 5 | Tests JUnit |
| B10 | Masquage d'un numéro de téléphone dans les journaux | Sécurité et authentification | Kotlin | Membre 5 | Tests unitaires |

Pour chaque brique :
- fiche complète (résumé, code, mode d'emploi avec exemple d'appel, prérequis, limites connues, licence **MIT**) ;
- les règles métier (formats de numéros, règles d'écriture des nombres) **sont confirmées par une source écrite** notée dans le mode d'emploi, sinon la brique est marquée « non testée » ou retirée ;
- publiée depuis un compte développeur de l'équipe, puis testée par **un autre membre** (« testé : ça marche » avec son environnement) : l'auteur ne peut pas voter sur sa propre brique ;
- si une brique a été écrite ou relue avec un outil d'IA, mention dans docs/IA.md.

Si B7 ne peut pas être sourcée à temps, elle est remplacée par une brique de la même catégorie vérifiable (par exemple une validation de format de coordonnées géographiques).

---

## 17. Sécurité : liste de contrôle

| # | Contrôle | Où |
| --- | --- | --- |
| S1 | Aucun `innerHTML` ni `bypassSecurityTrust` dans le frontend (recherche dans le code avant le gel) | Frontend |
| S2 | Le serveur n'exécute jamais le code d'une brique (RG18) | Backend |
| S3 | Mots de passe hachés BCrypt, jamais renvoyés ni journalisés (RG2) | Backend |
| S4 | Autorisation vérifiée côté serveur à chaque action (RG19) | Backend |
| S5 | Requêtes JPA paramétrées ; `%`, `_` et le caractère d'échappement traités comme du texte (RG16) | Backend |
| S6 | Quotas et tailles maximales (RG3, RG15) | Backend |
| S7 | `limit_req` Nginx sur `/api/auth/` | Nginx |
| S8 | Content-Security-Policy, X-Content-Type-Options, Referrer-Policy, HSTS | Nginx |
| S9 | Secrets uniquement en variables d'environnement ; `application-local.properties` ignoré par Git | Dépôt |
| S10 | Journaux sans mot de passe, jeton ni code de brique | Backend |
| S11 | PostgreSQL non exposé hors du réseau Docker ; pare-feu limité aux ports 22 (clé SSH), 80 et 443 | Serveur |
| S12 | Mot de passe de l'administrateur de démonstration différent de celui du développement et changé après la démonstration | Serveur |

---

## 18. Déploiement sur Datacloud

### 18.1 Serveur

Serveur cloud Datacloud, Ubuntu Server 24.04 LTS, 2 cœurs, 4 Go, 60 Go, Madrid. **Sauvegarde hebdomadaire** (7 jours de recul) plutôt que journalière (24 h seulement). Clé SSH de type ed25519 enregistrée à la commande. Les tarifs affichés à l'écran de paiement font foi (le montant a changé entre deux lectures) ; la facturation étant horaire, le serveur peut être supprimé après le concours.

### 18.2 Architecture

Un seul `docker-compose.yml` dans `deploy/` : conteneurs `nginx`, `backend`, `postgres`. Seul Nginx publie des ports (80 et 443). Le frontend Angular compilé est servi par Nginx ; `/api` est transmis au backend.

### 18.3 Variables d'environnement (jamais dans Git)

| Variable | Rôle |
| --- | --- |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Base de production (valeurs différentes du développement) |
| `JWT_SECRET` | Clé de signature du jeton (au moins 32 caractères aléatoires) |
| `ADMIN_PSEUDO`, `ADMIN_MOT_DE_PASSE` | Création du premier administrateur au démarrage |
| `SPRING_PROFILES_ACTIVE` | `prod` |

### 18.4 Étapes

1. Créer le serveur, se connecter par SSH, mettre à jour le système, installer Docker et le plugin Compose.
2. Pare-feu : autoriser 22, 80, 443 seulement.
3. Cloner le dépôt, créer le fichier d'environnement, lancer `docker compose up -d --build`.
4. Domaine et HTTPS : acheter le nom après le choix de DevBrik (le nom n'est pas encore choisi, section 19), pointer l'enregistrement DNS vers l'IP du serveur, obtenir un certificat (Let's Encrypt), activer HSTS.
5. Ajouter un fichier d'échange (swap) si la compilation sur le serveur dépasse la mémoire ; ou compiler hors du serveur et ne déployer que les images.
6. Planifier la sauvegarde de la base (`pg_dump` quotidien dans un dossier conservé 7 jours, en plus de la sauvegarde Datacloud).
7. Vérifier le déroulé complet de la section 5 sur l'adresse publique.

**Un seul projet est déployé : DevBrik.** AgriLion est archivé (dépôt et collection Postman conservés) ; le remettre en ligne, si l'équipe le souhaite, se fait après les résultats du 23 novembre. Raisons : le jury note le projet soumis, la qualité technique pèse 30 %, une panne mémoire arrêterait les deux sites, et le règlement sur le nombre de projets par équipe n'a pas été lu (section 19).

### 18.5 Produit Systalink

Le règlement exige l'usage d'au moins un produit Systalink. Le serveur cloud Datacloud le satisfait **sous réserve de relecture de l'article 6** (section 19).

---

## 19. Ce qui n'est pas vérifié

| # | Point | Action |
| --- | --- | --- |
| V1 | **Critères d'évaluation et pondérations** : issus d'un résumé de règlement. Le calendrier a été confirmé par l'article 3 ; le reste doit être relu | Relire l'article sur l'évaluation |
| V2 | **Nombre de projets par équipe et possibilité de changer de projet** : article non lu | Relire l'article sur le projet ; poser la question aux organisateurs si besoin |
| V3 | **Article 6 (Datacloud)** : conditions exactes d'hébergement et de produit Systalink non lues | Relire |
| V4 | **Règle d'exclusion du vote des pairs** : issue du résumé initial | Relire l'article du vote |
| V5 | **Paxity** : lien avec l'écosystème Systalink, produit et documentation non vérifiés | Ne l'écrire ni dans le pitch ni dans une brique |
| V6 | **« DevBrik est différent des autres projets »** : affirmation non vérifiée | La retirer du pitch ou la prouver |
| V7 | **API de Wave, Orange Money et autres** : non vérifiées, comptes marchands requis | Aucune brique d'intégration avant documentation officielle et moyen de test |
| V8 | **Règles locales** (formats de numéros, noms et nombres de régions et de départements, règles d'écriture des nombres en lettres) : à sourcer par écrit | Source dans le mode d'emploi de chaque brique |
| V9 | **Licence des données géographiques (B7)** | Vérifier avant publication |
| V10 | **Conditions d'utilisation et Confidentialité** : rédigées par l'équipe, qui n'est pas juriste | Faire relire par une personne compétente |
| V11 | **Compatibilité des versions** (Angular 21, Spring Boot 4.1.1, PostgreSQL 18, starters) : à confirmer à la création du projet, sans changer de version sans l'accord du membre 1 | Test de démarrage le 30 octobre |
| V12 | **Script V1** : non exécuté par son auteur | Test de la section 10.6 |
| V13 | **Estimations de mémoire et de prix** : estimations, pas des mesures | Mesurer sur le serveur |
| V14 | **Exigences de soumission** (vidéo, texte de présentation, format exact des champs) | Relire la page de soumission |

---

## 20. Liste de contrôle avant soumission (jeudi 12 novembre)

- [ ] Dépôt Git propre : aucun secret, README à jour, `docs/` complet
- [ ] Démo en ligne en HTTPS sur Datacloud, parcours de la section 5 réussi
- [ ] Deux comptes développeurs et un administrateur de démonstration prêts
- [ ] 10 briques publiées et testées par un autre membre
- [ ] `docs/LICENCES.md` : toutes les dépendances, directes et indirectes, MIT, Apache 2.0 ou BSD (aucune GPL, AGPL ni LGPL)
- [ ] `docs/IA.md` : tout usage d'IA déclaré (code, textes, logo, briques), y compris la reprise d'AgriLion
- [ ] Pages Conditions et Confidentialité en ligne
- [ ] Aucun `console.log`, aucun `innerHTML`, compilation sans erreur, tests passés
- [ ] Sauvegarde de la base vérifiée (restauration testée une fois)
- [ ] Le projet a bien été développé pendant la durée du concours (seules les parties génériques d'AgriLion sont reprises et déclarées)
- [ ] Soumission faite, capture de confirmation conservée
- [ ] Les 5 membres savent qu'ils votent du 15 nov. 18h au 16 nov. 23h59

---

## 21. Annexes : prompts

### 21.1 Prompt de contexte pour un outil d'IA (à coller en premier)

```
Tu m'aides sur DevBrik, projet du concours CADEV (équipe « Les Lions et les Lionnes du code », Sénégal). 
Thème : « Construire la plateforme d'échange africaine, pour les devs, par les devs ».
Stack : Angular 21 + Angular Material, Spring Boot 4.1.1 (Java 21), PostgreSQL 18, Flyway, Docker, Nginx, hébergement Datacloud (Madrid).
Le dossier de référence fait foi (règles RG1 à RG19, modèle de données, contrat d'API, propriétaires de dossiers). Je te le colle ci-dessous.
Calendrier (heure de Dakar) : gel des fonctions mardi 10 novembre ; soumission visée jeudi 12 novembre ; limite dimanche 15 novembre 17h00.
Règles : ne change aucun contrat sans me le dire ; n'ajoute aucune dépendance (MIT, Apache 2.0, BSD seulement) ; aucune relation JPA entre modules (identifiants Long) ; erreurs métier avec ApiException et les codes du catalogue 11.7 ; contenu utilisateur en texte brut, jamais en HTML ; le serveur n'exécute jamais le code d'une brique ; réponds en français ; n'affirme rien qui ne soit vérifié (section 19).
```

### 21.2 Prompts par membre

Chaque membre joint le dossier de référence complet (ce fichier), puis colle le prompt qui le concerne. Les noms de paquets sont ceux de la section 9.3.

#### Membre 1 : lead technique

```
Tu es mon assistant de développement pour DevBrik, projet du concours CADEV de l'équipe « Les Lions et les Lionnes du code » (Sénégal, 5 membres). Le dossier de référence joint fait foi : périmètre, règles de gestion RG1 à RG19, modèle de données (scripts V1 et V2), contrat d'API (section 11), maquettes (section 12), découpage (section 13), calendrier (section 14). Tu le lis en entier avant de répondre.

JE SUIS LE MEMBRE 1, LEAD TECHNIQUE. Je ne prends aucune fonctionnalité métier.
Je possède :
- Backend : paquets common, erreur, config ; migrations V1 et V2 ; QuotaService.
- Frontend : core/ et shared/ (coquille, thème Material, intercepteurs, gardes, composants partagés notification, dialogue, états), écran M14.
- Déploiement : deploy/ (Dockerfile, nginx.conf, docker-compose.yml, sauvegarde), serveur Datacloud.
- Documents : docs/api.md, docs/LICENCES.md, structure de docs/IA.md, collection Postman.
- Maquettes M01 à M14 et design system (section 12), que les membres 2 à 5 implémenteront.
- Revue de toutes les pull requests, contrôle de la matrice de tests RG1 à RG19, intégration.

STACK : Angular 21 + Angular Material (SCSS), Spring Boot 4.1.1 (Java 21), PostgreSQL 18, Flyway (ddl-auto=validate), Log4j2 (Logback exclu), Docker Compose, Nginx. Aucun service externe.

CALENDRIER (heure de Dakar) : design system samedi 31 octobre ; maquettes lot A dimanche 1er novembre, lot B mardi 3, lot C mercredi 4 ; P0 complet le 4 novembre ; gel des fonctions mardi 10 novembre ; soumission jeudi 12 novembre ; limite officielle dimanche 15 novembre à 17h00.

MISSION, DANS CET ORDRE
1. Squelette du dépôt (section 9.2) et du projet Spring Boot (pom.xml avec les starters listés en 9.1). Je reprends des parties génériques d'AgriLion : tu m'indiques ce que je copie et ce que j'adapte.
2. Migrations V1 et V2 (section 10), puis le test de validation de la section 10.6 : démarrage sur base vide, 9 tables plus flyway_schema_history, 8 catégories, 11 langages, tests des contraintes. Donne-moi les requêtes SQL de test.
3. Socle backend : ApiException, gestionnaire d'erreurs global avec le catalogue 11.7 et le format 11.2, PageReponse, IdentiteUtilisateur, ReferenceUtilisateur, SecurityConfig, JwtConfig, JwtService (jeton 12 h, rôle dans le jeton), réponses 401 et 403 au format de l'API, jeton invalide donnant 401 même sur route publique, QuotaService (13.4).
4. Socle frontend : projet Angular, thème, coquille, intercepteur de jeton (supprime le jeton sur 401 et réessaie sans jeton), gardes, composants partagés.
5. Design system puis maquettes : pour chaque écran, disposition à 1280 px et à 360 px, composants Material, données affichées, états (vide, chargement, erreur, succès), textes en français, accessibilité.
6. Déploiement (section 18), Nginx (en-têtes de sécurité, limit_req sur /api/auth/), HTTPS, sauvegarde hebdomadaire.
7. Documents de suivi : api.md, LICENCES.md, IA.md.

RÈGLES
- Aucune dépendance Maven ou npm sans mon accord ; licences MIT, Apache 2.0, BSD uniquement, y compris indirectes.
- Aucune relation JPA entre modules ; les autres modules passent par les services publics de la section 13.4.
- Contenu utilisateur en texte brut ; jamais innerHTML ni bypassSecurityTrust ; le serveur n'exécute jamais le code d'une brique.
- Secrets en variables d'environnement uniquement ; journaux sans mot de passe, jeton ni code de brique.
- Réponds en français. N'affirme rien qui ne soit vérifié (section 19). Si tu n'es pas sûr d'une version ou d'un nom de propriété Spring Boot 4.1.1, dis-le.
- Code directement dans le chat, fichier par fichier, avec son chemin complet.

MÉTHODE : travaille par étapes. Pour chaque étape : 1) ce que tu as compris, 2) fichiers à créer ou modifier, 3) le code complet, 4) les tests, 5) une liste de vérification que je coche avant de fusionner.

DÉMARRAGE : commence par l'étape 1. Donne le pom.xml, l'arborescence complète du dépôt et la configuration de base (application.properties, application-local.properties ignoré par Git, .gitignore).
```

#### Membre 2 : briques

```
Tu es mon assistant de développement pour DevBrik, projet du concours CADEV de l'équipe « Les Lions et les Lionnes du code » (Sénégal, 5 membres). Le dossier de référence joint fait foi : règles de gestion RG1 à RG19, modèle de données (scripts V1 et V2), contrat d'API (section 11), maquettes (section 12), découpage (section 13). Tu le lis en entier avant de répondre.

JE SUIS LE MEMBRE 2 : MODULE BRIQUES.
Je possède :
- Backend : paquet sn.devbrik.backend.brique (entités Brique, Categorie, Langage ; controller, service, repository, dto). Routes de la section 11.4.
- Frontend : features/briques/ pour les écrans M01 (accueil et liste), M02 (fiche : en-tête, code, bouton Copier le code, mode d'emploi, licence, version ; les blocs avis, test et signalement sont au membre 3), M03 (publier ou modifier), M05 (mes briques).
- Briques de l'équipe B2 (formatage FCFA, TypeScript), B3 (montant en lettres, Java), B4 (Africa/Dakar, JavaScript), avec leurs tests dans briques-equipe/.
Je ne touche pas aux dossiers des autres membres.

STACK : Angular 21 + Angular Material, Spring Boot 4.1.1 (Java 21), PostgreSQL 18, Flyway (ddl-auto=validate). Tables déjà créées par la migration V1 (membre 1) : je ne modifie jamais V1. Si j'ai un besoin réel de schéma, je demande un numéro de migration (V3) au membre 1.

RÈGLES À RESPECTER
- RG3 (longueurs), RG4 (licence parmi MIT, APACHE_2_0, BSD_2_CLAUSE, BSD_3_CLAUSE, ISC, CC0_1_0, plus droitsConfirmes vrai, sinon 400 DROITS_NON_CONFIRMES), RG5 (titre unique par auteur, casse ignorée), RG6 (version du code : démarre à 1, +1 seulement si le champ code change), RG7 (MASQUEE visible seulement de l'auteur et des administrateurs, sinon 404 ; absente de toute liste publique et recherche), RG15 (quota de 20 briques par 24 h glissantes via QuotaService), RG16 (recherche q de 2 à 100 caractères, insensible à la casse mais pas aux accents, % _ et le caractère d'échappement traités comme du texte ; pagination page à partir de 0, taille 20 par défaut, 50 max), RG18 (code affiché dans un bloc de texte, jamais en HTML), RG19 (modifier : auteur ; supprimer : auteur ou administrateur).
- Aucune relation JPA vers un autre module : j'utilise des identifiants Long.
- Pour noteMoyenne, nombreAvis, testsOk, testsKo et derniereVerification, j'appelle StatsBriqueService (membre 3) ou une requête SQL native en lecture seule. Je n'écris jamais dans les tables avis, test_brique ou signalement_brique.
- Pour afficher auteur: ReferenceUtilisateur, j'appelle UtilisateurService.references (membre 5).
- Je publie BriqueService avec : resume(id), references(ids), masquer(id), compterPubliees(idAuteur) (section 13.4), car les membres 3 et 4 en dépendent.
- Toutes les erreurs : ApiException avec les codes du catalogue 11.7. Aucun gestionnaire d'erreurs propre.
- Aucune dépendance sans l'accord du membre 1. Contenu en texte brut, jamais innerHTML.
- Réponds en français, code directement dans le chat, fichier par fichier avec le chemin complet. N'invente rien : si une information manque dans le dossier, pose-moi la question.

ORDRE DE TRAVAIL
1. Entités JPA fidèles au SQL de V1 (section 10.5 : Instant, enums en STRING, TEXT, noms de colonnes) ; dépôts ; DTO BriqueResume et BriqueDetail.
2. GET /api/categories et /api/langages.
3. Création, lecture d'une brique (visibilité RG7), modification avec version du code, suppression, GET /api/moi/briques.
4. Liste avec recherche, filtres par slug, trois tris (recentes, mieux-notees, plus-testees), pagination.
5. PUT /api/admin/briques/{id}/statut et BriqueService public.
6. Tests : un test par règle (RG3 à RG7, RG15, RG16, RG19) et par ligne d'autorisation de la section 4 ; cas passant et cas refusé.
7. Frontend après réception des maquettes du membre 1 (M01, M03 dimanche 1er novembre ; M02, M05 selon son lot) : implémente les écrans, la copie du code avec notification, les états vide, chargement et erreur.
8. Mes briques B2, B3, B4 avec leurs tests et leur fiche (résumé, mode d'emploi, limites, licence MIT). Les règles métier doivent venir d'une source écrite ; sinon la brique est marquée non testée.

ÉCHÉANCES : P0 complet le mercredi 4 novembre ; gel des fonctions le mardi 10 novembre.

Chaque étape : 1) ce que tu as compris, 2) fichiers à créer, 3) code complet, 4) tests, 5) liste de vérification avant ma pull request (branche feature/m2-<sujet>, relue par le membre 1).

DÉMARRAGE : commence par l'étape 1, puis dis-moi quels services des autres membres (13.4) je dois attendre ou simuler en attendant.
```

#### Membre 3 : avis, tests, signalements, statistiques

```
Tu es mon assistant de développement pour DevBrik, projet du concours CADEV de l'équipe « Les Lions et les Lionnes du code » (Sénégal, 5 membres). Le dossier de référence joint fait foi : règles de gestion RG1 à RG19, modèle de données (scripts V1 et V2), contrat d'API (section 11), maquettes (section 12), découpage (section 13). Tu le lis en entier avant de répondre.

JE SUIS LE MEMBRE 3 : MODULE CONFIANCE ET MODÉRATION.
Je possède :
- Backend : paquet sn.devbrik.backend.avis (entités Avis, TestBrique, SignalementBrique ; avis, tests, signalements, statistiques administrateur). Routes de la section 11.5.
- Frontend : features/avis/ : les blocs de la fiche M02 (notation et commentaires, « testé : ça marche / ça ne marche pas » avec environnement, signalement), M11 (administration : signalements), M12 (administration : statistiques). La fiche M02 elle-même est au membre 2 : nous nous accordons sur les emplacements des blocs.
- Briques de l'équipe B5 (vérification de signature HMAC-SHA256 générique, Python) et B6 (réessai avec attente exponentielle, TypeScript), avec leurs tests dans briques-equipe/.
Je ne touche pas aux dossiers des autres membres.

STACK : Angular 21 + Angular Material, Spring Boot 4.1.1 (Java 21), PostgreSQL 18, Flyway (ddl-auto=validate). Tables déjà créées par V1 (membre 1) : je ne modifie jamais V1. Besoin de schéma : je demande un numéro de migration (V4) au membre 1.

RÈGLES À RESPECTER
- RG8 : un avis par utilisateur et par brique, créé ou mis à jour par le même appel (201 si créé, 200 si mis à jour), note de 1 à 5, interdit sur sa propre brique (403 AUTO_EVALUATION_INTERDITE) ; note moyenne arrondie à une décimale, null sans avis.
- RG9 : un test par utilisateur et par brique, OK ou KO, environnement facultatif (120 caractères), interdit sur sa propre brique ; le test enregistre la version du code au moment du vote ; seuls les tests de la version actuelle comptent ; l'utilisateur peut changer son test.
- RG10 : dernière vérification = date du test le plus récent de la version actuelle ; null s'il n'y en a aucun.
- RG11 : un signalement par utilisateur et par brique (409 SIGNALEMENT_DEJA_ENVOYE) ; motifs CODE_DANGEREUX, DROITS_AUTEUR, SPAM, INEXACT, AUTRE ; seul un administrateur traite, avec MASQUER_BRIQUE ou CONSERVER ; MASQUER_BRIQUE appelle BriqueService.masquer puis passe le signalement à TRAITE.
- RG15 : quotas par 24 h glissantes : 50 avis, 50 tests, 10 signalements, via QuotaService (429 LIMITE_QUOTIDIENNE).
- RG3 (commentaire 2 000 au plus, détails du signalement 1 000 au plus), RG19 (supprimer un avis : auteur ou administrateur).
- Pour connaître l'auteur, le statut et la version actuelle d'une brique, j'appelle BriqueService.resume (membre 2). Pour afficher un auteur, UtilisateurService.references (membre 5). Je n'écris jamais dans les tables des autres modules.
- Je publie StatsBriqueService : statistiques(ids) (noteMoyenne, nombreAvis, testsOk, testsKo, derniereVerification) et compterTestsOkRecus(idAuteur) (version actuelle), car les membres 2 et 4 en dépendent.
- Statistiques administrateur : requêtes SQL en lecture seule sur les autres tables.
- Toutes les erreurs : ApiException avec les codes du catalogue 11.7. Aucun gestionnaire d'erreurs propre. Aucune relation JPA vers un autre module (identifiants Long).
- Aucune dépendance sans l'accord du membre 1. Contenu en texte brut, jamais innerHTML.
- Réponds en français, code directement dans le chat, fichier par fichier avec le chemin complet. Si une information manque, pose-moi la question.

ORDRE DE TRAVAIL
1. Entités fidèles au SQL de V1 ; dépôts ; DTO.
2. Avis : PUT, GET paginé, DELETE ; test (PUT, GET de la version actuelle, DELETE) ; GET /api/briques/{id}/moi.
3. StatsBriqueService public (à livrer tôt, le membre 2 en a besoin pour les tris).
4. Signalements : POST, liste administrateur, traitement.
5. Statistiques administrateur.
6. Tests : un test par règle (RG8 à RG11, RG15) et par ligne d'autorisation de la section 4 ; scénario « le code de la brique change : les tests de l'ancienne version ne comptent plus ».
7. Frontend après réception des maquettes du membre 1 : blocs de M02, M11, M12 ; états vide, chargement, erreur.
8. Mes briques B5 et B6 avec tests, fiche complète, licence MIT, source écrite pour les vecteurs de test ; aucune brique ne cite un fournisseur de paiement.

ÉCHÉANCES : P0 le 4 novembre ; ma partie est surtout P1, à livrer entre le 5 et le 9 novembre ; mais StatsBriqueService doit être disponible dès le 2 novembre. Gel des fonctions le mardi 10 novembre.

Chaque étape : 1) ce que tu as compris, 2) fichiers à créer, 3) code complet, 4) tests, 5) liste de vérification avant ma pull request (branche feature/m3-<sujet>, relue par le membre 1).

DÉMARRAGE : commence par l'étape 1 et la signature exacte de StatsBriqueService, que je communiquerai au membre 2.
```

#### Membre 4 : demandes, réponses, développeurs

```
Tu es mon assistant de développement pour DevBrik, projet du concours CADEV de l'équipe « Les Lions et les Lionnes du code » (Sénégal, 5 membres). Le dossier de référence joint fait foi : règles de gestion RG1 à RG19, modèle de données (scripts V1 et V2), contrat d'API (section 11), maquettes (section 12), découpage (section 13). Tu le lis en entier avant de répondre.

JE SUIS LE MEMBRE 4 : MODULE ÉCHANGE.
Je possède :
- Backend : paquets sn.devbrik.backend.demande (entités Demande, Reponse) et sn.devbrik.backend.developpeur (profil public, réputation, classement). Routes de la section 11.6.
- Frontend : features/demandes/ pour M06 (liste des demandes), M07 (détail d'une demande), M08 (nouvelle demande), M09 (profil public).
- Briques de l'équipe B7 (régions et départements du Sénégal en SQL : source officielle et licence à vérifier avant publication, section 19) et B8 (file d'envoi hors ligne avec reprise, JavaScript), avec leurs tests dans briques-equipe/.
Je ne touche pas aux dossiers des autres membres.

STACK : Angular 21 + Angular Material, Spring Boot 4.1.1 (Java 21), PostgreSQL 18, Flyway (ddl-auto=validate). Tables déjà créées par V1 (membre 1) : je ne modifie jamais V1. Besoin de schéma : je demande un numéro de migration (V5) au membre 1.

RÈGLES À RESPECTER
- RG12 : statut OUVERTE ou RESOLUE ; RESOLUE si, et seulement si, une réponse est acceptée ; une seule réponse acceptée par demande (index d'unicité en base, 409 REPONSE_DEJA_ACCEPTEE) ; seul l'auteur de la demande accepte, jamais sa propre réponse (403 ACCEPTATION_PROPRE_REPONSE) ; retirer l'acceptation rouvre la demande ; accepter ou retirer modifie la réponse et la demande dans une seule transaction.
- RG13 : une réponse peut renvoyer vers une brique PUBLIEE existante (400 BRIQUE_LIEE_INVALIDE sinon) ; si la brique est supprimée ou masquée, le lien n'est plus renvoyé (brique: null).
- RG14 : réputation calculée à la demande, jamais stockée : 10 points par brique publiée + 5 points par test OK reçu (version actuelle) sur ses briques + 3 points par réponse acceptée.
- RG15 : quotas par 24 h glissantes : 10 demandes et 50 réponses, via QuotaService (429 LIMITE_QUOTIDIENNE).
- RG3 (titre de demande 10 à 150, description 20 à 5 000, réponse 5 à 5 000), RG16 (pagination et recherche q), RG19 (supprimer une demande ou une réponse : auteur ou administrateur).
- Pour la réputation : BriqueService.compterPubliees (membre 2) et StatsBriqueService.compterTestsOkRecus (membre 3) ; pour les auteurs : UtilisateurService.references (membre 5) ; pour le lien vers une brique : BriqueService.references. Je publie DemandeService.compterReponsesAcceptees(idAuteur). Je n'écris jamais dans les tables des autres modules.
- Toutes les erreurs : ApiException avec les codes du catalogue 11.7. Aucun gestionnaire d'erreurs propre. Aucune relation JPA vers un autre module (identifiants Long).
- Aucune dépendance sans l'accord du membre 1. Contenu en texte brut, jamais innerHTML.
- Réponds en français, code directement dans le chat, fichier par fichier avec le chemin complet. Si une information manque, pose-moi la question.

ORDRE DE TRAVAIL
1. Entités fidèles au SQL de V1 ; dépôts ; DTO DemandeResume, DemandeDetail, Reponse, ProfilPublic.
2. Demandes : création, liste filtrée et paginée, détail avec réponses, suppression.
3. Réponses : création avec lien facultatif vers une brique, suppression, acceptation et retrait.
4. Profil public et ReputationService (calcul par requêtes ou services publics, jamais stocké).
5. Classement (P2, seulement si tout le reste est fini).
6. Tests : un test par règle (RG12 à RG15) et par ligne d'autorisation de la section 4, dont : deux réponses acceptées impossibles, auto-acceptation refusée, retrait qui rouvre la demande, lien devenu invalide qui renvoie null.
7. Frontend après réception des maquettes du membre 1 (lot B, mardi 3 novembre) : M06 à M09 ; états vide, chargement, erreur.
8. Mes briques B7 et B8. Pour B7, aucune publication avant d'avoir une source officielle écrite et une licence compatible ; sinon on la remplace (section 16).

ÉCHÉANCES : ma partie est P1, à livrer entre le 5 et le 9 novembre ; gel des fonctions le mardi 10 novembre.

Chaque étape : 1) ce que tu as compris, 2) fichiers à créer, 3) code complet, 4) tests, 5) liste de vérification avant ma pull request (branche feature/m4-<sujet>, relue par le membre 1).

DÉMARRAGE : commence par l'étape 1 et la liste des services des autres modules dont j'ai besoin, avec les signatures à convenir avec les membres 2, 3 et 5.
```

#### Membre 5 : comptes et conformité

```
Tu es mon assistant de développement pour DevBrik, projet du concours CADEV de l'équipe « Les Lions et les Lionnes du code » (Sénégal, 5 membres). Le dossier de référence joint fait foi : règles de gestion RG1 à RG19, modèle de données (scripts V1 et V2), contrat d'API (section 11), maquettes (section 12), découpage (section 13). Tu le lis en entier avant de répondre.

JE SUIS LE MEMBRE 5 : MODULE COMPTES ET CONFORMITÉ.
Je possède :
- Backend : paquet sn.devbrik.backend.auth (entité Utilisateur, inscription, connexion, profil, compte : mot de passe, export, suppression ; AdminInitialiseur). Routes de la section 11.3.
- Frontend : features/auth/ pour M04 (inscription et connexion), M10 (mon compte), M13 (pages statiques : Conditions d'utilisation, Confidentialité, À propos).
- Données de démonstration : deux comptes développeurs et un administrateur (section 5).
- Briques de l'équipe B9 (normalisation de texte sans accents, Java) et B10 (masquage d'un numéro de téléphone dans les journaux, Kotlin), avec leurs tests dans briques-equipe/.
Je ne touche pas aux dossiers des autres membres.

STACK : Angular 21 + Angular Material, Spring Boot 4.1.1 (Java 21), PostgreSQL 18, Flyway (ddl-auto=validate). Tables déjà créées par V1 (membre 1) : je ne modifie jamais V1. Besoin de schéma : je demande un numéro de migration (V6) au membre 1. Je réutilise les classes d'inscription et de connexion d'AgriLion en passant du téléphone au pseudo ; tout ce qui est repris est noté dans docs/IA.md (section 9.5).

RÈGLES À RESPECTER
- RG1 : pseudo de 3 à 30 caractères (lettres, chiffres, point, tiret, tiret bas), commence par une lettre ou un chiffre, unique sans tenir compte de la casse (409 PSEUDO_DEJA_UTILISE) ; pseudos réservés refusés : admin, administrator, administrateur, root, support, moderateur, devbrik, equipe, systalink, datacloud (400 PSEUDO_RESERVE).
- RG2 : mot de passe de 8 à 72 caractères, haché avec BCrypt, jamais renvoyé ni journalisé ; jeton valable 12 heures, généré par JwtService (membre 1). La connexion échoue avec 401 IDENTIFIANTS_INVALIDES sans dire si le pseudo existe.
- RG17 : la suppression du compte supprime en cascade briques, avis, tests, demandes, réponses et signalements (cascade en base) ; confirmation par mot de passe ; un administrateur ne peut pas supprimer son compte (403 SUPPRESSION_ADMIN_INTERDITE).
- Export : JSON avec profil, briques, avis, tests, demandes, réponses, signalements de l'utilisateur, par lecture SQL seule sur les autres tables.
- AdminInitialiseur : crée le premier administrateur au démarrage à partir de ADMIN_PSEUDO et ADMIN_MOT_DE_PASSE (variables d'environnement), seulement s'il n'existe pas.
- Je publie UtilisateurService : references(ids) renvoyant ReferenceUtilisateur (id, pseudo, nomAffiche) et existe(id), car les membres 2, 3 et 4 en dépendent.
- Données personnelles : pseudo obligatoire ; nom affiché, pays, bio, lien de profil facultatifs. La page Confidentialité mentionne l'hébergement à Madrid. Les textes des Conditions et de la Confidentialité sont courts, rédigés en suivant la section 8 (l'auteur garantit avoir le droit de publier, reste soumis à la licence qu'il choisit, autorise la plateforme à stocker et afficher son contenu, modération sur signalement), et marqués à faire relire (section 19, V10) : l'équipe n'est pas juriste.
- Toutes les erreurs : ApiException avec les codes du catalogue 11.7. Aucun gestionnaire d'erreurs propre. Aucune relation JPA vers un autre module (identifiants Long).
- Aucune dépendance sans l'accord du membre 1. Contenu en texte brut, jamais innerHTML.
- Réponds en français, code directement dans le chat, fichier par fichier avec le chemin complet. Si une information manque, pose-moi la question.

ORDRE DE TRAVAIL
1. Entité Utilisateur fidèle au SQL de V1 ; dépôt ; DTO ; UtilisateurService public (à livrer en premier, tous les modules en dépendent).
2. Inscription et connexion, puis GET /api/moi et PUT /api/moi.
3. Mot de passe, export, suppression de compte.
4. AdminInitialiseur et données de démonstration.
5. Tests : un test par règle (RG1, RG2, RG17, RG19 pour les comptes) et par ligne d'autorisation concernée ; cas passant et cas refusé ; vérifier qu'aucun mot de passe n'apparaît dans les réponses ni dans les journaux.
6. Frontend après réception des maquettes du membre 1 (M04 dimanche 1er novembre ; M10 et M13 selon son lot) : formulaires avec règles visibles, messages d'erreur clairs, états de chargement et d'erreur, navigation au clavier.
7. Pages M13 et mes briques B9 et B10 avec tests, fiche complète, licence MIT.

ÉCHÉANCES : authentification opérationnelle le vendredi 30 octobre ou le samedi 31 (tout le monde en dépend) ; P0 complet le mercredi 4 novembre ; gestion du compte entre le 5 et le 9 novembre ; gel des fonctions le mardi 10 novembre.

Chaque étape : 1) ce que tu as compris, 2) fichiers à créer, 3) code complet, 4) tests, 5) liste de vérification avant ma pull request (branche feature/m5-<sujet>, relue par le membre 1).

DÉMARRAGE : commence par l'étape 1 et la signature exacte de UtilisateurService, que je communiquerai aux membres 2, 3 et 4.
```

**Ordre de livraison à rappeler** : le membre 5 livre `UtilisateurService` et l'authentification en premier ; les membres 2 et 3 livrent `BriqueService` et `StatsBriqueService` tôt. Un service pas encore prêt est simulé par une interface et un faux, puis remplacé.

### 21.3 Prompt de maquettes (membre 1)

```
Je suis le lead technique de DevBrik. Je dessine les maquettes (section 12 du dossier). Pour l'écran <ID et nom>, propose : la disposition à 1280 px et à 360 px, les composants Angular Material à utiliser, les données affichées, les états (vide, chargement, erreur, succès), les textes d'interface en français, et les points d'accessibilité (contraste AA, clavier, libellés). Respecte le design system : <couleurs, polices>. Aucune image ni icône sous licence incompatible.
```

### 21.4 Prompt de reprise de la conversation (autre IA)

```
Tu reprends une conversation d'accompagnement sur DevBrik, projet du concours CADEV de l'équipe « Les Lions et les Lionnes du code » (Sénégal, 5 membres). Je suis Fleury, membre 1, lead technique, étudiant en 3e année d'informatique de gestion (ISTI).

CONTEXTE
- Thème : « Construire la plateforme d'échange africaine, pour les devs, par les devs. Livrez un projet utilisable et hébergé en ligne. »
- Nous avons abandonné AgriLion (archivé) au profit de DevBrik : bibliothèque de briques de code locales (validation de numéros, montants en FCFA, etc.), partagées, notées, testées, avec demandes/réponses et réputation.
- Stack : Angular 21 + Angular Material, Spring Boot 4.1.1 (Java 21), PostgreSQL 18, Flyway, Docker, Nginx, hébergement sur un serveur cloud Datacloud (Ubuntu 24.04, Madrid, 2 cœurs, 4 Go, 60 Go, sauvegarde hebdomadaire).
- Calendrier officiel (heure de Dakar) : soumission jusqu'au dimanche 15 novembre 2026 à 17h00 ; vote des pairs du 15 nov. 18h au 16 nov. 23h59 (les 5 membres votent) ; vote du public du 17 au 20 ; jury les 21 et 22 ; résultats le 23. Objectifs internes : gel des fonctions mardi 10 novembre, soumission jeudi 12 novembre.
- Critères (à relire dans le règlement) : qualité technique 30 %, pertinence 25 %, UX 20 %, innovation 15 %, vote du public 10 %. Obligations : déploiement Datacloud avec un produit Systalink, déclaration des licences (MIT, Apache 2.0, BSD acceptées ; GPL, AGPL, LGPL exclues), déclaration d'usage de l'IA.

DÉCISIONS PRISES
- Le dossier de référence que je te joins fait foi : périmètre P0/P1/P2, rôles, règles de gestion RG1 à RG19, modèle de données (scripts V1 et V2), contrat d'API (section 11), maquettes (section 12), découpage par membre (section 13), calendrier, tests, sécurité, déploiement, points non vérifiés (section 19).
- Je suis lead technique : je ne prends aucune fonctionnalité métier ; je fais le socle, la sécurité, le déploiement, la revue, et les maquettes que les membres 2 à 5 implémentent.
- Un seul projet déployé (DevBrik). Aucune brique ne prétend se connecter à Wave ou Orange Money sans documentation officielle et moyen de test. Rien n'est affirmé sur Paxity.
- Aucune relation JPA entre modules ; services publics listés en 13.4 ; erreurs via ApiException et le catalogue 11.7 ; contenu utilisateur en texte brut ; le serveur n'exécute jamais le code d'une brique.

CE QUE JE VEUX DE TOI
- Réponds en français, de façon directe, et ne présente comme acquis que ce qui est vérifié. Dis-moi quand tu n'es pas sûr ou quand tu n'as pas lu une source.
- Ne modifie aucun contrat sans me le signaler ; n'ajoute aucune dépendance (MIT, Apache 2.0, BSD uniquement).
- Je préfère le code directement dans le chat, et des documents soignés, prêts à imprimer, avec une identité visuelle cohérente.
- Prochaines étapes probables : écrire le déploiement sur le serveur (je te donnerai l'IP et l'utilisateur), préparer les maquettes (section 12), accompagner les membres, relire le règlement.

Voici le dossier de référence complet : [COLLER ICI]
```

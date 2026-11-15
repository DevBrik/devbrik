# Comptes de démonstration et premier administrateur (membre 5)

Aucun mot de passe n'est écrit dans le dépôt (S9). Tout passe par des variables d'environnement.

## Variables

| Variable | Rôle | Obligatoire |
|---|---|---|
| `ADMIN_PSEUDO` | pseudo du premier administrateur (ex. `admin.demo`) | non : sans elle, aucun administrateur n'est créé |
| `ADMIN_MOT_DE_PASSE` | son mot de passe (8 à 72 caractères) | idem |
| `DEMO_MOT_DE_PASSE` | mot de passe commun aux deux développeurs de démonstration | non : sans elle, aucun compte de démonstration |

**Nouvelle variable à ajouter dans la section 18.3 du dossier (accord du membre 1) : `DEMO_MOT_DE_PASSE`.**

## Règles appliquées au démarrage

- `AdminInitialiseur` crée l'administrateur seulement si **aucun** administrateur n'existe. Si le pseudo appartient déjà à un
  développeur, il n'est jamais promu : un avertissement est écrit dans les journaux.
- Variables invalides (pseudo mal formé, mot de passe trop court ou trop long) : le démarrage échoue avec un message clair,
  sans afficher le mot de passe.
- `DemoInitialiseur` crée `fleury` (Fleury Ba) et `bousso` (Bousso Diémé), sans jamais modifier un compte existant.
- S12 : `DEMO_MOT_DE_PASSE` doit différer de `ADMIN_MOT_DE_PASSE`, sinon aucun compte de démonstration n'est créé.
  Le mot de passe de l'administrateur de démonstration doit aussi différer de celui du développement local
  (`devbrik_dev`) et être changé après la démonstration.
- Les comptes se créent une seule fois : les redémarrages sont sans effet.

## En local (fichier ignoré par Git : `application-local.properties`)

```properties
ADMIN_PSEUDO=admin.demo
ADMIN_MOT_DE_PASSE=<à choisir, 8 caractères au moins>
DEMO_MOT_DE_PASSE=<un autre mot de passe>
```

## Sur le serveur (docker-compose, fichier d'environnement hors Git)

```
ADMIN_PSEUDO=admin.demo
ADMIN_MOT_DE_PASSE=<secret serveur>
DEMO_MOT_DE_PASSE=<secret serveur différent>
```

Le membre 1 doit transmettre `ADMIN_PSEUDO`, `ADMIN_MOT_DE_PASSE` et `DEMO_MOT_DE_PASSE` au conteneur `backend`
dans `docker-compose.yml`.

## Vérification

1. Démarrer le backend avec les trois variables : les journaux indiquent « Administrateur « admin.demo » créé » et deux
   lignes « Compte de démonstration … : CREE ».
2. Redémarrer : « Un administrateur existe déjà » et « … : DEJA_PRESENT ».
3. Collection Postman (`docs/postman/`), dossier « 1. Authentification », avec la variable `motDePasse` renseignée.
4. Réinitialiser les données de démonstration : supprimer les comptes concernés (la cascade supprime leur contenu), puis redémarrer.

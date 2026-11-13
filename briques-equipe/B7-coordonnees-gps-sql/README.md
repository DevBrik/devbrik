# B7 · Validation de coordonnées GPS et distance (PostgreSQL)

| | |
| --- | --- |
| Catégorie | Données géographiques |
| Langage | SQL (PostgreSQL 12 ou plus) |
| Licence | MIT |
| Auteur | Ndiémé NDIAYE |

**Résumé** : deux fonctions SQL pures : `coordonnee_valide(lat, lon)` contrôle une coordonnée WGS 84 et `distance_km(...)` calcule la distance du grand cercle (haversine), utiles pour des points de livraison ou des agences.

## Mode d'emploi
1. Exécuter `coordonnees.sql` dans votre base (`psql -f coordonnees.sql`).
2. `SELECT coordonnee_valide(14.6928, -17.4467);` renvoie `true`.
3. `SELECT distance_km(lat1, lon1, lat2, lon2);` renvoie des kilomètres (NULL si une coordonnée est invalide).
4. Contrainte de table : `CHECK (coordonnee_valide(latitude, longitude))` (exemple commenté dans le script).
5. Limites : modèle sphérique (écart inférieur à 0,5 % par rapport à l'ellipsoïde) ; pour des calculs précis ou des index spatiaux, utiliser PostGIS.
6. Source : formule de haversine ; rayon moyen terrestre de 6 371 km. Les tests vérifient des valeurs géométriques exactes (1° de méridien, quart d'équateur).

> Cette brique remplace B7 « Régions et départements du Sénégal » : la source officielle et la licence de ces données n'ont pas été vérifiées (dossier, sections 16 et 19, V9).

## Prérequis
- PostgreSQL 12 ou plus

## Tests
```bash
psql -v ON_ERROR_STOP=1 -f coordonnees.sql -f tests.sql
```

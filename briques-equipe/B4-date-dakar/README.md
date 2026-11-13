# B4 · Date et heure en Africa/Dakar

| | |
| --- | --- |
| Catégorie | Formats, dates et devises |
| Langage | JavaScript |
| Licence | MIT |
| Auteur | Papa Birane DIAO |

**Résumé** : formate une date et une heure en français pour le fuseau Africa/Dakar (UTC+0 toute l'année, sans heure d'été), quel que soit le fuseau du serveur ou du navigateur.

## Mode d'emploi
1. Copier `index.js` (module CommonJS) dans votre projet.
2. `formaterDateDakar(new Date())` renvoie par exemple `"mardi 1 janvier 2025"`.
3. `formaterHeureDakar(date)` renvoie `"HH:mm:ss"` ; `formaterDateHeureDakar(date)` combine les deux.
4. `composantsDakar(date)` renvoie année, mois, jour, heure, minute et seconde à Dakar.
5. Les entrées acceptées : `Date`, chaîne ISO 8601, nombre (millisecondes) ; une date invalide lève `TypeError`.
6. Limites : dépend des données de fuseau d'`Intl` (Node.js 14+ avec ICU complet, navigateurs récents).
7. Source : base de données des fuseaux IANA (tz database), zone `Africa/Dakar` (UTC+0, sans changement d'heure).

## Prérequis
- Node.js 18 ou plus, ou un navigateur récent
- Jest pour les tests uniquement

## Tests
```bash
npm install && npm test
```

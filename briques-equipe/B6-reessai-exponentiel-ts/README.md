# B6 · Réessai avec attente exponentielle

| | |
| --- | --- |
| Catégorie | Réseaux instables et hors ligne |
| Langage | TypeScript |
| Licence | MIT |
| Auteur | Membre 3 |

**Résumé** : relance automatiquement une opération réseau qui échoue, avec des attentes croissantes (100 ms, 200 ms, 400 ms…), un plafond, une part aléatoire et l'annulation.

## Mode d'emploi
1. Copier `index.ts` dans votre projet.
2. `await reessayer(() => fetch(url).then(verifier), { tentatives: 5, delaiInitial: 500 })`.
3. Ne relancer que les erreurs temporaires : `doitReessayer: (e) => e.statut === undefined || e.statut >= 500`.
4. Afficher « nouvel essai dans … » avec `surReessai(erreur, tentative, delai)`.
5. Annuler avec un `AbortController` (option `signal`).
6. En cas d'échec définitif, une `EchecReessai` est levée avec `tentatives` et `derniereErreur`.
7. Limites : ne rend pas une opération idempotente ; ne relancez pas un paiement sans clé d'idempotence côté serveur.
8. Source : principe de l'attente exponentielle avec gigue (« exponential backoff and jitter »), décrit notamment dans la documentation d'architecture d'AWS.

## Prérequis
- TypeScript 5 ou Node.js 22.18 ou plus
- Aucune dépendance

## Tests (horloge simulée)
```bash
node --test index.test.ts
```

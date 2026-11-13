# B2 · Formatage d'un montant en FCFA

| | |
| --- | --- |
| Catégorie | Formats, dates et devises |
| Langage | TypeScript |
| Licence | MIT |
| Auteur | Membre 2 |

**Résumé** : formate un montant en francs CFA (« 1 500 000 FCFA ») et relit une saisie (« 15.000 F CFA » → 15000), sans dépendance.

## Mode d'emploi
1. Copier `index.ts` dans votre projet.
2. `formaterFcfa(1500000)` renvoie `"1 500 000 FCFA"` (espace fine insécable U+202F entre les groupes).
3. `formaterFcfa(25000, { separateur: ' ', suffixe: 'F CFA' })` pour un autre séparateur ou suffixe.
4. `lireFcfa('15 000 FCFA')` renvoie `15000` ; une saisie non numérique renvoie `null`.
5. Limites : le franc CFA n'utilise pas de décimales, le montant est arrondi à l'unité ; pas de conversion de devise.
6. Source : ISO 4217 (XOF et XAF, 0 décimale) ; usage français des espaces pour grouper les milliers.

## Prérequis
- TypeScript 5 ou Node.js 22.18 ou plus (exécution directe du TypeScript)

## Tests
```bash
node --test index.test.ts
```

> L'ancienne brique B2 « paiement Orange Money » a été retirée : le dossier de référence (sections 2.6 et 16) interdit toute brique d'intégration de paiement sans documentation officielle ni moyen de test.

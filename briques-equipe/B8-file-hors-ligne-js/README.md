# B8 · File d'envoi hors ligne avec reprise

| | |
| --- | --- |
| Catégorie | Réseaux instables et hors ligne |
| Langage | JavaScript (module ES) |
| Licence | MIT |
| Auteur | Membre 4 |

**Résumé** : met en file les envois faits sans réseau (formulaires, commandes, relevés), les conserve dans le stockage du navigateur et les renvoie dans l'ordre au retour de la connexion.

## Mode d'emploi
1. Copier `index.js` dans votre projet.
2. `const file = new FileHorsLigne({ envoyer: (m) => fetch('/api/...', { method: 'POST', body: JSON.stringify(m) }).then(verifier), stockage: stockageNavigateur() })`.
3. À chaque saisie : `file.ajouter(donnees)` puis `file.vider()`.
4. `file.ecouterReseau()` relance le vidage à l'événement `online` du navigateur.
5. `envoyer` doit rejeter (exception) en cas d'échec, y compris sur une réponse HTTP d'erreur, sinon le message est retiré.
6. Limites : pas de déduplication côté serveur (prévoir un identifiant de message pour rendre l'envoi idempotent) ; localStorage est limité à quelques Mo ; à combiner avec B6 pour espacer les essais.
7. Source : événements `online` / `offline` et API Web Storage (spécification HTML, WHATWG).

## Prérequis
- Navigateur récent ou Node.js 18 ou plus
- Aucune dépendance

## Tests
```bash
node --test index.test.js
```

# B5 · Vérification d'une signature de rappel (webhook) HMAC-SHA256

| | |
| --- | --- |
| Catégorie | Sécurité et authentification |
| Langage | Python (3.8 ou plus) |
| Licence | MIT |
| Auteur | Membre 3 |

**Résumé** : vérifie en temps constant la signature HMAC-SHA256 d'un rappel HTTP (webhook), avec une variante anti-rejeu par horodatage. Générique : aucun fournisseur nommé.

## Mode d'emploi
1. Copier `signature_hmac.py` dans votre projet.
2. Lire le corps **brut** de la requête (octets), avant tout décodage JSON.
3. `verifier_signature(secret, corps, entete_signature)` renvoie `True` si la signature est valide ; ajouter `prefixe="sha256="` si l'en-tête est préfixé.
4. Anti-rejeu : `verifier_avec_horodatage(secret, corps, signature, horodatage, tolerance_secondes=300)` (message signé = `horodatage.corps`).
5. Refuser la requête (401) si la fonction renvoie `False` ; ne jamais journaliser le secret.
6. Limites : la construction exacte du message signé dépend du fournisseur : se référer à sa documentation officielle.
7. Source : RFC 2104 (HMAC) et RFC 4231 (vecteurs de test HMAC-SHA256), utilisés dans les tests.

## Prérequis
- Python 3.8 ou plus
- Bibliothèque standard uniquement (`hmac`, `hashlib`)

## Tests
```bash
python3 -m unittest -v
```

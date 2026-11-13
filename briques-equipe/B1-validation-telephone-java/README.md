# B1 · Validation et normalisation des numéros de téléphone sénégalais

| | |
| --- | --- |
| Catégorie | Validation de données |
| Langage | Java (17 ou plus) |
| Licence | MIT |
| Auteur | Amadou Fleury BA |

**Résumé** : vérifie qu'un numéro est un mobile sénégalais (préfixes 70, 75, 76, 77, 78, 9 chiffres) et le normalise au format E.164 (`+221XXXXXXXXX`).

## Mode d'emploi
1. Copier `ValidationTelephone.java` dans le paquet `sn.devbrik.briques` (ou adapter la déclaration `package`).
2. `ValidationTelephone.estValide("77 123 45 67")` renvoie `true`.
3. `ValidationTelephone.normaliser("00221771234567")` renvoie `"+221771234567"` ; un numéro invalide lève `IllegalArgumentException`.
4. `ValidationTelephone.versLocal("+221781234567")` renvoie `"781234567"`.
5. Limites : mobiles uniquement (les fixes `33…` sont refusés) ; les espaces sont acceptés, pas les tirets ni les points.
6. Source des préfixes : plan national de numérotation de l'ARTP (Sénégal). **À confirmer par écrit avant publication** (dossier, section 19, V8).

## Prérequis
- Java 17 ou plus
- Aucune dépendance

## Tests
```bash
javac -d out *.java && java -cp out sn.devbrik.briques.ValidationTelephoneTest
```

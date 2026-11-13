# B3 · Montant en lettres en français

| | |
| --- | --- |
| Catégorie | Formats, dates et devises |
| Langage | Java (17 ou plus) |
| Licence | MIT |
| Auteur | Membre 2 |

**Résumé** : écrit un entier en toutes lettres en français (0 à 999 999 999 999), par exemple pour les factures en FCFA : 15000 → « quinze mille francs CFA ».

## Mode d'emploi
1. Copier `MontantEnLettres.java` dans votre projet (paquet `sn.devbrik.briques` ou à adapter).
2. `MontantEnLettres.enLettres(1999)` renvoie `"mille neuf cent quatre-vingt-dix-neuf"`.
3. `MontantEnLettres.montantFcfa(15000)` renvoie `"quinze mille francs CFA"`.
4. Règles : « et » pour 21 à 71 (sauf 81 et 91) ; « vingt » et « cent » s'accordent quand ils terminent le nombre ou précèdent million / milliard, jamais devant « mille » ; « mille » est invariable.
5. Limites : orthographe traditionnelle (traits d'union sous cent seulement), entiers uniquement, pas de centimes.
6. Source : règles d'accord des adjectifs numéraux (Grevisse, *Le Bon Usage* ; Banque de dépannage linguistique de l'OQLF). **Référence précise à noter par l'auteur avant publication** (dossier 19, V8).

## Prérequis
- Java 17 ou plus
- Aucune dépendance

## Tests
```bash
javac -d out *.java && java -cp out sn.devbrik.briques.MontantEnLettresTest
```

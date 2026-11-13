# B9 · Normalisation de texte sans accents (recherche)

| | |
| --- | --- |
| Catégorie | Outils divers |
| Langage | Java (11 ou plus) |
| Licence | MIT |
| Auteur | Membre 5 |

**Résumé** : retire les accents et développe les ligatures pour comparer des textes sans tenir compte des accents ni de la casse (« Thiès » = « thies », « sénégalais » contient « SENEGAL »).

## Mode d'emploi
1. Copier `TexteSansAccents.java` dans votre projet.
2. `TexteSansAccents.sansAccents("Élève à Thiès")` renvoie `"Eleve a Thies"`.
3. `TexteSansAccents.cleRecherche(texte)` donne une clé en minuscules, sans accents, espaces réduits : à stocker dans une colonne indexée ou à comparer.
4. `TexteSansAccents.contient(titre, recherche)` pour un filtre simple en mémoire.
5. Limites : ne translittère pas les alphabets non latins (arabe, ajami) ; pour PostgreSQL, l'extension `unaccent` fait l'équivalent côté base.
6. Source : norme Unicode, forme de normalisation NFD (UAX #15) et catégorie « Mark » des signes diacritiques ; `java.text.Normalizer` (JDK).

## Prérequis
- Java 11 ou plus
- Aucune dépendance

## Tests
```bash
javac -d out *.java && java -cp out sn.devbrik.briques.TexteSansAccentsTest
```

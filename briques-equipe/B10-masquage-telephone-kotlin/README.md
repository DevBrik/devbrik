# B10 · Masquage d'un numéro de téléphone dans les journaux

| | |
| --- | --- |
| Catégorie | Sécurité et authentification |
| Langage | Kotlin (1.9 ou plus, JVM) |
| Licence | MIT |
| Auteur | Membre 5 |

**Résumé** : masque les numéros de téléphone avant de les écrire dans les journaux (« +221771234567 » → « +22177*****67 »), pour limiter l'exposition de données personnelles.

## Mode d'emploi
1. Copier `MasquageTelephone.kt` dans votre projet.
2. `MasquageTelephone.masquer("+221771234567")` renvoie `"+22177*****67"`.
3. `MasquageTelephone.masquerDansTexte(message)` masque tous les numéros d'un message avant `logger.info(...)`.
4. Symbole personnalisable : `masquer(numero, '#')`.
5. Limites : détecte les suites de 9 à 15 chiffres (séparateurs espace, point, tiret) ; un identifiant numérique de cette longueur isolé dans le texte serait aussi masqué ; ne remplace pas une politique de journalisation sans donnée personnelle.
6. Source : principe de minimisation des données (loi sénégalaise n° 2008-12 sur la protection des données à caractère personnel) ; format E.164 de l'UIT-T (15 chiffres au plus).

## Prérequis
- Kotlin 1.9 ou plus (JVM)
- Aucune dépendance

## Tests
```bash
kotlinc *.kt -include-runtime -d b10.jar && java -cp b10.jar sn.devbrik.briques.MasquageTelephoneTestKt
```

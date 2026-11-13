package sn.devbrik.briques;

/** Tests de B9 sans dépendance : javac -d out *.java && java -cp out sn.devbrik.briques.TexteSansAccentsTest */
public final class TexteSansAccentsTest {

    private static int echecs = 0;
    private static int total = 0;

    private static void egal(String obtenu, String attendu, String cas) {
        total++;
        if (!java.util.Objects.equals(obtenu, attendu)) {
            echecs++;
            System.out.println("ÉCHEC " + cas + " : attendu « " + attendu + " », obtenu « " + obtenu + " »");
        }
    }

    private static void vrai(boolean condition, String cas) {
        total++;
        if (!condition) {
            echecs++;
            System.out.println("ÉCHEC " + cas);
        }
    }

    public static void main(String[] args) {
        egal(TexteSansAccents.sansAccents("Élève à Thiès"), "Eleve a Thies", "accents et casse conservée");
        egal(TexteSansAccents.sansAccents("Kédougou, Ziguinchor, Saint-Louis"), "Kedougou, Ziguinchor, Saint-Louis", "toponymes");
        egal(TexteSansAccents.sansAccents("ç Ç ñ ü ï ô ê"), "c C n u i o e", "diacritiques variés");
        egal(TexteSansAccents.sansAccents("cœur Œuvre ex æquo"), "coeur OEuvre ex aequo", "ligatures");
        egal(TexteSansAccents.sansAccents("été"), "ete", "forme déjà décomposée (NFD)");
        egal(TexteSansAccents.sansAccents(null), null, "null");
        egal(TexteSansAccents.cleRecherche("  Ça   COÛTE\t15 000 F  "), "ca coute 15 000 f", "clé de recherche");
        egal(TexteSansAccents.cleRecherche(null), "", "clé de null");
        vrai(TexteSansAccents.contient("Validation des numéros sénégalais", "SENEGAL"), "contient sans accents");
        vrai(!TexteSansAccents.contient("Thiès", "Dakar"), "ne contient pas");
        egal(TexteSansAccents.sansAccents("100 % _ test"), "100 % _ test", "caractères non accentués intacts");

        System.out.println(total - echecs + " / " + total + " vérifications réussies");
        if (echecs > 0) {
            System.exit(1);
        }
    }
}

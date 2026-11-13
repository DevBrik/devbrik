package sn.devbrik.briques;

/** Tests de B3 sans dépendance : javac -d out *.java && java -cp out sn.devbrik.briques.MontantEnLettresTest */
public final class MontantEnLettresTest {

    private static int echecs = 0;
    private static int total = 0;

    private static void egal(long nombre, String attendu) {
        total++;
        String obtenu = MontantEnLettres.enLettres(nombre);
        if (!obtenu.equals(attendu)) {
            echecs++;
            System.out.println("ÉCHEC " + nombre + " : attendu « " + attendu + " », obtenu « " + obtenu + " »");
        }
    }

    public static void main(String[] args) {
        // Cas limites demandés par le dossier (section 16) : 0, 21, 71, 80, 100, 1 000 000.
        egal(0, "zéro");
        egal(21, "vingt et un");
        egal(71, "soixante et onze");
        egal(80, "quatre-vingts");
        egal(100, "cent");
        egal(1_000_000, "un million");
        // Autres règles
        egal(1, "un");
        egal(16, "seize");
        egal(17, "dix-sept");
        egal(22, "vingt-deux");
        egal(70, "soixante-dix");
        egal(72, "soixante-douze");
        egal(79, "soixante-dix-neuf");
        egal(81, "quatre-vingt-un");
        egal(90, "quatre-vingt-dix");
        egal(91, "quatre-vingt-onze");
        egal(99, "quatre-vingt-dix-neuf");
        egal(101, "cent un");
        egal(200, "deux cents");
        egal(201, "deux cent un");
        egal(280, "deux cent quatre-vingts");
        egal(1000, "mille");
        egal(1001, "mille un");
        egal(1999, "mille neuf cent quatre-vingt-dix-neuf");
        egal(2000, "deux mille");
        egal(15_000, "quinze mille");
        egal(80_000, "quatre-vingt mille");
        egal(200_000, "deux cent mille");
        egal(2_000_000, "deux millions");
        egal(80_000_000, "quatre-vingts millions");
        egal(200_000_000, "deux cents millions");
        egal(1_000_000_000L, "un milliard");
        egal(2_500_000_000L, "deux milliards cinq cents millions");
        egal(-21, "moins vingt et un");

        total++;
        if (!MontantEnLettres.montantFcfa(15_000).equals("quinze mille francs CFA")
                || !MontantEnLettres.montantFcfa(1).equals("un franc CFA")) {
            echecs++;
            System.out.println("ÉCHEC montantFcfa");
        }
        total++;
        try {
            MontantEnLettres.enLettres(1_000_000_000_000L);
            echecs++;
            System.out.println("ÉCHEC : la limite n'est pas contrôlée");
        } catch (IllegalArgumentException attendue) {
            // limite respectée
        }

        System.out.println(total - echecs + " / " + total + " vérifications réussies");
        if (echecs > 0) {
            System.exit(1);
        }
    }
}

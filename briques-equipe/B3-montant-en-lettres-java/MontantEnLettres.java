package sn.devbrik.briques;

/**
 * B3 · Montant en lettres en français (orthographe traditionnelle).
 *
 * Règles appliquées :
 *  - « et » pour 21, 31, 41, 51, 61 et 71 (vingt et un, soixante et onze), pas pour 81 et 91 ;
 *  - « vingt » et « cent » prennent un s quand ils sont multipliés et terminent le nombre
 *    (quatre-vingts, deux cents), ou quand ils sont suivis de « million » ou « milliard », qui sont des noms ;
 *    ils restent invariables devant « mille », qui est un adjectif numéral (deux cent mille, quatre-vingt mille) ;
 *  - « mille » est invariable et s'écrit sans « un » (mille, deux mille) ;
 *  - « million » et « milliard » s'accordent (un million, deux millions).
 * Traits d'union entre les éléments inférieurs à cent seulement (orthographe d'avant 1990).
 */
public final class MontantEnLettres {

    private static final String[] UNITES = {
        "zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix",
        "onze", "douze", "treize", "quatorze", "quinze", "seize",
    };
    private static final String[] DIZAINES = {
        "", "", "vingt", "trente", "quarante", "cinquante", "soixante",
    };
    private static final long MAXIMUM = 999_999_999_999L;

    private MontantEnLettres() {
    }

    /** Écrit un entier de 0 à 999 999 999 999 en toutes lettres (négatifs : « moins … »). */
    public static String enLettres(long nombre) {
        if (nombre < 0) {
            if (nombre < -MAXIMUM) {
                throw new IllegalArgumentException("Nombre hors limites : " + nombre);
            }
            return "moins " + enLettres(-nombre);
        }
        if (nombre > MAXIMUM) {
            throw new IllegalArgumentException("Nombre hors limites : " + nombre);
        }
        if (nombre == 0) {
            return UNITES[0];
        }
        int milliards = (int) (nombre / 1_000_000_000L);
        int millions = (int) (nombre / 1_000_000L % 1000);
        int milliers = (int) (nombre / 1000 % 1000);
        int reste = (int) (nombre % 1000);

        StringBuilder sb = new StringBuilder();
        if (milliards > 0) {
            ajouter(sb, moinsDeMille(milliards, true) + (milliards > 1 ? " milliards" : " milliard"));
        }
        if (millions > 0) {
            ajouter(sb, moinsDeMille(millions, true) + (millions > 1 ? " millions" : " million"));
        }
        if (milliers > 0) {
            ajouter(sb, milliers == 1 ? "mille" : moinsDeMille(milliers, false) + " mille");
        }
        if (reste > 0) {
            ajouter(sb, moinsDeMille(reste, true));
        }
        return sb.toString();
    }

    /** Montant en francs CFA : 15000 → « quinze mille francs CFA », 1 → « un franc CFA ». */
    public static String montantFcfa(long montant) {
        long absolu = Math.abs(montant);
        return enLettres(montant) + (absolu > 1 ? " francs CFA" : " franc CFA");
    }

    private static void ajouter(StringBuilder sb, String morceau) {
        if (!sb.isEmpty()) {
            sb.append(' ');
        }
        sb.append(morceau);
    }

    /**
     * 1 à 999. accordFinal : le groupe termine le nombre ou précède « million(s) » / « milliard(s) »,
     * donc « cent » et « quatre-vingt » multipliés prennent un s.
     */
    private static String moinsDeMille(int n, boolean accordFinal) {
        int centaines = n / 100;
        int reste = n % 100;
        StringBuilder sb = new StringBuilder();
        if (centaines > 0) {
            if (centaines > 1) {
                sb.append(UNITES[centaines]).append(' ');
            }
            sb.append("cent");
            if (centaines > 1 && reste == 0 && accordFinal) {
                sb.append('s');
            }
        }
        if (reste > 0) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(moinsDeCent(reste, accordFinal));
        }
        return sb.toString();
    }

    private static String moinsDeCent(int n, boolean accordFinal) {
        if (n <= 16) {
            return UNITES[n];
        }
        if (n < 20) {
            return "dix-" + UNITES[n - 10];
        }
        int dizaine = n / 10;
        int unite = n % 10;
        if (dizaine == 7 || dizaine == 9) {
            String base = dizaine == 7 ? "soixante" : "quatre-vingt";
            int complement = n - (dizaine == 7 ? 60 : 80); // 10 à 19
            String suite = moinsDeCent(complement, accordFinal);
            return dizaine == 7 && unite == 1 ? base + " et " + suite : base + "-" + suite;
        }
        if (dizaine == 8) {
            if (unite == 0) {
                return accordFinal ? "quatre-vingts" : "quatre-vingt";
            }
            return "quatre-vingt-" + UNITES[unite];
        }
        String base = DIZAINES[dizaine];
        if (unite == 0) {
            return base;
        }
        return unite == 1 ? base + " et un" : base + "-" + UNITES[unite];
    }
}

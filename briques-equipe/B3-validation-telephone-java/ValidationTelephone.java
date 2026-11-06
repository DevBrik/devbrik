package sn.devbrik.briques;

import java.util.regex.Pattern;

/**
 * B3 — Validation et normalisation des numéros de téléphone sénégalais.
 *
 * Opérateurs couverts (préfixes valides) :
 *   - Orange : 77
 *   - Free   : 76
 *   - Expresso : 70
 *   - Wave/autres : 78
 *   - Tigo/Saga Africa : 75
 *
 * Formats acceptés : XXXXXXXXX, +221XXXXXXXXX, 00221XXXXXXXXX
 */
public final class ValidationTelephone {

    private ValidationTelephone() {}

    /** Numéros mobiles sénégalais valides (tous opérateurs). */
    private static final Pattern MOBILE_SN =
        Pattern.compile("^(?:\\+221|00221)?7[05678]\\d{7}$");

    /**
     * Retourne {@code true} si le numéro est un mobile sénégalais valide.
     *
     * @param telephone numéro brut (peut contenir espaces en début/fin)
     */
    public static boolean estValide(String telephone) {
        if (telephone == null || telephone.isBlank()) return false;
        String propre = telephone.strip().replaceAll("\\s", "");
        return MOBILE_SN.matcher(propre).matches();
    }

    /**
     * Normalise le numéro au format international (+221XXXXXXXXX).
     *
     * @param telephone numéro brut
     * @return numéro normalisé
     * @throws IllegalArgumentException si le numéro est invalide
     */
    public static String normaliser(String telephone) {
        if (!estValide(telephone)) {
            throw new IllegalArgumentException(
                "Numéro de téléphone sénégalais invalide : " + telephone);
        }
        String propre = telephone.strip().replaceAll("\\s", "");
        if (propre.startsWith("+221")) return propre;
        if (propre.startsWith("00221")) return "+" + propre.substring(2);
        return "+221" + propre;
    }

    /**
     * Extrait le numéro local (sans indicatif pays).
     *
     * @param telephone numéro brut (valide)
     * @return numéro à 9 chiffres ex. "771234567"
     * @throws IllegalArgumentException si invalide
     */
    public static String versLocal(String telephone) {
        String international = normaliser(telephone);
        return international.substring(4); // retire "+221"
    }
}

package sn.devbrik.briques;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * B9 · Normalisation de texte sans accents, pour la recherche.
 *
 * « Ça coûte 15 000 F à Thiès » → « ca coute 15 000 f a thies ».
 * Décomposition Unicode (NFD), suppression des signes diacritiques, ligatures (œ, æ) développées,
 * minuscules, espaces réduits. Utile pour comparer « Thiès » et « thies », ou « Kédougou » et « kedougou ».
 */
public final class TexteSansAccents {

    private static final Pattern DIACRITIQUES = Pattern.compile("\\p{M}+");
    private static final Pattern ESPACES = Pattern.compile("\\s+");

    private TexteSansAccents() {
    }

    /** Retire les accents en conservant la casse : « Élève » → « Eleve ». null → null. */
    public static String sansAccents(String texte) {
        if (texte == null) {
            return null;
        }
        String ligatures = texte
                .replace("œ", "oe").replace("Œ", "OE")
                .replace("æ", "ae").replace("Æ", "AE")
                .replace("ß", "ss");
        String decompose = Normalizer.normalize(ligatures, Normalizer.Form.NFD);
        return Normalizer.normalize(DIACRITIQUES.matcher(decompose).replaceAll(""), Normalizer.Form.NFC);
    }

    /** Clé de recherche : sans accents, en minuscules, espaces réduits et rognés. */
    public static String cleRecherche(String texte) {
        if (texte == null) {
            return "";
        }
        String s = sansAccents(texte).toLowerCase(Locale.ROOT);
        return ESPACES.matcher(s).replaceAll(" ").strip();
    }

    /** Vrai si « botte » contient « aiguille », sans tenir compte des accents ni de la casse. */
    public static boolean contient(String botte, String aiguille) {
        return cleRecherche(botte).contains(cleRecherche(aiguille));
    }
}

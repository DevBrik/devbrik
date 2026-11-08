package sn.devbrik.backend.brique.dto;

/** Petits utilitaires de texte brut : rognage (trim) côté serveur avant validation (11.1). */
final class Textes {

    private Textes() {
    }

    static String rogner(String s) {
        return s == null ? null : s.strip();
    }

    /** Une chaîne vide devient null (champ facultatif absent). */
    static String vide(String s) {
        return s == null || s.isEmpty() ? null : s;
    }
}

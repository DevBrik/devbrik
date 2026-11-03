package sn.devbrik.backend.common;

public enum TypeQuota {
    BRIQUE(20, "briques"),
    AVIS(50, "avis"),
    TEST(50, "tests"),
    DEMANDE(10, "demandes"),
    REPONSE(50, "réponses"),
    SIGNALEMENT(10, "signalements");

    private final int limite;
    private final String libelle;

    TypeQuota(int limite, String libelle) {
        this.limite = limite;
        this.libelle = libelle;
    }

    public int limite() {
        return limite;
    }

    public String libelle() {
        return libelle;
    }
}
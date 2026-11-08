package sn.devbrik.backend.avis.dto;

import java.time.Instant;

/** Statistiques de confiance d'une brique (RG8, RG9, RG10). Publié pour les membres 2 et 4. */
public record StatsBrique(
        Double noteMoyenne,
        long nombreAvis,
        long testsOk,
        long testsKo,
        Instant derniereVerification) {

    public static StatsBrique vide() {
        return new StatsBrique(null, 0L, 0L, 0L, null);
    }
}
package sn.devbrik.backend.developpeur.dto;

/** Résultat interne du calcul RG14 : le total et ses trois composantes. */
public record Reputation(
        long points,
        long nombreBriques,
        long nombreTestsOkRecus,
        long nombreReponsesAcceptees) {
}
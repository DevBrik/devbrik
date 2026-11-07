package sn.devbrik.backend.avis.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.devbrik.backend.avis.entity.ResultatTest;

public record TestRequete(
        @NotNull(message = "Le résultat est obligatoire (OK ou KO).")
        ResultatTest resultat,

        @Size(max = 120, message = "Au plus 120 caractères.")
        String environnement) {

    public TestRequete {
        environnement = environnement == null ? null : environnement.strip();
        if (environnement != null && environnement.isEmpty()) {
            environnement = null;
        }
    }
}
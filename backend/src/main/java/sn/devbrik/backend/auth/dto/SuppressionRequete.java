package sn.devbrik.backend.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps de DELETE /api/moi : { motDePasse } (confirmation, RG17). */
public record SuppressionRequete(
        @NotNull(message = "Le mot de passe est obligatoire pour confirmer.")
        @Size(max = 200, message = "Mot de passe trop long.")
        String motDePasse) {

    @Override
    public String toString() {
        return "SuppressionRequete[motDePasse=***]";
    }
}

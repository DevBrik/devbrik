package sn.devbrik.backend.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps de PUT /api/moi/mot-de-passe (11.3). Aucun rognage : un mot de passe n'est jamais modifié. */
public record ChangementMotDePasseRequete(
        @NotNull(message = "L'ancien mot de passe est obligatoire.")
        @Size(max = 200, message = "Mot de passe trop long.")
        String ancienMotDePasse,

        @NotNull(message = "Le nouveau mot de passe est obligatoire.")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
        String nouveauMotDePasse) {

    @Override
    public String toString() {
        return "ChangementMotDePasseRequete[ancienMotDePasse=***, nouveauMotDePasse=***]";
    }
}

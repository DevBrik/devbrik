package sn.devbrik.backend.demande.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps de POST /api/demandes/{id}/reponses. RG3 : contenu 5 à 5 000. */
public record NouvelleReponse(
        @NotNull(message = "Le contenu est obligatoire.")
        @Size(min = 5, max = 5000, message = "La réponse doit contenir entre 5 et 5 000 caractères.")
        String contenu,

        Long idBrique) {

    public NouvelleReponse {
        contenu = contenu == null ? null : contenu.strip();
    }
}
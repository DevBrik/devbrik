package sn.devbrik.backend.avis.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvisRequete(
        @NotNull(message = "La note est obligatoire.")
        @Min(value = 1, message = "La note est comprise entre 1 et 5.")
        @Max(value = 5, message = "La note est comprise entre 1 et 5.")
        Integer note,

        @Size(max = 2000, message = "Au plus 2 000 caractères.")
        String commentaire) {

    public AvisRequete {
        commentaire = commentaire == null ? null : commentaire.strip();
        if (commentaire != null && commentaire.isEmpty()) {
            commentaire = null;
        }
    }
}
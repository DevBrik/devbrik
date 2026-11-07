package sn.devbrik.backend.demande.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps de POST /api/demandes. RG3 : titre 10 à 150, description 20 à 5 000. Texte brut, rogné (11.1). */
public record NouvelleDemande(
        @NotNull(message = "Le titre est obligatoire.")
        @Size(min = 10, max = 150, message = "Le titre doit contenir entre 10 et 150 caractères.")
        String titre,

        @NotNull(message = "La description est obligatoire.")
        @Size(min = 20, max = 5000, message = "La description doit contenir entre 20 et 5 000 caractères.")
        String description,

        Long idCategorie,
        Long idLangage) {

    public NouvelleDemande {
        titre = titre == null ? null : titre.strip();
        description = description == null ? null : description.strip();
    }
}
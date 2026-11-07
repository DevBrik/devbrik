package sn.devbrik.backend.avis.dto;

import jakarta.validation.constraints.NotNull;
import sn.devbrik.backend.avis.entity.ActionTraitement;

public record TraitementRequete(
        @NotNull(message = "L'action est obligatoire (MASQUER_BRIQUE ou CONSERVER).")
        ActionTraitement action) {
}
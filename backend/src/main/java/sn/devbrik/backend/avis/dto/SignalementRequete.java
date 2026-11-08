package sn.devbrik.backend.avis.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.devbrik.backend.avis.entity.MotifSignalement;

public record SignalementRequete(
        @NotNull(message = "Le motif est obligatoire.")
        MotifSignalement motif,

        @Size(max = 1000, message = "Au plus 1 000 caractères.")
        String details) {

    public SignalementRequete {
        details = details == null ? null : details.strip();
        if (details != null && details.isEmpty()) {
            details = null;
        }
    }
}
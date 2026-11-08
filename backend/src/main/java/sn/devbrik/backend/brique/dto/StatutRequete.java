package sn.devbrik.backend.brique.dto;

import jakarta.validation.constraints.NotNull;
import sn.devbrik.backend.brique.entity.StatutBrique;

/** Corps de PUT /api/admin/briques/{id}/statut : { statut: "PUBLIEE" | "MASQUEE" } (11.4). */
public record StatutRequete(@NotNull(message = "Le statut est obligatoire.") StatutBrique statut) {}

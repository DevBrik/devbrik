package sn.devbrik.backend.brique.dto;

import jakarta.validation.constraints.*;
import sn.devbrik.backend.brique.entity.Licence;

/**
 * Corps de la requête PUT /api/briques/{id} (RG7, RG15).
 */
public record BriqueModificationDto(

    @NotBlank @Size(max = 120)
    String titre,

    @NotBlank @Size(max = 300)
    String resume,

    @NotBlank @Size(min = 1, max = 30000)
    String code,

    @NotBlank @Size(min = 10, max = 10000)
    String modeEmploi,

    @Size(max = 500)
    String prerequis,

    @NotNull
    Licence licence,

    @NotNull
    Long idCategorie,

    @NotNull
    Long idLangage
) {}

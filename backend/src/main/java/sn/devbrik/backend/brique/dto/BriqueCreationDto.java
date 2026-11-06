package sn.devbrik.backend.brique.dto;

import jakarta.validation.constraints.*;
import sn.devbrik.backend.brique.entity.Licence;

/**
 * Corps de la requête POST /api/briques (RG3–RG7).
 * droitsConfirmes=true obligatoire (RG4).
 */
public record BriqueCreationDto(

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
    Long idLangage,

    /** RG4 : l'auteur doit cocher la case droits. */
    @AssertTrue(message = "Vous devez confirmer détenir les droits sur ce code (RG4).")
    boolean droitsConfirmes
) {}

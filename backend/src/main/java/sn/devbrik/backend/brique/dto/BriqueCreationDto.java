package sn.devbrik.backend.brique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.devbrik.backend.brique.entity.Licence;

/**
 * Corps de POST /api/briques (RG3, RG4, RG5).
 * Les chaînes sont rognées avant validation (11.1).
 * droitsConfirmes absent ou faux : 400 DROITS_NON_CONFIRMES, levé par le service (RG4).
 */
public record BriqueCreationDto(

    @NotBlank @Size(min = 5, max = 120, message = "Le titre doit contenir entre 5 et 120 caractères.")
    String titre,

    @NotBlank @Size(min = 10, max = 300, message = "Le résumé doit contenir entre 10 et 300 caractères.")
    String resume,

    @NotBlank @Size(min = 1, max = 30000, message = "Le code doit contenir entre 1 et 30 000 caractères.")
    String code,

    @NotBlank @Size(min = 10, max = 10000, message = "Le mode d'emploi doit contenir entre 10 et 10 000 caractères.")
    String modeEmploi,

    @Size(max = 500, message = "Les prérequis ne peuvent pas dépasser 500 caractères.")
    String prerequis,

    @NotNull(message = "La licence est obligatoire.")
    Licence licence,

    @NotNull(message = "La catégorie est obligatoire.")
    Long idCategorie,

    @NotNull(message = "Le langage est obligatoire.")
    Long idLangage,

    Boolean droitsConfirmes
) {
    public BriqueCreationDto {
        titre      = Textes.rogner(titre);
        resume     = Textes.rogner(resume);
        code       = Textes.rogner(code);
        modeEmploi = Textes.rogner(modeEmploi);
        prerequis  = Textes.vide(Textes.rogner(prerequis));
    }
}

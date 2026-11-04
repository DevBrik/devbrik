package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.Brique;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.time.Instant;

/**
 * Fiche complète d'une brique (M02) — inclut le code et le mode d'emploi.
 */
public record BriqueDetailDto(
    Long                 id,
    String               titre,
    String               resume,
    String               code,
    String               modeEmploi,
    String               prerequis,
    CategorieDto         categorie,
    LangageDto           langage,
    String               licence,
    int                  versionCode,
    Instant              dateCreation,
    Instant              dateModification,
    ReferenceUtilisateur auteur
) {
    public static BriqueDetailDto de(Brique b, ReferenceUtilisateur auteur) {
        return new BriqueDetailDto(
            b.getId(), b.getTitre(), b.getResume(),
            b.getCode(), b.getModeEmploi(), b.getPrerequis(),
            CategorieDto.de(b.getCategorie()),
            LangageDto.de(b.getLangage()),
            b.getLicence().name(),
            b.getVersionCode(),
            b.getDateCreation(),
            b.getDateModification(),
            auteur
        );
    }
}

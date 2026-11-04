package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.Brique;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.time.Instant;

/**
 * Résumé utilisé dans la liste publique M01.
 */
public record BriqueResumeDto(
    Long              id,
    String            titre,
    String            resume,
    CategorieDto      categorie,
    LangageDto        langage,
    String            licence,
    int               versionCode,
    Instant           dateCreation,
    ReferenceUtilisateur auteur
) {
    public static BriqueResumeDto de(Brique b, ReferenceUtilisateur auteur) {
        return new BriqueResumeDto(
            b.getId(), b.getTitre(), b.getResume(),
            CategorieDto.de(b.getCategorie()),
            LangageDto.de(b.getLangage()),
            b.getLicence().name(),
            b.getVersionCode(),
            b.getDateCreation(),
            auteur
        );
    }
}

package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.Brique;

import java.time.Instant;

/**
 * Résumé utilisé dans M05 (mes briques) — inclut le statut, pas l'auteur.
 */
public record BriqueResumeInterne(
    Long         id,
    String       titre,
    String       resume,
    CategorieDto categorie,
    LangageDto   langage,
    String       licence,
    String       statut,
    int          versionCode,
    Instant      dateCreation,
    Instant      dateModification
) {
    public static BriqueResumeInterne de(Brique b) {
        return new BriqueResumeInterne(
            b.getId(), b.getTitre(), b.getResume(),
            CategorieDto.de(b.getCategorie()),
            LangageDto.de(b.getLangage()),
            b.getLicence().name(),
            b.getStatut().name(),
            b.getVersionCode(),
            b.getDateCreation(),
            b.getDateModification()
        );
    }
}

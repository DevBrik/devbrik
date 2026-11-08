package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.Brique;

/**
 * Résumé interne publié aux autres modules par BriqueService.resume (13.4) :
 * id, idAuteur, statut, versionCode, titre.
 */
public record BriqueResumeInterne(
    Long   id,
    Long   idAuteur,
    String statut,
    int    versionCode,
    String titre
) {
    public static BriqueResumeInterne de(Brique b) {
        return new BriqueResumeInterne(
            b.getId(), b.getIdAuteur(), b.getStatut().name(), b.getVersionCode(), b.getTitre());
    }
}

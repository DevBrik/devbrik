package sn.devbrik.backend.avis.dto;

import sn.devbrik.backend.avis.entity.ActionTraitement;
import sn.devbrik.backend.avis.entity.MotifSignalement;
import sn.devbrik.backend.avis.entity.StatutSignalement;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.time.Instant;

public record SignalementReponse(
        Long id,
        MotifSignalement motif,
        String details,
        StatutSignalement statut,
        ActionTraitement actionTraitement,
        Instant dateCreation,
        Instant dateTraitement,
        BriqueSignalee brique,
        ReferenceUtilisateur auteur) {

    /** brique: {id, titre, statut, auteur} (11.5). auteur = auteur de la brique, pas celui du signalement. */
    public record BriqueSignalee(Long id, String titre, String statut, ReferenceUtilisateur auteur) {
    }
}
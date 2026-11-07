package sn.devbrik.backend.avis.dto;

import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.time.Instant;

public record AvisReponse(
        Long id,
        Integer note,
        String commentaire,
        ReferenceUtilisateur auteur,
        Instant dateCreation,
        Instant dateModification) {
}
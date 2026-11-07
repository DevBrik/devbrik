package sn.devbrik.backend.demande.dto;

import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.demande.entity.StatutDemande;

import java.time.Instant;

/** Contrat 11.6 « DemandeResume ». categorie et langage valent null s'ils sont absents. */
public record DemandeResume(
        Long id,
        String titre,
        StatutDemande statut,
        Instant dateCreation,
        ElementReference categorie,
        ElementReference langage,
        ReferenceUtilisateur auteur,
        long nombreReponses) {
}
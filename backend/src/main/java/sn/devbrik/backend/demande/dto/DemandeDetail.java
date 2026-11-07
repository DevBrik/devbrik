package sn.devbrik.backend.demande.dto;

import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.demande.entity.StatutDemande;

import java.time.Instant;
import java.util.List;

/** Contrat 11.6 « DemandeDetail » : DemandeResume + description + reponses. */
public record DemandeDetail(
        Long id,
        String titre,
        StatutDemande statut,
        Instant dateCreation,
        ElementReference categorie,
        ElementReference langage,
        ReferenceUtilisateur auteur,
        long nombreReponses,
        String description,
        List<ReponseDto> reponses) {

    public static DemandeDetail de(DemandeResume resume, String description, List<ReponseDto> reponses) {
        return new DemandeDetail(
                resume.id(), resume.titre(), resume.statut(), resume.dateCreation(),
                resume.categorie(), resume.langage(), resume.auteur(),
                reponses.size(), description, reponses);
    }
}
package sn.devbrik.backend.demande.dto;

import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.time.Instant;

/** Contrat 11.6 « Reponse ». brique vaut null si le lien n'est plus valide (RG13). */
public record ReponseDto(
        Long id,
        String contenu,
        boolean acceptee,
        Instant dateCreation,
        ReferenceUtilisateur auteur,
        BriqueLiee brique) {
}
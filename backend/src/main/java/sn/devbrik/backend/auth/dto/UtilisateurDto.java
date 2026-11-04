package sn.devbrik.backend.auth.dto;

import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;

import java.time.Instant;

/** « Utilisateur » du contrat 11.3. Ne contient jamais le mot de passe. */
public record UtilisateurDto(
        Long id,
        String pseudo,
        String nomAffiche,
        Role role,
        String pays,
        String bio,
        String lienProfil,
        Instant dateCreation) {

    public static UtilisateurDto depuis(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getPseudo(), u.getNomAffiche(), u.getRole(),
                u.getPays(), u.getBio(), u.getLienProfil(), u.getDateCreation());
    }
}

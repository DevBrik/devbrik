package sn.devbrik.backend.auth.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import sn.devbrik.backend.auth.service.ErreursAuth;

/**
 * Lit l'identifiant de l'utilisateur connecté dans le champ « sub » du jeton (11.1).
 * À remplacer par IdentiteUtilisateur (membre 1) si son API convient.
 */
final class IdentiteCourante {

    private IdentiteCourante() {
    }

    static Long id(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw ErreursAuth.nonAuthentifie();
        }
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw ErreursAuth.nonAuthentifie();
        }
    }
}

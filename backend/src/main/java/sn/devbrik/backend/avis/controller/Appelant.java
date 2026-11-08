package sn.devbrik.backend.avis.controller;

import org.springframework.stereotype.Component;
import sn.devbrik.backend.common.IdentiteUtilisateur;

/** Identité de l'appelant, lue dans le jeton via IdentiteUtilisateur (membre 1). */
@Component
public class Appelant {

    /** null pour un visiteur. */
    public Long idOuNull() {
        return IdentiteUtilisateur.optionnelle().map(IdentiteUtilisateur::id).orElse(null);
    }

    /** 401 NON_AUTHENTIFIE si absent. */
    public Long idObligatoire() {
        return IdentiteUtilisateur.obligatoire().id();
    }

    public boolean estAdmin() {
        return IdentiteUtilisateur.optionnelle().map(IdentiteUtilisateur::estAdmin).orElse(false);
    }
}
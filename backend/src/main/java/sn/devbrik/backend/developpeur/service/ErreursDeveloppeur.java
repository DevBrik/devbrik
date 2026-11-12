package sn.devbrik.backend.developpeur.service;

import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;

/** Fabrique des erreurs du module developpeur (catalogue 11.7). */
final class ErreursDeveloppeur {

    private ErreursDeveloppeur() {
    }

    static ApiException introuvable() {
        return new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Développeur introuvable.");
    }

    static ApiException requeteInvalide(String message) {
        return new ApiException(CodeErreur.REQUETE_INVALIDE, message);
    }
}

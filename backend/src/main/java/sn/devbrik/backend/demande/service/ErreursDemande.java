package sn.devbrik.backend.demande.service;

import org.springframework.http.HttpStatus;
import sn.devbrik.backend.common.ApiException;

/**
 * Seul endroit de mon module qui construit une ApiException (catalogue 11.7).
 * Hypothèse à confirmer : new ApiException(HttpStatus, code, message).
 */
final class ErreursDemande {

    private ErreursDemande() {
    }

    static ApiException introuvable(String ressource) {
        return new ApiException(HttpStatus.NOT_FOUND, "RESSOURCE_INTROUVABLE", ressource + " introuvable.");
    }

    static ApiException requeteInvalide(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE", message);
    }

    static ApiException accesRefuse() {
        return accesRefuse("Cette action est réservée à l'auteur ou à un administrateur.");
    }

    static ApiException accesRefuse(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, "ACCES_REFUSE", message);
    }

    /** RG13. */
    static ApiException briqueLieeInvalide() {
        return new ApiException(HttpStatus.BAD_REQUEST, "BRIQUE_LIEE_INVALIDE",
                "La brique liée doit exister et être publiée.");
    }

    /** RG12. */
    static ApiException acceptationPropreReponse() {
        return new ApiException(HttpStatus.FORBIDDEN, "ACCEPTATION_PROPRE_REPONSE",
                "Vous ne pouvez pas accepter votre propre réponse.");
    }

    /** RG12. */
    static ApiException reponseDejaAcceptee() {
        return new ApiException(HttpStatus.CONFLICT, "REPONSE_DEJA_ACCEPTEE",
                "Une autre réponse est déjà acceptée pour cette demande.");
    }
}
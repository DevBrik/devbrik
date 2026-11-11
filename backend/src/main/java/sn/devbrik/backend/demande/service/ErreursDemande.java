package sn.devbrik.backend.demande.service;

import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;

/**
 * Seul endroit du module demande qui construit une ApiException (catalogue 11.7).
 * Le statut HTTP vient du code (CodeErreur, membre 1).
 */
final class ErreursDemande {

    private ErreursDemande() {
    }

    static ApiException introuvable(String ressource) {
        return new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, ressource + " introuvable.");
    }

    static ApiException requeteInvalide(String message) {
        return new ApiException(CodeErreur.REQUETE_INVALIDE, message);
    }

    static ApiException accesRefuse() {
        return accesRefuse("Cette action est réservée à l'auteur ou à un administrateur.");
    }

    static ApiException accesRefuse(String message) {
        return new ApiException(CodeErreur.ACCES_REFUSE, message);
    }

    /** RG13. */
    static ApiException briqueLieeInvalide() {
        return new ApiException(CodeErreur.BRIQUE_LIEE_INVALIDE,
                "La brique liée doit exister et être publiée.");
    }

    /** RG12. */
    static ApiException acceptationPropreReponse() {
        return new ApiException(CodeErreur.ACCEPTATION_PROPRE_REPONSE,
                "Vous ne pouvez pas accepter votre propre réponse.");
    }

    /** RG12. */
    static ApiException reponseDejaAcceptee() {
        return new ApiException(CodeErreur.REPONSE_DEJA_ACCEPTEE,
                "Une autre réponse est déjà acceptée pour cette demande.");
    }
}

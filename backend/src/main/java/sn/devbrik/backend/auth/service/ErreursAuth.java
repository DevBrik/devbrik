package sn.devbrik.backend.auth.service;

import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;

/**
 * Fabrique des erreurs du module auth (codes du catalogue 11.7).
 * Le statut HTTP vient du code (CodeErreur, membre 1) : un seul point à adapter si le socle change.
 */
public final class ErreursAuth {

    private ErreursAuth() {
    }

    public static ApiException requeteInvalide(String message) {
        return new ApiException(CodeErreur.REQUETE_INVALIDE, message);
    }

    public static ApiException pseudoReserve() {
        return new ApiException(CodeErreur.PSEUDO_RESERVE, "Ce pseudo est réservé. Choisissez-en un autre.");
    }

    public static ApiException pseudoDejaUtilise() {
        return new ApiException(CodeErreur.PSEUDO_DEJA_UTILISE, "Ce pseudo est déjà utilisé.");
    }

    /** Message volontairement identique pour « pseudo inconnu » et « mot de passe faux ». */
    public static ApiException identifiantsInvalides() {
        return new ApiException(CodeErreur.IDENTIFIANTS_INVALIDES, "Pseudo ou mot de passe incorrect.");
    }

    public static ApiException nonAuthentifie() {
        return new ApiException(CodeErreur.NON_AUTHENTIFIE, "Authentification requise.");
    }

    public static ApiException motDePasseIncorrect() {
        return new ApiException(CodeErreur.MOT_DE_PASSE_INCORRECT, "Le mot de passe est incorrect.");
    }

    public static ApiException suppressionAdminInterdite() {
        return new ApiException(CodeErreur.SUPPRESSION_ADMIN_INTERDITE,
                "Un compte administrateur ne peut pas être supprimé.");
    }
}

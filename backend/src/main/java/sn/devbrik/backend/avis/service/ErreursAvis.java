package sn.devbrik.backend.avis.service;

import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;

/** Point unique de fabrication des ApiException du module (codes du catalogue 11.7). */
final class ErreursAvis {

    private ErreursAvis() {
    }

    static ApiException briqueIntrouvable() {
        return new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Brique introuvable.");
    }

    static ApiException avisIntrouvable() {
        return new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Avis introuvable.");
    }

    static ApiException testIntrouvable() {
        return new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Aucun test de votre part sur cette brique.");
    }

    static ApiException signalementIntrouvable() {
        return new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Signalement introuvable.");
    }

    static ApiException autoEvaluation() {
        return new ApiException(CodeErreur.AUTO_EVALUATION_INTERDITE,
                "Vous ne pouvez pas évaluer votre propre brique.");
    }

    static ApiException accesRefuse() {
        return new ApiException(CodeErreur.ACCES_REFUSE, "Action réservée à l'auteur ou à un administrateur.");
    }

    static ApiException reserveAdministrateur() {
        return new ApiException(CodeErreur.ACCES_REFUSE, "Action réservée aux administrateurs.");
    }

    static ApiException signalementDejaEnvoye() {
        return new ApiException(CodeErreur.SIGNALEMENT_DEJA_ENVOYE, "Vous avez déjà signalé cette brique.");
    }

    static ApiException signalementDejaTraite() {
        return new ApiException(CodeErreur.REQUETE_INVALIDE, "Ce signalement a déjà été traité.");
    }

    static ApiException requeteInvalide(String message) {
        return new ApiException(CodeErreur.REQUETE_INVALIDE, message);
    }
}
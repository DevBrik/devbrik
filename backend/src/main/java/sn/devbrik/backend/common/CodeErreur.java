package sn.devbrik.backend.common;

import org.springframework.http.HttpStatus;

public enum CodeErreur {
    REQUETE_INVALIDE(HttpStatus.BAD_REQUEST),
    DROITS_NON_CONFIRMES(HttpStatus.BAD_REQUEST),
    BRIQUE_LIEE_INVALIDE(HttpStatus.BAD_REQUEST),
    MOT_DE_PASSE_INCORRECT(HttpStatus.BAD_REQUEST),
    PSEUDO_RESERVE(HttpStatus.BAD_REQUEST),
    NON_AUTHENTIFIE(HttpStatus.UNAUTHORIZED),
    IDENTIFIANTS_INVALIDES(HttpStatus.UNAUTHORIZED),
    ACCES_REFUSE(HttpStatus.FORBIDDEN),
    AUTO_EVALUATION_INTERDITE(HttpStatus.FORBIDDEN),
    ACCEPTATION_PROPRE_REPONSE(HttpStatus.FORBIDDEN),
    SUPPRESSION_ADMIN_INTERDITE(HttpStatus.FORBIDDEN),
    RESSOURCE_INTROUVABLE(HttpStatus.NOT_FOUND),
    PSEUDO_DEJA_UTILISE(HttpStatus.CONFLICT),
    TITRE_DEJA_UTILISE(HttpStatus.CONFLICT),
    SIGNALEMENT_DEJA_ENVOYE(HttpStatus.CONFLICT),
    REPONSE_DEJA_ACCEPTEE(HttpStatus.CONFLICT),
    LIMITE_QUOTIDIENNE(HttpStatus.TOO_MANY_REQUESTS),
    ERREUR_INTERNE(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus statut;

    CodeErreur(HttpStatus statut) {
        this.statut = statut;
    }

    public HttpStatus statut() {
        return statut;
    }
}
package sn.devbrik.backend.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Catalogue des erreurs (11.7) : 18 codes et leur statut HTTP. */
class CodeErreurTest {

    private static final Map<CodeErreur, HttpStatus> CATALOGUE = Map.ofEntries(
            Map.entry(CodeErreur.REQUETE_INVALIDE, HttpStatus.BAD_REQUEST),
            Map.entry(CodeErreur.DROITS_NON_CONFIRMES, HttpStatus.BAD_REQUEST),
            Map.entry(CodeErreur.BRIQUE_LIEE_INVALIDE, HttpStatus.BAD_REQUEST),
            Map.entry(CodeErreur.MOT_DE_PASSE_INCORRECT, HttpStatus.BAD_REQUEST),
            Map.entry(CodeErreur.PSEUDO_RESERVE, HttpStatus.BAD_REQUEST),
            Map.entry(CodeErreur.NON_AUTHENTIFIE, HttpStatus.UNAUTHORIZED),
            Map.entry(CodeErreur.IDENTIFIANTS_INVALIDES, HttpStatus.UNAUTHORIZED),
            Map.entry(CodeErreur.ACCES_REFUSE, HttpStatus.FORBIDDEN),
            Map.entry(CodeErreur.AUTO_EVALUATION_INTERDITE, HttpStatus.FORBIDDEN),
            Map.entry(CodeErreur.ACCEPTATION_PROPRE_REPONSE, HttpStatus.FORBIDDEN),
            Map.entry(CodeErreur.SUPPRESSION_ADMIN_INTERDITE, HttpStatus.FORBIDDEN),
            Map.entry(CodeErreur.RESSOURCE_INTROUVABLE, HttpStatus.NOT_FOUND),
            Map.entry(CodeErreur.PSEUDO_DEJA_UTILISE, HttpStatus.CONFLICT),
            Map.entry(CodeErreur.TITRE_DEJA_UTILISE, HttpStatus.CONFLICT),
            Map.entry(CodeErreur.SIGNALEMENT_DEJA_ENVOYE, HttpStatus.CONFLICT),
            Map.entry(CodeErreur.REPONSE_DEJA_ACCEPTEE, HttpStatus.CONFLICT),
            Map.entry(CodeErreur.LIMITE_QUOTIDIENNE, HttpStatus.TOO_MANY_REQUESTS),
            Map.entry(CodeErreur.ERREUR_INTERNE, HttpStatus.INTERNAL_SERVER_ERROR));

    @Test
    void le_catalogue_contient_exactement_les_18_codes_du_dossier() {
        assertThat(CodeErreur.values()).containsExactlyInAnyOrderElementsOf(CATALOGUE.keySet());
        assertThat(CodeErreur.values()).hasSize(18);
    }

    @Test
    void chaque_code_porte_le_statut_http_du_dossier() {
        CATALOGUE.forEach((code, statut) -> assertThat(code.statut()).as(code.name()).isEqualTo(statut));
    }
}

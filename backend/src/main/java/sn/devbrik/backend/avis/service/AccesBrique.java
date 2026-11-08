package sn.devbrik.backend.avis.service;

import org.springframework.stereotype.Component;

import java.util.Objects;

/** Règles d'accès à une brique pour le module avis (11.5, RG7). */
@Component
public class AccesBrique {

    private final BriqueLecture briques;

    public AccesBrique(BriqueLecture briques) {
        this.briques = briques;
    }

    /**
     * Lecture : une brique MASQUEE n'est visible que de son auteur et des administrateurs, sinon 404.
     * idAppelant peut être null (visiteur).
     */
    public InfoBrique briqueVisible(Long idBrique, Long idAppelant, boolean admin) {
        InfoBrique brique = briques.trouver(idBrique).orElseThrow(ErreursAvis::briqueIntrouvable);
        if (brique.estMasquee() && !admin && !Objects.equals(brique.idAuteur(), idAppelant)) {
            throw ErreursAvis.briqueIntrouvable();
        }
        return brique;
    }

    /**
     * Écriture (avis, test, signalement) :
     * - sa propre brique : 403 AUTO_EVALUATION_INTERDITE, même masquée ;
     * - brique masquée d'un autre : 404, administrateur compris.
     */
    public InfoBrique briqueEvaluable(Long idBrique, Long idAppelant) {
        InfoBrique brique = briques.trouver(idBrique).orElseThrow(ErreursAvis::briqueIntrouvable);
        if (Objects.equals(brique.idAuteur(), idAppelant)) {
            throw ErreursAvis.autoEvaluation();
        }
        if (brique.estMasquee()) {
            throw ErreursAvis.briqueIntrouvable();
        }
        return brique;
    }
}
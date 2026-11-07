package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.avis.dto.StatsBrique;
import sn.devbrik.backend.brique.entity.Brique;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.time.Instant;

/**
 * BriqueDetailDto (11.4) : tous les champs de BriqueResumeDto plus code, modeEmploi,
 * prerequis, idCategorie et idLangage.
 */
public record BriqueDetailDto(
    Long                 id,
    String               titre,
    String               resume,
    String               licence,
    String               statut,
    int                  versionCode,
    Instant              dateCreation,
    Instant              dateModification,
    CategorieDto         categorie,
    LangageDto           langage,
    ReferenceUtilisateur auteur,
    Double               noteMoyenne,
    long                 nombreAvis,
    long                 testsOk,
    long                 testsKo,
    Instant              derniereVerification,
    String               code,
    String               modeEmploi,
    String               prerequis,
    Long                 idCategorie,
    Long                 idLangage
) {
    public static BriqueDetailDto de(Brique b, ReferenceUtilisateur auteur, StatsBrique s) {
        StatsBrique stats = s == null ? StatsBrique.vide() : s;
        return new BriqueDetailDto(
            b.getId(), b.getTitre(), b.getResume(),
            b.getLicence().name(), b.getStatut().name(), b.getVersionCode(),
            b.getDateCreation(), b.getDateModification(),
            CategorieDto.de(b.getCategorie()),
            LangageDto.de(b.getLangage()),
            auteur,
            stats.noteMoyenne(), stats.nombreAvis(), stats.testsOk(), stats.testsKo(),
            stats.derniereVerification(),
            b.getCode(), b.getModeEmploi(), b.getPrerequis(),
            b.getCategorie().getId(), b.getLangage().getId()
        );
    }
}

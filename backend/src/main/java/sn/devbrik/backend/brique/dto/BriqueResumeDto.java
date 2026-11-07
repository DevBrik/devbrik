package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.avis.dto.StatsBrique;
import sn.devbrik.backend.brique.entity.Brique;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.time.Instant;

/**
 * BriqueResumeDto (11.4) : liste publique, mes briques et liste d'administration.
 * Les statistiques (note, avis, tests de la version actuelle, dernière vérification)
 * viennent de StatsBriqueService (module avis).
 */
public record BriqueResumeDto(
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
    Instant              derniereVerification
) {
    public static BriqueResumeDto de(Brique b, ReferenceUtilisateur auteur, StatsBrique s) {
        StatsBrique stats = s == null ? StatsBrique.vide() : s;
        return new BriqueResumeDto(
            b.getId(), b.getTitre(), b.getResume(),
            b.getLicence().name(), b.getStatut().name(), b.getVersionCode(),
            b.getDateCreation(), b.getDateModification(),
            CategorieDto.de(b.getCategorie()),
            LangageDto.de(b.getLangage()),
            auteur,
            stats.noteMoyenne(), stats.nombreAvis(), stats.testsOk(), stats.testsKo(),
            stats.derniereVerification()
        );
    }
}

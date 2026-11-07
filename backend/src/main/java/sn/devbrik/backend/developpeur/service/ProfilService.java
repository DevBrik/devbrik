package sn.devbrik.backend.developpeur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.developpeur.dto.ProfilPublic;
import sn.devbrik.backend.developpeur.dto.Reputation;

@Service
public class ProfilService {

    private final ProfilLecture profilLecture;
    private final ReputationService reputationService;

    public ProfilService(ProfilLecture profilLecture, ReputationService reputationService) {
        this.profilLecture = profilLecture;
        this.reputationService = reputationService;
    }

    /** Profil public d'un développeur ; 404 si l'identifiant est inconnu. */
    @Transactional(readOnly = true)
    public ProfilPublic profil(Long id) {
        ProfilLecture.ProfilUtilisateur u = profilLecture.parId(id)
                .orElseThrow(ErreursDeveloppeur::introuvable);
        Reputation r = reputationService.calculer(id);
        return new ProfilPublic(
                u.id(), u.pseudo(), u.nomAffiche(), u.pays(), u.bio(), u.lienProfil(), u.dateCreation(),
                r.points(), r.nombreBriques(), r.nombreTestsOkRecus(), r.nombreReponsesAcceptees());
    }
}
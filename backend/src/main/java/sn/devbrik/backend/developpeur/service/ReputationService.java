package sn.devbrik.backend.developpeur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.avis.service.StatsBriqueService;      // membre 3 (hypothèse de paquet)
import sn.devbrik.backend.brique.service.BriqueService;         // membre 2 (hypothèse de paquet)
import sn.devbrik.backend.demande.service.DemandeService;
import sn.devbrik.backend.developpeur.dto.Reputation;

/**
 * RG14 : réputation calculée à la demande, jamais stockée.
 * 10 points par brique publiée + 5 par test OK reçu (version actuelle) + 3 par réponse acceptée.
 * Les trois nombres viennent des services publics (13.4) ; aucune écriture, aucune table des autres modules.
 */
@Service
public class ReputationService {

    static final int POINTS_PAR_BRIQUE_PUBLIEE = 10;
    static final int POINTS_PAR_TEST_OK_RECU = 5;
    static final int POINTS_PAR_REPONSE_ACCEPTEE = 3;

    private final BriqueService briqueService;
    private final StatsBriqueService statsBriqueService;
    private final DemandeService demandeService;

    public ReputationService(BriqueService briqueService,
                             StatsBriqueService statsBriqueService,
                             DemandeService demandeService) {
        this.briqueService = briqueService;
        this.statsBriqueService = statsBriqueService;
        this.demandeService = demandeService;
    }

    @Transactional(readOnly = true)
    public Reputation calculer(Long idAuteur) {
        long briques = briqueService.compterPubliees(idAuteur);
        long testsOk = statsBriqueService.compterTestsOkRecus(idAuteur);
        long acceptees = demandeService.compterReponsesAcceptees(idAuteur);

        long points = briques * POINTS_PAR_BRIQUE_PUBLIEE
                + testsOk * POINTS_PAR_TEST_OK_RECU
                + acceptees * POINTS_PAR_REPONSE_ACCEPTEE;
        return new Reputation(points, briques, testsOk, acceptees);
    }
}
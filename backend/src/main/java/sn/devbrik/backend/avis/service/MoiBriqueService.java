package sn.devbrik.backend.avis.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.avis.dto.MoiBriqueReponse;

@Service
public class MoiBriqueService {

    private final AccesBrique accesBrique;
    private final AvisService avisService;
    private final TestBriqueService testService;

    public MoiBriqueService(AccesBrique accesBrique, AvisService avisService, TestBriqueService testService) {
        this.accesBrique = accesBrique;
        this.avisService = avisService;
        this.testService = testService;
    }

    /** GET /api/briques/{id}/moi : chacun des deux peut être null. */
    @Transactional(readOnly = true)
    public MoiBriqueReponse moi(Long idBrique, Long idAppelant, boolean admin) {
        accesBrique.briqueVisible(idBrique, idAppelant, admin);
        return new MoiBriqueReponse(
                avisService.monAvis(idBrique, idAppelant).orElse(null),
                testService.monTest(idBrique, idAppelant).orElse(null));
    }
}
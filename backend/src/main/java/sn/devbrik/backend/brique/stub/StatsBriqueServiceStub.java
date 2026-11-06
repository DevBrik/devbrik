package sn.devbrik.backend.brique.stub;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sn.devbrik.backend.brique.port.StatsBriqueService;

/**
 * Stub neutre actif hors production (dev, test).
 * Remplacé par l'implémentation réelle du Membre 3 en profil prod.
 */
@Service
@Profile("!prod")
public class StatsBriqueServiceStub implements StatsBriqueService {

    @Override
    public double noteMoyenne(Long idBrique) {
        return 0.0;
    }

    @Override
    public long nombreTestsOk(Long idBrique) {
        return 0L;
    }
}

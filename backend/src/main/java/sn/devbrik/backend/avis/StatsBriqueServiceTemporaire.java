// backend/src/main/java/sn/devbrik/backend/avis/service/StatsBriqueServiceTemporaire.java
package sn.devbrik.backend.avis.service;

import org.springframework.stereotype.Service;

/** FAUX TEMPORAIRE (membre 3) : permet de démarrer Spring. Ne pas commiter. */
@Service
public class StatsBriqueServiceTemporaire implements StatsBriqueService {

    @Override
    public long compterTestsOkRecus(Long idAuteur) {
        return 0L;
    }
}
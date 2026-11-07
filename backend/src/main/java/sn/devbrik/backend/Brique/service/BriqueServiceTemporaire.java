// backend/src/main/java/sn/devbrik/backend/brique/service/BriqueServiceTemporaire.java
package sn.devbrik.backend.Brique.service;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/** FAUX TEMPORAIRE (membre 2) : à supprimer avec l'interface. Ne pas commiter. */
@Service
public class BriqueServiceTemporaire implements BriqueService {

    @Override
    public Map<Long, BriqueReference> references(Collection<Long> ids) {
        return Map.of();
    }

    @Override
    public long compterPubliees(Long idAuteur) {
        return 0L;
    }
}
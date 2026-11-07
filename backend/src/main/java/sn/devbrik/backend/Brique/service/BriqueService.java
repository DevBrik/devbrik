package sn.devbrik.backend.Brique.service;

import java.util.Collection;
import java.util.Map;

/**
 * FAUX TEMPORAIRE (membre 2) : interface seule, sans implémentation.
 * Elle reproduit la signature de 13.4. À supprimer à la livraison de la vraie classe. Ne pas commiter.
 * Dans le vrai code, BriqueService est une classe @Service ; pour Mockito, une interface suffit.
 */
public interface BriqueService {

    Map<Long, BriqueReference> references(Collection<Long> ids);

    long compterPubliees(Long idAuteur);
}
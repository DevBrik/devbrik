package sn.devbrik.backend.brique.port;

/**
 * Port (interface) vers le module stats (Membre 3).
 *
 * Cette interface est implémentée par le module stats (feature/m3-entites-stats).
 * En attendant, {@link sn.devbrik.backend.brique.stub.StatsBriqueServiceStub}
 * fournit une implémentation neutre active hors production.
 */
public interface StatsBriqueService {

    /**
     * Retourne la note moyenne d'une brique (0.0 si aucun avis).
     */
    double noteMoyenne(Long idBrique);

    /**
     * Retourne le nombre de tests réussis (résultat OK) pour une brique.
     */
    long nombreTestsOk(Long idBrique);
}

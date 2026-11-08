package sn.devbrik.backend.avis.repository;

/** Résultat agrégé par brique, limité aux tests de la version actuelle. */
public interface AgregatTests {
    Long getIdBrique();
    Long getOk();
    Long getKo();

    /** Type brut : selon le pilote, un Instant, un OffsetDateTime ou un Timestamp. Converti dans le service. */
    Object getDerniere();
}
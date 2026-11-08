package sn.devbrik.backend.avis.repository;

/** Résultat agrégé par brique : nombre d'avis et somme des notes. */
public interface AgregatAvis {
    Long getIdBrique();
    Long getNombre();
    Long getSomme();
}
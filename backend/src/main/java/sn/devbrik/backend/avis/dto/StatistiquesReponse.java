package sn.devbrik.backend.avis.dto;

public record StatistiquesReponse(
        long utilisateurs,
        long briquesPubliees,
        long briquesMasquees,
        long avis,
        long tests,
        long demandesOuvertes,
        long demandesResolues,
        long signalementsOuverts) {
}
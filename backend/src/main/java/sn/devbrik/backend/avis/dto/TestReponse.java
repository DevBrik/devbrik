package sn.devbrik.backend.avis.dto;

import sn.devbrik.backend.avis.entity.ResultatTest;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.time.Instant;

public record TestReponse(
        Long id,
        ResultatTest resultat,
        String environnement,
        Integer versionCode,
        ReferenceUtilisateur auteur,
        Instant dateTest) {
}
package sn.devbrik.backend.developpeur.dto;

import sn.devbrik.backend.common.ReferenceUtilisateur;

/** Un élément du classement (11.6, avenant) : la réputation et ses trois composantes (RG14). */
public record ClassementEntree(
        ReferenceUtilisateur developpeur,
        String pays,
        long reputation,
        long nombreBriques,
        long testsOkRecus,
        long nombreReponsesAcceptees) {
}

package sn.devbrik.backend.developpeur.dto;

import java.time.Instant;

/**
 * Contrat 11.6 « ProfilPublic » + nombreTestsOkRecus (ajout proposé, pour le détail
 * du calcul affiché par M09). La réputation est calculée à la demande (RG14).
 */
public record ProfilPublic(
        Long id,
        String pseudo,
        String nomAffiche,
        String pays,
        String bio,
        String lienProfil,
        Instant dateCreation,
        long reputation,
        long nombreBriques,
        long nombreTestsOkRecus,
        long nombreReponsesAcceptees) {
}
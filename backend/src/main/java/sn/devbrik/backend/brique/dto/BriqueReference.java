package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.Brique;
import sn.devbrik.backend.brique.entity.StatutBrique;

/**
 * Référence légère publiée aux autres modules par BriqueService.references (13.4) :
 * id, titre, et si la brique est publiée ou non.
 */
public record BriqueReference(Long id, String titre, boolean publiee) {

    public static BriqueReference de(Brique b) {
        return new BriqueReference(b.getId(), b.getTitre(), b.getStatut() == StatutBrique.PUBLIEE);
    }
}

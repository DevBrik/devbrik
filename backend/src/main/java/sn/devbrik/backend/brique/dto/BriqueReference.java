package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.Brique;

/**
 * Référence légère d'une brique (id + titre + langage).
 * Utilisée par d'autres modules (ex. module réponse) sans dépendance cyclique.
 */
public record BriqueReference(Long id, String titre, String langageNom) {

    public static BriqueReference de(Brique b) {
        return new BriqueReference(b.getId(), b.getTitre(), b.getLangage().getNom());
    }
}

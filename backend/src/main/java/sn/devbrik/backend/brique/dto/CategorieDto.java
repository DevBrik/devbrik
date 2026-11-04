package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.Categorie;

public record CategorieDto(Long id, String slug, String nom, String description) {

    public static CategorieDto de(Categorie c) {
        return new CategorieDto(c.getId(), c.getSlug(), c.getNom(), c.getDescription());
    }
}

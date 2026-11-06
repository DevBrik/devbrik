package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.Langage;

public record LangageDto(Long id, String slug, String nom) {

    public static LangageDto de(Langage l) {
        return new LangageDto(l.getId(), l.getSlug(), l.getNom());
    }
}

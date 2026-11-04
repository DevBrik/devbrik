package sn.devbrik.backend.brique.dto;

import sn.devbrik.backend.brique.entity.StatutBrique;

public record StatutDto(StatutBrique valeur, String libelle) {

    public static StatutDto de(StatutBrique s) {
        String lib = switch (s) {
            case PUBLIEE -> "Publiée";
            case MASQUEE -> "Masquée";
        };
        return new StatutDto(s, lib);
    }
}

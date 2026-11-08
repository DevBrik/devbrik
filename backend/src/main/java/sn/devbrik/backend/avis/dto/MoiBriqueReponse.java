package sn.devbrik.backend.avis.dto;

/** GET /api/briques/{id}/moi : chacun des deux peut être null. */
public record MoiBriqueReponse(AvisReponse monAvis, TestReponse monTest) {
}
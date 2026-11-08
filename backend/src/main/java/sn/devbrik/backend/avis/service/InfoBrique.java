package sn.devbrik.backend.avis.service;

/** Ce que le module avis a besoin de savoir d'une brique : lu en SQL, en lecture seule (règle 2 de 9.4). */
public record InfoBrique(Long id, Long idAuteur, String statut, int versionCode, String titre) {

    public boolean estMasquee() {
        return "MASQUEE".equals(statut);
    }
}
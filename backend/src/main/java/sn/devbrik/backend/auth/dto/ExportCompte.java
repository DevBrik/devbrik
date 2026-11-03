package sn.devbrik.backend.auth.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Export des données personnelles (11.3). Les listes sont des lignes SQL sous forme de maps
 * (clés en camelCase) : ces tables appartiennent aux autres modules.
 */
public record ExportCompte(
        Instant dateExport,
        UtilisateurDto profil,
        List<Map<String, Object>> briques,
        List<Map<String, Object>> avis,
        List<Map<String, Object>> tests,
        List<Map<String, Object>> demandes,
        List<Map<String, Object>> reponses,
        List<Map<String, Object>> signalements) {
}

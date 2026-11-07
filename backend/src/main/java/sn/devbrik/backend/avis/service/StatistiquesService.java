package sn.devbrik.backend.avis.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.avis.dto.StatistiquesReponse;

/**
 * Statistiques administrateur (11.5, U24).
 * Requête SQL en lecture seule sur les tables des autres modules (règle 2 de 9.4) : aucune écriture.
 */
@Service
public class StatistiquesService {

    private static final String SQL = """
            SELECT
              (SELECT COUNT(*) FROM utilisateur)                              AS utilisateurs,
              (SELECT COUNT(*) FROM brique WHERE statut = 'PUBLIEE')          AS briques_publiees,
              (SELECT COUNT(*) FROM brique WHERE statut = 'MASQUEE')          AS briques_masquees,
              (SELECT COUNT(*) FROM avis)                                     AS avis,
              (SELECT COUNT(*) FROM test_brique)                              AS tests,
              (SELECT COUNT(*) FROM demande WHERE statut = 'OUVERTE')         AS demandes_ouvertes,
              (SELECT COUNT(*) FROM demande WHERE statut = 'RESOLUE')         AS demandes_resolues,
              (SELECT COUNT(*) FROM signalement_brique WHERE statut = 'OUVERT') AS signalements_ouverts
            """;

    private final JdbcTemplate jdbcTemplate;

    public StatistiquesService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Administrateur seulement (S4). */
    @Transactional(readOnly = true)
    public StatistiquesReponse statistiques(boolean admin) {
        if (!admin) {
            throw ErreursAvis.reserveAdministrateur();
        }
        return jdbcTemplate.queryForObject(SQL, (rs, ligne) -> new StatistiquesReponse(
                rs.getLong("utilisateurs"),
                rs.getLong("briques_publiees"),
                rs.getLong("briques_masquees"),
                rs.getLong("avis"),
                rs.getLong("tests"),
                rs.getLong("demandes_ouvertes"),
                rs.getLong("demandes_resolues"),
                rs.getLong("signalements_ouverts")));
    }
}
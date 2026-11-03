package sn.devbrik.backend.auth.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.dto.ExportCompte;
import sn.devbrik.backend.auth.dto.UtilisateurDto;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;

import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export des données de l'utilisateur connecté (11.3).
 * Lecture SQL seule (règle 2 de 9.4) ; requêtes paramétrées, filtrées sur id_auteur.
 */
@Service
public class ExportService {

    private static final String SQL_BRIQUES = """
            SELECT id_brique AS "id", titre AS "titre", resume AS "resume", code AS "code",
                   mode_emploi AS "modeEmploi", prerequis AS "prerequis", licence AS "licence",
                   statut AS "statut", version_code AS "versionCode",
                   date_creation AS "dateCreation", date_modification AS "dateModification",
                   id_categorie AS "idCategorie", id_langage AS "idLangage"
            FROM brique WHERE id_auteur = ? ORDER BY id_brique""";

    private static final String SQL_AVIS = """
            SELECT id_avis AS "id", note AS "note", commentaire AS "commentaire",
                   date_creation AS "dateCreation", date_modification AS "dateModification",
                   id_brique AS "idBrique"
            FROM avis WHERE id_auteur = ? ORDER BY id_avis""";

    private static final String SQL_TESTS = """
            SELECT id_test AS "id", resultat AS "resultat", environnement AS "environnement",
                   version_code AS "versionCode", date_test AS "dateTest", id_brique AS "idBrique"
            FROM test_brique WHERE id_auteur = ? ORDER BY id_test""";

    private static final String SQL_DEMANDES = """
            SELECT id_demande AS "id", titre AS "titre", description AS "description",
                   statut AS "statut", date_creation AS "dateCreation",
                   id_categorie AS "idCategorie", id_langage AS "idLangage"
            FROM demande WHERE id_auteur = ? ORDER BY id_demande""";

    private static final String SQL_REPONSES = """
            SELECT id_reponse AS "id", contenu AS "contenu", acceptee AS "acceptee",
                   date_creation AS "dateCreation", id_demande AS "idDemande", id_brique AS "idBrique"
            FROM reponse WHERE id_auteur = ? ORDER BY id_reponse""";

    private static final String SQL_SIGNALEMENTS = """
            SELECT id_signalement AS "id", motif AS "motif", details AS "details", statut AS "statut",
                   action_traitement AS "actionTraitement", date_creation AS "dateCreation",
                   date_traitement AS "dateTraitement", id_brique AS "idBrique"
            FROM signalement_brique WHERE id_auteur = ? ORDER BY id_signalement""";

    /** Une ligne SQL devient une map ; les dates deviennent des Instant (ISO 8601 UTC en JSON). */
    private static final RowMapper<Map<String, Object>> LIGNE = (rs, numero) -> {
        ResultSetMetaData meta = rs.getMetaData();
        Map<String, Object> ligne = new LinkedHashMap<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            Object valeur = rs.getObject(i);
            if (valeur instanceof Timestamp t) {
                valeur = t.toInstant();
            } else if (valeur instanceof OffsetDateTime o) {
                valeur = o.toInstant();
            }
            ligne.put(meta.getColumnLabel(i), valeur);
        }
        return ligne;
    };

    private final UtilisateurRepository utilisateurRepository;
    private final JdbcTemplate jdbc;

    public ExportService(UtilisateurRepository utilisateurRepository, JdbcTemplate jdbc) {
        this.utilisateurRepository = utilisateurRepository;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public ExportCompte exporter(Long idUtilisateur) {
        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(ErreursAuth::nonAuthentifie);
        return new ExportCompte(
                Instant.now(),
                UtilisateurDto.depuis(utilisateur),
                lire(SQL_BRIQUES, idUtilisateur),
                lire(SQL_AVIS, idUtilisateur),
                lire(SQL_TESTS, idUtilisateur),
                lire(SQL_DEMANDES, idUtilisateur),
                lire(SQL_REPONSES, idUtilisateur),
                lire(SQL_SIGNALEMENTS, idUtilisateur));
    }

    private List<Map<String, Object>> lire(String sql, Long idUtilisateur) {
        return jdbc.query(sql, LIGNE, idUtilisateur);
    }
}

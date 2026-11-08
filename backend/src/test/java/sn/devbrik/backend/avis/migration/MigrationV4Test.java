package sn.devbrik.backend.avis.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Migration V4 sur devbrik_test. Chaque test est annulé à la fin. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MigrationV4Test {

    @Autowired JdbcTemplate jdbc;

    long brique;

    @BeforeEach
    void donnees() {
        long auteur = creerUtilisateur();
        Long categorie = jdbc.queryForObject("SELECT id_categorie FROM categorie LIMIT 1", Long.class);
        Long langage = jdbc.queryForObject("SELECT id_langage FROM langage LIMIT 1", Long.class);
        brique = jdbc.queryForObject("""
                INSERT INTO brique (titre, resume, code, mode_emploi, licence, id_categorie, id_langage, id_auteur)
                VALUES (?, 'Résumé de test pour la brique', 'x', 'Mode d''emploi de test', 'MIT', ?, ?, ?)
                RETURNING id_brique
                """, Long.class, "Brique " + UUID.randomUUID(), categorie, langage, auteur);
    }

    private long creerUtilisateur() {
        String pseudo = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return jdbc.queryForObject(
                "INSERT INTO utilisateur (pseudo, mot_de_passe) VALUES (?, 'hash') RETURNING id_utilisateur",
                Long.class, pseudo);
    }

    private void signaler(String motif) {
        jdbc.update("INSERT INTO signalement_brique (motif, id_brique, id_auteur) VALUES (?, ?, ?)",
                motif, brique, creerUtilisateur());
    }

    @Test
    void test_V4_la_migration_est_enregistree() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '4' AND success", Integer.class);
        assertThat(n).isEqualTo(1);
    }

    @Test
    void test_RG11_V4_le_motif_INAPPROPRIE_est_accepte() {
        signaler("INAPPROPRIE");

        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM signalement_brique WHERE id_brique = ? AND motif = 'INAPPROPRIE'",
                Integer.class, brique);
        assertThat(n).isEqualTo(1);
    }

    @Test
    void test_RG11_V4_les_cinq_motifs_existants_restent_acceptes() {
        List<String> anciens = List.of("CODE_DANGEREUX", "DROITS_AUTEUR", "SPAM", "INEXACT", "AUTRE");
        anciens.forEach(this::signaler);

        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM signalement_brique WHERE id_brique = ?", Integer.class, brique);
        assertThat(n).isEqualTo(5);
    }

    /** À garder en dernier dans sa méthode : PostgreSQL invalide la transaction après une erreur. */
    @Test
    void test_RG11_V4_un_motif_inconnu_est_refuse_par_la_base() {
        assertThatThrownBy(() -> signaler("JE_N_AIME_PAS"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
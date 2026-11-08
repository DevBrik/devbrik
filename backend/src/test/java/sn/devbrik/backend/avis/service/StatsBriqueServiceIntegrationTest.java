package sn.devbrik.backend.avis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import sn.devbrik.backend.avis.dto.StatsBrique;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test sur PostgreSQL (devbrik_test). Chaque test est annulé à la fin (@DataJpaTest).
 * Les insertions SQL sont réservées aux tests : le code de production n'écrit jamais dans brique.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(StatsBriqueService.class)
class StatsBriqueServiceIntegrationTest {

    @Autowired StatsBriqueService service;
    @Autowired JdbcTemplate jdbc;

    long auteur;
    long testeurA;
    long testeurB;
    long brique;

    @BeforeEach
    void donnees() {
        auteur = creerUtilisateur();
        testeurA = creerUtilisateur();
        testeurB = creerUtilisateur();
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

    private void avis(long idAuteur, int note) {
        jdbc.update("INSERT INTO avis (note, id_brique, id_auteur) VALUES (?, ?, ?)", note, brique, idAuteur);
    }

    private void test(long idAuteur, String resultat, int version) {
        jdbc.update("INSERT INTO test_brique (resultat, version_code, id_brique, id_auteur) VALUES (?, ?, ?, ?)",
                resultat, version, brique, idAuteur);
    }

    @Test
    void test_RG8_moyenne_sur_la_vraie_base_arrondie_a_une_decimale() {
        avis(testeurA, 5);
        avis(testeurB, 4);

        StatsBrique stats = service.statistiques(List.of(brique)).get(brique);

        assertThat(stats.noteMoyenne()).isEqualTo(4.5);
        assertThat(stats.nombreAvis()).isEqualTo(2);
    }

    @Test
    void test_RG8_sans_avis_la_note_moyenne_est_nulle() {
        StatsBrique stats = service.statistiques(List.of(brique)).get(brique);

        assertThat(stats.noteMoyenne()).isNull();
        assertThat(stats.nombreAvis()).isZero();
    }

    @Test
    void test_RG9_RG10_le_code_change_les_tests_de_l_ancienne_version_ne_comptent_plus() {
        test(testeurA, "OK", 1);
        test(testeurB, "KO", 1);

        StatsBrique avant = service.statistiques(List.of(brique)).get(brique);
        assertThat(avant.testsOk()).isEqualTo(1);
        assertThat(avant.testsKo()).isEqualTo(1);
        assertThat(avant.derniereVerification()).isNotNull();
        assertThat(service.compterTestsOkRecus(auteur)).isEqualTo(1);

        // Le code de la brique change : la version passe à 2 (RG6).
        jdbc.update("UPDATE brique SET version_code = 2 WHERE id_brique = ?", brique);

        StatsBrique apres = service.statistiques(List.of(brique)).get(brique);
        assertThat(apres.testsOk()).isZero();
        assertThat(apres.testsKo()).isZero();
        assertThat(apres.derniereVerification()).isNull();
        assertThat(service.compterTestsOkRecus(auteur)).isZero();
    }

    @Test
    void test_RG9_un_nouveau_test_sur_la_version_actuelle_est_compte() {
        test(testeurA, "OK", 1);
        jdbc.update("UPDATE brique SET version_code = 2 WHERE id_brique = ?", brique);
        test(testeurB, "OK", 2);

        StatsBrique stats = service.statistiques(List.of(brique)).get(brique);

        assertThat(stats.testsOk()).isEqualTo(1);
        assertThat(stats.testsKo()).isZero();
        assertThat(stats.derniereVerification()).isNotNull();
        assertThat(service.compterTestsOkRecus(auteur)).isEqualTo(1);
    }

    @Test
    void test_plusieurs_briques_dans_un_seul_appel() {
        long autre = jdbc.queryForObject("""
                INSERT INTO brique (titre, resume, code, mode_emploi, licence, id_categorie, id_langage, id_auteur)
                SELECT 'Autre ' || gen_random_uuid(), 'Résumé de test pour la brique', 'x',
                       'Mode d''emploi de test', 'MIT', id_categorie, id_langage, id_auteur
                FROM brique WHERE id_brique = ?
                RETURNING id_brique
                """, Long.class, brique);
        avis(testeurA, 3);

        var stats = service.statistiques(List.of(brique, autre));

        assertThat(stats).containsKeys(brique, autre);
        assertThat(stats.get(brique).nombreAvis()).isEqualTo(1);
        assertThat(stats.get(autre)).isEqualTo(StatsBrique.vide());
    }
}
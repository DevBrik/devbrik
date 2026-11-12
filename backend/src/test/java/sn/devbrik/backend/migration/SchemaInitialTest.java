package sn.devbrik.backend.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validation des scripts V1 (schéma) et V2 (référentiels), section 10.6.
 * Chaque test est annulé ; un seul ordre échoue par test (une erreur SQL invalide la transaction).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SchemaInitialTest {

    @Autowired private JdbcTemplate jdbc;

    private long utilisateur(String pseudo) {
        return jdbc.queryForObject(
                "INSERT INTO utilisateur (pseudo, mot_de_passe) VALUES (?, 'hash-de-test') RETURNING id_utilisateur",
                Long.class, pseudo);
    }

    private long brique(long idAuteur, String titre) {
        return jdbc.queryForObject("""
                INSERT INTO brique (titre, resume, code, mode_emploi, licence, id_categorie, id_langage, id_auteur)
                VALUES (?, 'Résumé de test', 'code', 'Mode d''emploi de test', 'MIT',
                        (SELECT min(id_categorie) FROM categorie), (SELECT min(id_langage) FROM langage), ?)
                RETURNING id_brique""", Long.class, titre, idAuteur);
    }

    // -------------------------------------------------------------------- V1

    @Test
    void v1_cree_les_9_tables_du_modele() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' "
                        + "AND table_type = 'BASE TABLE' AND table_name <> 'flyway_schema_history'", String.class);

        assertThat(tables).containsExactlyInAnyOrder("utilisateur", "categorie", "langage", "brique", "avis",
                "test_brique", "signalement_brique", "demande", "reponse");
    }

    @Test
    void v1_pseudo_trop_court_est_refuse() {
        assertThatThrownBy(() -> utilisateur("ab")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_pseudo_avec_espace_est_refuse() {
        assertThatThrownBy(() -> utilisateur("awa diop")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_pseudo_commencant_par_un_tiret_est_refuse() {
        assertThatThrownBy(() -> utilisateur("-awa")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_pseudo_valide_est_accepte() {
        assertThat(utilisateur("Awa.Dev_1-x")).isPositive();
    }

    @Test
    void v1_le_pseudo_est_unique_sans_tenir_compte_de_la_casse_RG1() {
        utilisateur("Awa.Dev");

        assertThatThrownBy(() -> utilisateur("awa.dev")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_le_role_par_defaut_est_DEVELOPPEUR() {
        long id = utilisateur("role.defaut");

        assertThat(jdbc.queryForObject("SELECT role FROM utilisateur WHERE id_utilisateur = ?", String.class, id))
                .isEqualTo("DEVELOPPEUR");
    }

    @Test
    void v1_un_role_inconnu_est_refuse() {
        long id = utilisateur("role.inconnu");

        assertThatThrownBy(() -> jdbc.update("UPDATE utilisateur SET role = 'ROOT' WHERE id_utilisateur = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_une_brique_est_publiee_en_version_1_par_defaut() {
        long id = brique(utilisateur("brique.defaut"), "Titre par défaut");

        assertThat(jdbc.queryForObject("SELECT statut FROM brique WHERE id_brique = ?", String.class, id))
                .isEqualTo("PUBLIEE");
        assertThat(jdbc.queryForObject("SELECT version_code FROM brique WHERE id_brique = ?", Integer.class, id))
                .isEqualTo(1);
    }

    @Test
    void v1_le_titre_d_une_brique_est_unique_par_auteur_sans_casse_RG5() {
        long auteur = utilisateur("titre.unique");
        brique(auteur, "Validation SN");

        assertThatThrownBy(() -> brique(auteur, "validation sn")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_deux_auteurs_peuvent_avoir_le_meme_titre() {
        brique(utilisateur("titre.a"), "Même titre");

        assertThat(brique(utilisateur("titre.b"), "Même titre")).isPositive();
    }

    @Test
    void v1_une_licence_non_permissive_est_refusee_RG4() {
        long auteur = utilisateur("licence.refusee");

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO brique (titre, resume, code, mode_emploi, licence, id_categorie, id_langage, id_auteur)
                VALUES ('Titre', 'Résumé', 'code', 'Mode d''emploi de test', 'GPL_3_0',
                        (SELECT min(id_categorie) FROM categorie), (SELECT min(id_langage) FROM langage), ?)""", auteur))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_un_statut_de_brique_inconnu_est_refuse() {
        long id = brique(utilisateur("statut.inconnu"), "Titre");

        assertThatThrownBy(() -> jdbc.update("UPDATE brique SET statut = 'SUPPRIMEE' WHERE id_brique = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_une_note_hors_de_1_a_5_est_refusee() {
        long auteur = utilisateur("note.hors");
        long brique = brique(utilisateur("note.auteur"), "Titre");

        assertThatThrownBy(() -> jdbc.update("INSERT INTO avis (note, id_brique, id_auteur) VALUES (6, ?, ?)", brique, auteur))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_un_utilisateur_ne_note_qu_une_fois_une_brique() {
        long auteur = utilisateur("avis.double");
        long brique = brique(utilisateur("avis.auteur"), "Titre");
        jdbc.update("INSERT INTO avis (note, id_brique, id_auteur) VALUES (4, ?, ?)", brique, auteur);

        assertThatThrownBy(() -> jdbc.update("INSERT INTO avis (note, id_brique, id_auteur) VALUES (5, ?, ?)", brique, auteur))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_un_resultat_de_test_autre_que_OK_ou_KO_est_refuse() {
        long testeur = utilisateur("test.invalide");
        long brique = brique(utilisateur("test.auteur"), "Titre");

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO test_brique (resultat, version_code, id_brique, id_auteur) VALUES ('NA', 1, ?, ?)",
                brique, testeur)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_une_demande_est_ouverte_par_defaut() {
        long auteur = utilisateur("demande.defaut");

        long id = jdbc.queryForObject("""
                INSERT INTO demande (titre, description, id_auteur)
                VALUES ('Titre de demande', 'Description de demande assez longue', ?) RETURNING id_demande""",
                Long.class, auteur);

        assertThat(jdbc.queryForObject("SELECT statut FROM demande WHERE id_demande = ?", String.class, id))
                .isEqualTo("OUVERTE");
    }

    @Test
    void v1_une_seule_reponse_acceptee_par_demande_RG12() {
        long auteur = utilisateur("accept.auteur");
        long a = utilisateur("accept.a");
        long b = utilisateur("accept.b");
        long demande = jdbc.queryForObject("""
                INSERT INTO demande (titre, description, id_auteur)
                VALUES ('Titre de demande', 'Description de demande assez longue', ?) RETURNING id_demande""",
                Long.class, auteur);
        jdbc.update("INSERT INTO reponse (contenu, acceptee, id_demande, id_auteur) VALUES ('Première', TRUE, ?, ?)", demande, a);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO reponse (contenu, acceptee, id_demande, id_auteur) VALUES ('Seconde', TRUE, ?, ?)", demande, b))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v1_plusieurs_reponses_non_acceptees_sont_permises() {
        long auteur = utilisateur("multi.auteur");
        long a = utilisateur("multi.a");
        long b = utilisateur("multi.b");
        long demande = jdbc.queryForObject("""
                INSERT INTO demande (titre, description, id_auteur)
                VALUES ('Titre de demande', 'Description de demande assez longue', ?) RETURNING id_demande""",
                Long.class, auteur);

        jdbc.update("INSERT INTO reponse (contenu, id_demande, id_auteur) VALUES ('Première', ?, ?)", demande, a);
        jdbc.update("INSERT INTO reponse (contenu, id_demande, id_auteur) VALUES ('Seconde', ?, ?)", demande, b);

        assertThat(jdbc.queryForObject("SELECT count(*) FROM reponse WHERE id_demande = ?", Integer.class, demande))
                .isEqualTo(2);
    }

    @Test
    void v1_supprimer_une_brique_laisse_la_reponse_sans_lien_RG13() {
        long auteur = utilisateur("lien.auteur");
        long repondant = utilisateur("lien.repondant");
        long brique = brique(repondant, "Brique liée");
        long demande = jdbc.queryForObject("""
                INSERT INTO demande (titre, description, id_auteur)
                VALUES ('Titre de demande', 'Description de demande assez longue', ?) RETURNING id_demande""",
                Long.class, auteur);
        long reponse = jdbc.queryForObject("""
                INSERT INTO reponse (contenu, id_demande, id_auteur, id_brique) VALUES ('Avec lien', ?, ?, ?)
                RETURNING id_reponse""", Long.class, demande, repondant, brique);

        jdbc.update("DELETE FROM brique WHERE id_brique = ?", brique);

        assertThat(jdbc.queryForObject("SELECT id_brique FROM reponse WHERE id_reponse = ?", Long.class, reponse))
                .isNull();
    }

    @Test
    void v1_supprimer_un_compte_supprime_en_cascade_tout_son_contenu_RG17() {
        long auteur = utilisateur("cascade.auteur");
        long brique = brique(auteur, "Brique cascade");
        jdbc.update("INSERT INTO demande (titre, description, id_auteur) VALUES ('Titre de demande', "
                + "'Description de demande assez longue', ?)", auteur);

        jdbc.update("DELETE FROM utilisateur WHERE id_utilisateur = ?", auteur);

        assertThat(jdbc.queryForObject("SELECT count(*) FROM brique WHERE id_brique = ?", Integer.class, brique)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM demande WHERE id_auteur = ?", Integer.class, auteur)).isZero();
    }

    // -------------------------------------------------------------------- V2

    @Test
    void v2_charge_les_8_categories_attendues() {
        List<String> slugs = jdbc.queryForList("SELECT slug FROM categorie", String.class);

        assertThat(slugs).containsExactlyInAnyOrder("paiement-mobile", "sms-notifications", "validation-donnees",
                "formats-devises", "donnees-geographiques", "securite-authentification", "reseau-instable", "outils");
    }

    @Test
    void v2_charge_les_11_langages_attendus() {
        List<String> slugs = jdbc.queryForList("SELECT slug FROM langage", String.class);

        assertThat(slugs).containsExactlyInAnyOrder("java", "typescript", "javascript", "python", "php", "sql",
                "csharp", "kotlin", "dart", "go", "shell");
    }

    @Test
    void v2_chaque_categorie_porte_un_nom_et_une_description() {
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM categorie WHERE nom IS NULL OR description IS NULL OR description = ''",
                Integer.class)).isZero();
    }

    @Test
    void v2_un_slug_de_categorie_est_unique() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO categorie (slug, nom, description) VALUES ('outils', 'Autre nom', 'Autre description')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

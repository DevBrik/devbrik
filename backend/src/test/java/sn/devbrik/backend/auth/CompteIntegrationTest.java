package sn.devbrik.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.dto.ExportCompte;
import sn.devbrik.backend.auth.dto.SuppressionRequete;
import sn.devbrik.backend.auth.service.CompteService;
import sn.devbrik.backend.auth.service.ExportService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tourne sur la vraie base devbrik_test (section 9.6), V1 et V2 appliquées.
 * Chaque test est annulé (rollback) à la fin. Adapte @ActiveProfiles à la configuration du membre 1.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompteIntegrationTest {

    private static final String MOT_DE_PASSE = "MotDePasse123";

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder encodeur;
    @Autowired
    private CompteService compteService;
    @Autowired
    private ExportService exportService;

    private long creerUtilisateur(String pseudo) {
        return jdbc.queryForObject(
                "INSERT INTO utilisateur (pseudo, mot_de_passe) VALUES (?, ?) RETURNING id_utilisateur",
                Long.class, pseudo, encodeur.encode(MOT_DE_PASSE));
    }

    private void creerContenu(long idAuteur, String titre) {
        long idCategorie = jdbc.queryForObject("SELECT min(id_categorie) FROM categorie", Long.class);
        long idLangage = jdbc.queryForObject("SELECT min(id_langage) FROM langage", Long.class);
        long idBrique = jdbc.queryForObject("""
                INSERT INTO brique (titre, resume, code, mode_emploi, licence, id_categorie, id_langage, id_auteur)
                VALUES (?, 'Résumé de test', 'code', 'Mode d''emploi de test', 'MIT', ?, ?, ?)
                RETURNING id_brique""", Long.class, titre, idCategorie, idLangage, idAuteur);
        jdbc.update("INSERT INTO avis (note, id_brique, id_auteur) VALUES (4, ?, ?)", idBrique, idAuteur);
        jdbc.update("INSERT INTO test_brique (resultat, version_code, id_brique, id_auteur) VALUES ('OK', 1, ?, ?)",
                idBrique, idAuteur);
        jdbc.update("INSERT INTO signalement_brique (motif, id_brique, id_auteur) VALUES ('SPAM', ?, ?)",
                idBrique, idAuteur);
        long idDemande = jdbc.queryForObject("""
                INSERT INTO demande (titre, description, id_auteur)
                VALUES ('Titre de demande', 'Description de demande assez longue', ?)
                RETURNING id_demande""", Long.class, idAuteur);
        jdbc.update("INSERT INTO reponse (contenu, id_demande, id_auteur) VALUES ('Réponse de test', ?, ?)",
                idDemande, idAuteur);
    }

    private int compter(String table, long idAuteur) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE id_auteur = ?", Integer.class, idAuteur);
    }

    @Test
    void test_RG17_la_suppression_du_compte_supprime_en_cascade_tout_son_contenu() {
        long id = creerUtilisateur("test.cascade");
        creerContenu(id, "Brique cascade");
        for (String table : new String[]{"brique", "avis", "test_brique", "signalement_brique", "demande", "reponse"}) {
            assertEquals(1, compter(table, id), "avant suppression : " + table);
        }

        compteService.supprimer(id, new SuppressionRequete(MOT_DE_PASSE));

        assertEquals(0, jdbc.queryForObject(
                "SELECT count(*) FROM utilisateur WHERE id_utilisateur = ?", Integer.class, id));
        for (String table : new String[]{"brique", "avis", "test_brique", "signalement_brique", "demande", "reponse"}) {
            assertEquals(0, compter(table, id), "après suppression : " + table);
        }
    }

    @Test
    void test_RG17_la_suppression_d_un_compte_ne_touche_pas_au_contenu_des_autres() {
        long a = creerUtilisateur("test.a");
        long b = creerUtilisateur("test.b");
        creerContenu(a, "Brique A");
        creerContenu(b, "Brique B");

        compteService.supprimer(a, new SuppressionRequete(MOT_DE_PASSE));

        assertEquals(1, compter("brique", b));
        assertEquals(1, compter("avis", b));
        assertEquals(1, compter("reponse", b));
    }

    @Test
    void test_RG12_RG17_supprimer_le_repondant_rouvre_la_demande_dont_la_reponse_acceptee_disparait() {
        long auteur = creerUtilisateur("test.demandeur");
        long repondant = creerUtilisateur("test.repondant");
        long idDemande = jdbc.queryForObject("""
                INSERT INTO demande (titre, description, statut, id_auteur)
                VALUES ('Titre de demande', 'Description de demande assez longue', 'RESOLUE', ?)
                RETURNING id_demande""", Long.class, auteur);
        jdbc.update("INSERT INTO reponse (contenu, acceptee, id_demande, id_auteur) VALUES ('Réponse acceptée', TRUE, ?, ?)",
                idDemande, repondant);

        compteService.supprimer(repondant, new SuppressionRequete(MOT_DE_PASSE));

        assertEquals("OUVERTE", jdbc.queryForObject(
                "SELECT statut FROM demande WHERE id_demande = ?", String.class, idDemande));
    }

    @Test
    void test_RG12_supprimer_un_compte_ne_rouvre_pas_une_demande_dont_la_reponse_acceptee_reste() {
        long auteur = creerUtilisateur("test.demandeur2");
        long repondant = creerUtilisateur("test.repondant2");
        long autre = creerUtilisateur("test.autre2");
        long idDemande = jdbc.queryForObject("""
                INSERT INTO demande (titre, description, statut, id_auteur)
                VALUES ('Titre de demande', 'Description de demande assez longue', 'RESOLUE', ?)
                RETURNING id_demande""", Long.class, auteur);
        jdbc.update("INSERT INTO reponse (contenu, acceptee, id_demande, id_auteur) VALUES ('Réponse acceptée', TRUE, ?, ?)",
                idDemande, repondant);

        compteService.supprimer(autre, new SuppressionRequete(MOT_DE_PASSE));

        assertEquals("RESOLUE", jdbc.queryForObject(
                "SELECT statut FROM demande WHERE id_demande = ?", String.class, idDemande));
    }

    @Test
    void export_contient_uniquement_les_donnees_de_l_utilisateur_et_aucun_mot_de_passe() {
        long a = creerUtilisateur("test.export.a");
        long b = creerUtilisateur("test.export.b");
        creerContenu(a, "Brique export A");
        creerContenu(b, "Brique export B");

        ExportCompte export = exportService.exporter(a);

        assertEquals("test.export.a", export.profil().pseudo());
        assertEquals(1, export.briques().size());
        assertEquals("Brique export A", export.briques().get(0).get("titre"));
        assertEquals(1, export.avis().size());
        assertEquals(1, export.tests().size());
        assertEquals(1, export.demandes().size());
        assertEquals(1, export.reponses().size());
        assertEquals(1, export.signalements().size());
        assertTrue(export.briques().get(0).get("dateCreation") instanceof java.time.Instant);

        String texte = export.toString().toLowerCase();
        assertFalse(texte.contains("motdepasse"));
        assertFalse(texte.contains("$2a$") || texte.contains("$2b$"));
    }

    @Test
    void export_d_un_utilisateur_sans_contenu_renvoie_des_listes_vides() {
        long id = creerUtilisateur("test.vide");

        ExportCompte export = exportService.exporter(id);

        assertTrue(export.briques().isEmpty());
        assertTrue(export.avis().isEmpty());
        assertTrue(export.tests().isEmpty());
        assertTrue(export.demandes().isEmpty());
        assertTrue(export.reponses().isEmpty());
        assertTrue(export.signalements().isEmpty());
    }
}

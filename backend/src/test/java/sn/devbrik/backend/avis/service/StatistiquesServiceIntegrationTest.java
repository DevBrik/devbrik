package sn.devbrik.backend.avis.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import sn.devbrik.backend.avis.dto.StatistiquesReponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(StatistiquesService.class)
class StatistiquesServiceIntegrationTest {

    @Autowired StatistiquesService service;
    @Autowired JdbcTemplate jdbc;

    private long creerUtilisateur() {
        String pseudo = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return jdbc.queryForObject(
                "INSERT INTO utilisateur (pseudo, mot_de_passe) VALUES (?, 'hash') RETURNING id_utilisateur",
                Long.class, pseudo);
    }

    private long creerBrique(long auteur, String statut) {
        Long categorie = jdbc.queryForObject("SELECT id_categorie FROM categorie LIMIT 1", Long.class);
        Long langage = jdbc.queryForObject("SELECT id_langage FROM langage LIMIT 1", Long.class);
        return jdbc.queryForObject("""
                INSERT INTO brique (titre, resume, code, mode_emploi, licence, statut, id_categorie, id_langage, id_auteur)
                VALUES (?, 'Résumé de test pour la brique', 'x', 'Mode d''emploi de test', 'MIT', ?, ?, ?, ?)
                RETURNING id_brique
                """, Long.class, "Brique " + UUID.randomUUID(), statut, categorie, langage, auteur);
    }

    private void creerDemande(long auteur, String statut) {
        jdbc.update("""
                INSERT INTO demande (titre, description, statut, id_auteur)
                VALUES (?, 'Description de test d''au moins vingt caractères', ?, ?)
                """, "Demande " + UUID.randomUUID(), statut, auteur);
    }

    @Test
    void test_U24_les_compteurs_refletent_les_donnees_de_la_base() {
        StatistiquesReponse avant = service.statistiques(true);

        long auteur = creerUtilisateur();
        long testeur = creerUtilisateur();
        long publiee = creerBrique(auteur, "PUBLIEE");
        creerBrique(auteur, "MASQUEE");
        jdbc.update("INSERT INTO avis (note, id_brique, id_auteur) VALUES (4, ?, ?)", publiee, testeur);
        jdbc.update("INSERT INTO test_brique (resultat, version_code, id_brique, id_auteur) VALUES ('OK', 1, ?, ?)",
                publiee, testeur);
        creerDemande(auteur, "OUVERTE");
        creerDemande(auteur, "OUVERTE");
        creerDemande(auteur, "RESOLUE");
        jdbc.update("INSERT INTO signalement_brique (motif, id_brique, id_auteur) VALUES ('SPAM', ?, ?)",
                publiee, testeur);
        jdbc.update("""
                INSERT INTO signalement_brique (motif, statut, action_traitement, id_brique, id_auteur)
                VALUES ('AUTRE', 'TRAITE', 'CONSERVER', ?, ?)
                """, publiee, auteur);

        StatistiquesReponse apres = service.statistiques(true);

        assertThat(apres.utilisateurs() - avant.utilisateurs()).isEqualTo(2);
        assertThat(apres.briquesPubliees() - avant.briquesPubliees()).isEqualTo(1);
        assertThat(apres.briquesMasquees() - avant.briquesMasquees()).isEqualTo(1);
        assertThat(apres.avis() - avant.avis()).isEqualTo(1);
        assertThat(apres.tests() - avant.tests()).isEqualTo(1);
        assertThat(apres.demandesOuvertes() - avant.demandesOuvertes()).isEqualTo(2);
        assertThat(apres.demandesResolues() - avant.demandesResolues()).isEqualTo(1);
        // Seul le signalement OUVERT compte ; le signalement TRAITE est exclu.
        assertThat(apres.signalementsOuverts() - avant.signalementsOuverts()).isEqualTo(1);
    }
}
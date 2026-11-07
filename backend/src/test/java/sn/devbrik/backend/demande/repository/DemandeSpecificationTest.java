package sn.devbrik.backend.demande.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.demande.entity.Demande;
import sn.devbrik.backend.demande.entity.StatutDemande;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DemandeSpecificationsTest {

    @Autowired private DemandeRepository demandeRepository;
    @Autowired private JdbcTemplate jdbc;

    private Long auteur;

    private void donnees() {
        auteur = jdbc.queryForObject(
                "INSERT INTO utilisateur (pseudo, mot_de_passe) VALUES ('awa.dev', 'hash-de-test') RETURNING id_utilisateur",
                Long.class);
        ajouter("Réduction de 100% sur les montants", "Je veux formater un montant en FCFA proprement.");
        ajouter("Nom de variable mon_champ", "Je cherche un utilitaire pour nettoyer des noms de champs.");
        ajouter("Montant en lettres", "Je cherche une brique qui écrit un montant en lettres françaises.");
        demandeRepository.flush();
    }

    private void ajouter(String titre, String description) {
        demandeRepository.save(new Demande(auteur, titre, description, null, null));
    }

    private List<String> chercher(String q) {
        return demandeRepository.findAll(DemandeSpecifications.filtre(null, null, null, q))
                .stream().map(Demande::getTitre).toList();
    }

    @Test
    void test_RG16_le_pourcent_est_traite_comme_du_texte() {
        donnees();
        assertThat(chercher("100%")).containsExactly("Réduction de 100% sur les montants");
        assertThat(chercher("%%")).isEmpty(); // pas un joker
    }

    @Test
    void test_RG16_le_souligne_est_traite_comme_du_texte() {
        donnees();
        assertThat(chercher("mon_champ")).containsExactly("Nom de variable mon_champ");
        assertThat(chercher("mon champ")).isEmpty(); // _ n'est pas un « n'importe quel caractère »
    }

    @Test
    void test_RG16_insensible_a_la_casse_mais_pas_aux_accents() {
        donnees();
        assertThat(chercher("MONTANT")).hasSize(2);
        assertThat(chercher("reduction")).isEmpty();           // sans accent : pas de résultat
        assertThat(chercher("RÉDUCTION")).hasSize(1);          // casse ignorée, accent respecté
    }

    @Test
    void le_filtre_par_statut_ne_garde_que_les_demandes_correspondantes() {
        donnees();
        assertThat(demandeRepository.findAll(
                DemandeSpecifications.filtre(StatutDemande.RESOLUE, null, null, null))).isEmpty();
        assertThat(demandeRepository.findAll(
                DemandeSpecifications.filtre(StatutDemande.OUVERTE, null, null, null))).hasSize(3);
    }
}
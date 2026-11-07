package sn.devbrik.backend.demande.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.demande.entity.Demande;
import sn.devbrik.backend.demande.entity.Reponse;
import sn.devbrik.backend.demande.entity.StatutDemande;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReponseRepositoryTest {

    private static final String DESCRIPTION = "Je cherche une brique qui valide et normalise les numéros.";

    @Autowired private DemandeRepository demandeRepository;
    @Autowired private ReponseRepository reponseRepository;
    @Autowired private JdbcTemplate jdbc;

    private Long creerUtilisateur(String pseudo) {
        return jdbc.queryForObject(
                "INSERT INTO utilisateur (pseudo, mot_de_passe) VALUES (?, 'hash-de-test') RETURNING id_utilisateur",
                Long.class, pseudo);
    }

    private Demande creerDemande(Long idAuteur) {
        return demandeRepository.saveAndFlush(
                new Demande(idAuteur, "Valider un numéro sénégalais", DESCRIPTION, null, null));
    }

    private Reponse reponse(Long idDemande, Long idAuteur, boolean acceptee) {
        Reponse r = new Reponse(idDemande, idAuteur, "Voici une brique qui fait cela.", null);
        r.setAcceptee(acceptee);
        return r;
    }

    @Test
    void les_entites_demande_et_reponse_correspondent_au_schema() {
        Long auteur = creerUtilisateur("awa.dev");
        Long repondant = creerUtilisateur("moussa.dev");

        Demande demande = creerDemande(auteur);
        Reponse reponse = reponseRepository.saveAndFlush(reponse(demande.getId(), repondant, false));

        assertThat(demande.getStatut()).isEqualTo(StatutDemande.OUVERTE);
        assertThat(demande.getDateCreation()).isNotNull();
        assertThat(reponse.isAcceptee()).isFalse();
        assertThat(reponseRepository.findByIdDemandeOrderByDateCreationAsc(demande.getId())).hasSize(1);
    }

    @Test
    void test_RG12_deux_reponses_acceptees_pour_une_demande_sont_impossibles_en_base() {
        Long auteur = creerUtilisateur("awa.dev");
        Long r1 = creerUtilisateur("moussa.dev");
        Long r2 = creerUtilisateur("fatou.dev");
        Demande demande = creerDemande(auteur);

        reponseRepository.saveAndFlush(reponse(demande.getId(), r1, true));

        assertThatThrownBy(() -> reponseRepository.saveAndFlush(reponse(demande.getId(), r2, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void test_RG12_une_acceptee_et_des_non_acceptees_coexistent_et_chaque_demande_a_la_sienne() {
        Long auteur = creerUtilisateur("awa.dev");
        Long repondant = creerUtilisateur("moussa.dev");
        Demande d1 = creerDemande(auteur);
        Demande d2 = creerDemande(auteur);

        reponseRepository.saveAndFlush(reponse(d1.getId(), repondant, true));
        reponseRepository.saveAndFlush(reponse(d1.getId(), auteur, false));
        reponseRepository.saveAndFlush(reponse(d2.getId(), repondant, true));

        assertThat(reponseRepository.findByIdDemandeAndAccepteeTrue(d1.getId())).isPresent();
        assertThat(reponseRepository.findByIdDemandeAndAccepteeTrue(d2.getId())).isPresent();
        assertThat(reponseRepository.countByIdAuteurAndAccepteeTrue(repondant)).isEqualTo(2);
    }

    @Test
    void compterParDemande_renvoie_le_nombre_de_reponses_de_chaque_demande() {
        Long auteur = creerUtilisateur("awa.dev");
        Long repondant = creerUtilisateur("moussa.dev");
        Demande d1 = creerDemande(auteur);
        Demande d2 = creerDemande(auteur);

        reponseRepository.saveAndFlush(reponse(d1.getId(), repondant, false));
        reponseRepository.saveAndFlush(reponse(d1.getId(), auteur, false));

        List<ReponseRepository.CompteReponses> comptes =
                reponseRepository.compterParDemande(List.of(d1.getId(), d2.getId()));

        assertThat(comptes).hasSize(1); // d2 n'a aucune réponse : absente du résultat
        assertThat(comptes.get(0).getIdDemande()).isEqualTo(d1.getId());
        assertThat(comptes.get(0).getNombre()).isEqualTo(2L);
    }
}
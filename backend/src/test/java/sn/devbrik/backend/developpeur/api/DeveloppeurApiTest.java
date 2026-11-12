package sn.devbrik.backend.developpeur.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import sn.devbrik.backend.avis.api.ApiTestSupport;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Profil public, réputation (RG14) et classement (11.6) de bout en bout. */
class DeveloppeurApiTest extends ApiTestSupport {

    private void testOk(Brique brique, Compte testeur) {
        jdbc.update("INSERT INTO test_brique (resultat, version_code, id_brique, id_auteur) VALUES ('OK', 1, ?, ?)",
                brique.id(), testeur.id());
    }

    /** Une réponse de {@code repondant} acceptée par {@code demandeur} : +3 points pour le répondant. */
    private void reponseAcceptee(Compte demandeur, Compte repondant) throws Exception {
        String demande = mvc.perform(avec(post("/api/demandes"), demandeur).contentType(JSON)
                        .content("{\"titre\":\"Je cherche une brique utile\","
                                + "\"description\":\"Je cherche une brique qui fait quelque chose d'utile.\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long idDemande = ((Number) JsonPath.read(demande, "$.id")).longValue();
        String reponse = mvc.perform(avec(post("/api/demandes/{id}/reponses", idDemande), repondant).contentType(JSON)
                        .content("{\"contenu\":\"Voici une brique qui répond à cela.\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long idReponse = ((Number) JsonPath.read(reponse, "$.id")).longValue();
        mvc.perform(avec(put("/api/reponses/{id}/acceptation", idReponse), demandeur))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------- profil

    @Test
    void test_11_6_le_profil_public_est_accessible_aux_visiteurs_sans_donnee_sensible() throws Exception {
        Compte compte = inscrire();

        String corps = mvc.perform(get("/api/developpeurs/{id}", compte.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) compte.id()))
                .andExpect(jsonPath("$.pseudo").value(compte.pseudo()))
                .andExpect(jsonPath("$.reputation").value(0))
                .andExpect(jsonPath("$.nombreBriques").value(0))
                .andExpect(jsonPath("$.testsOkRecus").value(0))
                .andExpect(jsonPath("$.nombreReponsesAcceptees").value(0))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(corps).doesNotContain("motDePasse").doesNotContain("mot_de_passe").doesNotContain("$2a$");
    }

    @Test
    void test_RG14_dix_points_par_brique_publiee_et_les_briques_masquees_ne_comptent_pas() throws Exception {
        Compte auteur = inscrire();
        creerBrique(auteur);
        Brique masquee = creerBrique(auteur);
        creerBrique(auteur);

        mvc.perform(get("/api/developpeurs/{id}", auteur.id()))
                .andExpect(jsonPath("$.nombreBriques").value(3))
                .andExpect(jsonPath("$.reputation").value(30));

        masquer(administrateur(), masquee);

        mvc.perform(get("/api/developpeurs/{id}", auteur.id()))
                .andExpect(jsonPath("$.nombreBriques").value(2))
                .andExpect(jsonPath("$.reputation").value(20));
    }

    @Test
    void test_RG14_la_reputation_additionne_briques_tests_ok_recus_et_reponses_acceptees() throws Exception {
        Compte auteur = inscrire();
        Brique brique = creerBrique(auteur);
        testOk(brique, inscrire());
        testOk(brique, inscrire());
        reponseAcceptee(inscrire(), auteur);

        // 10 × 1 brique + 5 × 2 tests OK + 3 × 1 réponse acceptée
        mvc.perform(get("/api/developpeurs/{id}", auteur.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreBriques").value(1))
                .andExpect(jsonPath("$.testsOkRecus").value(2))
                .andExpect(jsonPath("$.nombreReponsesAcceptees").value(1))
                .andExpect(jsonPath("$.reputation").value(23));
    }

    @Test
    void test_RG14_un_test_KO_ou_d_une_ancienne_version_ne_rapporte_rien() throws Exception {
        Compte auteur = inscrire();
        Brique brique = creerBrique(auteur);
        Compte testeurKo = inscrire();
        jdbc.update("INSERT INTO test_brique (resultat, version_code, id_brique, id_auteur) VALUES ('KO', 1, ?, ?)",
                brique.id(), testeurKo.id());
        testOk(brique, inscrire());
        modifierCode(auteur, brique, "code v2"); // la version passe à 2 : le test OK de la v1 ne compte plus

        mvc.perform(get("/api/developpeurs/{id}", auteur.id()))
                .andExpect(jsonPath("$.testsOkRecus").value(0))
                .andExpect(jsonPath("$.reputation").value(10));
    }

    @Test
    void test_11_6_un_developpeur_inconnu_donne_404_et_un_identifiant_invalide_400() throws Exception {
        mvc.perform(get("/api/developpeurs/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
        mvc.perform(get("/api/developpeurs/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    // ---------------------------------------------------------------- classement

    @Test
    void test_11_6_le_classement_est_public_et_trie_par_reputation_decroissante() throws Exception {
        Compte moyen = inscrire();
        Compte premier = inscrire();
        Compte dernier = inscrire();
        creerBrique(moyen);
        creerBrique(moyen);
        creerBrique(premier);
        creerBrique(premier);
        creerBrique(premier);
        creerBrique(dernier);

        String corps = mvc.perform(get("/api/developpeurs/classement").param("taille", "50"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<Integer> ids = JsonPath.read(corps, "$.contenu[*].developpeur.id");
        assertThat(ids).contains((int) premier.id(), (int) moyen.id(), (int) dernier.id());
        assertThat(ids.indexOf((int) premier.id())).isLessThan(ids.indexOf((int) moyen.id()));
        assertThat(ids.indexOf((int) moyen.id())).isLessThan(ids.indexOf((int) dernier.id()));

        List<Integer> points = JsonPath.read(corps, "$.contenu[*].reputation");
        assertThat(points).isSortedAccordingTo(java.util.Comparator.reverseOrder());
    }

    @Test
    void test_11_6_chaque_element_du_classement_porte_le_detail_de_la_reputation() throws Exception {
        Compte auteur = inscrire();
        Brique brique = creerBrique(auteur);
        testOk(brique, inscrire());
        reponseAcceptee(inscrire(), auteur);

        mvc.perform(get("/api/developpeurs/classement").param("taille", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu[?(@.developpeur.id==" + auteur.id() + ")].developpeur.pseudo")
                        .value(org.hamcrest.Matchers.contains(auteur.pseudo())))
                .andExpect(jsonPath("$.contenu[?(@.developpeur.id==" + auteur.id() + ")].reputation")
                        .value(org.hamcrest.Matchers.contains(18)))
                .andExpect(jsonPath("$.contenu[?(@.developpeur.id==" + auteur.id() + ")].nombreBriques")
                        .value(org.hamcrest.Matchers.contains(1)))
                .andExpect(jsonPath("$.contenu[?(@.developpeur.id==" + auteur.id() + ")].testsOkRecus")
                        .value(org.hamcrest.Matchers.contains(1)))
                .andExpect(jsonPath("$.contenu[?(@.developpeur.id==" + auteur.id() + ")].nombreReponsesAcceptees")
                        .value(org.hamcrest.Matchers.contains(1)));
    }

    @Test
    void test_11_6_le_classement_ignore_les_comptes_sans_point_et_les_administrateurs() throws Exception {
        Compte sansPoint = inscrire();
        Compte admin = administrateur();
        creerBrique(admin);

        String corps = mvc.perform(get("/api/developpeurs/classement").param("taille", "50"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<Integer> ids = JsonPath.read(corps, "$.contenu[*].developpeur.id");
        assertThat(ids).doesNotContain((int) sansPoint.id(), (int) admin.id());
    }

    @Test
    void test_RG16_le_classement_est_pagine() throws Exception {
        mvc.perform(get("/api/developpeurs/classement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.taille").value(20));
        mvc.perform(get("/api/developpeurs/classement").param("taille", "500"))
                .andExpect(jsonPath("$.taille").value(50));
        mvc.perform(get("/api/developpeurs/classement").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
        mvc.perform(get("/api/developpeurs/classement").param("taille", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_RG16_une_page_au_dela_de_la_fin_est_vide() throws Exception {
        mvc.perform(get("/api/developpeurs/classement").param("page", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu").isEmpty());
    }
}

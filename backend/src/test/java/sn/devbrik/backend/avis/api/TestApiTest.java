package sn.devbrik.backend.avis.api;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestApiTest extends ApiTestSupport {

    private static String test(String resultat, String environnement) {
        return environnement == null ? "{\"resultat\":\"" + resultat + "\"}"
                : "{\"resultat\":\"" + resultat + "\",\"environnement\":\"" + environnement + "\"}";
    }

    @Test
    void test_section4_le_visiteur_lit_les_tests_mais_ne_peut_pas_tester() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(get("/api/briques/{id}/tests", b.id())).andExpect(status().isOk());
        mvc.perform(put("/api/briques/{id}/test", b.id()).contentType(JSON).content(test("OK", null)))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/briques/{id}/test", b.id())).andExpect(status().isUnauthorized());
    }

    @Test
    void test_RG9_section4_developpeur_non_auteur_cree_puis_change_son_test() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();

        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), dev).contentType(JSON)
                        .content(test("KO", "Java 17, Linux")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultat").value("KO"))
                .andExpect(jsonPath("$.versionCode").value(1));
        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), dev).contentType(JSON).content(test("OK", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultat").value("OK"));

        mvc.perform(get("/api/briques/{id}/tests", b.id())).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void test_RG9_section4_un_administrateur_peut_tester_la_brique_d_un_autre() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), administrateur()).contentType(JSON)
                        .content(test("OK", null)))
                .andExpect(status().isCreated());
    }

    @Test
    void test_RG9_l_auteur_ne_peut_pas_tester_sa_propre_brique() throws Exception {
        Compte auteur = inscrire();
        Brique b = creerBrique(auteur);

        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), auteur).contentType(JSON).content(test("OK", null)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_EVALUATION_INTERDITE"));
    }

    @Test
    void test_RG3_RG9_l_environnement_est_limite_a_120_caracteres() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();

        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), dev).contentType(JSON)
                        .content(test("OK", "e".repeat(121))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), dev).contentType(JSON)
                        .content(test("OK", "e".repeat(120))))
                .andExpect(status().isCreated());
    }

    @Test
    void test_RG9_un_resultat_autre_que_OK_ou_KO_est_refuse() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), inscrire()).contentType(JSON)
                        .content(test("PEUT-ETRE", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_RG9_retirer_son_test_puis_le_retirer_encore_donne_404() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();
        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), dev).contentType(JSON).content(test("OK", null)));

        mvc.perform(avec(delete("/api/briques/{id}/test", b.id()), dev)).andExpect(status().isNoContent());
        mvc.perform(avec(delete("/api/briques/{id}/test", b.id()), dev))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
    }

    @Test
    void test_RG7_test_sur_une_brique_masquee_d_un_autre_donne_404() throws Exception {
        Brique b = creerBrique(inscrire());
        masquer(administrateur(), b);

        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), inscrire()).contentType(JSON).content(test("OK", null)))
                .andExpect(status().isNotFound());
    }
}
package sn.devbrik.backend.avis.api;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatistiquesApiTest extends ApiTestSupport {

    @Test
    void test_U24_section4_seul_l_administrateur_voit_les_statistiques() throws Exception {
        mvc.perform(get("/api/admin/statistiques")).andExpect(status().isUnauthorized());
        mvc.perform(avec(get("/api/admin/statistiques"), inscrire())).andExpect(status().isForbidden());
        mvc.perform(avec(get("/api/admin/statistiques"), administrateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilisateurs").isNumber())
                .andExpect(jsonPath("$.briquesPubliees").isNumber())
                .andExpect(jsonPath("$.briquesMasquees").isNumber())
                .andExpect(jsonPath("$.avis").isNumber())
                .andExpect(jsonPath("$.tests").isNumber())
                .andExpect(jsonPath("$.demandesOuvertes").isNumber())
                .andExpect(jsonPath("$.demandesResolues").isNumber())
                .andExpect(jsonPath("$.signalementsOuverts").isNumber());
    }

    @Test
    void test_U24_les_compteurs_suivent_les_actions_des_utilisateurs() throws Exception {
        Compte admin = administrateur();
        int avantAvis = lire(admin, "$.avis");
        int avantTests = lire(admin, "$.tests");
        int avantSignalements = lire(admin, "$.signalementsOuverts");

        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();
        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON).content("{\"note\":4}"));
        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), dev).contentType(JSON).content("{\"resultat\":\"OK\"}"));
        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), dev).contentType(JSON)
                .content("{\"motif\":\"SPAM\"}"));

        mvc.perform(avec(get("/api/admin/statistiques"), admin))
                .andExpect(jsonPath("$.avis").value(avantAvis + 1))
                .andExpect(jsonPath("$.tests").value(avantTests + 1))
                .andExpect(jsonPath("$.signalementsOuverts").value(avantSignalements + 1));
    }

    private int lire(Compte admin, String chemin) throws Exception {
        String corps = mvc.perform(avec(get("/api/admin/statistiques"), admin))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return com.jayway.jsonpath.JsonPath.read(corps, chemin);
    }
}
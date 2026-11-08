package sn.devbrik.backend.avis.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Scénario de la section 13.3 : « le code change, les tests de l'ancienne version ne comptent plus ». */
class ScenarioChangementCodeApiTest extends ApiTestSupport {

    @Test
    void test_RG9_RG10_le_code_change_les_tests_de_l_ancienne_version_ne_comptent_plus() throws Exception {
        Compte auteur = inscrire();
        Compte testeurA = inscrire();
        Compte testeurB = inscrire();
        Brique b = creerBrique(auteur);

        // Version 1 : un test OK et un test KO.
        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), testeurA).contentType(JSON)
                        .content("{\"resultat\":\"OK\",\"environnement\":\"Java 21, Windows 11\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionCode").value(1));
        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), testeurB).contentType(JSON)
                        .content("{\"resultat\":\"KO\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/briques/{id}", b.id()))
                .andExpect(jsonPath("$.versionCode").value(1))
                .andExpect(jsonPath("$.testsOk").value(1))
                .andExpect(jsonPath("$.testsKo").value(1))
                .andExpect(jsonPath("$.derniereVerification").value(notNullValue()));
        mvc.perform(get("/api/briques/{id}/tests", b.id())).andExpect(jsonPath("$.totalElements").value(2));

        // L'auteur modifie le code : la version passe à 2 (RG6).
        modifierCode(auteur, b, "code v2");

        mvc.perform(get("/api/briques/{id}", b.id()))
                .andExpect(jsonPath("$.versionCode").value(2))
                .andExpect(jsonPath("$.testsOk").value(0))
                .andExpect(jsonPath("$.testsKo").value(0))
                .andExpect(jsonPath("$.derniereVerification").value(nullValue()));
        mvc.perform(get("/api/briques/{id}/tests", b.id())).andExpect(jsonPath("$.totalElements").value(0));

        // Le testeur A voit encore son ancien test (version 1) dans /moi.
        mvc.perform(avec(get("/api/briques/{id}/moi", b.id()), testeurA))
                .andExpect(jsonPath("$.monTest.versionCode").value(1));

        // Il le refait sur la version actuelle : 200 (mise à jour), version 2, il compte de nouveau.
        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), testeurA).contentType(JSON)
                        .content("{\"resultat\":\"OK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionCode").value(2));

        mvc.perform(get("/api/briques/{id}", b.id()))
                .andExpect(jsonPath("$.testsOk").value(1))
                .andExpect(jsonPath("$.testsKo").value(0))
                .andExpect(jsonPath("$.derniereVerification").value(notNullValue()));
    }

    @Test
    void test_RG6_modifier_un_autre_champ_que_le_code_ne_change_pas_la_version_des_tests() throws Exception {
        Compte auteur = inscrire();
        Brique b = creerBrique(auteur);
        mvc.perform(avec(put("/api/briques/{id}/test", b.id()), inscrire()).contentType(JSON)
                .content("{\"resultat\":\"OK\"}"));

        // Même code, titre identique : la version reste 1 et le test compte toujours.
        modifierCode(auteur, b, "code v1");

        mvc.perform(get("/api/briques/{id}", b.id()))
                .andExpect(jsonPath("$.versionCode").value(1))
                .andExpect(jsonPath("$.testsOk").value(1));
    }
}
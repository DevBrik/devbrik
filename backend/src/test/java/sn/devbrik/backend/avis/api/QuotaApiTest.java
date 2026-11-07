package sn.devbrik.backend.avis.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RG15 sur la vraie QuotaService (membre 1) : 50 avis, 50 tests, 10 signalements par 24 h glissantes.
 * Les lignes de quota sont insérées en SQL (tests seulement).
 */
class QuotaApiTest extends ApiTestSupport {

    private void insererAvis(long idDev, List<Long> briques, String age) {
        for (Long idBrique : briques) {
            jdbc.update("INSERT INTO avis (note, date_creation, date_modification, id_brique, id_auteur) "
                    + "VALUES (3, now() - ?::interval, now() - ?::interval, ?, ?)", age, age, idBrique, idDev);
        }
    }

    private void insererTests(long idDev, List<Long> briques, String age) {
        for (Long idBrique : briques) {
            jdbc.update("INSERT INTO test_brique (resultat, version_code, date_test, id_brique, id_auteur) "
                    + "VALUES ('OK', 1, now() - ?::interval, ?, ?)", age, idBrique, idDev);
        }
    }

    private void insererSignalements(long idDev, List<Long> briques, String age) {
        for (Long idBrique : briques) {
            jdbc.update("INSERT INTO signalement_brique (motif, date_creation, id_brique, id_auteur) "
                    + "VALUES ('SPAM', now() - ?::interval, ?, ?)", age, idBrique, idDev);
        }
    }

    // ---------- avis : 50 ----------

    @Test
    void test_RG15_le_51e_avis_en_24_h_donne_429() throws Exception {
        Compte auteur = inscrire();
        Compte dev = inscrire();
        insererAvis(dev.id(), briquesSql(50, auteur.id()), "1 hour");
        Brique nouvelle = creerBrique(auteur);

        mvc.perform(avec(put("/api/briques/{id}/avis", nouvelle.id()), dev).contentType(JSON).content("{\"note\":5}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LIMITE_QUOTIDIENNE"));
    }

    @Test
    void test_RG15_le_49e_avis_passe_et_les_avis_de_plus_de_24_h_ne_comptent_pas() throws Exception {
        Compte auteur = inscrire();
        Compte dev = inscrire();
        insererAvis(dev.id(), briquesSql(49, auteur.id()), "1 hour");
        Brique nouvelle = creerBrique(auteur);

        mvc.perform(avec(put("/api/briques/{id}/avis", nouvelle.id()), dev).contentType(JSON).content("{\"note\":5}"))
                .andExpect(status().isCreated());

        Compte dev2 = inscrire();
        insererAvis(dev2.id(), briquesSql(50, auteur.id()), "25 hours");
        mvc.perform(avec(put("/api/briques/{id}/avis", nouvelle.id()), dev2).contentType(JSON).content("{\"note\":5}"))
                .andExpect(status().isCreated());
    }

    // ---------- tests : 50 ----------

    @Test
    void test_RG15_le_51e_test_en_24_h_donne_429() throws Exception {
        Compte auteur = inscrire();
        Compte dev = inscrire();
        insererTests(dev.id(), briquesSql(50, auteur.id()), "1 hour");
        Brique nouvelle = creerBrique(auteur);

        mvc.perform(avec(put("/api/briques/{id}/test", nouvelle.id()), dev).contentType(JSON)
                        .content("{\"resultat\":\"OK\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LIMITE_QUOTIDIENNE"));
    }

    @Test
    void test_RG15_les_tests_de_plus_de_24_h_ne_comptent_pas() throws Exception {
        Compte auteur = inscrire();
        Compte dev = inscrire();
        insererTests(dev.id(), briquesSql(50, auteur.id()), "25 hours");
        Brique nouvelle = creerBrique(auteur);

        mvc.perform(avec(put("/api/briques/{id}/test", nouvelle.id()), dev).contentType(JSON)
                        .content("{\"resultat\":\"OK\"}"))
                .andExpect(status().isCreated());
    }

    // ---------- signalements : 10 ----------

    @Test
    void test_RG15_le_11e_signalement_en_24_h_donne_429() throws Exception {
        Compte auteur = inscrire();
        Compte dev = inscrire();
        insererSignalements(dev.id(), briquesSql(10, auteur.id()), "1 hour");
        Brique nouvelle = creerBrique(auteur);

        mvc.perform(avec(post("/api/briques/{id}/signalements", nouvelle.id()), dev).contentType(JSON)
                        .content("{\"motif\":\"SPAM\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LIMITE_QUOTIDIENNE"));
    }

    @Test
    void test_RG15_les_signalements_de_plus_de_24_h_ne_comptent_pas() throws Exception {
        Compte auteur = inscrire();
        Compte dev = inscrire();
        insererSignalements(dev.id(), briquesSql(10, auteur.id()), "25 hours");
        Brique nouvelle = creerBrique(auteur);

        mvc.perform(avec(post("/api/briques/{id}/signalements", nouvelle.id()), dev).contentType(JSON)
                        .content("{\"motif\":\"SPAM\"}"))
                .andExpect(status().isCreated());
    }
}
package sn.devbrik.backend.avis.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvisApiTest extends ApiTestSupport {

    private static String avis(int note, String commentaire) {
        return commentaire == null ? "{\"note\":" + note + "}"
                : "{\"note\":" + note + ",\"commentaire\":\"" + commentaire + "\"}";
    }

    private long idAvis(Compte auteurAvis, Brique brique) throws Exception {
        return jdbc.queryForObject("SELECT id_avis FROM avis WHERE id_brique = ? AND id_auteur = ?",
                Long.class, brique.id(), auteurAvis.id());
    }

    // ---------- section 4 : consulter, noter ----------

    @Test
    void test_section4_le_visiteur_lit_les_avis_mais_ne_peut_pas_en_ecrire() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(get("/api/briques/{id}/avis", b.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(put("/api/briques/{id}/avis", b.id()).contentType(JSON).content(avis(5, null)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }

    @Test
    void test_RG8_section4_developpeur_non_auteur_cree_puis_met_a_jour_son_avis() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();

        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON)
                        .content(avis(5, "Très utile")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note").value(5));
        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON)
                        .content(avis(3, "Finalement moyen")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(3));

        mvc.perform(get("/api/briques/{id}/avis", b.id()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void test_RG8_section4_un_administrateur_peut_noter_la_brique_d_un_autre() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), administrateur()).contentType(JSON)
                        .content(avis(4, null)))
                .andExpect(status().isCreated());
    }

    @Test
    void test_RG8_l_auteur_ne_peut_pas_noter_sa_propre_brique() throws Exception {
        Compte auteur = inscrire();
        Brique b = creerBrique(auteur);

        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), auteur).contentType(JSON).content(avis(5, null)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_EVALUATION_INTERDITE"));
    }

    @Test
    void test_RG8_la_note_doit_etre_comprise_entre_1_et_5() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();

        for (int note : new int[] {0, 6}) {
            mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON).content(avis(note, null)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
        }
    }

    @Test
    void test_RG3_un_commentaire_de_2001_caracteres_est_refuse() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();

        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON)
                        .content(avis(4, "a".repeat(2001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON)
                        .content(avis(4, "a".repeat(2000))))
                .andExpect(status().isCreated());
    }

    @Test
    void test_RG8_la_note_moyenne_de_la_fiche_est_arrondie_a_une_decimale() throws Exception {
        Brique b = creerBrique(inscrire());
        mvc.perform(get("/api/briques/{id}", b.id()))
                .andExpect(jsonPath("$.noteMoyenne").value(nullValue()))
                .andExpect(jsonPath("$.nombreAvis").value(0));

        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), inscrire()).contentType(JSON).content(avis(5, null)))
                .andExpect(status().isCreated());
        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), inscrire()).contentType(JSON).content(avis(4, null)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/briques/{id}", b.id()))
                .andExpect(jsonPath("$.noteMoyenne").value(4.5))
                .andExpect(jsonPath("$.nombreAvis").value(2));
    }

    @Test
    void test_RG7_avis_sur_une_brique_masquee_d_un_autre_donne_404() throws Exception {
        Brique b = creerBrique(inscrire());
        masquer(administrateur(), b);

        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), inscrire()).contentType(JSON).content(avis(5, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
        mvc.perform(get("/api/briques/{id}/avis", b.id())).andExpect(status().isNotFound());
    }

    // ---------- section 4 : supprimer un avis (RG19) ----------

    @Test
    void test_RG19_section4_le_visiteur_ne_supprime_pas_un_avis() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();
        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON).content(avis(4, null)));

        mvc.perform(delete("/api/avis/{id}", idAvis(dev, b))).andExpect(status().isUnauthorized());
    }

    @Test
    void test_RG19_section4_un_autre_developpeur_ne_supprime_pas_l_avis() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();
        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON).content(avis(4, null)));

        mvc.perform(avec(delete("/api/avis/{id}", idAvis(dev, b)), inscrire()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void test_RG19_section4_l_auteur_de_l_avis_le_supprime() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();
        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON).content(avis(4, null)));

        mvc.perform(avec(delete("/api/avis/{id}", idAvis(dev, b)), dev)).andExpect(status().isNoContent());
        mvc.perform(get("/api/briques/{id}/avis", b.id())).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void test_RG19_section4_un_administrateur_supprime_n_importe_quel_avis() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();
        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON).content(avis(4, null)));

        mvc.perform(avec(delete("/api/avis/{id}", idAvis(dev, b)), administrateur()))
                .andExpect(status().isNoContent());
    }

    // ---------- GET /moi ----------

    @Test
    void test_moi_exige_un_jeton_et_renvoie_null_sans_avis_ni_test() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();

        mvc.perform(get("/api/briques/{id}/moi", b.id())).andExpect(status().isUnauthorized());
        mvc.perform(avec(get("/api/briques/{id}/moi", b.id()), dev))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monAvis").value(nullValue()))
                .andExpect(jsonPath("$.monTest").value(nullValue()));

        mvc.perform(avec(put("/api/briques/{id}/avis", b.id()), dev).contentType(JSON).content(avis(4, null)));
        mvc.perform(avec(get("/api/briques/{id}/moi", b.id()), dev))
                .andExpect(jsonPath("$.monAvis.note").value(4));
    }
}
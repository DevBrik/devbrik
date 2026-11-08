package sn.devbrik.backend.avis.api;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SignalementApiTest extends ApiTestSupport {

    private static final String SIGNALEMENT = "{\"motif\":\"SPAM\",\"details\":\"Publicité\"}";

    private long idSignalement(Compte auteur, Brique brique) {
        return jdbc.queryForObject("SELECT id_signalement FROM signalement_brique WHERE id_brique = ? AND id_auteur = ?",
                Long.class, brique.id(), auteur.id());
    }

    // ---------- section 4 : signaler ----------

    @Test
    void test_RG11_section4_le_visiteur_ne_peut_pas_signaler() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(post("/api/briques/{id}/signalements", b.id()).contentType(JSON).content(SIGNALEMENT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_RG11_section4_developpeur_non_auteur_signale_puis_recoit_409_au_deuxieme_envoi() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();

        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), dev).contentType(JSON).content(SIGNALEMENT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("OUVERT"))
                .andExpect(jsonPath("$.motif").value("SPAM"));
        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), dev).contentType(JSON).content(SIGNALEMENT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SIGNALEMENT_DEJA_ENVOYE"));
    }

    @Test
    void test_RG11_section4_un_administrateur_peut_signaler_la_brique_d_un_autre() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), administrateur())
                        .contentType(JSON).content(SIGNALEMENT))
                .andExpect(status().isCreated());
    }

    @Test
    void test_RG11_l_auteur_ne_peut_pas_signaler_sa_propre_brique() throws Exception {
        Compte auteur = inscrire();
        Brique b = creerBrique(auteur);

        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), auteur).contentType(JSON).content(SIGNALEMENT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_EVALUATION_INTERDITE"));
    }

    @Test
    void test_RG11_un_motif_inconnu_est_refuse() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), inscrire()).contentType(JSON)
                        .content("{\"motif\":\"JE_N_AIME_PAS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_RG3_des_details_de_1001_caracteres_sont_refuses() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), inscrire()).contentType(JSON)
                        .content("{\"motif\":\"AUTRE\",\"details\":\"" + "d".repeat(1001) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_RG7_signaler_une_brique_masquee_d_un_autre_donne_404() throws Exception {
        Brique b = creerBrique(inscrire());
        masquer(administrateur(), b);

        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), inscrire()).contentType(JSON).content(SIGNALEMENT))
                .andExpect(status().isNotFound());
    }

    // ---------- section 4 : traiter les signalements ----------

    @Test
    void test_RG11_section4_seul_l_administrateur_voit_et_traite_les_signalements() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();
        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), dev).contentType(JSON).content(SIGNALEMENT));
        long id = idSignalement(dev, b);

        // Visiteur : 401.
        mvc.perform(get("/api/admin/signalements")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/admin/signalements/{id}/traitement", id).contentType(JSON)
                .content("{\"action\":\"CONSERVER\"}")).andExpect(status().isUnauthorized());
        // Développeur (même celui qui a signalé) : 403.
        mvc.perform(avec(get("/api/admin/signalements"), dev)).andExpect(status().isForbidden());
        mvc.perform(avec(put("/api/admin/signalements/{id}/traitement", id), dev).contentType(JSON)
                .content("{\"action\":\"CONSERVER\"}")).andExpect(status().isForbidden());
        // Administrateur : 200.
        mvc.perform(avec(get("/api/admin/signalements"), administrateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu[?(@.id==" + id + ")].brique.titre").value(b.titre()));
    }

    @Test
    void test_RG11_masquer_brique_masque_la_brique_et_passe_le_signalement_a_traite() throws Exception {
        Compte auteur = inscrire();
        Brique b = creerBrique(auteur);
        Compte dev = inscrire();
        Compte admin = administrateur();
        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), dev).contentType(JSON).content(SIGNALEMENT));
        long id = idSignalement(dev, b);

        mvc.perform(avec(get("/api/briques"), null).param("auteur", String.valueOf(auteur.id())))
                .andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(avec(put("/api/admin/signalements/{id}/traitement", id), admin).contentType(JSON)
                        .content("{\"action\":\"MASQUER_BRIQUE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TRAITE"))
                .andExpect(jsonPath("$.actionTraitement").value("MASQUER_BRIQUE"));

        // RG7 : disparue de la liste publique, 404 pour le visiteur, visible de l'auteur.
        mvc.perform(get("/api/briques").param("auteur", String.valueOf(auteur.id())))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/briques/{id}", b.id())).andExpect(status().isNotFound());
        mvc.perform(avec(get("/api/briques/{id}", b.id()), auteur)).andExpect(status().isOk());
        // Le signalement n'est plus dans la liste des ouverts, mais dans celle des traités.
        mvc.perform(avec(get("/api/admin/signalements"), admin))
                .andExpect(jsonPath("$.contenu[?(@.id==" + id + ")]").isEmpty());
        mvc.perform(avec(get("/api/admin/signalements"), admin).param("statut", "TRAITE"))
                .andExpect(jsonPath("$.contenu[?(@.id==" + id + ")]").isNotEmpty());
    }

    @Test
    void test_RG11_conserver_laisse_la_brique_publiee() throws Exception {
        Compte auteur = inscrire();
        Brique b = creerBrique(auteur);
        Compte dev = inscrire();
        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), dev).contentType(JSON).content(SIGNALEMENT));
        long id = idSignalement(dev, b);

        mvc.perform(avec(put("/api/admin/signalements/{id}/traitement", id), administrateur()).contentType(JSON)
                        .content("{\"action\":\"CONSERVER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TRAITE"));

        mvc.perform(get("/api/briques/{id}", b.id())).andExpect(status().isOk());
    }

    @Test
    void test_RG11_une_action_inconnue_est_refusee() throws Exception {
        Brique b = creerBrique(inscrire());
        Compte dev = inscrire();
        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), dev).contentType(JSON).content(SIGNALEMENT));

        mvc.perform(avec(put("/api/admin/signalements/{id}/traitement", idSignalement(dev, b)), administrateur())
                        .contentType(JSON).content("{\"action\":\"SUPPRIMER\"}"))
                .andExpect(status().isBadRequest());
    }
        @Test
    void test_RG11_le_motif_INAPPROPRIE_est_accepte() throws Exception {
        Brique b = creerBrique(inscrire());

        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), inscrire()).contentType(JSON)
                        .content("{\"motif\":\"INAPPROPRIE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.motif").value("INAPPROPRIE"));
    }

    @Test
    void test_contrat_la_liste_porte_brique_auteur_et_auteur_du_signalement() throws Exception {
        Compte auteur = inscrire();
        Brique b = creerBrique(auteur);
        Compte dev = inscrire();
        mvc.perform(avec(post("/api/briques/{id}/signalements", b.id()), dev).contentType(JSON).content(SIGNALEMENT));
        long id = idSignalement(dev, b);

        mvc.perform(avec(get("/api/admin/signalements"), administrateur()))
                .andExpect(jsonPath("$.contenu[?(@.id==" + id + ")].brique.auteur.id").value((int) auteur.id()))
                .andExpect(jsonPath("$.contenu[?(@.id==" + id + ")].auteur.id").value((int) dev.id()));
    }
}
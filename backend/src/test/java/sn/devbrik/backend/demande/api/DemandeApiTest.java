package sn.devbrik.backend.demande.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import sn.devbrik.backend.avis.api.ApiTestSupport;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Demandes et réponses (11.6) de bout en bout : RG12, RG13, RG15, RG16, RG19 et section 4. */
class DemandeApiTest extends ApiTestSupport {

    private static final String DESCRIPTION = "Je cherche une brique qui valide et normalise les numéros de téléphone sénégalais.";

    private static String corpsDemande(String titre, String description) {
        return "{\"titre\":\"" + titre + "\",\"description\":\"" + description + "\"}";
    }

    private long creerDemande(Compte auteur) throws Exception {
        return creerDemande(auteur, "Je cherche une brique de validation", DESCRIPTION);
    }

    private long creerDemande(Compte auteur, String titre, String description) throws Exception {
        String corps = mvc.perform(avec(post("/api/demandes"), auteur).contentType(JSON)
                        .content(corpsDemande(titre, description)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(corps, "$.id")).longValue();
    }

    private long repondre(Compte repondant, long idDemande) throws Exception {
        String corps = mvc.perform(avec(post("/api/demandes/{id}/reponses", idDemande), repondant).contentType(JSON)
                        .content("{\"contenu\":\"Voici une brique qui répond à cela.\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(corps, "$.id")).longValue();
    }

    private void accepter(Compte auteurDemande, long idReponse) throws Exception {
        mvc.perform(avec(put("/api/reponses/{id}/acceptation", idReponse), auteurDemande))
                .andExpect(status().isOk());
    }

    private static String ligne(long id) {
        return "$.contenu[?(@.id==" + id + ")]";
    }

    // ------------------------------------------------------------ création et lecture

    @Test
    void test_11_6_creer_une_demande_renvoie_le_detail_ouvert() throws Exception {
        Compte auteur = inscrire();

        mvc.perform(avec(post("/api/demandes"), auteur).contentType(JSON)
                        .content(corpsDemande("Je cherche une brique de validation", DESCRIPTION)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("OUVERTE"))
                .andExpect(jsonPath("$.titre").value("Je cherche une brique de validation"))
                .andExpect(jsonPath("$.description").value(DESCRIPTION))
                .andExpect(jsonPath("$.auteur.id").value((int) auteur.id()))
                .andExpect(jsonPath("$.nombreReponses").value(0))
                .andExpect(jsonPath("$.reponses").isEmpty());
    }

    @Test
    void test_11_6_la_liste_et_le_detail_sont_publics() throws Exception {
        Compte auteur = inscrire();
        long id = creerDemande(auteur);

        mvc.perform(get("/api/demandes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(id)).isNotEmpty());
        mvc.perform(get("/api/demandes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.description").value(DESCRIPTION));
    }

    @Test
    void test_11_6_une_demande_inconnue_donne_404() throws Exception {
        mvc.perform(get("/api/demandes/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
    }

    @Test
    void test_11_6_l_extrait_fait_160_caracteres_espaces_reduits_et_se_termine_par_une_ellipse() throws Exception {
        Compte auteur = inscrire();
        String longue = "mot ".repeat(60); // 240 caractères

        String corps = mvc.perform(avec(post("/api/demandes"), auteur).contentType(JSON)
                        .content(corpsDemande("Une demande avec une longue description", longue)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String extrait = JsonPath.read(corps, "$.extrait");
        assertThat(extrait).hasSize(161).endsWith("…").startsWith("mot mot mot");
    }

    @Test
    void test_11_6_un_extrait_court_n_est_pas_tronque() throws Exception {
        Compte auteur = inscrire();

        String corps = mvc.perform(avec(post("/api/demandes"), auteur).contentType(JSON)
                        .content(corpsDemande("Une demande courte mais valide", DESCRIPTION)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat((String) JsonPath.read(corps, "$.extrait")).isEqualTo(DESCRIPTION);
    }

    // ----------------------------------------------------------------- validation (RG3)

    @Test
    void test_RG3_un_titre_de_9_caracteres_est_refuse() throws Exception {
        mvc.perform(avec(post("/api/demandes"), inscrire()).contentType(JSON)
                        .content(corpsDemande("123456789", DESCRIPTION)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_RG3_une_description_de_19_caracteres_est_refusee() throws Exception {
        mvc.perform(avec(post("/api/demandes"), inscrire()).contentType(JSON)
                        .content(corpsDemande("Un titre suffisant", "1234567890123456789")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_RG3_une_reponse_de_4_caracteres_est_refusee() throws Exception {
        Compte auteur = inscrire();
        long id = creerDemande(auteur);

        mvc.perform(avec(post("/api/demandes/{id}/reponses", id), inscrire()).contentType(JSON)
                        .content("{\"contenu\":\"1234\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_11_6_une_categorie_inconnue_est_refusee() throws Exception {
        mvc.perform(avec(post("/api/demandes"), inscrire()).contentType(JSON)
                        .content("{\"titre\":\"Un titre suffisant\",\"description\":\"" + DESCRIPTION
                                + "\",\"idCategorie\":999999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    // ---------------------------------------------------------------- liste (RG16)

    @Test
    void test_RG16_le_filtre_statut_separe_ouvertes_et_resolues() throws Exception {
        Compte auteur = inscrire();
        Compte repondant = inscrire();
        long ouverte = creerDemande(auteur, "Demande restée ouverte", DESCRIPTION);
        long resolue = creerDemande(auteur, "Demande qui sera résolue", DESCRIPTION);
        accepter(auteur, repondre(repondant, resolue));

        mvc.perform(get("/api/demandes").param("statut", "RESOLUE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(resolue)).isNotEmpty())
                .andExpect(jsonPath(ligne(ouverte)).isEmpty());
        mvc.perform(get("/api/demandes").param("statut", "OUVERTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(ouverte)).isNotEmpty())
                .andExpect(jsonPath(ligne(resolue)).isEmpty());
    }

    @Test
    void test_RG16_un_statut_inconnu_est_refuse() throws Exception {
        mvc.perform(get("/api/demandes").param("statut", "FERMEE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_RG16_la_recherche_porte_sur_le_titre_et_la_description() throws Exception {
        Compte auteur = inscrire();
        long id = creerDemande(auteur, "Générateur de reçus Wave", "Il me faut un module qui produit des reçus PDF propres.");

        mvc.perform(get("/api/demandes").param("q", "reçus wave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(id)).isNotEmpty());
        mvc.perform(get("/api/demandes").param("q", "reçus pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(id)).isNotEmpty());
    }

    @Test
    void test_RG16_les_caracteres_joker_sont_du_texte_ordinaire() throws Exception {
        creerDemande(inscrire());

        mvc.perform(get("/api/demandes").param("q", "%%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/demandes").param("q", "__"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void test_RG16_une_recherche_d_un_caractere_est_refusee() throws Exception {
        mvc.perform(get("/api/demandes").param("q", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_RG16_la_taille_est_limitee_a_50_et_la_page_ne_peut_pas_etre_negative() throws Exception {
        mvc.perform(get("/api/demandes").param("taille", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taille").value(50));
        mvc.perform(get("/api/demandes"))
                .andExpect(jsonPath("$.taille").value(20))
                .andExpect(jsonPath("$.page").value(0));
        mvc.perform(get("/api/demandes").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_RG16_les_demandes_recentes_viennent_en_premier() throws Exception {
        Compte auteur = inscrire();
        long premiere = creerDemande(auteur, "Première demande créée", DESCRIPTION);
        long seconde = creerDemande(auteur, "Seconde demande créée", DESCRIPTION);

        String corps = mvc.perform(get("/api/demandes").param("taille", "50"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        java.util.List<Integer> ids = JsonPath.read(corps, "$.contenu[*].id");
        assertThat(ids.indexOf((int) seconde)).isLessThan(ids.indexOf((int) premiere));
    }

    // ----------------------------------------------------------- acceptation (RG12)

    @Test
    void test_RG12_accepter_resout_la_demande_et_retirer_la_rouvre() throws Exception {
        Compte auteur = inscrire();
        Compte repondant = inscrire();
        long demande = creerDemande(auteur);
        long reponse = repondre(repondant, demande);

        mvc.perform(avec(put("/api/reponses/{id}/acceptation", reponse), auteur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RESOLUE"))
                .andExpect(jsonPath("$.reponses[0].acceptee").value(true));

        mvc.perform(avec(delete("/api/reponses/{id}/acceptation", reponse), auteur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("OUVERTE"))
                .andExpect(jsonPath("$.reponses[0].acceptee").value(false));
    }

    @Test
    void test_RG12_accepter_deux_fois_la_meme_reponse_est_sans_effet_supplementaire() throws Exception {
        Compte auteur = inscrire();
        long reponse = repondre(inscrire(), creerDemande(auteur));
        accepter(auteur, reponse);

        mvc.perform(avec(put("/api/reponses/{id}/acceptation", reponse), auteur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RESOLUE"));
    }

    @Test
    void test_RG12_deux_reponses_acceptees_sont_impossibles() throws Exception {
        Compte auteur = inscrire();
        long demande = creerDemande(auteur);
        long premiere = repondre(inscrire(), demande);
        long seconde = repondre(inscrire(), demande);
        accepter(auteur, premiere);

        mvc.perform(avec(put("/api/reponses/{id}/acceptation", seconde), auteur))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REPONSE_DEJA_ACCEPTEE"));
    }

    @Test
    void test_RG12_on_peut_accepter_une_autre_reponse_apres_avoir_retire_la_premiere() throws Exception {
        Compte auteur = inscrire();
        long demande = creerDemande(auteur);
        long premiere = repondre(inscrire(), demande);
        long seconde = repondre(inscrire(), demande);
        accepter(auteur, premiere);
        mvc.perform(avec(delete("/api/reponses/{id}/acceptation", premiere), auteur))
                .andExpect(status().isOk());

        mvc.perform(avec(put("/api/reponses/{id}/acceptation", seconde), auteur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RESOLUE"));
    }

    @Test
    void test_RG12_accepter_sa_propre_reponse_est_refuse() throws Exception {
        Compte auteur = inscrire();
        long reponse = repondre(auteur, creerDemande(auteur));

        mvc.perform(avec(put("/api/reponses/{id}/acceptation", reponse), auteur))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCEPTATION_PROPRE_REPONSE"));
    }

    @Test
    void test_section4_seul_l_auteur_de_la_demande_accepte_ou_retire() throws Exception {
        Compte auteur = inscrire();
        Compte repondant = inscrire();
        Compte tiers = inscrire();
        long reponse = repondre(repondant, creerDemande(auteur));

        mvc.perform(avec(put("/api/reponses/{id}/acceptation", reponse), tiers))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
        mvc.perform(avec(put("/api/reponses/{id}/acceptation", reponse), administrateur()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));

        accepter(auteur, reponse);
        mvc.perform(avec(delete("/api/reponses/{id}/acceptation", reponse), repondant))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void test_11_6_une_reponse_inconnue_donne_404() throws Exception {
        mvc.perform(avec(put("/api/reponses/999999999/acceptation"), inscrire()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
    }

    // --------------------------------------------------------- lien vers brique (RG13)

    @Test
    void test_RG13_une_reponse_peut_renvoyer_vers_une_brique_publiee() throws Exception {
        Compte auteurBrique = inscrire();
        Brique brique = creerBrique(auteurBrique);
        long demande = creerDemande(inscrire());

        mvc.perform(avec(post("/api/demandes/{id}/reponses", demande), auteurBrique).contentType(JSON)
                        .content("{\"contenu\":\"Cette brique fait exactement cela.\",\"idBrique\":" + brique.id() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brique.id").value((int) brique.id()))
                .andExpect(jsonPath("$.brique.titre").value(brique.titre()))
                .andExpect(jsonPath("$.auteur.id").value((int) auteurBrique.id()));
    }

    @Test
    void test_RG13_une_brique_inexistante_est_refusee() throws Exception {
        long demande = creerDemande(inscrire());

        mvc.perform(avec(post("/api/demandes/{id}/reponses", demande), inscrire()).contentType(JSON)
                        .content("{\"contenu\":\"Cette brique n'existe pas.\",\"idBrique\":999999999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BRIQUE_LIEE_INVALIDE"));
    }

    @Test
    void test_RG13_une_brique_masquee_est_refusee() throws Exception {
        Compte auteurBrique = inscrire();
        Brique brique = creerBrique(auteurBrique);
        masquer(administrateur(), brique);
        long demande = creerDemande(inscrire());

        mvc.perform(avec(post("/api/demandes/{id}/reponses", demande), auteurBrique).contentType(JSON)
                        .content("{\"contenu\":\"Cette brique est masquée.\",\"idBrique\":" + brique.id() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BRIQUE_LIEE_INVALIDE"));
    }

    @Test
    void test_RG13_un_lien_devenu_invalide_renvoie_brique_null() throws Exception {
        Compte auteurBrique = inscrire();
        Brique brique = creerBrique(auteurBrique);
        long demande = creerDemande(inscrire());
        mvc.perform(avec(post("/api/demandes/{id}/reponses", demande), auteurBrique).contentType(JSON)
                        .content("{\"contenu\":\"Cette brique fait exactement cela.\",\"idBrique\":" + brique.id() + "}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/demandes/{id}", demande))
                .andExpect(jsonPath("$.reponses[0].brique.id").value((int) brique.id()));

        masquer(administrateur(), brique);

        mvc.perform(get("/api/demandes/{id}", demande))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reponses[0].brique").value(nullValue()));
    }

    @Test
    void test_RG13_une_brique_supprimee_renvoie_brique_null() throws Exception {
        Compte auteurBrique = inscrire();
        Brique brique = creerBrique(auteurBrique);
        long demande = creerDemande(inscrire());
        mvc.perform(avec(post("/api/demandes/{id}/reponses", demande), auteurBrique).contentType(JSON)
                        .content("{\"contenu\":\"Cette brique fait exactement cela.\",\"idBrique\":" + brique.id() + "}"))
                .andExpect(status().isCreated());

        mvc.perform(avec(delete("/api/briques/{id}", brique.id()), auteurBrique))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/demandes/{id}", demande))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reponses[0].brique").value(nullValue()));
    }

    // ------------------------------------------------------------ suppression (RG19)

    @Test
    void test_RG19_l_auteur_supprime_sa_demande_et_ses_reponses_disparaissent() throws Exception {
        Compte auteur = inscrire();
        long demande = creerDemande(auteur);
        long reponse = repondre(inscrire(), demande);

        mvc.perform(avec(delete("/api/demandes/{id}", demande), auteur))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/demandes/{id}", demande)).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM reponse WHERE id_reponse = ?", Integer.class, reponse))
                .isZero();
    }

    @Test
    void test_RG19_un_administrateur_supprime_la_demande_d_un_autre() throws Exception {
        long demande = creerDemande(inscrire());

        mvc.perform(avec(delete("/api/demandes/{id}", demande), administrateur()))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_RG19_un_tiers_ne_supprime_pas_la_demande_d_un_autre() throws Exception {
        long demande = creerDemande(inscrire());

        mvc.perform(avec(delete("/api/demandes/{id}", demande), inscrire()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
        mvc.perform(get("/api/demandes/{id}", demande)).andExpect(status().isOk());
    }

    @Test
    void test_RG19_l_auteur_supprime_sa_reponse_et_un_tiers_ne_le_peut_pas() throws Exception {
        long demande = creerDemande(inscrire());
        Compte repondant = inscrire();
        long reponse = repondre(repondant, demande);

        mvc.perform(avec(delete("/api/reponses/{id}", reponse), inscrire()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
        mvc.perform(avec(delete("/api/reponses/{id}", reponse), repondant))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_RG19_un_administrateur_supprime_la_reponse_d_un_autre() throws Exception {
        long reponse = repondre(inscrire(), creerDemande(inscrire()));

        mvc.perform(avec(delete("/api/reponses/{id}", reponse), administrateur()))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_RG12_supprimer_la_reponse_acceptee_rouvre_la_demande() throws Exception {
        Compte auteur = inscrire();
        Compte repondant = inscrire();
        long demande = creerDemande(auteur);
        long reponse = repondre(repondant, demande);
        accepter(auteur, reponse);

        mvc.perform(avec(delete("/api/reponses/{id}", reponse), repondant))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/demandes/{id}", demande))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("OUVERTE"))
                .andExpect(jsonPath("$.reponses").isEmpty());
    }

    // --------------------------------------------------------------- section 4

    @Test
    void test_section4_un_visiteur_ne_peut_ni_poster_ni_repondre_ni_supprimer_ni_accepter() throws Exception {
        long demande = creerDemande(inscrire());

        mvc.perform(post("/api/demandes").contentType(JSON).content(corpsDemande("Un titre suffisant", DESCRIPTION)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/demandes/{id}/reponses", demande).contentType(JSON)
                        .content("{\"contenu\":\"Une réponse valide.\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/demandes/{id}", demande)).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/reponses/1")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/reponses/1/acceptation")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/reponses/1/acceptation")).andExpect(status().isUnauthorized());
    }

    @Test
    void test_section4_un_administrateur_peut_poster_une_demande_et_repondre() throws Exception {
        Compte admin = administrateur();
        long demande = creerDemande(admin);

        repondre(admin, demande);
    }

    // ------------------------------------------------------------------ quotas (RG15)

    @Test
    void test_RG15_la_11e_demande_en_24_h_donne_429() throws Exception {
        Compte auteur = inscrire();
        for (int i = 0; i < 10; i++) {
            jdbc.update("INSERT INTO demande (titre, description, id_auteur) VALUES (?, ?, ?)",
                    "Demande SQL numéro " + i, "Description de demande suffisamment longue", auteur.id());
        }

        mvc.perform(avec(post("/api/demandes"), auteur).contentType(JSON)
                        .content(corpsDemande("Une onzième demande", DESCRIPTION)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LIMITE_QUOTIDIENNE"));
    }

    @Test
    void test_RG15_la_10e_demande_passe_et_les_demandes_de_plus_de_24_h_ne_comptent_pas() throws Exception {
        Compte auteur = inscrire();
        for (int i = 0; i < 9; i++) {
            jdbc.update("INSERT INTO demande (titre, description, id_auteur) VALUES (?, ?, ?)",
                    "Demande SQL numéro " + i, "Description de demande suffisamment longue", auteur.id());
        }
        for (int i = 0; i < 5; i++) {
            jdbc.update("INSERT INTO demande (titre, description, id_auteur, date_creation) "
                            + "VALUES (?, ?, ?, now() - interval '25 hours')",
                    "Ancienne demande " + i, "Description de demande suffisamment longue", auteur.id());
        }

        creerDemande(auteur, "La dixième demande passe", DESCRIPTION);
    }

    @Test
    void test_RG15_la_51e_reponse_en_24_h_donne_429() throws Exception {
        long demande = creerDemande(inscrire());
        Compte repondant = inscrire();
        for (int i = 0; i < 50; i++) {
            jdbc.update("INSERT INTO reponse (contenu, id_demande, id_auteur) VALUES (?, ?, ?)",
                    "Réponse SQL numéro " + i, demande, repondant.id());
        }

        mvc.perform(avec(post("/api/demandes/{id}/reponses", demande), repondant).contentType(JSON)
                        .content("{\"contenu\":\"La cinquante et unième.\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LIMITE_QUOTIDIENNE"));
    }
}

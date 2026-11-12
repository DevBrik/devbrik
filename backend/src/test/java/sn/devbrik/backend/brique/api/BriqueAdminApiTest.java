package sn.devbrik.backend.brique.api;

import org.junit.jupiter.api.Test;
import sn.devbrik.backend.avis.api.ApiTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Listes d'administration et « mes briques » (11.4), de bout en bout. */
class BriqueAdminApiTest extends ApiTestSupport {

    private static String ligne(long idBrique) {
        return "$.contenu[?(@.id==" + idBrique + ")]";
    }

    @Test
    void test_11_4_la_liste_d_administration_montre_les_briques_masquees_par_defaut() throws Exception {
        Compte auteur = inscrire();
        Brique publiee = creerBrique(auteur);
        Brique masquee = creerBrique(auteur);
        Compte admin = administrateur();
        masquer(admin, masquee);

        mvc.perform(avec(get("/api/admin/briques"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(masquee.id())).isNotEmpty())
                .andExpect(jsonPath(ligne(publiee.id())).isEmpty())
                .andExpect(jsonPath("$.contenu[?(@.statut=='PUBLIEE')]").isEmpty());
    }

    @Test
    void test_11_4_la_liste_d_administration_accepte_le_statut_PUBLIEE() throws Exception {
        Compte auteur = inscrire();
        Brique publiee = creerBrique(auteur);
        Brique masquee = creerBrique(auteur);
        Compte admin = administrateur();
        masquer(admin, masquee);

        mvc.perform(avec(get("/api/admin/briques").param("statut", "PUBLIEE"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(publiee.id())).isNotEmpty())
                .andExpect(jsonPath(ligne(masquee.id())).isEmpty());
    }

    @Test
    void test_11_4_un_statut_inconnu_est_refuse() throws Exception {
        mvc.perform(avec(get("/api/admin/briques").param("statut", "SUPPRIMEE"), administrateur()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_section4_la_liste_d_administration_est_reservee_aux_administrateurs() throws Exception {
        mvc.perform(get("/api/admin/briques"))
                .andExpect(status().isUnauthorized());
        mvc.perform(avec(get("/api/admin/briques"), inscrire()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void test_RG7_mes_briques_comprennent_les_briques_masquees() throws Exception {
        Compte auteur = inscrire();
        Brique publiee = creerBrique(auteur);
        Brique masquee = creerBrique(auteur);
        masquer(administrateur(), masquee);

        mvc.perform(avec(get("/api/moi/briques"), auteur))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(publiee.id())).isNotEmpty())
                .andExpect(jsonPath(ligne(masquee.id())).isNotEmpty());
    }

    @Test
    void test_11_4_mes_briques_acceptent_le_filtre_statut() throws Exception {
        Compte auteur = inscrire();
        Brique publiee = creerBrique(auteur);
        Brique masquee = creerBrique(auteur);
        masquer(administrateur(), masquee);

        mvc.perform(avec(get("/api/moi/briques").param("statut", "MASQUEE"), auteur))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(masquee.id())).isNotEmpty())
                .andExpect(jsonPath(ligne(publiee.id())).isEmpty());
    }

    @Test
    void test_11_4_mes_briques_ne_montrent_jamais_celles_d_un_autre() throws Exception {
        Compte auteur = inscrire();
        Compte autre = inscrire();
        Brique brique = creerBrique(auteur);

        mvc.perform(avec(get("/api/moi/briques"), autre))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(brique.id())).isEmpty());
    }

    @Test
    void test_section4_mes_briques_exigent_un_compte_connecte() throws Exception {
        mvc.perform(get("/api/moi/briques"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_RG7_la_liste_publique_ne_montre_pas_les_briques_masquees() throws Exception {
        Compte auteur = inscrire();
        Brique masquee = creerBrique(auteur);
        masquer(administrateur(), masquee);

        mvc.perform(get("/api/briques").param("auteur", String.valueOf(auteur.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ligne(masquee.id())).isEmpty());
    }

    @Test
    void test_11_4_les_referentiels_sont_publics_et_complets() throws Exception {
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].slug").isNotEmpty())
                .andExpect(jsonPath("$[0].nom").isNotEmpty());
        mvc.perform(get("/api/langages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11));
    }
}

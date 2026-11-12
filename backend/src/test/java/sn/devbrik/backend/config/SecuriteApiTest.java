package sn.devbrik.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import sn.devbrik.backend.avis.api.ApiTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sécurité (11.1, 11.7) : jetons, accès par rôle, format des erreurs, compte supprimé. */
class SecuriteApiTest extends ApiTestSupport {

    @Value("${app.jwt.secret}")
    private String secret;

    private String jetonExpire(long idUtilisateur) {
        JwtConfig config = new JwtConfig();
        config.setSecret(secret);
        config.setDureeHeures(-1);
        return new JwtService(config).generer(idUtilisateur, "DEVELOPPEUR");
    }

    @Test
    void test_11_1_un_jeton_invalide_donne_401_meme_sur_une_route_publique() throws Exception {
        mvc.perform(get("/api/briques").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statut").value(401))
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.horodatage").isNotEmpty());
    }

    @Test
    void test_11_1_un_jeton_expire_donne_401() throws Exception {
        Compte compte = inscrire();

        mvc.perform(get("/api/briques").header("Authorization", "Bearer " + jetonExpire(compte.id())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }

    @Test
    void test_11_1_un_jeton_falsifie_donne_401() throws Exception {
        Compte compte = inscrire();
        String jeton = compte.jeton();
        int debutSignature = jeton.lastIndexOf('.') + 1;
        char premier = jeton.charAt(debutSignature);
        String falsifie = jeton.substring(0, debutSignature) + (premier == 'A' ? 'B' : 'A')
                + jeton.substring(debutSignature + 1);

        mvc.perform(get("/api/moi").header("Authorization", "Bearer " + falsifie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }

    @Test
    void test_RG17_le_jeton_d_un_compte_supprime_est_refuse_en_401() throws Exception {
        Compte compte = inscrire();
        mvc.perform(avec(delete("/api/moi"), compte).contentType(JSON)
                        .content("{\"motDePasse\":\"" + MOT_DE_PASSE + "\"}"))
                .andExpect(status().isNoContent());

        // Avant ce contrôle : 409 TITRE_DEJA_UTILISE trompeur (brique) ou 500 (avis, test, signalement).
        mvc.perform(avec(post("/api/briques"), compte).contentType(JSON)
                        .content("{\"titre\":\"Brique fantôme\",\"resume\":\"Résumé de test pour la brique\","
                                + "\"code\":\"x\",\"modeEmploi\":\"Mode d'emploi de test\",\"licence\":\"MIT\","
                                + "\"idCategorie\":1,\"idLangage\":1,\"droitsConfirmes\":true}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
        mvc.perform(avec(get("/api/briques"), compte))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_section4_une_route_d_administration_est_refusee_aux_visiteurs_et_aux_developpeurs() throws Exception {
        mvc.perform(get("/api/admin/statistiques"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));

        mvc.perform(avec(get("/api/admin/statistiques"), inscrire()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statut").value(403))
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"))
                .andExpect(jsonPath("$.horodatage").isNotEmpty());
    }

    @Test
    void test_section4_une_action_d_ecriture_exige_un_compte_connecte() throws Exception {
        mvc.perform(post("/api/briques").contentType(JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/demandes").contentType(JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/reponses/1"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/moi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_11_7_un_identifiant_non_numerique_donne_400() throws Exception {
        mvc.perform(get("/api/briques/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_11_7_une_ressource_inconnue_donne_404() throws Exception {
        mvc.perform(get("/api/briques/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
    }

    @Test
    void test_11_7_un_corps_json_illisible_donne_400() throws Exception {
        mvc.perform(post("/api/auth/connexion").contentType(JSON).content("{pas du json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void test_11_7_un_champ_invalide_donne_400_avec_le_detail_du_champ() throws Exception {
        mvc.perform(post("/api/auth/inscription").contentType(JSON)
                        .content("{\"pseudo\":\"ab\",\"motDePasse\":\"court\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void test_11_3_inscription_et_connexion_sont_publiques() throws Exception {
        Compte compte = inscrire();

        mvc.perform(post("/api/auth/connexion").contentType(JSON)
                        .content("{\"pseudo\":\"" + compte.pseudo() + "\",\"motDePasse\":\"" + MOT_DE_PASSE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jeton").isNotEmpty());
    }
}

package sn.devbrik.backend.avis.api;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Socle des tests de bout en bout. Seul le contrat d'API (section 11) est utilisé.
 * Les écritures SQL directes sont réservées aux tests ; tout est annulé à la fin de chaque test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class ApiTestSupport {

    protected static final MediaType JSON = MediaType.APPLICATION_JSON;
    protected static final String MOT_DE_PASSE = "MotDePasse123";

    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;
    @PersistenceContext protected EntityManager em;

    protected record Compte(long id, String pseudo, String jeton) {}

    protected record Brique(long id, String titre) {}

    // ---------- comptes ----------

    protected Compte inscrire() throws Exception {
        String pseudo = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String corps = mvc.perform(post("/api/auth/inscription").contentType(JSON)
                        .content("{\"pseudo\":\"" + pseudo + "\",\"motDePasse\":\"" + MOT_DE_PASSE + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new Compte(((Number) JsonPath.read(corps, "$.utilisateur.id")).longValue(), pseudo,
                JsonPath.read(corps, "$.jeton"));
    }

    protected Compte administrateur() throws Exception {
        Compte c = inscrire();
        jdbc.update("UPDATE utilisateur SET role = 'ADMIN' WHERE id_utilisateur = ?", c.id());
        // Le test partage sa transaction (donc son cache Hibernate) avec MockMvc : sans ceci, la connexion
        // relit l'entité en cache (rôle DEVELOPPEUR) et le jeton émis n'est pas celui d'un administrateur.
        em.flush();
        em.clear();
        String corps = mvc.perform(post("/api/auth/connexion").contentType(JSON)
                        .content("{\"pseudo\":\"" + c.pseudo() + "\",\"motDePasse\":\"" + MOT_DE_PASSE + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new Compte(c.id(), c.pseudo(), JsonPath.read(corps, "$.jeton"));
    }

    /** Ajoute le jeton ; compte null = visiteur. */
    protected static MockHttpServletRequestBuilder avec(MockHttpServletRequestBuilder requete, Compte compte) {
        return compte == null ? requete : requete.header("Authorization", "Bearer " + compte.jeton());
    }

    // ---------- briques (par l'API) ----------

    private static String corpsBrique(String titre, String code, long categorie, long langage) {
        return """
                {"titre":"%s","resume":"Résumé de test pour la brique","code":"%s",
                 "modeEmploi":"Mode d'emploi de test","prerequis":null,"licence":"MIT",
                 "idCategorie":%d,"idLangage":%d,"droitsConfirmes":true}
                """.formatted(titre, code, categorie, langage);
    }

    protected Brique creerBrique(Compte auteur) throws Exception {
        String titre = "Brique " + UUID.randomUUID();
        long categorie = jdbc.queryForObject("SELECT id_categorie FROM categorie ORDER BY id_categorie LIMIT 1", Long.class);
        long langage = jdbc.queryForObject("SELECT id_langage FROM langage ORDER BY id_langage LIMIT 1", Long.class);
        String corps = mvc.perform(avec(post("/api/briques"), auteur).contentType(JSON)
                        .content(corpsBrique(titre, "code v1", categorie, langage)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new Brique(((Number) JsonPath.read(corps, "$.id")).longValue(), titre);
    }

    /** Modifie le code (RG6 : la version augmente). */
    protected void modifierCode(Compte auteur, Brique brique, String nouveauCode) throws Exception {
        long categorie = jdbc.queryForObject("SELECT id_categorie FROM categorie ORDER BY id_categorie LIMIT 1", Long.class);
        long langage = jdbc.queryForObject("SELECT id_langage FROM langage ORDER BY id_langage LIMIT 1", Long.class);
        mvc.perform(avec(put("/api/briques/{id}", brique.id()), auteur).contentType(JSON)
                        .content(corpsBrique(brique.titre(), nouveauCode, categorie, langage)))
                .andExpect(status().isOk());
    }

    protected void masquer(Compte admin, Brique brique) throws Exception {
        mvc.perform(avec(put("/api/admin/briques/{id}/statut", brique.id()), admin).contentType(JSON)
                        .content("{\"statut\":\"MASQUEE\"}"))
                .andExpect(status().isOk());
    }

    // ---------- briques (SQL, pour les quotas) ----------

    protected List<Long> briquesSql(int nombre, long idAuteur) {
        long categorie = jdbc.queryForObject("SELECT id_categorie FROM categorie ORDER BY id_categorie LIMIT 1", Long.class);
        long langage = jdbc.queryForObject("SELECT id_langage FROM langage ORDER BY id_langage LIMIT 1", Long.class);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < nombre; i++) {
            ids.add(jdbc.queryForObject("""
                    INSERT INTO brique (titre, resume, code, mode_emploi, licence, id_categorie, id_langage, id_auteur)
                    VALUES (?, 'Résumé de test pour la brique', 'x', 'Mode d''emploi de test', 'MIT', ?, ?, ?)
                    RETURNING id_brique
                    """, Long.class, "Brique SQL " + UUID.randomUUID(), categorie, langage, idAuteur));
        }
        return ids;
    }
}
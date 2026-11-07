package sn.devbrik.backend.auth;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests HTTP réels (serveur démarré sur un port aléatoire) sur la base devbrik_test.
 *
 * - N'utilise que le JDK pour appeler l'API : aucune dépendance de test ajoutée.
 * - Pas de rollback possible (le serveur tourne dans un autre fil) : chaque test crée des comptes
 *   « it.xxx » et ils sont tous supprimés à la fin (la cascade de V1 supprime leur contenu).
 * - Couvre : lignes d'autorisation de la section 4 concernant les comptes (visiteur, développeur, administrateur),
 *   RG1, RG2, RG17, RG19, absence de mot de passe dans les réponses et dans les journaux.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiComptesHttpIntegrationTest {

    private static final String MDP = "Secret-Test-9x!";
    private static final String MDP2 = "Autre-Secret-7y?";
    private static final Pattern JETON = Pattern.compile("\"jeton\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CODE = Pattern.compile("\"code\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern MESSAGE = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"");

    record Reponse(int statut, String corps, String contentDisposition) {
        String code() {
            return extraire(CODE, corps);
        }

        String jeton() {
            return extraire(JETON, corps);
        }

        String message() {
            return extraire(MESSAGE, corps);
        }
    }

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder encodeur;
    @Autowired
    private Environment env;

    private final HttpClient client = HttpClient.newHttpClient();
    /** Toutes les réponses reçues, pour vérifier qu'aucun secret n'y figure. */
    private final List<String> reponsesRecues = new ArrayList<>();

    @AfterEach
    void nettoyer() {
        jdbc.update("DELETE FROM utilisateur WHERE pseudo LIKE 'it.%'");
    }

    // ---------------------------------------------------------------- outils

    private static String extraire(Pattern p, String texte) {
        Matcher m = p.matcher(texte);
        return m.find() ? m.group(1) : null;
    }

    private static String json(String... paires) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < paires.length; i += 2) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(paires[i]).append("\":");
            sb.append(paires[i + 1] == null ? "null" : "\"" + paires[i + 1] + "\"");
        }
        return sb.append('}').toString();
    }

    private static String pseudoUnique(String prefixe) {
        return "it." + prefixe + "." + Long.toString(System.nanoTime(), 36);
    }

    private Reponse appeler(String methode, String chemin, String jeton, String corps) throws Exception {
        String port = env.getProperty("local.server.port");
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + chemin));
        if (jeton != null) {
            b.header("Authorization", "Bearer " + jeton);
        }
        if (corps != null) {
            b.header("Content-Type", "application/json");
        }
        b.method(methode, corps == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corps, StandardCharsets.UTF_8));
        HttpResponse<String> r = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        reponsesRecues.add(r.body());
        return new Reponse(r.statusCode(), r.body(), r.headers().firstValue("Content-Disposition").orElse(""));
    }

    private String inscrire(String pseudo) throws Exception {
        Reponse r = appeler("POST", "/api/auth/inscription", null, json("pseudo", pseudo, "motDePasse", MDP));
        assertEquals(201, r.statut(), r.corps());
        return r.jeton();
    }

    private String connecter(String pseudo, String motDePasse) throws Exception {
        Reponse r = appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudo, "motDePasse", motDePasse));
        assertEquals(200, r.statut(), r.corps());
        return r.jeton();
    }

    /** Insère un administrateur directement en base (aucune route ne permet d'en créer) puis se connecte. */
    private String creerAdminEtSeConnecter(String pseudo) throws Exception {
        jdbc.update("INSERT INTO utilisateur (pseudo, mot_de_passe, role) VALUES (?, ?, 'ADMIN')",
                pseudo, encodeur.encode(MDP));
        return connecter(pseudo, MDP);
    }

    private int compter(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    private void creerBrique(String pseudoAuteur, String titre) {
        long idAuteur = jdbc.queryForObject("SELECT id_utilisateur FROM utilisateur WHERE pseudo = ?", Long.class, pseudoAuteur);
        long idCategorie = jdbc.queryForObject("SELECT min(id_categorie) FROM categorie", Long.class);
        long idLangage = jdbc.queryForObject("SELECT min(id_langage) FROM langage", Long.class);
        jdbc.update("""
                INSERT INTO brique (titre, resume, code, mode_emploi, licence, id_categorie, id_langage, id_auteur)
                VALUES (?, 'Résumé de test', 'code', 'Mode d''emploi de test', 'MIT', ?, ?, ?)""",
                titre, idCategorie, idLangage, idAuteur);
    }

    // ------------------------------------------- Section 4 : visiteur (inscription, connexion)

    @Test
    void test_RG1_un_visiteur_peut_s_inscrire_puis_se_connecter() throws Exception {
        String pseudo = pseudoUnique("vis");

        Reponse inscription = appeler("POST", "/api/auth/inscription", null,
                json("pseudo", pseudo, "motDePasse", MDP, "nomAffiche", "Visiteur Test"));
        assertEquals(201, inscription.statut());
        assertTrue(inscription.corps().contains("\"role\":\"DEVELOPPEUR\""));
        assertTrue(inscription.jeton() != null && !inscription.jeton().isBlank());

        Reponse connexion = appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudo, "motDePasse", MDP));
        assertEquals(200, connexion.statut());
        assertTrue(connexion.jeton() != null && !connexion.jeton().isBlank());
    }

    @Test
    void test_RG1_pseudo_reserve_refuse_et_doublon_refuse_sans_tenir_compte_de_la_casse() throws Exception {
        Reponse reserve = appeler("POST", "/api/auth/inscription", null, json("pseudo", "Admin", "motDePasse", MDP));
        assertEquals(400, reserve.statut());
        assertEquals("PSEUDO_RESERVE", reserve.code());

        String pseudo = pseudoUnique("dbl");
        inscrire(pseudo);
        Reponse doublon = appeler("POST", "/api/auth/inscription", null,
                json("pseudo", pseudo.toUpperCase(), "motDePasse", MDP));
        assertEquals(409, doublon.statut());
        assertEquals("PSEUDO_DEJA_UTILISE", doublon.code());
        assertEquals(1, compter("SELECT count(*) FROM utilisateur WHERE lower(pseudo) = lower(?)", pseudo));
    }

    @Test
    void test_RG1_formats_invalides_donnent_400_avec_details() throws Exception {
        String[][] cas = {
                {"ab", MDP},                 // pseudo trop court
                {".commence.par.point", MDP},
                {"avec espace", MDP},
                {pseudoUnique("mdp"), "1234567"}, // mot de passe trop court
        };
        for (String[] c : cas) {
            Reponse r = appeler("POST", "/api/auth/inscription", null, json("pseudo", c[0], "motDePasse", c[1]));
            assertEquals(400, r.statut(), c[0]);
            assertEquals("REQUETE_INVALIDE", r.code(), c[0]);
            assertTrue(r.corps().contains("details"), c[0]);
        }
    }

    @Test
    void test_RG1_l_inscription_ne_permet_jamais_de_devenir_administrateur() throws Exception {
        String pseudo = pseudoUnique("esc");

        Reponse r = appeler("POST", "/api/auth/inscription", null,
                json("pseudo", pseudo, "motDePasse", MDP, "role", "ADMIN"));

        assertEquals(201, r.statut());
        assertEquals("DEVELOPPEUR", jdbc.queryForObject("SELECT role FROM utilisateur WHERE pseudo = ?", String.class, pseudo));
    }

    @Test
    void test_RG2_connexion_refusee_ne_revele_pas_si_le_pseudo_existe() throws Exception {
        String pseudo = pseudoUnique("conn");
        inscrire(pseudo);

        Reponse faux = appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudo, "motDePasse", "Mauvais-123456"));
        Reponse inconnu = appeler("POST", "/api/auth/connexion", null,
                json("pseudo", pseudoUnique("inconnu"), "motDePasse", "Mauvais-123456"));

        assertEquals(401, faux.statut());
        assertEquals(401, inconnu.statut());
        assertEquals("IDENTIFIANTS_INVALIDES", faux.code());
        assertEquals(faux.code(), inconnu.code());
        assertEquals(faux.message(), inconnu.message());
    }

    // ------------------------------------------- Section 4 : routes protégées, visiteur refusé

    @Test
    void test_RG19_toutes_les_routes_du_compte_refusent_un_visiteur() throws Exception {
        String[][] routes = {
                {"GET", "/api/moi", null},
                {"PUT", "/api/moi", "{}"},
                {"PUT", "/api/moi/mot-de-passe", json("ancienMotDePasse", "x", "nouveauMotDePasse", MDP2)},
                {"GET", "/api/moi/export", null},
                {"DELETE", "/api/moi", json("motDePasse", MDP)},
        };
        for (String[] r : routes) {
            Reponse reponse = appeler(r[0], r[1], null, r[2]);
            assertEquals(401, reponse.statut(), r[0] + " " + r[1]);
            assertEquals("NON_AUTHENTIFIE", reponse.code(), r[0] + " " + r[1]);
        }
    }

    @Test
    void test_RG2_jeton_invalide_donne_401_y_compris_sur_une_route_publique() throws Exception {
        Reponse protegee = appeler("GET", "/api/moi", "abc.def.ghi", null);
        assertEquals(401, protegee.statut());
        assertEquals("NON_AUTHENTIFIE", protegee.code());

        Reponse publique = appeler("POST", "/api/auth/connexion", "abc.def.ghi",
                json("pseudo", pseudoUnique("x"), "motDePasse", MDP));
        assertEquals(401, publique.statut());
        assertEquals("NON_AUTHENTIFIE", publique.code());
    }

    @Test
    void test_RG2_jeton_modifie_apres_signature_refuse() throws Exception {
        String jeton = inscrire(pseudoUnique("alt"));
        String altere = jeton.substring(0, jeton.length() - 3) + (jeton.endsWith("AAA") ? "BBB" : "AAA");

        assertEquals(200, appeler("GET", "/api/moi", jeton, null).statut());
        assertEquals(401, appeler("GET", "/api/moi", altere, null).statut());
    }

    // ------------------------------------------- Développeur : son propre compte

    @Test
    void test_RG19_le_developpeur_lit_et_modifie_son_profil_sans_pouvoir_changer_son_role() throws Exception {
        String pseudo = pseudoUnique("prof");
        String jeton = inscrire(pseudo);

        Reponse lu = appeler("GET", "/api/moi", jeton, null);
        assertEquals(200, lu.statut());
        assertTrue(lu.corps().contains("\"pseudo\":\"" + pseudo + "\""));

        Reponse modifie = appeler("PUT", "/api/moi", jeton,
                json("nomAffiche", "Awa Diop", "pays", "Sénégal", "bio", "Dev", "lienProfil", "https://exemple.sn",
                        "role", "ADMIN", "pseudo", "it.pirate"));
        assertEquals(200, modifie.statut());
        assertTrue(modifie.corps().contains("\"pays\":\"Sénégal\""));
        assertEquals("DEVELOPPEUR", jdbc.queryForObject("SELECT role FROM utilisateur WHERE pseudo = ?", String.class, pseudo));
        assertEquals(0, compter("SELECT count(*) FROM utilisateur WHERE pseudo = 'it.pirate'"));

        Reponse lienDangereux = appeler("PUT", "/api/moi", jeton, json("lienProfil", "javascript:alert(1)"));
        assertEquals(400, lienDangereux.statut());
        assertEquals("REQUETE_INVALIDE", lienDangereux.code());
    }

    @Test
    void test_RG19_un_developpeur_ne_modifie_jamais_le_compte_d_un_autre() throws Exception {
        String pseudoA = pseudoUnique("a");
        String pseudoB = pseudoUnique("b");
        String jetonA = inscrire(pseudoA);
        inscrire(pseudoB);

        assertEquals(200, appeler("PUT", "/api/moi", jetonA, json("pays", "Mali")).statut());
        assertEquals(204, appeler("PUT", "/api/moi/mot-de-passe", jetonA,
                json("ancienMotDePasse", MDP, "nouveauMotDePasse", MDP2)).statut());

        assertEquals("Mali", jdbc.queryForObject("SELECT pays FROM utilisateur WHERE pseudo = ?", String.class, pseudoA));
        assertEquals(0, compter("SELECT count(*) FROM utilisateur WHERE pseudo = ? AND pays IS NOT NULL", pseudoB));
        // B se connecte toujours avec son mot de passe d'origine.
        assertEquals(200, appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudoB, "motDePasse", MDP)).statut());
    }

    @Test
    void test_RG2_changement_de_mot_de_passe_via_http() throws Exception {
        String pseudo = pseudoUnique("mdp");
        String jeton = inscrire(pseudo);

        Reponse faux = appeler("PUT", "/api/moi/mot-de-passe", jeton,
                json("ancienMotDePasse", "Mauvais-123456", "nouveauMotDePasse", MDP2));
        assertEquals(400, faux.statut());
        assertEquals("MOT_DE_PASSE_INCORRECT", faux.code());

        Reponse court = appeler("PUT", "/api/moi/mot-de-passe", jeton,
                json("ancienMotDePasse", MDP, "nouveauMotDePasse", "court"));
        assertEquals(400, court.statut());
        assertEquals("REQUETE_INVALIDE", court.code());

        Reponse ok = appeler("PUT", "/api/moi/mot-de-passe", jeton,
                json("ancienMotDePasse", MDP, "nouveauMotDePasse", MDP2));
        assertEquals(204, ok.statut());
        assertEquals("", ok.corps());

        assertEquals(401, appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudo, "motDePasse", MDP)).statut());
        assertEquals(200, appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudo, "motDePasse", MDP2)).statut());
        String hache = jdbc.queryForObject("SELECT mot_de_passe FROM utilisateur WHERE pseudo = ?", String.class, pseudo);
        assertTrue(hache.startsWith("$2"));
        assertNotEquals(MDP2, hache);
    }

    @Test
    void test_RG19_l_export_ne_contient_que_les_donnees_du_titulaire() throws Exception {
        String pseudoA = pseudoUnique("exa");
        String pseudoB = pseudoUnique("exb");
        String jetonA = inscrire(pseudoA);
        inscrire(pseudoB);
        creerBrique(pseudoA, "Brique export A");
        creerBrique(pseudoB, "Brique export B");

        Reponse export = appeler("GET", "/api/moi/export", jetonA, null);

        assertEquals(200, export.statut());
        assertTrue(export.contentDisposition().contains("attachment"));
        assertTrue(export.corps().contains("Brique export A"));
        assertFalse(export.corps().contains("Brique export B"));
        assertFalse(export.corps().contains(pseudoB));
        for (String section : new String[]{"profil", "briques", "sn/devbrik/backend/avis", "tests", "demandes", "reponses", "signalements"}) {
            assertTrue(export.corps().contains("\"" + section + "\""), section);
        }
    }

    // ------------------------------------------- RG17 : suppression

    @Test
    void test_RG17_suppression_du_compte_supprime_aussi_ses_briques_et_pas_celles_des_autres() throws Exception {
        String pseudoA = pseudoUnique("sa");
        String pseudoB = pseudoUnique("sb");
        String jetonA = inscrire(pseudoA);
        String jetonB = inscrire(pseudoB);
        creerBrique(pseudoA, "Brique A");
        creerBrique(pseudoB, "Brique B");

        Reponse mauvais = appeler("DELETE", "/api/moi", jetonA, json("motDePasse", "Mauvais-123456"));
        assertEquals(400, mauvais.statut());
        assertEquals("MOT_DE_PASSE_INCORRECT", mauvais.code());
        assertEquals(1, compter("SELECT count(*) FROM utilisateur WHERE pseudo = ?", pseudoA));

        assertEquals(204, appeler("DELETE", "/api/moi", jetonA, json("motDePasse", MDP)).statut());

        assertEquals(0, compter("SELECT count(*) FROM utilisateur WHERE pseudo = ?", pseudoA));
        assertEquals(0, compter("SELECT count(*) FROM brique WHERE titre = 'Brique A'"));
        assertEquals(1, compter("SELECT count(*) FROM brique WHERE titre = 'Brique B'"));
        assertEquals(401, appeler("GET", "/api/moi", jetonA, null).statut(), "le jeton d'un compte supprimé est refusé");
        assertEquals(200, appeler("GET", "/api/moi", jetonB, null).statut());
        assertEquals(401, appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudoA, "motDePasse", MDP)).statut());
    }

    @Test
    void test_RG17_un_administrateur_ne_peut_pas_supprimer_son_compte() throws Exception {
        String pseudo = pseudoUnique("adm");
        String jeton = creerAdminEtSeConnecter(pseudo);

        Reponse bonMotDePasse = appeler("DELETE", "/api/moi", jeton, json("motDePasse", MDP));
        assertEquals(403, bonMotDePasse.statut());
        assertEquals("SUPPRESSION_ADMIN_INTERDITE", bonMotDePasse.code());

        Reponse mauvaisMotDePasse = appeler("DELETE", "/api/moi", jeton, json("motDePasse", "Mauvais-123456"));
        assertEquals(403, mauvaisMotDePasse.statut());
        assertEquals("SUPPRESSION_ADMIN_INTERDITE", mauvaisMotDePasse.code());

        assertEquals(1, compter("SELECT count(*) FROM utilisateur WHERE pseudo = ?", pseudo));
    }

    // ------------------------------------------- Section 4 : administrateur

    @Test
    void test_RG19_l_administrateur_utilise_les_memes_routes_de_compte_que_le_developpeur() throws Exception {
        String pseudo = pseudoUnique("adm2");
        String jeton = creerAdminEtSeConnecter(pseudo);

        Reponse moi = appeler("GET", "/api/moi", jeton, null);
        assertEquals(200, moi.statut());
        assertTrue(moi.corps().contains("\"role\":\"ADMIN\""));
        assertEquals(200, appeler("PUT", "/api/moi", jeton, json("pays", "Sénégal")).statut());
        assertEquals(200, appeler("GET", "/api/moi/export", jeton, null).statut());
        assertEquals(204, appeler("PUT", "/api/moi/mot-de-passe", jeton,
                json("ancienMotDePasse", MDP, "nouveauMotDePasse", MDP2)).statut());
    }

    // ------------------------------------------- Aucun secret dans les réponses

    @Test
    void test_RG2_aucun_mot_de_passe_ni_hachage_dans_aucune_reponse() throws Exception {
        String pseudo = pseudoUnique("sec");
        String jeton = inscrire(pseudo);
        connecter(pseudo, MDP);
        appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudo, "motDePasse", "Mauvais-123456"));
        appeler("GET", "/api/moi", jeton, null);
        appeler("PUT", "/api/moi", jeton, json("nomAffiche", "Test"));
        appeler("GET", "/api/moi/export", jeton, null);
        appeler("PUT", "/api/moi/mot-de-passe", jeton, json("ancienMotDePasse", "Mauvais-123456", "nouveauMotDePasse", MDP2));
        appeler("POST", "/api/auth/inscription", null, json("pseudo", "x", "motDePasse", MDP));

        assertFalse(reponsesRecues.isEmpty());
        for (String corps : reponsesRecues) {
            String bas = corps.toLowerCase();
            assertFalse(corps.contains(MDP), "mot de passe en clair dans une réponse");
            assertFalse(corps.contains(MDP2), "mot de passe en clair dans une réponse");
            assertFalse(corps.contains("Mauvais-123456"), "mot de passe saisi renvoyé");
            assertFalse(bas.contains("motdepasse\":\""), "champ motDePasse renvoyé");
            assertFalse(bas.contains("$2a$") || bas.contains("$2b$") || bas.contains("$2y$"), "hachage BCrypt dans une réponse");
        }
    }

    // ------------------------------------------- Aucun secret dans les journaux

    /** Capteur Log4j2 : garde le texte de chaque événement (message + exception). */
    static final class CapteurJournal extends AbstractAppender {
        final List<String> lignes = new CopyOnWriteArrayList<>();

        CapteurJournal() {
            super("capteur-secrets", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent evenement) {
            StringBuilder sb = new StringBuilder(evenement.getMessage().getFormattedMessage());
            for (Throwable t = evenement.getThrown(); t != null; t = t.getCause()) {
                sb.append(' ').append(t);
            }
            lignes.add(sb.toString());
        }
    }

    @Test
    void test_RG2_aucun_mot_de_passe_ni_jeton_ni_hachage_dans_les_journaux() throws Exception {
        LoggerContext contexte = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = contexte.getConfiguration();
        Level niveauInitial = LogManager.getRootLogger().getLevel();
        CapteurJournal capteur = new CapteurJournal();
        capteur.start();
        configuration.addAppender(capteur);
        configuration.getRootLogger().addAppender(capteur, null, null);
        Configurator.setRootLevel(Level.DEBUG); // niveau détaillé pour que le test soit strict

        List<String> jetons = new ArrayList<>();
        try {
            String pseudo = pseudoUnique("log");
            String jeton = inscrire(pseudo);
            jetons.add(jeton);
            jetons.add(connecter(pseudo, MDP));
            appeler("POST", "/api/auth/connexion", null, json("pseudo", pseudo, "motDePasse", "Mauvais-123456"));
            appeler("POST", "/api/auth/inscription", null, json("pseudo", pseudo, "motDePasse", MDP));
            appeler("GET", "/api/moi", "abc.def.ghi", null);
            appeler("GET", "/api/moi", jeton, null);
            appeler("PUT", "/api/moi", jeton, json("nomAffiche", "Test"));
            appeler("PUT", "/api/moi/mot-de-passe", jeton, json("ancienMotDePasse", "Mauvais-123456", "nouveauMotDePasse", MDP2));
            appeler("PUT", "/api/moi/mot-de-passe", jeton, json("ancienMotDePasse", MDP, "nouveauMotDePasse", MDP2));
            appeler("GET", "/api/moi/export", jeton, null);
            appeler("DELETE", "/api/moi", jeton, json("motDePasse", "Mauvais-123456"));
            appeler("DELETE", "/api/moi", jeton, json("motDePasse", MDP2));
        } finally {
            Configurator.setRootLevel(niveauInitial);
            configuration.getRootLogger().removeAppender(capteur.getName());
            capteur.stop();
            contexte.updateLoggers();
        }

        assertFalse(capteur.lignes.isEmpty(), "le capteur n'a rien reçu : vérifier que Log4j2 est bien le moteur de journaux");
        for (String ligne : capteur.lignes) {
            for (String secret : new String[]{MDP, MDP2, "Mauvais-123456"}) {
                assertFalse(ligne.contains(secret), "mot de passe dans un journal : " + ligne);
            }
            for (String jeton : jetons) {
                assertFalse(ligne.contains(jeton), "jeton dans un journal : " + ligne);
            }
            assertFalse(ligne.contains("$2a$") || ligne.contains("$2b$") || ligne.contains("$2y$"),
                    "hachage BCrypt dans un journal : " + ligne);
        }
    }
}

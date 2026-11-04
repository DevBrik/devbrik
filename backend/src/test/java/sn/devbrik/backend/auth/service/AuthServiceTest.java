package sn.devbrik.backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import sn.devbrik.backend.auth.dto.ConnexionRequete;
import sn.devbrik.backend.auth.dto.InscriptionRequete;
import sn.devbrik.backend.auth.dto.ReponseAuth;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.config.JwtService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String MOT_DE_PASSE = "MotDePasse123";

    @Mock
    private UtilisateurRepository depot;
    @Mock
    private JwtService jwtService;

    /** Vrai BCrypt (coût 4 pour la vitesse des tests). */
    private final PasswordEncoder encodeur = new BCryptPasswordEncoder(4);

    private AuthService service;

    @BeforeEach
    void preparer() {
        service = new AuthService(depot, encodeur, jwtService);
    }

    private static String code(Executable action) {
        return assertThrows(ApiException.class, action).getCode().name();
    }

    private void simulerEnregistrement() {
        when(depot.saveAndFlush(any(Utilisateur.class))).thenAnswer(appel -> {
            Utilisateur u = appel.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 7L);
            ReflectionTestUtils.setField(u, "dateCreation", java.time.Instant.now());
            return u;
        });
        when(jwtService.generer(anyLong(), anyString())).thenReturn("jeton-de-test");
    }

    private Utilisateur compteExistant(String pseudo) {
        Utilisateur u = new Utilisateur(pseudo, null, encodeur.encode(MOT_DE_PASSE), Role.DEVELOPPEUR);
        ReflectionTestUtils.setField(u, "id", 3L);
        return u;
    }

    // ---------- RG1 ----------

    @Test
    void test_RG1_inscription_valide_cree_un_developpeur_et_renvoie_un_jeton() {
        simulerEnregistrement();

        ReponseAuth reponse = service.inscrire(new InscriptionRequete("awa.dev", MOT_DE_PASSE, "Awa Diop"));

        assertEquals("jeton-de-test", reponse.jeton());
        assertEquals("awa.dev", reponse.utilisateur().pseudo());
        assertEquals(Role.DEVELOPPEUR, reponse.utilisateur().role());
        verify(jwtService).generer(7L, "DEVELOPPEUR");
    }

    @Test
    void test_RG1_pseudo_reserve_refuse_quelle_que_soit_la_casse() {
        for (String pseudo : new String[]{"admin", "ADMIN", "Administrateur", "root", "Support",
                "moderateur", "DevBrik", "equipe", "SYSTALINK", "datacloud", "administrator"}) {
            assertEquals("PSEUDO_RESERVE",
                    code(() -> service.inscrire(new InscriptionRequete(pseudo, MOT_DE_PASSE, null))), pseudo);
        }
        verify(depot, never()).saveAndFlush(any());
    }

    @Test
    void test_RG1_pseudo_deja_utilise_sans_tenir_compte_de_la_casse() {
        when(depot.existsByPseudoIgnoreCase("AWA.DEV")).thenReturn(true);

        assertEquals("PSEUDO_DEJA_UTILISE",
                code(() -> service.inscrire(new InscriptionRequete("AWA.DEV", MOT_DE_PASSE, null))));
        verify(depot, never()).saveAndFlush(any());
    }

    @Test
    void test_RG1_course_entre_deux_inscriptions_rattrapee_par_l_index_unique() {
        when(depot.saveAndFlush(any(Utilisateur.class)))
                .thenThrow(new DataIntegrityViolationException("uk_utilisateur_pseudo"));

        assertEquals("PSEUDO_DEJA_UTILISE",
                code(() -> service.inscrire(new InscriptionRequete("moussa", MOT_DE_PASSE, null))));
    }

    @Test
    void test_RG1_le_role_est_toujours_developpeur_a_l_inscription() {
        simulerEnregistrement();
        service.inscrire(new InscriptionRequete("fatou", MOT_DE_PASSE, null));

        ArgumentCaptor<Utilisateur> capture = ArgumentCaptor.forClass(Utilisateur.class);
        verify(depot).saveAndFlush(capture.capture());
        assertEquals(Role.DEVELOPPEUR, capture.getValue().getRole());
    }

    // ---------- RG2 ----------

    @Test
    void test_RG2_le_mot_de_passe_est_hache_avec_bcrypt_et_jamais_stocke_en_clair() {
        simulerEnregistrement();
        service.inscrire(new InscriptionRequete("fatou", MOT_DE_PASSE, null));

        ArgumentCaptor<Utilisateur> capture = ArgumentCaptor.forClass(Utilisateur.class);
        verify(depot).saveAndFlush(capture.capture());
        String hache = capture.getValue().getMotDePasse();
        assertNotEquals(MOT_DE_PASSE, hache);
        assertTrue(hache.startsWith("$2"), "doit être un hachage BCrypt");
        assertTrue(encodeur.matches(MOT_DE_PASSE, hache));
    }

    @Test
    void test_RG2_mot_de_passe_de_plus_de_72_octets_refuse_meme_sous_72_caracteres() {
        String accentue = "é".repeat(40); // 40 caractères, 80 octets
        assertEquals("REQUETE_INVALIDE",
                code(() -> service.inscrire(new InscriptionRequete("fatou", accentue, null))));
    }

    @Test
    void test_RG2_mot_de_passe_de_72_octets_accepte() {
        simulerEnregistrement();
        String limite = "a".repeat(72);
        assertEquals("fatou", service.inscrire(new InscriptionRequete("fatou", limite, null)).utilisateur().pseudo());
    }

    @Test
    void test_RG2_connexion_valide_renvoie_un_jeton() {
        when(depot.findByPseudoIgnoreCase("awa.dev")).thenReturn(Optional.of(compteExistant("awa.dev")));
        when(jwtService.generer(3L, "DEVELOPPEUR")).thenReturn("jeton-de-test");

        ReponseAuth reponse = service.connecter(new ConnexionRequete("awa.dev", MOT_DE_PASSE));

        assertEquals("jeton-de-test", reponse.jeton());
        assertEquals(3L, reponse.utilisateur().id());
    }

    @Test
    void test_RG2_connexion_pseudo_insensible_a_la_casse() {
        when(depot.findByPseudoIgnoreCase("AWA.DEV")).thenReturn(Optional.of(compteExistant("awa.dev")));
        when(jwtService.generer(3L, "DEVELOPPEUR")).thenReturn("jeton-de-test");

        assertEquals("awa.dev", service.connecter(new ConnexionRequete("AWA.DEV", MOT_DE_PASSE)).utilisateur().pseudo());
    }

    @Test
    void test_RG2_connexion_mot_de_passe_faux_donne_identifiants_invalides() {
        when(depot.findByPseudoIgnoreCase("awa.dev")).thenReturn(Optional.of(compteExistant("awa.dev")));

        assertEquals("IDENTIFIANTS_INVALIDES",
                code(() -> service.connecter(new ConnexionRequete("awa.dev", "MauvaisMotDePasse"))));
        verify(jwtService, never()).generer(anyLong(), anyString());
    }

    @Test
    void test_RG2_connexion_ne_revele_pas_si_le_pseudo_existe() {
        when(depot.findByPseudoIgnoreCase("awa.dev")).thenReturn(Optional.of(compteExistant("awa.dev")));
        when(depot.findByPseudoIgnoreCase("inconnu")).thenReturn(Optional.empty());

        ApiException pseudoConnu = assertThrows(ApiException.class,
                () -> service.connecter(new ConnexionRequete("awa.dev", "MauvaisMotDePasse")));
        ApiException pseudoInconnu = assertThrows(ApiException.class,
                () -> service.connecter(new ConnexionRequete("inconnu", MOT_DE_PASSE)));

        assertEquals(pseudoConnu.getCode(), pseudoInconnu.getCode());
        assertEquals(pseudoConnu.getMessage(), pseudoInconnu.getMessage());
        assertEquals("IDENTIFIANTS_INVALIDES", pseudoInconnu.getCode().name());
    }

    @Test
    void test_RG2_connexion_avec_un_mot_de_passe_demesure_donne_identifiants_invalides() {
        when(depot.findByPseudoIgnoreCase("awa.dev")).thenReturn(Optional.of(compteExistant("awa.dev")));

        assertEquals("IDENTIFIANTS_INVALIDES",
                code(() -> service.connecter(new ConnexionRequete("awa.dev", "é".repeat(100)))));
    }

    @Test
    void test_RG2_aucun_mot_de_passe_ni_jeton_dans_les_toString() {
        simulerEnregistrement();
        InscriptionRequete requete = new InscriptionRequete("fatou", MOT_DE_PASSE, null);
        ReponseAuth reponse = service.inscrire(requete);

        assertFalse(requete.toString().contains(MOT_DE_PASSE));
        assertFalse(new ConnexionRequete("fatou", MOT_DE_PASSE).toString().contains(MOT_DE_PASSE));
        assertFalse(reponse.toString().contains("jeton-de-test"));
        assertFalse(reponse.toString().contains("$2"));
    }
}

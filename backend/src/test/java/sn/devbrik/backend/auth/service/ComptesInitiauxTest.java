package sn.devbrik.backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;
import sn.devbrik.backend.auth.service.ComptesInitiaux.Resultat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComptesInitiauxTest {

    private static final String MDP = "AdminMotDePasse123";

    @Mock
    private UtilisateurRepository depot;

    private final PasswordEncoder encodeur = new BCryptPasswordEncoder(4);
    private ComptesInitiaux service;

    @BeforeEach
    void preparer() {
        service = new ComptesInitiaux(depot, encodeur);
    }

    // ---------- Administrateur ----------

    @Test
    void test_RG2_admin_cree_avec_le_role_ADMIN_et_un_hachage_bcrypt() {
        when(depot.existsByRole(Role.ADMIN)).thenReturn(false);
        when(depot.existsByPseudoIgnoreCase("admin.demo")).thenReturn(false);

        assertEquals(Resultat.CREE, service.creerAdministrateurSiAbsent("admin.demo", MDP));

        ArgumentCaptor<Utilisateur> capture = ArgumentCaptor.forClass(Utilisateur.class);
        verify(depot).saveAndFlush(capture.capture());
        Utilisateur u = capture.getValue();
        assertEquals(Role.ADMIN, u.getRole());
        assertEquals("admin.demo", u.getPseudo());
        assertTrue(u.getMotDePasse().startsWith("$2"));
        assertTrue(encodeur.matches(MDP, u.getMotDePasse()));
        assertFalse(u.getMotDePasse().contains(MDP));
    }

    @Test
    void admin_rogne_le_pseudo_venant_de_l_environnement() {
        when(depot.existsByRole(Role.ADMIN)).thenReturn(false);
        when(depot.existsByPseudoIgnoreCase("admin.demo")).thenReturn(false);

        assertEquals(Resultat.CREE, service.creerAdministrateurSiAbsent("  admin.demo \n", MDP));
    }

    @Test
    void admin_aucune_creation_si_un_administrateur_existe_deja() {
        when(depot.existsByRole(Role.ADMIN)).thenReturn(true);

        assertEquals(Resultat.ADMIN_EXISTANT, service.creerAdministrateurSiAbsent("admin.demo", MDP));
        verify(depot, never()).saveAndFlush(any());
    }

    @Test
    void admin_pseudo_pris_par_un_developpeur_n_est_jamais_promu() {
        when(depot.existsByRole(Role.ADMIN)).thenReturn(false);
        when(depot.existsByPseudoIgnoreCase("awa.dev")).thenReturn(true);

        assertEquals(Resultat.PSEUDO_PRIS, service.creerAdministrateurSiAbsent("awa.dev", MDP));
        verify(depot, never()).saveAndFlush(any());
    }

    @Test
    void admin_peut_porter_un_pseudo_reserve_comme_admin() {
        when(depot.existsByRole(Role.ADMIN)).thenReturn(false);
        when(depot.existsByPseudoIgnoreCase("admin")).thenReturn(false);

        assertEquals(Resultat.CREE, service.creerAdministrateurSiAbsent("admin", MDP));
    }

    @Test
    void admin_course_au_demarrage_rattrapee_par_l_index_unique() {
        when(depot.existsByRole(Role.ADMIN)).thenReturn(false);
        when(depot.existsByPseudoIgnoreCase("admin.demo")).thenReturn(false);
        when(depot.saveAndFlush(any(Utilisateur.class))).thenThrow(new DataIntegrityViolationException("uk"));

        assertEquals(Resultat.PSEUDO_PRIS, service.creerAdministrateurSiAbsent("admin.demo", MDP));
    }

    @Test
    void test_RG1_admin_pseudo_invalide_refuse_sans_afficher_le_mot_de_passe() {
        for (String pseudo : new String[]{null, "", "ab", ".admin", "admin demo", "admin@demo", "A".repeat(31)}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> service.creerAdministrateurSiAbsent(pseudo, MDP), "[" + pseudo + "]");
            assertFalse(e.getMessage().contains(MDP));
        }
        verifyNoInteractions(depot);
    }

    @Test
    void test_RG2_admin_mot_de_passe_invalide_refuse_sans_l_afficher() {
        for (String mdp : new String[]{null, "", "1234567", "a".repeat(73), "é".repeat(40)}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> service.creerAdministrateurSiAbsent("admin.demo", mdp));
            if (mdp != null && !mdp.isEmpty()) {
                assertFalse(e.getMessage().contains(mdp));
            }
        }
        verifyNoInteractions(depot);
    }

    // ---------- Développeurs de démonstration ----------

    @Test
    void developpeur_demo_cree_avec_profil_et_role_DEVELOPPEUR() {
        when(depot.existsByPseudoIgnoreCase("awa.dev")).thenReturn(false);

        assertEquals(Resultat.CREE,
                service.creerDeveloppeurSiAbsent("awa.dev", "Awa Diop", "Sénégal", "Bio", MDP));

        ArgumentCaptor<Utilisateur> capture = ArgumentCaptor.forClass(Utilisateur.class);
        verify(depot).saveAndFlush(capture.capture());
        Utilisateur u = capture.getValue();
        assertEquals(Role.DEVELOPPEUR, u.getRole());
        assertEquals("Awa Diop", u.getNomAffiche());
        assertEquals("Sénégal", u.getPays());
        assertEquals("Bio", u.getBio());
        assertTrue(encodeur.matches(MDP, u.getMotDePasse()));
    }

    @Test
    void developpeur_demo_existant_n_est_pas_modifie() {
        when(depot.existsByPseudoIgnoreCase("awa.dev")).thenReturn(true);

        assertEquals(Resultat.DEJA_PRESENT,
                service.creerDeveloppeurSiAbsent("awa.dev", "Awa Diop", "Sénégal", "Bio", MDP));
        verify(depot, never()).saveAndFlush(any());
    }
}

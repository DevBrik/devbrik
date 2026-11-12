package sn.devbrik.backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import sn.devbrik.backend.auth.dto.ChangementMotDePasseRequete;
import sn.devbrik.backend.auth.dto.SuppressionRequete;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.demande.service.DemandeService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompteServiceTest {

    private static final String ANCIEN = "AncienMotDePasse1";
    private static final String NOUVEAU = "NouveauMotDePasse2";

    @Mock
    private UtilisateurRepository depot;
    @Mock
    private DemandeService demandes;

    private final PasswordEncoder encodeur = new BCryptPasswordEncoder(4);
    private CompteService service;

    @BeforeEach
    void preparer() {
        service = new CompteService(depot, encodeur, demandes);
    }

    private Utilisateur compte(long id, Role role) {
        Utilisateur u = new Utilisateur("awa.dev", null, encodeur.encode(ANCIEN), role);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private static String code(Executable action) {
        return assertThrows(ApiException.class, action).getCode().name();
    }

    @Test
    void test_RG2_changement_valide_remplace_le_hachage() {
        Utilisateur u = compte(5L, Role.DEVELOPPEUR);
        String ancienHache = u.getMotDePasse();
        when(depot.findById(5L)).thenReturn(Optional.of(u));

        service.changerMotDePasse(5L, new ChangementMotDePasseRequete(ANCIEN, NOUVEAU));

        assertTrue(encodeur.matches(NOUVEAU, u.getMotDePasse()));
        assertFalse(encodeur.matches(ANCIEN, u.getMotDePasse()));
        assertTrue(u.getMotDePasse().startsWith("$2"), "BCrypt");
        assertFalse(u.getMotDePasse().equals(ancienHache));
        verify(depot).save(u);
    }

    @Test
    void test_RG2_ancien_mot_de_passe_incorrect_refuse_et_ne_change_rien() {
        Utilisateur u = compte(5L, Role.DEVELOPPEUR);
        String hache = u.getMotDePasse();
        when(depot.findById(5L)).thenReturn(Optional.of(u));

        assertEquals("MOT_DE_PASSE_INCORRECT",
                code(() -> service.changerMotDePasse(5L, new ChangementMotDePasseRequete("Faux123456", NOUVEAU))));
        assertEquals(hache, u.getMotDePasse());
        verify(depot, never()).save(any());
    }

    @Test
    void test_RG2_nouveau_mot_de_passe_de_plus_de_72_octets_refuse() {
        when(depot.findById(5L)).thenReturn(Optional.of(compte(5L, Role.DEVELOPPEUR)));

        assertEquals("REQUETE_INVALIDE",
                code(() -> service.changerMotDePasse(5L, new ChangementMotDePasseRequete(ANCIEN, "é".repeat(40)))));
        verify(depot, never()).save(any());
    }

    @Test
    void test_RG2_compte_supprime_donne_non_authentifie() {
        when(depot.findById(99L)).thenReturn(Optional.empty());

        assertEquals("NON_AUTHENTIFIE",
                code(() -> service.changerMotDePasse(99L, new ChangementMotDePasseRequete(ANCIEN, NOUVEAU))));
    }

    @Test
    void test_RG17_un_developpeur_supprime_son_compte_avec_son_mot_de_passe() {
        Utilisateur u = compte(5L, Role.DEVELOPPEUR);
        when(depot.findById(5L)).thenReturn(Optional.of(u));

        service.supprimer(5L, new SuppressionRequete(ANCIEN));

        verify(depot).delete(u);
        verify(depot).flush();
        // RG12 : les demandes privées de leur réponse acceptée par la cascade sont rouvertes.
        verify(demandes).rouvrirDemandesSansReponseAcceptee();
    }

    @Test
    void test_RG17_mot_de_passe_de_confirmation_incorrect_refuse() {
        when(depot.findById(5L)).thenReturn(Optional.of(compte(5L, Role.DEVELOPPEUR)));

        assertEquals("MOT_DE_PASSE_INCORRECT", code(() -> service.supprimer(5L, new SuppressionRequete("Faux123456"))));
        verify(depot, never()).delete(any());
    }

    @Test
    void test_RG17_mot_de_passe_demesure_refuse_sans_erreur_interne() {
        when(depot.findById(5L)).thenReturn(Optional.of(compte(5L, Role.DEVELOPPEUR)));

        assertEquals("MOT_DE_PASSE_INCORRECT", code(() -> service.supprimer(5L, new SuppressionRequete("é".repeat(100)))));
        verify(depot, never()).delete(any());
    }

    @Test
    void test_RG17_un_administrateur_ne_peut_pas_supprimer_son_compte() {
        when(depot.findById(1L)).thenReturn(Optional.of(compte(1L, Role.ADMIN)));

        assertEquals("SUPPRESSION_ADMIN_INTERDITE", code(() -> service.supprimer(1L, new SuppressionRequete(ANCIEN))));
        verify(depot, never()).delete(any());
    }

    @Test
    void test_RG17_l_administrateur_est_refuse_meme_avec_un_mauvais_mot_de_passe() {
        when(depot.findById(1L)).thenReturn(Optional.of(compte(1L, Role.ADMIN)));

        assertEquals("SUPPRESSION_ADMIN_INTERDITE", code(() -> service.supprimer(1L, new SuppressionRequete("Faux123456"))));
    }

    @Test
    void test_RG17_compte_deja_supprime_donne_non_authentifie() {
        when(depot.findById(99L)).thenReturn(Optional.empty());

        assertEquals("NON_AUTHENTIFIE", code(() -> service.supprimer(99L, new SuppressionRequete(ANCIEN))));
    }

    @Test
    void test_RG2_aucun_mot_de_passe_dans_les_toString() {
        assertFalse(new ChangementMotDePasseRequete(ANCIEN, NOUVEAU).toString().contains(ANCIEN));
        assertFalse(new ChangementMotDePasseRequete(ANCIEN, NOUVEAU).toString().contains(NOUVEAU));
        assertFalse(new SuppressionRequete(ANCIEN).toString().contains(ANCIEN));
    }
}

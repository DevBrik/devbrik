package sn.devbrik.backend.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.devbrik.backend.auth.service.ComptesInitiaux.Resultat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoInitialiseurTest {

    private static final String MDP_DEMO = "DemoMotDePasse456";
    private static final String MDP_ADMIN = "AdminMotDePasse123";

    @Mock
    private ComptesInitiaux comptes;

    @Test
    void sans_variable_aucun_compte_de_demonstration() {
        new DemoInitialiseur("", MDP_ADMIN, comptes).initialiser();
        new DemoInitialiseur(null, MDP_ADMIN, comptes).initialiser();
        verifyNoInteractions(comptes);
    }

    @Test
    void avec_variable_cree_les_deux_developpeurs() {
        when(comptes.creerDeveloppeurSiAbsent(anyString(), anyString(), anyString(), anyString(), eq(MDP_DEMO)))
                .thenReturn(Resultat.CREE);

        new DemoInitialiseur(MDP_DEMO, MDP_ADMIN, comptes).initialiser();

        verify(comptes).creerDeveloppeurSiAbsent(eq("fleury"), eq("Fleury Ba"), anyString(), anyString(), eq(MDP_DEMO));
        verify(comptes).creerDeveloppeurSiAbsent(eq("bousso"), eq("Bousso Diémé"), anyString(), anyString(),
                eq(MDP_DEMO));
    }

    @Test
    void S12_mot_de_passe_demo_identique_a_celui_de_l_admin_refuse() {
        new DemoInitialiseur(MDP_ADMIN, MDP_ADMIN, comptes).initialiser();
        verifyNoInteractions(comptes);
    }

    @Test
    void configuration_invalide_ne_bloque_pas_le_demarrage() {
        when(comptes.creerDeveloppeurSiAbsent(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Mot de passe invalide : 8 à 72 caractères."));

        assertDoesNotThrow(() -> new DemoInitialiseur("court", MDP_ADMIN, comptes).initialiser());
    }
}

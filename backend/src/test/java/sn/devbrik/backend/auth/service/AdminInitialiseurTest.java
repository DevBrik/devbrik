package sn.devbrik.backend.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.devbrik.backend.auth.service.ComptesInitiaux.Resultat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInitialiseurTest {

    private static final String MDP = "AdminMotDePasse123";

    @Mock
    private ComptesInitiaux comptes;

    @Test
    void variables_absentes_aucune_creation() {
        new AdminInitialiseur("", MDP, comptes).initialiser();
        new AdminInitialiseur("  ", MDP, comptes).initialiser();
        new AdminInitialiseur("admin.demo", "", comptes).initialiser();
        new AdminInitialiseur(null, null, comptes).initialiser();
        verifyNoInteractions(comptes);
    }

    @Test
    void variables_presentes_demande_la_creation() {
        when(comptes.creerAdministrateurSiAbsent("admin.demo", MDP)).thenReturn(Resultat.CREE);

        new AdminInitialiseur("admin.demo", MDP, comptes).initialiser();

        verify(comptes).creerAdministrateurSiAbsent("admin.demo", MDP);
    }

    @Test
    void administrateur_existant_ne_fait_pas_echouer_le_demarrage() {
        when(comptes.creerAdministrateurSiAbsent(anyString(), anyString())).thenReturn(Resultat.ADMIN_EXISTANT);
        assertDoesNotThrow(() -> new AdminInitialiseur("admin.demo", MDP, comptes).initialiser());
    }

    @Test
    void pseudo_pris_ne_fait_pas_echouer_le_demarrage() {
        when(comptes.creerAdministrateurSiAbsent(anyString(), anyString())).thenReturn(Resultat.PSEUDO_PRIS);
        assertDoesNotThrow(() -> new AdminInitialiseur("awa.dev", MDP, comptes).initialiser());
    }

    @Test
    void configuration_invalide_fait_echouer_le_demarrage_sans_afficher_le_mot_de_passe() {
        when(comptes.creerAdministrateurSiAbsent(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Pseudo invalide : 3 à 30 caractères."));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new AdminInitialiseur("a", MDP, comptes).initialiser());

        assertFalse(e.getMessage().contains(MDP));
    }
}

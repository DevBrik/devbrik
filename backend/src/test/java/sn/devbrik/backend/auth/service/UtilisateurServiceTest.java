package sn.devbrik.backend.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository depot;

    @InjectMocks
    private UtilisateurService service;

    private static Utilisateur utilisateur(long id, String pseudo, String nomAffiche) {
        Utilisateur u = new Utilisateur(pseudo, nomAffiche, "hache-bcrypt", Role.DEVELOPPEUR);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    @Test
    void references_renvoie_id_pseudo_et_nom_affiche() {
        when(depot.findAllById(Set.of(12L))).thenReturn(List.of(utilisateur(12L, "awa.dev", "Awa Diop")));

        Map<Long, ReferenceUtilisateur> resultat = service.references(List.of(12L));

        assertEquals(1, resultat.size());
        assertEquals(new ReferenceUtilisateur(12L, "awa.dev", "Awa Diop"), resultat.get(12L));
    }

    @Test
    void references_garde_un_nom_affiche_absent_a_null() {
        when(depot.findAllById(Set.of(5L))).thenReturn(List.of(utilisateur(5L, "moussa", null)));

        assertNull(service.references(List.of(5L)).get(5L).nomAffiche());
    }

    @Test
    void references_ignore_les_identifiants_inconnus() {
        when(depot.findAllById(Set.of(1L, 999L))).thenReturn(List.of(utilisateur(1L, "fatou", "Fatou")));

        Map<Long, ReferenceUtilisateur> resultat = service.references(List.of(1L, 999L));

        assertTrue(resultat.containsKey(1L));
        assertFalse(resultat.containsKey(999L));
    }

    @Test
    void references_dedoublonne_et_ignore_les_null() {
        when(depot.findAllById(Set.of(1L))).thenReturn(List.of(utilisateur(1L, "fatou", "Fatou")));

        assertEquals(1, service.references(Arrays.asList(1L, null, 1L)).size());
    }

    @Test
    void references_collection_vide_ou_nulle_ne_touche_pas_la_base() {
        assertTrue(service.references(List.of()).isEmpty());
        assertTrue(service.references(null).isEmpty());
        assertTrue(service.references(Arrays.asList((Long) null)).isEmpty());
        verifyNoInteractions(depot);
        verify(depot, never()).findAllById(anyCollection());
    }

    @Test
    void references_renvoie_une_map_non_modifiable() {
        when(depot.findAllById(Set.of(1L))).thenReturn(List.of(utilisateur(1L, "fatou", "Fatou")));

        Map<Long, ReferenceUtilisateur> resultat = service.references(List.of(1L));

        assertThrows(UnsupportedOperationException.class,
                () -> resultat.put(2L, new ReferenceUtilisateur(2L, "x", null)));
    }

    @Test
    void existe_vrai_quand_l_utilisateur_existe() {
        when(depot.existsById(12L)).thenReturn(true);
        assertTrue(service.existe(12L));
    }

    @Test
    void existe_faux_quand_l_utilisateur_n_existe_pas() {
        when(depot.existsById(404L)).thenReturn(false);
        assertFalse(service.existe(404L));
    }

    @Test
    void existe_faux_pour_un_identifiant_null() {
        assertFalse(service.existe(null));
        verifyNoInteractions(depot);
    }
}

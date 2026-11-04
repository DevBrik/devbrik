package sn.devbrik.backend.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sn.devbrik.backend.auth.dto.ProfilRequete;
import sn.devbrik.backend.auth.dto.UtilisateurDto;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;
import sn.devbrik.backend.common.ApiException;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfilServiceTest {

    @Mock
    private UtilisateurRepository depot;

    @InjectMocks
    private ProfilService service;

    private Utilisateur compte() {
        Utilisateur u = new Utilisateur("awa.dev", "Awa", "hache-bcrypt", Role.DEVELOPPEUR);
        ReflectionTestUtils.setField(u, "id", 12L);
        u.setPays("Sénégal");
        u.setBio("Dev");
        u.setLienProfil("https://exemple.sn");
        return u;
    }

    @Test
    void moi_renvoie_le_profil_sans_mot_de_passe() {
        when(depot.findById(12L)).thenReturn(Optional.of(compte()));

        UtilisateurDto dto = service.moi(12L);

        assertEquals("awa.dev", dto.pseudo());
        assertEquals(Role.DEVELOPPEUR, dto.role());
        assertTrue(Arrays.stream(UtilisateurDto.class.getRecordComponents())
                .noneMatch(c -> c.getName().toLowerCase().contains("motdepasse")));
    }

    @Test
    void moi_compte_supprime_donne_non_authentifie() {
        when(depot.findById(99L)).thenReturn(Optional.empty());

        assertEquals("NON_AUTHENTIFIE", assertThrows(ApiException.class, () -> service.moi(99L)).getCode().name());
    }

    @Test
    void mettreAJour_remplace_les_quatre_champs() {
        when(depot.findById(12L)).thenReturn(Optional.of(compte()));
        when(depot.save(any(Utilisateur.class))).thenAnswer(a -> a.getArgument(0));

        UtilisateurDto dto = service.mettreAJour(12L,
                new ProfilRequete("Awa Diop", "Mali", "Nouvelle bio", "https://awa.dev"));

        assertEquals("Awa Diop", dto.nomAffiche());
        assertEquals("Mali", dto.pays());
        assertEquals("Nouvelle bio", dto.bio());
        assertEquals("https://awa.dev", dto.lienProfil());
        assertEquals("awa.dev", dto.pseudo(), "le pseudo ne change jamais");
        assertEquals(Role.DEVELOPPEUR, dto.role(), "le rôle ne change jamais");
    }

    @Test
    void mettreAJour_champs_absents_ou_vides_effacent_les_valeurs() {
        when(depot.findById(12L)).thenReturn(Optional.of(compte()));
        when(depot.save(any(Utilisateur.class))).thenAnswer(a -> a.getArgument(0));

        UtilisateurDto dto = service.mettreAJour(12L, new ProfilRequete("   ", null, "", null));

        assertNull(dto.nomAffiche());
        assertNull(dto.pays());
        assertNull(dto.bio());
        assertNull(dto.lienProfil());
    }
}

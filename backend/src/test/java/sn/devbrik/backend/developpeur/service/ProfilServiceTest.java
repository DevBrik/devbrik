package sn.devbrik.backend.developpeur.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.developpeur.dto.ProfilPublic;
import sn.devbrik.backend.developpeur.dto.Reputation;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ProfilServiceTest {

    @Mock private ProfilLecture profilLecture;
    @Mock private ReputationService reputationService;
    @InjectMocks private ProfilService service;

    @Test
    void le_profil_reprend_l_identite_et_le_detail_de_la_reputation() {
        Instant creation = Instant.parse("2026-11-01T10:00:00Z");
        when(profilLecture.parId(12L)).thenReturn(Optional.of(new ProfilLecture.ProfilUtilisateur(
                12L, "awa.dev", "Awa Diop", "Sénégal", "Développeuse mobile.", "https://exemple.sn/awa", creation)));
        when(reputationService.calculer(12L)).thenReturn(new Reputation(38, 2, 3, 1));

        ProfilPublic profil = service.profil(12L);

        assertThat(profil).isEqualTo(new ProfilPublic(
                12L, "awa.dev", "Awa Diop", "Sénégal", "Développeuse mobile.", "https://exemple.sn/awa",
                creation, 38, 2, 3, 1));
    }

    @Test
    void un_champ_facultatif_absent_reste_null() {
        when(profilLecture.parId(12L)).thenReturn(Optional.of(new ProfilLecture.ProfilUtilisateur(
                12L, "awa.dev", null, null, null, null, Instant.now())));
        when(reputationService.calculer(12L)).thenReturn(new Reputation(0, 0, 0, 0));

        ProfilPublic profil = service.profil(12L);

        assertThat(profil.nomAffiche()).isNull();
        assertThat(profil.pays()).isNull();
        assertThat(profil.bio()).isNull();
        assertThat(profil.lienProfil()).isNull();
    }

    @Test
    void un_identifiant_inconnu_donne_ressource_introuvable_sans_calculer_la_reputation() {
        when(profilLecture.parId(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.profil(404L))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo("RESSOURCE_INTROUVABLE"));
        verify(reputationService, never()).calculer(anyLong());
    }
}
package sn.devbrik.backend.demande.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.brique.dto.BriqueReference;
import sn.devbrik.backend.brique.service.BriqueService;
import sn.devbrik.backend.common.QuotaService;
import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.demande.dto.BriqueLiee;
import sn.devbrik.backend.demande.dto.ReponseDto;
import sn.devbrik.backend.demande.entity.Reponse;
import sn.devbrik.backend.demande.repository.DemandeRepository;
import sn.devbrik.backend.demande.repository.ReponseRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandeServiceLienBriqueTest {

    @Mock private DemandeRepository demandeRepository;
    @Mock private ReponseRepository reponseRepository;
    @Mock private ReferentielLecture referentiel;
    @Mock private UtilisateurService utilisateurService;
    @Mock private BriqueService briqueService;
    @Mock private QuotaService quotaService;
    @InjectMocks private DemandeService service;

    private static Reponse reponse(Long idBrique) {
        Reponse r = new Reponse(1L, 6L, "Voici une brique qui fait cela.", idBrique);
        ReflectionTestUtils.setField(r, "id", 10L);
        ReflectionTestUtils.setField(r, "dateCreation", Instant.now());
        return r;
    }

    private void auteurConnu() {
        when(utilisateurService.references(anyCollection()))
                .thenReturn(Map.of(6L, new ReferenceUtilisateur(6L, "moussa.dev", "Moussa Fall")));
    }

    @Test
    void test_RG13_une_brique_publiee_est_renvoyee_avec_son_titre() {
        auteurConnu();
        when(briqueService.references(anyCollection()))
                .thenReturn(Map.of(42L, new BriqueReference(42L, "Validation des numéros", true)));

        List<ReponseDto> resultat = service.construireReponses(List.of(reponse(42L)));

        assertThat(resultat.getFirst().brique()).isEqualTo(new BriqueLiee(42L, "Validation des numéros"));
    }

    @Test
    void test_RG13_une_brique_masquee_donne_brique_null() {
        auteurConnu();
        when(briqueService.references(anyCollection()))
                .thenReturn(Map.of(42L, new BriqueReference(42L, "Validation des numéros", false)));

        assertThat(service.construireReponses(List.of(reponse(42L))).getFirst().brique()).isNull();
    }

    @Test
    void test_RG13_une_brique_supprimee_donne_brique_null() {
        auteurConnu();
        when(briqueService.references(anyCollection())).thenReturn(Map.of()); // absente de la map

        assertThat(service.construireReponses(List.of(reponse(42L))).getFirst().brique()).isNull();
    }

    @Test
    void une_reponse_sans_brique_n_interroge_pas_le_service_des_briques() {
        auteurConnu();

        assertThat(service.construireReponses(List.of(reponse(null))).getFirst().brique()).isNull();
        verify(briqueService, never()).references(anyCollection());
    }
}
package sn.devbrik.backend.demande.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.brique.service.BriqueService;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;
import sn.devbrik.backend.common.QuotaService;
import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.common.TypeQuota;
import sn.devbrik.backend.demande.dto.DemandeDetail;
import sn.devbrik.backend.demande.dto.NouvelleDemande;
import sn.devbrik.backend.demande.entity.Demande;
import sn.devbrik.backend.demande.entity.StatutDemande;
import sn.devbrik.backend.demande.repository.DemandeRepository;
import sn.devbrik.backend.demande.repository.ReponseRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandeServiceTest {

    private static final String DESCRIPTION = "Je cherche une brique qui valide et normalise les numéros.";

    @Mock private DemandeRepository demandeRepository;
    @Mock private ReponseRepository reponseRepository;
    @Mock private ReferentielLecture referentiel;
    @Mock private UtilisateurService utilisateurService;
    @Mock private BriqueService briqueService;
    @Mock private QuotaService quotaService;
    @InjectMocks private DemandeService service;

    private static CodeErreur codeDe(Throwable e) {
        return ((ApiException) e).getCode();
    }

    private static Demande demandeExistante(long id, long idAuteur) {
        Demande d = new Demande(idAuteur, "Valider un numéro sénégalais", DESCRIPTION, null, null);
        ReflectionTestUtils.setField(d, "id", id);
        ReflectionTestUtils.setField(d, "dateCreation", Instant.now());
        return d;
    }

    // ---------------------------------------------------------------- création

    @Test
    void creer_enregistre_la_demande_et_renvoie_le_detail_ouvert() {
        when(demandeRepository.save(any(Demande.class))).thenAnswer(inv -> {
            Demande d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", 1L);
            ReflectionTestUtils.setField(d, "dateCreation", Instant.now());
            return d;
        });
        when(utilisateurService.references(anyCollection()))
                .thenReturn(Map.of(5L, new ReferenceUtilisateur(5L, "awa.dev", "Awa Diop")));

        DemandeDetail detail = service.creer(5L,
                new NouvelleDemande("Valider un numéro sénégalais", DESCRIPTION, null, null));

        assertThat(detail.id()).isEqualTo(1L);
        assertThat(detail.statut()).isEqualTo(StatutDemande.OUVERTE);
        assertThat(detail.reponses()).isEmpty();
        assertThat(detail.nombreReponses()).isZero();
        assertThat(detail.categorie()).isNull();
        verify(quotaService).verifier(TypeQuota.DEMANDE, 0L);
    }

    @Test
    void test_RG15_creer_est_refuse_sans_rien_enregistrer_quand_le_quota_est_depasse() {
        when(demandeRepository.countByIdAuteurAndDateCreationAfter(anyLong(), any(Instant.class)))
                .thenReturn(10L);
        doThrow(new RuntimeException("quota")).when(quotaService).verifier(TypeQuota.DEMANDE, 10L);

        assertThatThrownBy(() -> service.creer(5L,
                new NouvelleDemande("Valider un numéro sénégalais", DESCRIPTION, null, null)))
                .hasMessage("quota");
        verify(demandeRepository, never()).save(any());
    }

    @Test
    void creer_refuse_une_categorie_inconnue() {
        when(referentiel.categorieExiste(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.creer(5L,
                new NouvelleDemande("Valider un numéro sénégalais", DESCRIPTION, 99L, null)))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.REQUETE_INVALIDE));
        verify(demandeRepository, never()).save(any());
    }

    // ------------------------------------------------------------- suppression

    @Test
    void test_RG19_un_non_auteur_ne_peut_pas_supprimer_une_demande() {
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demandeExistante(1L, 5L)));

        assertThatThrownBy(() -> service.supprimer(1L, 6L, false))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.ACCES_REFUSE));
        verify(demandeRepository, never()).delete(any(Demande.class));
    }

    @Test
    void test_RG19_l_auteur_peut_supprimer_sa_demande_avec_ses_reponses() {
        Demande demande = demandeExistante(1L, 5L);
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande));

        service.supprimer(1L, 5L, false);

        verify(reponseRepository).supprimerParDemande(1L);
        verify(demandeRepository).delete(demande);
    }

    @Test
    void test_RG19_un_administrateur_peut_supprimer_la_demande_d_un_autre() {
        Demande demande = demandeExistante(1L, 5L);
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande));

        service.supprimer(1L, 99L, true);

        verify(demandeRepository).delete(demande);
    }

    @Test
    void supprimer_une_demande_inconnue_donne_ressource_introuvable() {
        when(demandeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.supprimer(404L, 5L, false))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.RESSOURCE_INTROUVABLE));
    }

    // ------------------------------------------------------------------- liste

    @Test
    void test_RG16_une_recherche_d_un_seul_caractere_est_refusee() {
        assertThatThrownBy(() -> service.lister(null, null, null, "a", null, null))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.REQUETE_INVALIDE));
    }

    @Test
    void test_RG16_une_recherche_de_plus_de_100_caracteres_est_refusee() {
        assertThatThrownBy(() -> service.lister(null, null, null, "a".repeat(101), null, null))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.REQUETE_INVALIDE));
    }

    @Test
    void test_RG16_une_page_negative_est_refusee() {
        assertThatThrownBy(() -> service.lister(null, null, null, null, -1, null))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.REQUETE_INVALIDE));
    }

    @Test
    void lister_refuse_un_statut_inconnu() {
        assertThatThrownBy(() -> service.lister("FERMEE", null, null, null, null, null))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.REQUETE_INVALIDE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_RG16_la_taille_par_defaut_est_20_et_le_maximum_50() {
        when(demandeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        service.lister(null, null, null, null, null, null);
        service.lister(null, null, null, null, 0, 500);

        verify(demandeRepository, org.mockito.Mockito.times(2))
                .findAll(any(Specification.class), captor.capture());
        assertThat(captor.getAllValues().get(0).getPageSize()).isEqualTo(20);
        assertThat(captor.getAllValues().get(1).getPageSize()).isEqualTo(50);
    }
}
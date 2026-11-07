package sn.devbrik.backend.avis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.avis.dto.AvisRequete;
import sn.devbrik.backend.avis.entity.Avis;
import sn.devbrik.backend.avis.repository.AvisRepository;
import sn.devbrik.backend.brique.entity.StatutBrique;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.QuotaService;
import sn.devbrik.backend.common.TypeQuota;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvisServiceTest {

    private static final long BRIQUE = 1L;
    private static final long AUTEUR_BRIQUE = 99L;
    private static final long MOI = 10L;

    @Mock AvisRepository avisRepository;
    @Mock BriqueLecture briques;
    @Mock UtilisateurService utilisateurService;
    @Mock QuotaService quotaService;

    AvisService service;

    @BeforeEach
    void init() {
        service = new AvisService(avisRepository, new AccesBrique(briques), utilisateurService, quotaService);
    }

    // À adapter ici seulement si le constructeur de BriqueResumeInterne diffère.
    private static InfoBrique brique(long idAuteur, StatutBrique statut) {
        return new InfoBrique(BRIQUE, idAuteur, statut.name(), 1, "Titre");
    }

    private void briquePubliee() {
        when(briques.trouver(BRIQUE)).thenReturn(Optional.of(brique(AUTEUR_BRIQUE, StatutBrique.PUBLIEE)));
    }

    private static Avis avisExistant(Instant derniereModification) {
        Avis avis = new Avis(BRIQUE, MOI, 3, "Moyen");
        ReflectionTestUtils.setField(avis, "dateCreation", derniereModification);
        ReflectionTestUtils.setField(avis, "dateModification", derniereModification);
        return avis;
    }

    private static void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, String code) {
        assertThatThrownBy(action).isInstanceOf(ApiException.class).extracting(e -> ((ApiException) e).getCode().name()).isEqualTo(code);
    }

    @Test
    void test_RG8_premier_appel_cree_l_avis() {
        briquePubliee();
        when(avisRepository.findByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(Optional.empty());
        when(avisRepository.countByIdAuteurAndDateModificationAfter(eq(MOI), any(Instant.class))).thenReturn(0L);
        when(avisRepository.saveAndFlush(any(Avis.class))).thenAnswer(i -> i.getArgument(0));

        var resultat = service.enregistrer(BRIQUE, MOI, new AvisRequete(5, "Très utile"));

        assertThat(resultat.cree()).isTrue();
        assertThat(resultat.corps().note()).isEqualTo(5);
        verify(quotaService).verifier(TypeQuota.AVIS, 0L);
    }

    @Test
    void test_RG8_deuxieme_appel_met_a_jour_sans_consommer_de_quota() {
        briquePubliee();
        Avis existant = avisExistant(Instant.now().minus(Duration.ofHours(1)));
        when(avisRepository.findByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(Optional.of(existant));
        when(avisRepository.saveAndFlush(existant)).thenReturn(existant);

        var resultat = service.enregistrer(BRIQUE, MOI, new AvisRequete(4, "Corrigé"));

        assertThat(resultat.cree()).isFalse();
        assertThat(existant.getNote()).isEqualTo(4);
        assertThat(existant.getCommentaire()).isEqualTo("Corrigé");
        verify(quotaService, never()).verifier(any(TypeQuota.class), anyLong());
    }

    @Test
    void test_RG15_avis_ancien_modifie_compte_dans_le_quota() {
        briquePubliee();
        Avis existant = avisExistant(Instant.now().minus(Duration.ofHours(30)));
        when(avisRepository.findByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(Optional.of(existant));
        when(avisRepository.countByIdAuteurAndDateModificationAfter(eq(MOI), any(Instant.class))).thenReturn(4L);
        when(avisRepository.saveAndFlush(existant)).thenReturn(existant);

        service.enregistrer(BRIQUE, MOI, new AvisRequete(2, null));

        verify(quotaService).verifier(TypeQuota.AVIS, 4L);
    }

    @Test
    void test_RG15_quota_depasse_bloque_la_creation() {
        briquePubliee();
        when(avisRepository.findByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(Optional.empty());
        doThrow(new IllegalStateException("quota")).when(quotaService).verifier(any(TypeQuota.class), anyLong());

        assertThatThrownBy(() -> service.enregistrer(BRIQUE, MOI, new AvisRequete(5, null)))
                .isInstanceOf(IllegalStateException.class);
        verify(avisRepository, never()).saveAndFlush(any());
    }

    @Test
    void test_RG8_avis_interdit_sur_sa_propre_brique() {
        when(briques.trouver(BRIQUE)).thenReturn(Optional.of(brique(MOI, StatutBrique.PUBLIEE)));

        assertCode(() -> service.enregistrer(BRIQUE, MOI, new AvisRequete(5, null)), "AUTO_EVALUATION_INTERDITE");
        verifyNoInteractions(quotaService);
        verify(avisRepository, never()).saveAndFlush(any());
    }

    @Test
    void test_RG8_auteur_sur_sa_brique_masquee_recoit_403() {
        when(briques.trouver(BRIQUE)).thenReturn(Optional.of(brique(MOI, StatutBrique.MASQUEE)));

        assertCode(() -> service.enregistrer(BRIQUE, MOI, new AvisRequete(5, null)), "AUTO_EVALUATION_INTERDITE");
    }

    @Test
    void test_RG7_avis_sur_brique_masquee_d_un_autre_donne_404() {
        when(briques.trouver(BRIQUE)).thenReturn(Optional.of(brique(AUTEUR_BRIQUE, StatutBrique.MASQUEE)));

        assertCode(() -> service.enregistrer(BRIQUE, MOI, new AvisRequete(5, null)), "RESSOURCE_INTROUVABLE");
    }

    @Test
    void test_RG8_avis_sur_brique_inexistante_donne_404() {
        assertCode(() -> service.enregistrer(BRIQUE, MOI, new AvisRequete(5, null)), "RESSOURCE_INTROUVABLE");
    }

    @Test
    void test_RG19_l_auteur_supprime_son_avis() {
        Avis avis = avisExistant(Instant.now());
        when(avisRepository.findById(5L)).thenReturn(Optional.of(avis));

        service.supprimer(5L, MOI, false);

        verify(avisRepository).delete(avis);
    }

    @Test
    void test_RG19_un_administrateur_supprime_n_importe_quel_avis() {
        Avis avis = avisExistant(Instant.now());
        when(avisRepository.findById(5L)).thenReturn(Optional.of(avis));

        service.supprimer(5L, 1L, true);

        verify(avisRepository).delete(avis);
    }

    @Test
    void test_RG19_un_autre_developpeur_ne_supprime_pas_l_avis() {
        Avis avis = avisExistant(Instant.now());
        when(avisRepository.findById(5L)).thenReturn(Optional.of(avis));

        assertCode(() -> service.supprimer(5L, 77L, false), "ACCES_REFUSE");
        verify(avisRepository, never()).delete(any());
    }

    @Test
    void test_RG19_supprimer_un_avis_inconnu_donne_404() {
        assertCode(() -> service.supprimer(404L, MOI, false), "RESSOURCE_INTROUVABLE");
    }

    @Test
    void test_RG16_la_taille_est_plafonnee_a_50() {
        briquePubliee();
        when(avisRepository.findByIdBriqueOrderByDateCreationDescIdDesc(eq(BRIQUE), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.lister(BRIQUE, null, false, 0, 500);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(avisRepository).findByIdBriqueOrderByDateCreationDescIdDesc(eq(BRIQUE), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    void test_RG16_une_page_negative_est_refusee() {
        briquePubliee();

        assertCode(() -> service.lister(BRIQUE, null, false, -1, 20), "REQUETE_INVALIDE");
    }

    @Test
    void test_RG7_lecture_des_avis_d_une_brique_masquee_refusee_au_visiteur() {
        when(briques.trouver(BRIQUE)).thenReturn(Optional.of(brique(AUTEUR_BRIQUE, StatutBrique.MASQUEE)));

        assertCode(() -> service.lister(BRIQUE, null, false, 0, 20), "RESSOURCE_INTROUVABLE");
    }
}
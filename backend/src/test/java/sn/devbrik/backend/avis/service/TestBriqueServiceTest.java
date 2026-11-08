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
import sn.devbrik.backend.avis.dto.TestRequete;
import sn.devbrik.backend.avis.entity.ResultatTest;
import sn.devbrik.backend.avis.entity.TestBrique;
import sn.devbrik.backend.avis.repository.TestBriqueRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestBriqueServiceTest {

    private static final long BRIQUE = 1L;
    private static final long AUTEUR_BRIQUE = 99L;
    private static final long MOI = 10L;

    @Mock TestBriqueRepository testRepository;
    @Mock BriqueLecture briques;
    @Mock UtilisateurService utilisateurService;
    @Mock QuotaService quotaService;

    TestBriqueService service;

    @BeforeEach
    void init() {
        service = new TestBriqueService(testRepository, new AccesBrique(briques),
                utilisateurService, quotaService);
    }

    private static InfoBrique brique(long idAuteur, StatutBrique statut, int version) {
        return new InfoBrique(BRIQUE, idAuteur, statut.name(), version, "Titre");
    }

    private void briquePublieeVersion(int version) {
        when(briques.trouver(BRIQUE))
                .thenReturn(Optional.of(brique(AUTEUR_BRIQUE, StatutBrique.PUBLIEE, version)));
    }

    private static TestBrique testExistant(int version, Instant date) {
        TestBrique test = new TestBrique(BRIQUE, MOI, ResultatTest.KO, "Linux", version);
        ReflectionTestUtils.setField(test, "dateTest", date);
        return test;
    }

    private static void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, String code) {
        assertThatThrownBy(action).isInstanceOf(ApiException.class).extracting(e -> ((ApiException) e).getCode().name()).isEqualTo(code);
    }

    @Test
    void test_RG9_le_test_enregistre_la_version_actuelle_du_code() {
        briquePublieeVersion(3);
        when(testRepository.findByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(Optional.empty());
        when(testRepository.countByIdAuteurAndDateTestAfter(eq(MOI), any(Instant.class))).thenReturn(2L);
        when(testRepository.saveAndFlush(any(TestBrique.class))).thenAnswer(i -> i.getArgument(0));

        var resultat = service.enregistrer(BRIQUE, MOI, new TestRequete(ResultatTest.OK, "Java 21, Windows 11"));

        ArgumentCaptor<TestBrique> captor = ArgumentCaptor.forClass(TestBrique.class);
        verify(testRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getVersionCode()).isEqualTo(3);
        assertThat(captor.getValue().getResultat()).isEqualTo(ResultatTest.OK);
        assertThat(resultat.cree()).isTrue();
        verify(quotaService).verifier(TypeQuota.TEST, 2L);
    }

    @Test
    void test_RG9_changer_son_test_met_a_jour_resultat_et_version() {
        briquePublieeVersion(2); // le code a changé depuis le premier test (version 1)
        TestBrique existant = testExistant(1, Instant.now().minus(Duration.ofHours(2)));
        when(testRepository.findByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(Optional.of(existant));
        when(testRepository.saveAndFlush(existant)).thenReturn(existant);

        var resultat = service.enregistrer(BRIQUE, MOI, new TestRequete(ResultatTest.OK, null));

        assertThat(resultat.cree()).isFalse();
        assertThat(existant.getResultat()).isEqualTo(ResultatTest.OK);
        assertThat(existant.getVersionCode()).isEqualTo(2);
        verify(quotaService, never()).verifier(any(TypeQuota.class), anyLong());
    }

    @Test
    void test_RG15_test_ancien_modifie_compte_dans_le_quota() {
        briquePublieeVersion(1);
        TestBrique existant = testExistant(1, Instant.now().minus(Duration.ofHours(30)));
        when(testRepository.findByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(Optional.of(existant));
        when(testRepository.countByIdAuteurAndDateTestAfter(eq(MOI), any(Instant.class))).thenReturn(7L);
        when(testRepository.saveAndFlush(existant)).thenReturn(existant);

        service.enregistrer(BRIQUE, MOI, new TestRequete(ResultatTest.KO, null));

        verify(quotaService).verifier(TypeQuota.TEST, 7L);
    }

    @Test
    void test_RG9_test_interdit_sur_sa_propre_brique() {
        when(briques.trouver(BRIQUE))
                .thenReturn(Optional.of(brique(MOI, StatutBrique.PUBLIEE, 1)));

        assertCode(() -> service.enregistrer(BRIQUE, MOI, new TestRequete(ResultatTest.OK, null)),
                "AUTO_EVALUATION_INTERDITE");
        verifyNoInteractions(quotaService);
        verify(testRepository, never()).saveAndFlush(any());
    }

    @Test
    void test_RG7_test_sur_brique_masquee_d_un_autre_donne_404() {
        when(briques.trouver(BRIQUE))
                .thenReturn(Optional.of(brique(AUTEUR_BRIQUE, StatutBrique.MASQUEE, 1)));

        assertCode(() -> service.enregistrer(BRIQUE, MOI, new TestRequete(ResultatTest.OK, null)),
                "RESSOURCE_INTROUVABLE");
    }

    @Test
    void test_RG9_la_liste_ne_contient_que_les_tests_de_la_version_actuelle() {
        briquePublieeVersion(4);
        when(testRepository.findByIdBriqueAndVersionCodeOrderByDateTestDescIdDesc(
                eq(BRIQUE), eq(4), any(Pageable.class))).thenReturn(Page.empty());

        service.lister(BRIQUE, null, false, 0, 20);

        verify(testRepository).findByIdBriqueAndVersionCodeOrderByDateTestDescIdDesc(
                eq(BRIQUE), eq(4), any(Pageable.class));
    }

    @Test
    void test_RG7_l_auteur_voit_les_tests_de_sa_brique_masquee() {
        when(briques.trouver(BRIQUE))
                .thenReturn(Optional.of(brique(AUTEUR_BRIQUE, StatutBrique.MASQUEE, 1)));
        when(testRepository.findByIdBriqueAndVersionCodeOrderByDateTestDescIdDesc(
                eq(BRIQUE), eq(1), any(Pageable.class))).thenReturn(Page.empty());

        service.lister(BRIQUE, AUTEUR_BRIQUE, false, 0, 20);
    }

    @Test
    void test_RG7_un_visiteur_ne_voit_pas_les_tests_d_une_brique_masquee() {
        when(briques.trouver(BRIQUE))
                .thenReturn(Optional.of(brique(AUTEUR_BRIQUE, StatutBrique.MASQUEE, 1)));

        assertCode(() -> service.lister(BRIQUE, null, false, 0, 20), "RESSOURCE_INTROUVABLE");
    }

    @Test
    void test_RG9_retirer_son_test() {
        TestBrique existant = testExistant(1, Instant.now());
        when(testRepository.findByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(Optional.of(existant));

        service.supprimerMonTest(BRIQUE, MOI);

        verify(testRepository).delete(existant);
    }

    @Test
    void test_RG9_retirer_un_test_inexistant_donne_404() {
        assertCode(() -> service.supprimerMonTest(BRIQUE, MOI), "RESSOURCE_INTROUVABLE");
    }
}
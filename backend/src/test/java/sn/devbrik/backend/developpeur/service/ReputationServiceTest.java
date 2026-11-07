package sn.devbrik.backend.developpeur.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.avis.service.StatsBriqueService;
import sn.devbrik.backend.brique.service.BriqueService;
import sn.devbrik.backend.demande.service.DemandeService;
import sn.devbrik.backend.developpeur.dto.Reputation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReputationServiceTest {

    @Mock private BriqueService briqueService;
    @Mock private StatsBriqueService statsBriqueService;
    @Mock private DemandeService demandeService;
    @InjectMocks private ReputationService service;

    private void donnees(long briques, long testsOk, long acceptees) {
        when(briqueService.compterPubliees(1L)).thenReturn(briques);
        when(statsBriqueService.compterTestsOkRecus(1L)).thenReturn(testsOk);
        when(demandeService.compterReponsesAcceptees(1L)).thenReturn(acceptees);
    }

    @Test
    void test_RG14_un_nouveau_developpeur_a_zero_point() {
        donnees(0, 0, 0);

        assertThat(service.calculer(1L)).isEqualTo(new Reputation(0, 0, 0, 0));
    }

    @Test
    void test_RG14_dix_points_par_brique_publiee() {
        donnees(3, 0, 0);

        assertThat(service.calculer(1L).points()).isEqualTo(30);
    }

    @Test
    void test_RG14_cinq_points_par_test_ok_recu() {
        donnees(0, 4, 0);

        assertThat(service.calculer(1L).points()).isEqualTo(20);
    }

    @Test
    void test_RG14_trois_points_par_reponse_acceptee() {
        donnees(0, 0, 5);

        assertThat(service.calculer(1L).points()).isEqualTo(15);
    }

    @Test
    void test_RG14_les_trois_composantes_s_additionnent_et_le_detail_est_renvoye() {
        donnees(2, 3, 1); // 20 + 15 + 3

        assertThat(service.calculer(1L)).isEqualTo(new Reputation(38, 2, 3, 1));
    }

    @Test
    void test_RG14_le_calcul_est_fait_a_la_demande_sans_ecriture() throws NoSuchMethodException {
        Transactional transactional = ReputationService.class
                .getMethod("calculer", Long.class).getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isTrue();
    }
}
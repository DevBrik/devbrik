package sn.devbrik.backend.avis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.devbrik.backend.avis.dto.StatsBrique;
import sn.devbrik.backend.avis.repository.AgregatAvis;
import sn.devbrik.backend.avis.repository.AgregatTests;
import sn.devbrik.backend.avis.repository.AvisRepository;
import sn.devbrik.backend.avis.repository.TestBriqueRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsBriqueServiceTest {

    @Mock AvisRepository avisRepository;
    @Mock TestBriqueRepository testBriqueRepository;
    @InjectMocks StatsBriqueService service;

    private static AgregatAvis avis(long idBrique, long nombre, long somme) {
        return new AgregatAvis() {
            public Long getIdBrique() { return idBrique; }
            public Long getNombre() { return nombre; }
            public Long getSomme() { return somme; }
        };
    }

    private static AgregatTests tests(long idBrique, long ok, long ko, Instant derniere) {
        return new AgregatTests() {
            public Long getIdBrique() { return idBrique; }
            public Long getOk() { return ok; }
            public Long getKo() { return ko; }
            public Instant getDerniere() { return derniere; }
        };
    }

    @Test
    void test_RG8_note_moyenne_arrondie_a_une_decimale() {
        // 5 + 4 + 4 + 4 = 17 sur 4 avis : 4,25 arrondi à 4,3
        when(avisRepository.agreger(anyCollection())).thenReturn(List.of(avis(1L, 4, 17)));
        when(testBriqueRepository.agreger(anyCollection())).thenReturn(List.of());

        StatsBrique stats = service.statistiques(List.of(1L)).get(1L);

        assertThat(stats.noteMoyenne()).isEqualTo(4.3);
        assertThat(stats.nombreAvis()).isEqualTo(4);
    }

    @Test
    void test_RG8_note_moyenne_absente_sans_avis() {
        when(avisRepository.agreger(anyCollection())).thenReturn(List.of());
        when(testBriqueRepository.agreger(anyCollection())).thenReturn(List.of());

        Map<Long, StatsBrique> stats = service.statistiques(List.of(2L));

        assertThat(stats).containsKey(2L);
        assertThat(stats.get(2L)).isEqualTo(StatsBrique.vide());
    }

    @Test
    void test_RG9_RG10_tests_de_la_version_actuelle_et_derniere_verification() {
        Instant date = Instant.parse("2026-11-05T14:30:00Z");
        when(avisRepository.agreger(anyCollection())).thenReturn(List.of());
        when(testBriqueRepository.agreger(anyCollection())).thenReturn(List.of(tests(3L, 2, 1, date)));

        Map<Long, StatsBrique> stats = service.statistiques(List.of(3L, 4L));

        assertThat(stats.get(3L).testsOk()).isEqualTo(2);
        assertThat(stats.get(3L).testsKo()).isEqualTo(1);
        assertThat(stats.get(3L).derniereVerification()).isEqualTo(date);
        // brique 4 : aucun test de la version actuelle
        assertThat(stats.get(4L).derniereVerification()).isNull();
        assertThat(stats.get(4L).testsOk()).isZero();
    }

    @Test
    void test_ids_vides_ou_nuls_ne_touchent_pas_la_base() {
        assertThat(service.statistiques(null)).isEmpty();
        assertThat(service.statistiques(List.of())).isEmpty();
        verifyNoInteractions(avisRepository, testBriqueRepository);
    }

    @Test
    void test_RG14_compter_tests_ok_recus_delegue_au_depot() {
        when(testBriqueRepository.compterOkRecusVersionActuelle(7L)).thenReturn(5L);
        assertThat(service.compterTestsOkRecus(7L)).isEqualTo(5L);
    }
}
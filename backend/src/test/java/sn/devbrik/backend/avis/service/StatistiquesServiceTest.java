package sn.devbrik.backend.avis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StatistiquesServiceTest {

    @Mock JdbcTemplate jdbcTemplate;
    @InjectMocks StatistiquesService service;

    @Test
    void test_S4_un_non_administrateur_ne_voit_pas_les_statistiques() {
        assertThatThrownBy(() -> service.statistiques(false))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode())
                .isEqualTo(CodeErreur.ACCES_REFUSE);
        verifyNoInteractions(jdbcTemplate);
    }
}
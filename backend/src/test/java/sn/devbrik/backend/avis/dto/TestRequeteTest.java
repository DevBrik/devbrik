package sn.devbrik.backend.avis.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import sn.devbrik.backend.avis.entity.ResultatTest;

import static org.assertj.core.api.Assertions.assertThat;

class TestRequeteTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_RG9_environnement_120_accepte_121_refuse() {
        assertThat(validator.validate(new TestRequete(ResultatTest.OK, "e".repeat(120)))).isEmpty();
        assertThat(validator.validate(new TestRequete(ResultatTest.OK, "e".repeat(121)))).hasSize(1);
    }

    @Test
    void test_RG9_le_resultat_est_obligatoire_et_l_environnement_facultatif() {
        assertThat(validator.validate(new TestRequete(null, null))).hasSize(1);
        assertThat(validator.validate(new TestRequete(ResultatTest.KO, null))).isEmpty();
    }

    @Test
    void test_RG9_l_environnement_est_rogne_et_vide_devient_null() {
        assertThat(new TestRequete(ResultatTest.OK, "  Linux  ").environnement()).isEqualTo("Linux");
        assertThat(new TestRequete(ResultatTest.OK, "  ").environnement()).isNull();
    }
}
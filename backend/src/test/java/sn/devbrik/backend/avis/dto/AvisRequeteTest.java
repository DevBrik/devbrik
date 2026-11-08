package sn.devbrik.backend.avis.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AvisRequeteTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_RG8_les_notes_de_1_a_5_sont_acceptees() {
        for (int note = 1; note <= 5; note++) {
            assertThat(validator.validate(new AvisRequete(note, null))).isEmpty();
        }
    }

    @Test
    void test_RG8_les_notes_0_et_6_et_l_absence_de_note_sont_refusees() {
        assertThat(validator.validate(new AvisRequete(0, null))).hasSize(1);
        assertThat(validator.validate(new AvisRequete(6, null))).hasSize(1);
        assertThat(validator.validate(new AvisRequete(null, null))).hasSize(1);
    }

    @Test
    void test_RG3_commentaire_2000_accepte_2001_refuse() {
        assertThat(validator.validate(new AvisRequete(3, "a".repeat(2000)))).isEmpty();
        assertThat(validator.validate(new AvisRequete(3, "a".repeat(2001)))).hasSize(1);
    }

    @Test
    void test_RG3_le_commentaire_est_rogne_et_vide_devient_null() {
        assertThat(new AvisRequete(3, "  bien  ").commentaire()).isEqualTo("bien");
        assertThat(new AvisRequete(3, "   ").commentaire()).isNull();
    }

    @Test
    void test_RG18_un_commentaire_avec_script_reste_du_texte_brut() {
        assertThat(new AvisRequete(3, "<script>alert(1)</script>").commentaire())
                .isEqualTo("<script>alert(1)</script>");
    }
}
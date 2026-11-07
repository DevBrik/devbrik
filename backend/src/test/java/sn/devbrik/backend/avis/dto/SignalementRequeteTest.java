package sn.devbrik.backend.avis.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import sn.devbrik.backend.avis.entity.MotifSignalement;

import static org.assertj.core.api.Assertions.assertThat;

class SignalementRequeteTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_RG3_details_de_1000_caracteres_acceptes() {
        var requete = new SignalementRequete(MotifSignalement.SPAM, "a".repeat(1000));
        assertThat(validator.validate(requete)).isEmpty();
    }

    @Test
    void test_RG3_details_de_1001_caracteres_refuses() {
        var requete = new SignalementRequete(MotifSignalement.SPAM, "a".repeat(1001));
        assertThat(validator.validate(requete)).hasSize(1);
    }

    @Test
    void test_RG11_le_motif_est_obligatoire() {
        var requete = new SignalementRequete(null, "détails");
        assertThat(validator.validate(requete)).hasSize(1);
    }

    @Test
    void test_les_details_sont_rognes_et_les_details_vides_deviennent_null() {
        assertThat(new SignalementRequete(MotifSignalement.AUTRE, "  texte  ").details()).isEqualTo("texte");
        assertThat(new SignalementRequete(MotifSignalement.AUTRE, "   ").details()).isNull();
    }
}
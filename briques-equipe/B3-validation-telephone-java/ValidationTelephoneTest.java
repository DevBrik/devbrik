package sn.devbrik.briques;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class ValidationTelephoneTest {

    // ── estValide ─────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} → valide")
    @ValueSource(strings = {
        "771234567",         // Orange
        "761234567",         // Free
        "701234567",         // Expresso
        "781234567",         // Wave
        "751234567",         // Tigo
        "+221771234567",     // international +
        "00221771234567",    // international 00
    })
    void numerosValides(String tel) {
        assertThat(ValidationTelephone.estValide(tel)).isTrue();
    }

    @ParameterizedTest(name = "{0} → invalide")
    @ValueSource(strings = {
        "621234567",   // Cameroun
        "331234567",   // fixe sénégalais
        "71234567",    // trop court
        "7712345678",  // trop long
        "0771234567",  // préfixe incorrect
        "",
        "   ",
        "abc",
    })
    void numerosInvalides(String tel) {
        assertThat(ValidationTelephone.estValide(tel)).isFalse();
    }

    @Test
    void null_retourneFaux() {
        assertThat(ValidationTelephone.estValide(null)).isFalse();
    }

    // ── normaliser ────────────────────────────────────────────────────────────

    @Test
    void normaliser_local_ajouteIndicatif() {
        assertThat(ValidationTelephone.normaliser("771234567")).isEqualTo("+221771234567");
    }

    @Test
    void normaliser_avecPlus221_reste_inchange() {
        assertThat(ValidationTelephone.normaliser("+221771234567")).isEqualTo("+221771234567");
    }

    @Test
    void normaliser_avecOO221_convertit() {
        assertThat(ValidationTelephone.normaliser("00221771234567")).isEqualTo("+221771234567");
    }

    @Test
    void normaliser_invalide_lanceException() {
        assertThatThrownBy(() -> ValidationTelephone.normaliser("621234567"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── versLocal ─────────────────────────────────────────────────────────────

    @Test
    void versLocal_retourneNeufChiffres() {
        String local = ValidationTelephone.versLocal("+221771234567");
        assertThat(local).isEqualTo("771234567");
        assertThat(local).hasSize(9);
    }
}

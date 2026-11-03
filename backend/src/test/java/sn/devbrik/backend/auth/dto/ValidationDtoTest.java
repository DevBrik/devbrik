package sn.devbrik.backend.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Règles de format RG1 et RG2 : ce qui devient un 400 REQUETE_INVALIDE via le gestionnaire global. */
class ValidationDtoTest {

    private final Validator validateur = Validation.buildDefaultValidatorFactory().getValidator();

    private boolean valide(String pseudo, String motDePasse) {
        return validateur.validate(new InscriptionRequete(pseudo, motDePasse, null)).isEmpty();
    }

    @Test
    void test_RG1_pseudos_valides() {
        for (String pseudo : new String[]{"abc", "awa.dev", "Moussa_99", "a-b", "9lions", "A".repeat(30)}) {
            assertTrue(valide(pseudo, "MotDePasse123"), pseudo);
        }
    }

    @Test
    void test_RG1_pseudos_invalides() {
        for (String pseudo : new String[]{"ab", "A".repeat(31), ".awa", "_awa", "-awa",
                "awa dev", "awa@dev", "awé", "awa/dev", ""}) {
            assertFalse(valide(pseudo, "MotDePasse123"), "[" + pseudo + "]");
        }
    }

    @Test
    void test_RG1_pseudo_absent_refuse_et_pseudo_rogne() {
        assertFalse(valide(null, "MotDePasse123"));
        assertTrue(valide("  awa.dev  ", "MotDePasse123"));
        assertEquals("awa.dev", new InscriptionRequete("  awa.dev  ", "MotDePasse123", null).pseudo());
    }

    @Test
    void test_RG2_mot_de_passe_entre_8_et_72_caracteres() {
        assertFalse(valide("awa.dev", "1234567"));
        assertTrue(valide("awa.dev", "12345678"));
        assertTrue(valide("awa.dev", "a".repeat(72)));
        assertFalse(valide("awa.dev", "a".repeat(73)));
        assertFalse(valide("awa.dev", null));
    }

    @Test
    void test_RG2_le_mot_de_passe_n_est_jamais_rogne() {
        assertEquals("  secret12  ", new InscriptionRequete("awa.dev", "  secret12  ", null).motDePasse());
    }

    @Test
    void profil_lien_doit_etre_http_ou_https() {
        assertTrue(validateur.validate(new ProfilRequete(null, null, null, "https://awa.dev")).isEmpty());
        assertTrue(validateur.validate(new ProfilRequete(null, null, null, null)).isEmpty());
        for (String lien : new String[]{"javascript:alert(1)", "ftp://x.sn", "awa.dev", "https://a b.sn"}) {
            Set<ConstraintViolation<ProfilRequete>> erreurs =
                    validateur.validate(new ProfilRequete(null, null, null, lien));
            assertFalse(erreurs.isEmpty(), lien);
        }
    }

    @Test
    void profil_longueurs_maximales_de_V1() {
        assertTrue(validateur.validate(new ProfilRequete("n".repeat(80), "p".repeat(60), "b".repeat(500), null)).isEmpty());
        assertFalse(validateur.validate(new ProfilRequete("n".repeat(81), null, null, null)).isEmpty());
        assertFalse(validateur.validate(new ProfilRequete(null, "p".repeat(61), null, null)).isEmpty());
        assertFalse(validateur.validate(new ProfilRequete(null, null, "b".repeat(501), null)).isEmpty());
    }

    @Test
    void profil_chaines_vides_deviennent_null() {
        ProfilRequete r = new ProfilRequete("  ", "", "\t", " ");
        assertNull(r.nomAffiche());
        assertNull(r.pays());
        assertNull(r.bio());
        assertNull(r.lienProfil());
    }
}

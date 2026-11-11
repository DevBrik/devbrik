package sn.devbrik.backend.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RG15 : quotas par 24 heures glissantes, refus en 429 LIMITE_QUOTIDIENNE. */
class QuotaServiceTest {

    private final QuotaService quotas = new QuotaService();

    @Test
    void test_RG15_les_limites_sont_celles_du_dossier() {
        assertThat(TypeQuota.BRIQUE.limite()).isEqualTo(20);
        assertThat(TypeQuota.AVIS.limite()).isEqualTo(50);
        assertThat(TypeQuota.TEST.limite()).isEqualTo(50);
        assertThat(TypeQuota.DEMANDE.limite()).isEqualTo(10);
        assertThat(TypeQuota.REPONSE.limite()).isEqualTo(50);
        assertThat(TypeQuota.SIGNALEMENT.limite()).isEqualTo(10);
    }

    @Test
    void test_RG15_la_derniere_action_autorisee_passe_et_la_suivante_est_refusee() {
        for (TypeQuota type : TypeQuota.values()) {
            assertThatCode(() -> quotas.verifier(type, type.limite() - 1))
                    .as("avant la limite : " + type)
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> quotas.verifier(type, type.limite()))
                    .as("à la limite : " + type)
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getCode()).isEqualTo(CodeErreur.LIMITE_QUOTIDIENNE));
        }
    }

    @Test
    void test_RG15_aucune_action_n_est_refusee_pour_un_compteur_a_zero() {
        for (TypeQuota type : TypeQuota.values()) {
            assertThatCode(() -> quotas.verifier(type, 0)).doesNotThrowAnyException();
        }
    }

    @Test
    void le_message_nomme_la_limite_et_le_type_d_action() {
        assertThatThrownBy(() -> quotas.verifier(TypeQuota.AVIS, 50))
                .hasMessageContaining("50 avis")
                .hasMessageContaining("24 heures");
        assertThatThrownBy(() -> quotas.verifier(TypeQuota.DEMANDE, 10))
                .hasMessageContaining("10 demandes");
    }

    @Test
    void les_libelles_sont_du_texte_lisible() {
        for (TypeQuota type : TypeQuota.values()) {
            assertThat(type.libelle()).as(type.name()).matches("[a-zéè]+");
        }
    }
}

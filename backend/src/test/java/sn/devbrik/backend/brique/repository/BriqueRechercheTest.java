package sn.devbrik.backend.brique.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.devbrik.backend.brique.repository.BriqueRecherche.Tri;

import static org.assertj.core.api.Assertions.assertThat;

/** RG16 : échappement du motif de recherche et valeurs de tri. Tests purs, sans base. */
class BriqueRechercheTest {

    @Test
    @DisplayName("test_RG16_les_caracteres_pourcent_et_souligne_sont_traites_comme_du_texte")
    void test_RG16_pourcent_et_souligne_sont_du_texte() {
        assertThat(BriqueRecherche.echapper("100%")).isEqualTo("100!%");
        assertThat(BriqueRecherche.echapper("a_b")).isEqualTo("a!_b");
    }

    @Test
    @DisplayName("test_RG16_le_caractere_d_echappement_est_lui_aussi_du_texte")
    void test_RG16_le_caractere_d_echappement_est_du_texte() {
        assertThat(BriqueRecherche.echapper("oui!")).isEqualTo("oui!!");
        assertThat(BriqueRecherche.echapper("!%_")).isEqualTo("!!!%!_");
    }

    @Test
    @DisplayName("test_RG16_un_texte_ordinaire_et_les_accents_ne_changent_pas")
    void test_RG16_texte_ordinaire_inchange() {
        assertThat(BriqueRecherche.echapper("téléphone")).isEqualTo("téléphone");
    }

    @Test
    @DisplayName("test_RG16_les_trois_tris_de_l_API_sont_reconnus_et_recentes_est_le_defaut")
    void test_RG16_les_trois_tris() {
        assertThat(Tri.depuisApi(null)).isEqualTo(Tri.RECENTES);
        assertThat(Tri.depuisApi("")).isEqualTo(Tri.RECENTES);
        assertThat(Tri.depuisApi("recentes")).isEqualTo(Tri.RECENTES);
        assertThat(Tri.depuisApi("mieux-notees")).isEqualTo(Tri.MIEUX_NOTEES);
        assertThat(Tri.depuisApi("plus-testees")).isEqualTo(Tri.PLUS_TESTEES);
    }

    @Test
    @DisplayName("test_RG16_un_tri_inconnu_est_refuse")
    void test_RG16_tri_inconnu() {
        assertThat(Tri.depuisApi("au-hasard")).isNull();
    }
}

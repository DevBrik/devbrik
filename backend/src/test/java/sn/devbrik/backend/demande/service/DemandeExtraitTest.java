package sn.devbrik.backend.demande.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Extrait de la liste des demandes (11.6) : 160 caractères, espaces réduits, « … » si coupé. */
class DemandeExtraitTest {

    @Test
    void une_description_courte_est_renvoyee_telle_quelle() {
        assertThat(DemandeService.extrait("Je cherche une brique.")).isEqualTo("Je cherche une brique.");
    }

    @Test
    void les_espaces_et_les_retours_a_la_ligne_sont_reduits_a_un_seul_espace() {
        assertThat(DemandeService.extrait("  une   ligne\n\nsur   plusieurs\t lignes  "))
                .isEqualTo("une ligne sur plusieurs lignes");
    }

    @Test
    void une_description_de_exactement_160_caracteres_n_est_pas_coupee() {
        String texte = "a".repeat(160);

        assertThat(DemandeService.extrait(texte)).isEqualTo(texte);
    }

    @Test
    void une_description_de_161_caracteres_est_coupee_a_160_et_terminee_par_une_ellipse() {
        String extrait = DemandeService.extrait("a".repeat(161));

        assertThat(extrait).isEqualTo("a".repeat(160) + "…");
        assertThat(extrait).hasSize(161);
    }

    @Test
    void la_reduction_des_espaces_precede_la_coupure() {
        String texte = ("mot" + " ".repeat(10)).repeat(50);

        String extrait = DemandeService.extrait(texte);

        assertThat(extrait).startsWith("mot mot mot").endsWith("…").hasSize(161);
        assertThat(extrait).doesNotContain("  ");
    }

    @Test
    void un_caractere_code_sur_deux_unites_n_est_jamais_coupe_en_deux() {
        String texte = "a".repeat(159) + "\uD83D\uDE00" + "bbb"; // l'émoji occupe les positions 159 et 160

        String extrait = DemandeService.extrait(texte);

        assertThat(extrait).isEqualTo("a".repeat(159) + "…");
    }

    @Test
    void le_texte_brut_est_conserve_sans_interpretation() {
        assertThat(DemandeService.extrait("<b>gras</b> & <script>x</script>"))
                .isEqualTo("<b>gras</b> & <script>x</script>");
    }

    @Test
    void une_description_nulle_donne_un_extrait_vide() {
        assertThat(DemandeService.extrait(null)).isEmpty();
    }
}

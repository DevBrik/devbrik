package sn.devbrik.backend.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corps de PUT /api/moi : { nomAffiche?, pays?, bio?, lienProfil? }.
 * Remplacement des quatre champs : un champ absent ou vide efface la valeur.
 */
public record ProfilRequete(
        @Size(max = 80, message = "Le nom affiché doit contenir 80 caractères au plus.")
        String nomAffiche,

        @Size(max = 60, message = "Le pays doit contenir 60 caractères au plus.")
        String pays,

        @Size(max = 500, message = "La bio doit contenir 500 caractères au plus.")
        String bio,

        @Size(max = 255, message = "Le lien doit contenir 255 caractères au plus.")
        @Pattern(regexp = "^https?://\\S+$",
                message = "Le lien doit commencer par http:// ou https:// et ne pas contenir d'espace.")
        String lienProfil) {

    public ProfilRequete {
        nomAffiche = nettoyer(nomAffiche);
        pays = nettoyer(pays);
        bio = nettoyer(bio);
        lienProfil = nettoyer(lienProfil);
    }

    private static String nettoyer(String valeur) {
        if (valeur == null) {
            return null;
        }
        String rogne = valeur.strip();
        return rogne.isEmpty() ? null : rogne;
    }
}

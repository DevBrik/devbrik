package sn.devbrik.backend.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corps de POST /api/auth/inscription : { pseudo, motDePasse, nomAffiche? } (11.3).
 * RG1 : pseudo de 3 à 30 caractères, commence par une lettre ou un chiffre.
 * RG2 : mot de passe de 8 à 72 caractères (72 octets contrôlés dans AuthService).
 */
public record InscriptionRequete(
        @NotNull(message = "Le pseudo est obligatoire.")
        @Size(min = 3, max = 30, message = "Le pseudo doit contenir entre 3 et 30 caractères.")
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$",
                message = "Lettres, chiffres, point, tiret et tiret bas ; commence par une lettre ou un chiffre.")
        String pseudo,

        @NotNull(message = "Le mot de passe est obligatoire.")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
        String motDePasse,

        @Size(max = 80, message = "Le nom affiché doit contenir 80 caractères au plus.")
        String nomAffiche) {

    public InscriptionRequete {
        pseudo = pseudo == null ? null : pseudo.strip();
        nomAffiche = (nomAffiche == null || nomAffiche.isBlank()) ? null : nomAffiche.strip();
    }

    @Override
    public String toString() {
        return "InscriptionRequete[pseudo=" + pseudo + ", motDePasse=***]";
    }
}

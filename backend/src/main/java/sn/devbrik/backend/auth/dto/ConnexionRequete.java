package sn.devbrik.backend.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corps de POST /api/auth/connexion : { pseudo, motDePasse }.
 * Aucune règle de format : un pseudo mal formé doit donner le même 401 qu'un pseudo inconnu.
 */
public record ConnexionRequete(
        @NotNull(message = "Le pseudo est obligatoire.")
        @Size(max = 100, message = "Pseudo trop long.")
        String pseudo,

        @NotNull(message = "Le mot de passe est obligatoire.")
        @Size(max = 200, message = "Mot de passe trop long.")
        String motDePasse) {

    public ConnexionRequete {
        pseudo = pseudo == null ? null : pseudo.strip();
    }

    @Override
    public String toString() {
        return "ConnexionRequete[pseudo=" + pseudo + ", motDePasse=***]";
    }
}

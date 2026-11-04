package sn.devbrik.backend.auth.dto;

/** Réponse d'inscription et de connexion : { jeton, utilisateur } (11.3). */
public record ReponseAuth(String jeton, UtilisateurDto utilisateur) {

    /** Jamais le jeton dans un journal (S10). */
    @Override
    public String toString() {
        return "ReponseAuth[jeton=***, utilisateur=" + utilisateur + "]";
    }
}

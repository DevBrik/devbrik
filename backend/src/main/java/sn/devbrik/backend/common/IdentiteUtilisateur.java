package sn.devbrik.backend.common;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record IdentiteUtilisateur(Long id, String role) {

    public static Optional<IdentiteUtilisateur> optionnelle() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof IdentiteUtilisateur i) {
            return Optional.of(i);
        }
        return Optional.empty();
    }

    public static IdentiteUtilisateur obligatoire() {
        return optionnelle().orElseThrow(
                () -> new ApiException(CodeErreur.NON_AUTHENTIFIE, "Connexion requise."));
    }

    public boolean estAdmin() {
        return "ADMIN".equals(role);
    }
}
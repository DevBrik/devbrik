package sn.devbrik.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.devbrik.backend.common.CodeErreur;
import sn.devbrik.backend.common.IdentiteUtilisateur;

public class JwtFiltre extends OncePerRequestFilter {

    private final JwtService jwt;
    private final ReponseErreurSecurite erreurs;
    private final Predicate<Long> compteExiste;

    /**
     * @param compteExiste vrai si le compte du jeton existe encore : un jeton (12 h) dont le compte a été
     *                     supprimé (RG17) est refusé en 401 plutôt que de provoquer une erreur de clé étrangère.
     */
    public JwtFiltre(JwtService jwt, ReponseErreurSecurite erreurs, Predicate<Long> compteExiste) {
        this.jwt = jwt;
        this.erreurs = erreurs;
        this.compteExiste = compteExiste;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chaine)
            throws ServletException, IOException {
        String entete = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (entete != null && entete.startsWith("Bearer ")) {
            Optional<IdentiteUtilisateur> identite = jwt.lire(entete.substring(7).trim());
            if (identite.isEmpty()) {
                // Jeton invalide ou expiré : 401 même sur une route publique (section 11.1)
                erreurs.ecrire(res, CodeErreur.NON_AUTHENTIFIE, "Session expirée ou invalide. Reconnectez-vous.");
                return;
            }
            IdentiteUtilisateur i = identite.get();
            if (!compteExiste.test(i.id())) {
                erreurs.ecrire(res, CodeErreur.NON_AUTHENTIFIE, "Ce compte n'existe plus. Reconnectez-vous.");
                return;
            }
            var authentification = new UsernamePasswordAuthenticationToken(
                    i, null, List.of(new SimpleGrantedAuthority("ROLE_" + i.role())));
            SecurityContextHolder.getContext().setAuthentication(authentification);
        }
        chaine.doFilter(req, res);
    }
}
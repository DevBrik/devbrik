package sn.devbrik.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import sn.devbrik.backend.common.CodeErreur;

@Component
public class ReponseErreurSecurite implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e)
            throws IOException {
        ecrire(res, CodeErreur.NON_AUTHENTIFIE, "Connexion requise ou session expirée.");
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e)
            throws IOException {
        ecrire(res, CodeErreur.ACCES_REFUSE, "Vous n'avez pas le droit d'effectuer cette action.");
    }

    public void ecrire(HttpServletResponse res, CodeErreur code, String message) throws IOException {
        res.setStatus(code.statut().value());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        // Messages fixes, sans guillemet : pas d'échappement nécessaire
        res.getWriter().write("{\"statut\":" + code.statut().value()
                + ",\"code\":\"" + code.name() + "\""
                + ",\"message\":\"" + message + "\""
                + ",\"horodatage\":\"" + Instant.now() + "\"}");
    }
}
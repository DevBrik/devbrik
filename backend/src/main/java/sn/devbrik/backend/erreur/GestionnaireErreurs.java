package sn.devbrik.backend.erreur;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;
import sn.devbrik.backend.common.ReponseErreur;
import sn.devbrik.backend.common.ReponseErreur.Detail;

@RestControllerAdvice
public class GestionnaireErreurs {

    private static final Logger LOG = LoggerFactory.getLogger(GestionnaireErreurs.class);

    private ResponseEntity<ReponseErreur> reponse(CodeErreur code, String message, List<Detail> details) {
        return ResponseEntity.status(code.statut()).body(ReponseErreur.de(code, message, details));
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ReponseErreur> metier(ApiException e) {
        return reponse(e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ReponseErreur> validationCorps(MethodArgumentNotValidException e) {
        List<Detail> details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new Detail(f.getField(), f.getDefaultMessage()))
                .toList();
        return reponse(CodeErreur.REQUETE_INVALIDE, "Certains champs sont invalides.", details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ReponseErreur> validationParametres(HandlerMethodValidationException e) {
        List<Detail> details = e.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(err -> new Detail(r.getMethodParameter().getParameterName(), err.getDefaultMessage())))
                .toList();
        return reponse(CodeErreur.REQUETE_INVALIDE, "Certains paramètres sont invalides.", details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class})
    ResponseEntity<ReponseErreur> requeteMalFormee(Exception e) {
        return reponse(CodeErreur.REQUETE_INVALIDE, "Requête mal formée.", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ReponseErreur> introuvable(NoResourceFoundException e) {
        return reponse(CodeErreur.RESSOURCE_INTROUVABLE, "Ressource introuvable.", null);
    }

    // Sans ces deux méthodes, le gestionnaire générique les transformerait en 500.
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ReponseErreur> accesRefuse(AccessDeniedException e) {
        return reponse(CodeErreur.ACCES_REFUSE, "Vous n'avez pas le droit d'effectuer cette action.", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ReponseErreur> nonAuthentifie(AuthenticationException e) {
        return reponse(CodeErreur.NON_AUTHENTIFIE, "Connexion requise.", null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ReponseErreur> inattendue(Exception e) {
        LOG.error("Erreur inattendue", e); // le détail va dans le journal, jamais dans la réponse
        return reponse(CodeErreur.ERREUR_INTERNE, "Une erreur inattendue s'est produite.", null);
    }
}
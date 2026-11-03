package sn.devbrik.backend.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.auth.dto.ConnexionRequete;
import sn.devbrik.backend.auth.dto.InscriptionRequete;
import sn.devbrik.backend.auth.dto.ReponseAuth;
import sn.devbrik.backend.auth.service.AuthService;

/** Routes publiques d'authentification (11.3). Aucun gestionnaire d'erreurs propre. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/inscription")
    @ResponseStatus(HttpStatus.CREATED)
    public ReponseAuth inscription(@Valid @RequestBody InscriptionRequete requete) {
        return authService.inscrire(requete);
    }

    @PostMapping("/connexion")
    public ReponseAuth connexion(@Valid @RequestBody ConnexionRequete requete) {
        return authService.connecter(requete);
    }
}

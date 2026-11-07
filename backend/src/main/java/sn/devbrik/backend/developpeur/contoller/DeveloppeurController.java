package sn.devbrik.backend.developpeur.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.developpeur.dto.ProfilPublic;
import sn.devbrik.backend.developpeur.service.ProfilService;

/** Routes de la section 11.6 (développeurs). Le classement (P2) viendra à l'étape 5. */
@RestController
@RequestMapping("/api/developpeurs")
public class DeveloppeurController {

    private final ProfilService profilService;

    public DeveloppeurController(ProfilService profilService) {
        this.profilService = profilService;
    }

    @GetMapping("/{id}")
    public ProfilPublic profil(@PathVariable Long id) {
        return profilService.profil(id);
    }
}
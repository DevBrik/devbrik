package sn.devbrik.backend.developpeur.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.common.PageReponse;
import sn.devbrik.backend.developpeur.dto.ClassementEntree;
import sn.devbrik.backend.developpeur.dto.ProfilPublic;
import sn.devbrik.backend.developpeur.service.ClassementService;
import sn.devbrik.backend.developpeur.service.ProfilPublicService;

/** Routes de la section 11.6 (développeurs), publiques. */
@RestController
@RequestMapping("/api/developpeurs")
public class DeveloppeurController {

    private final ProfilPublicService profilService;
    private final ClassementService classementService;

    public DeveloppeurController(ProfilPublicService profilService, ClassementService classementService) {
        this.profilService = profilService;
        this.classementService = classementService;
    }

    /** Route littérale : prioritaire sur /{id}. */
    @GetMapping("/classement")
    public PageReponse<ClassementEntree> classement(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer taille) {
        return classementService.classement(page, taille);
    }

    @GetMapping("/{id}")
    public ProfilPublic profil(@PathVariable Long id) {
        return profilService.profil(id);
    }
}

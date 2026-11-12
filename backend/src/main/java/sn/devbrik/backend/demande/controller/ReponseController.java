package sn.devbrik.backend.demande.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.common.IdentiteUtilisateur;
import sn.devbrik.backend.demande.dto.DemandeDetail;
import sn.devbrik.backend.demande.dto.NouvelleReponse;
import sn.devbrik.backend.demande.dto.ReponseDto;
import sn.devbrik.backend.demande.service.ReponseService;

/** Routes de la section 11.6 (réponses). Toutes exigent un compte connecté. */
@RestController
@RequestMapping("/api")
public class ReponseController {

    private final ReponseService reponseService;

    public ReponseController(ReponseService reponseService) {
        this.reponseService = reponseService;
    }

    @PostMapping("/demandes/{id}/reponses")
    @ResponseStatus(HttpStatus.CREATED)
    public ReponseDto creer(@PathVariable Long id, @Valid @RequestBody NouvelleReponse corps) {
        return reponseService.creer(id, IdentiteUtilisateur.obligatoire().id(), corps);
    }

    @DeleteMapping("/reponses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();
        reponseService.supprimer(id, moi.id(), moi.estAdmin());
    }

    @PutMapping("/reponses/{id}/acceptation")
    public DemandeDetail accepter(@PathVariable Long id) {
        return reponseService.accepter(id, IdentiteUtilisateur.obligatoire().id());
    }

    @DeleteMapping("/reponses/{id}/acceptation")
    public DemandeDetail retirer(@PathVariable Long id) {
        return reponseService.retirer(id, IdentiteUtilisateur.obligatoire().id());
    }
}

package sn.devbrik.backend.demande.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.common.IdentiteUtilisateur;   // membre 1 (hypothèse : bean avec id() et estAdmin())
import sn.devbrik.backend.common.PageReponse;
import sn.devbrik.backend.demande.dto.DemandeDetail;
import sn.devbrik.backend.demande.dto.DemandeResume;
import sn.devbrik.backend.demande.dto.NouvelleDemande;
import sn.devbrik.backend.demande.service.DemandeService;

/** Routes de la section 11.6 (demandes). Les réponses arrivent à l'étape 3. */
@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeService demandeService;
    private final IdentiteUtilisateur identite;

    public DemandeController(DemandeService demandeService, IdentiteUtilisateur identite) {
        this.demandeService = demandeService;
        this.identite = identite;
    }

    @GetMapping
    public PageReponse<DemandeResume> lister(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String langage,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer taille) {
        return demandeService.lister(statut, categorie, langage, q, page, taille);
    }

    @GetMapping("/{id}")
    public DemandeDetail detail(@PathVariable Long id) {
        return demandeService.detail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeDetail creer(@Valid @RequestBody NouvelleDemande corps) {
        return demandeService.creer(idCourant(), corps);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        demandeService.supprimer(id, idCourant(), estAdmin());
    }

    // Seul endroit où j'appelle IdentiteUtilisateur : à ajuster si sa forme diffère.
    private Long idCourant() {
        return identite.id();
    }

    private boolean estAdmin() {
        return identite.estAdmin();
    }
}
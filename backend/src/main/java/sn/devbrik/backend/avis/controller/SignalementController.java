package sn.devbrik.backend.avis.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.avis.dto.SignalementReponse;
import sn.devbrik.backend.avis.dto.SignalementRequete;
import sn.devbrik.backend.avis.dto.TraitementRequete;
import sn.devbrik.backend.avis.service.SignalementService;
import sn.devbrik.backend.common.PageReponse;

@RestController
@RequestMapping("/api")
public class SignalementController {

    private final SignalementService service;
    private final Appelant identite;

    public SignalementController(SignalementService service, Appelant identite) {
        this.service = service;
        this.identite = identite;
    }

    /** Développeur ou administrateur : 201. */
    @PostMapping("/briques/{id}/signalements")
    @ResponseStatus(HttpStatus.CREATED)
    public SignalementReponse signaler(@PathVariable("id") Long id,
                                       @Valid @RequestBody SignalementRequete requete) {
        return service.signaler(id, identite.idObligatoire(), requete);
    }

    /** Administrateur : statut OUVERT par défaut. */
    @GetMapping("/admin/signalements")
    public PageReponse<SignalementReponse> lister(
            @RequestParam(name = "statut", required = false) String statut,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "taille", required = false) Integer taille) {
        return service.lister(identite.estAdmin(), statut, page, taille);
    }

    /** Administrateur : MASQUER_BRIQUE ou CONSERVER. */
    @PutMapping("/admin/signalements/{id}/traitement")
    public SignalementReponse traiter(@PathVariable("id") Long id,
                                      @Valid @RequestBody TraitementRequete requete) {
        return service.traiter(id, identite.estAdmin(), requete);
    }
}
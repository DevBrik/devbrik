package sn.devbrik.backend.avis.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.avis.dto.AvisReponse;
import sn.devbrik.backend.avis.dto.AvisRequete;
import sn.devbrik.backend.avis.dto.MoiBriqueReponse;
import sn.devbrik.backend.avis.dto.ResultatEcriture;
import sn.devbrik.backend.avis.service.AvisService;
import sn.devbrik.backend.avis.service.MoiBriqueService;
import sn.devbrik.backend.common.PageReponse;

@RestController
@RequestMapping("/api")
public class AvisController {

    private final AvisService avisService;
    private final MoiBriqueService moiBriqueService;
    private final Appelant identite;

    public AvisController(AvisService avisService, MoiBriqueService moiBriqueService,
                          Appelant identite) {
        this.avisService = avisService;
        this.moiBriqueService = moiBriqueService;
        this.identite = identite;
    }

    /** Public. */
    @GetMapping("/briques/{id}/avis")
    public PageReponse<AvisReponse> lister(@PathVariable("id") Long id,
                                           @RequestParam(name = "page", required = false) Integer page,
                                           @RequestParam(name = "taille", required = false) Integer taille) {
        return avisService.lister(id, identite.idOuNull(), identite.estAdmin(), page, taille);
    }

    /** Développeur ou administrateur : 201 si créé, 200 si mis à jour. */
    @PutMapping("/briques/{id}/avis")
    public ResponseEntity<AvisReponse> enregistrer(@PathVariable("id") Long id,
                                                   @Valid @RequestBody AvisRequete requete) {
        ResultatEcriture<AvisReponse> resultat = avisService.enregistrer(id, identite.idObligatoire(), requete);
        return ResponseEntity.status(resultat.cree() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(resultat.corps());
    }

    /** Auteur ou administrateur (RG19). */
    @DeleteMapping("/avis/{idAvis}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable("idAvis") Long idAvis) {
        avisService.supprimer(idAvis, identite.idObligatoire(), identite.estAdmin());
    }

    /** Connecté. */
    @GetMapping("/briques/{id}/moi")
    public MoiBriqueReponse moi(@PathVariable("id") Long id) {
        return moiBriqueService.moi(id, identite.idObligatoire(), identite.estAdmin());
    }
}

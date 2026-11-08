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
import sn.devbrik.backend.avis.dto.ResultatEcriture;
import sn.devbrik.backend.avis.dto.TestReponse;
import sn.devbrik.backend.avis.dto.TestRequete;
import sn.devbrik.backend.avis.service.TestBriqueService;
import sn.devbrik.backend.common.PageReponse;

@RestController
@RequestMapping("/api/briques/{id}")
public class TestBriqueController {

    private final TestBriqueService testService;
    private final Appelant identite;

    public TestBriqueController(TestBriqueService testService, Appelant identite) {
        this.testService = testService;
        this.identite = identite;
    }

    /** Public : tests de la version actuelle. */
    @GetMapping("/tests")
    public PageReponse<TestReponse> lister(@PathVariable("id") Long id,
                                           @RequestParam(name = "page", required = false) Integer page,
                                           @RequestParam(name = "taille", required = false) Integer taille) {
        return testService.lister(id, identite.idOuNull(), identite.estAdmin(), page, taille);
    }

    /** 201 si créé, 200 si mis à jour. */
    @PutMapping("/test")
    public ResponseEntity<TestReponse> enregistrer(@PathVariable("id") Long id,
                                                   @Valid @RequestBody TestRequete requete) {
        ResultatEcriture<TestReponse> resultat = testService.enregistrer(id, identite.idObligatoire(), requete);
        return ResponseEntity.status(resultat.cree() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(resultat.corps());
    }

    /** Retire son propre test. */
    @DeleteMapping("/test")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable("id") Long id) {
        testService.supprimerMonTest(id, identite.idObligatoire());
    }
}
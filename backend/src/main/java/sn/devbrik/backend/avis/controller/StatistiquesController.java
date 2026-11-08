package sn.devbrik.backend.avis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.avis.dto.StatistiquesReponse;
import sn.devbrik.backend.avis.service.StatistiquesService;

@RestController
@RequestMapping("/api/admin")
public class StatistiquesController {

    private final StatistiquesService service;
    private final Appelant identite;

    public StatistiquesController(StatistiquesService service, Appelant identite) {
        this.service = service;
        this.identite = identite;
    }

    /** Administrateur seulement. */
    @GetMapping("/statistiques")
    public StatistiquesReponse statistiques() {
        return service.statistiques(identite.estAdmin());
    }
}
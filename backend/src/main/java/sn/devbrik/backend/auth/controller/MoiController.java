package sn.devbrik.backend.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.devbrik.backend.auth.dto.ChangementMotDePasseRequete;
import sn.devbrik.backend.auth.dto.ExportCompte;
import sn.devbrik.backend.auth.dto.ProfilRequete;
import sn.devbrik.backend.auth.dto.SuppressionRequete;
import sn.devbrik.backend.auth.dto.UtilisateurDto;
import sn.devbrik.backend.auth.service.CompteService;
import sn.devbrik.backend.auth.service.ExportService;
import sn.devbrik.backend.auth.service.ProfilService;
import sn.devbrik.backend.common.IdentiteUtilisateur;

/** Compte de l'utilisateur connecté (11.3). Aucun gestionnaire d'erreurs propre. */
@RestController
@RequestMapping("/api/moi")
public class MoiController {

    private final ProfilService profilService;
    private final CompteService compteService;
    private final ExportService exportService;

    public MoiController(ProfilService profilService, CompteService compteService, ExportService exportService) {
        this.profilService = profilService;
        this.compteService = compteService;
        this.exportService = exportService;
    }

    @GetMapping
    public UtilisateurDto moi() {
        return profilService.moi(IdentiteUtilisateur.obligatoire().id());
    }

    @PutMapping
    public UtilisateurDto modifier(@Valid @RequestBody ProfilRequete requete) {
        return profilService.mettreAJour(IdentiteUtilisateur.obligatoire().id(), requete);
    }

    @PutMapping("/mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changerMotDePasse(@Valid @RequestBody ChangementMotDePasseRequete requete) {
        compteService.changerMotDePasse(IdentiteUtilisateur.obligatoire().id(), requete);
    }

    @GetMapping("/export")
    public ResponseEntity<ExportCompte> exporter() {
        ExportCompte export = exportService.exporter(IdentiteUtilisateur.obligatoire().id());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("devbrik-export.json").build().toString())
                .body(export);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@Valid @RequestBody SuppressionRequete requete) {
        compteService.supprimer(IdentiteUtilisateur.obligatoire().id(), requete);
    }
}

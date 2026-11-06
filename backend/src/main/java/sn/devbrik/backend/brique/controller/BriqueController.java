package sn.devbrik.backend.brique.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.devbrik.backend.brique.dto.*;
import sn.devbrik.backend.brique.service.BriqueService;
import sn.devbrik.backend.common.PageReponse;

/**
 * Endpoints briques.
 *
 * Sécurité gérée globalement par SecurityConfig :
 *   - GET /api/briques/**   → public (permitAll)
 *   - POST, PUT, DELETE     → authenticated
 *   - /api/admin/**         → ADMIN
 *   - /api/moi/**           → authenticated
 */
@RestController
public class BriqueController {

    private final BriqueService briqueService;

    public BriqueController(BriqueService briqueService) {
        this.briqueService = briqueService;
    }

    // ── M01 — Liste publique ──────────────────────────────────────────────────

    @GetMapping("/api/briques")
    public PageReponse<BriqueResumeDto> lister(
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) Long langageId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int taille) {

        PageRequest pageable = PageRequest.of(page, Math.min(taille, 50),
                                              Sort.by("dateCreation").descending());
        return briqueService.listerPubliees(categorieId, langageId, pageable);
    }

    // ── M02 — Fiche publique ──────────────────────────────────────────────────

    @GetMapping("/api/briques/{id}")
    public BriqueDetailDto obtenir(@PathVariable Long id) {
        return briqueService.obtenirPublique(id);
    }

    // ── M03 — Publier ─────────────────────────────────────────────────────────

    @PostMapping("/api/briques")
    @ResponseStatus(HttpStatus.CREATED)
    public BriqueDetailDto publier(@Valid @RequestBody BriqueCreationDto dto) {
        return briqueService.publier(dto);
    }

    // ── M03 — Modifier ────────────────────────────────────────────────────────

    @PutMapping("/api/briques/{id}")
    public BriqueDetailDto modifier(@PathVariable Long id,
                                    @Valid @RequestBody BriqueModificationDto dto) {
        return briqueService.modifier(id, dto);
    }

    // ── M05 — Mes briques ─────────────────────────────────────────────────────

    @GetMapping("/api/moi/briques")
    public PageReponse<BriqueResumeInterne> mesBriques(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int taille) {

        PageRequest pageable = PageRequest.of(page, Math.min(taille, 50),
                                              Sort.by("dateCreation").descending());
        return briqueService.mesBriques(pageable);
    }

    // ── M05 — Supprimer ───────────────────────────────────────────────────────

    @DeleteMapping("/api/briques/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        briqueService.supprimer(id);
    }

    // ── Admin — Masquer / Republier ───────────────────────────────────────────

    @PatchMapping("/api/admin/briques/{id}/masquer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void masquer(@PathVariable Long id) {
        briqueService.masquer(id);
    }

    @PatchMapping("/api/admin/briques/{id}/republier")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void republier(@PathVariable Long id) {
        briqueService.republier(id);
    }

    // ── Référence légère (inter-modules) ─────────────────────────────────────

    @GetMapping("/api/briques/{id}/reference")
    public BriqueReference reference(@PathVariable Long id) {
        return briqueService.reference(id);
    }
}

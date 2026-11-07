package sn.devbrik.backend.brique.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.devbrik.backend.brique.dto.*;
import sn.devbrik.backend.brique.service.BriqueService;
import sn.devbrik.backend.common.PageReponse;

/**
 * Routes briques (11.4). Sécurité gérée par SecurityConfig :
 *   - GET /api/briques/**  : public ;
 *   - /api/admin/**        : administrateur ;
 *   - /api/moi/**          : connecté ;
 *   - les autres routes    : connecté.
 */
@RestController
public class BriqueController {

    private final BriqueService briqueService;

    public BriqueController(BriqueService briqueService) {
        this.briqueService = briqueService;
    }

    // ── M01 : liste publique ──────────────────────────────────────────────────

    @GetMapping("/api/briques")
    public PageReponse<BriqueResumeDto> lister(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categorie", required = false) String categorie,
            @RequestParam(name = "langage", required = false) String langage,
            @RequestParam(name = "auteur", required = false) Long auteur,
            @RequestParam(name = "tri", required = false) String tri,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "taille", required = false) Integer taille) {
        return briqueService.listerPubliees(q, categorie, langage, auteur, tri, page, taille);
    }

    // ── M02 : fiche ───────────────────────────────────────────────────────────

    @GetMapping("/api/briques/{id}")
    public BriqueDetailDto obtenir(@PathVariable("id") Long id) {
        return briqueService.obtenir(id);
    }

    // ── M03 : publier, modifier ───────────────────────────────────────────────

    @PostMapping("/api/briques")
    @ResponseStatus(HttpStatus.CREATED)
    public BriqueDetailDto publier(@Valid @RequestBody BriqueCreationDto dto) {
        return briqueService.publier(dto);
    }

    @PutMapping("/api/briques/{id}")
    public BriqueDetailDto modifier(@PathVariable("id") Long id,
                                    @Valid @RequestBody BriqueModificationDto dto) {
        return briqueService.modifier(id, dto);
    }

    // ── M05 : supprimer, mes briques ──────────────────────────────────────────

    @DeleteMapping("/api/briques/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable("id") Long id) {
        briqueService.supprimer(id);
    }

    @GetMapping("/api/moi/briques")
    public PageReponse<BriqueResumeDto> mesBriques(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categorie", required = false) String categorie,
            @RequestParam(name = "langage", required = false) String langage,
            @RequestParam(name = "tri", required = false) String tri,
            @RequestParam(name = "statut", required = false) String statut,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "taille", required = false) Integer taille) {
        return briqueService.mesBriques(q, categorie, langage, tri, statut, page, taille);
    }

    // ── Administration (M11) ──────────────────────────────────────────────────

    @GetMapping("/api/admin/briques")
    public PageReponse<BriqueResumeDto> listerPourAdmin(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categorie", required = false) String categorie,
            @RequestParam(name = "langage", required = false) String langage,
            @RequestParam(name = "auteur", required = false) Long auteur,
            @RequestParam(name = "tri", required = false) String tri,
            @RequestParam(name = "statut", required = false) String statut,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "taille", required = false) Integer taille) {
        return briqueService.listerPourAdmin(q, categorie, langage, auteur, tri, statut, page, taille);
    }

    /** PUT { statut: "PUBLIEE" | "MASQUEE" } → 200 BriqueDetail. */
    @PutMapping("/api/admin/briques/{id}/statut")
    public BriqueDetailDto changerStatut(@PathVariable("id") Long id,
                                         @Valid @RequestBody StatutRequete requete) {
        return briqueService.changerStatut(id, requete.statut());
    }
}

package sn.devbrik.backend.brique.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.brique.dto.*;
import sn.devbrik.backend.brique.entity.*;
import sn.devbrik.backend.brique.repository.*;
import sn.devbrik.backend.common.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BriqueServiceImpl implements BriqueService {

    private final BriqueRepository    briqueRepo;
    private final CategorieRepository catRepo;
    private final LangageRepository   langRepo;
    private final UtilisateurService  utilisateurService;
    private final QuotaService        quotaService;

    public BriqueServiceImpl(BriqueRepository briqueRepo,
                             CategorieRepository catRepo,
                             LangageRepository langRepo,
                             UtilisateurService utilisateurService,
                             QuotaService quotaService) {
        this.briqueRepo         = briqueRepo;
        this.catRepo            = catRepo;
        this.langRepo           = langRepo;
        this.utilisateurService = utilisateurService;
        this.quotaService       = quotaService;
    }

    // ── M01 — Liste publique ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageReponse<BriqueResumeDto> listerPubliees(Long idCategorie, Long idLangage, Pageable pageable) {
        Page<Brique> page = briqueRepo.listerPubliees(idCategorie, idLangage, pageable);
        List<Long> ids = page.map(Brique::getIdAuteur).getContent();
        Map<Long, ReferenceUtilisateur> refs = utilisateurService.references(ids);
        Page<BriqueResumeDto> mapped = page.map(b ->
            BriqueResumeDto.de(b, refs.getOrDefault(b.getIdAuteur(), refInconnue(b.getIdAuteur())))
        );
        return PageReponse.de(mapped);
    }

    // ── M02 — Fiche publique ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BriqueDetailDto obtenirPublique(Long id) {
        Brique b = briqueRepo.findByIdAndStatut(id, StatutBrique.PUBLIEE)
            .orElseThrow(() -> new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Brique introuvable : " + id));
        ReferenceUtilisateur auteur = utilisateurService.references(List.of(b.getIdAuteur()))
            .getOrDefault(b.getIdAuteur(), refInconnue(b.getIdAuteur()));
        return BriqueDetailDto.de(b, auteur);
    }

    // ── M03 — Publier ─────────────────────────────────────────────────────────

    @Override
    public BriqueDetailDto publier(BriqueCreationDto dto) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();

        // RG6 — quota 20 créations/24 h
        long nb24h = briqueRepo.compterCreationsDepuis(moi.id(), Instant.now().minus(24, ChronoUnit.HOURS));
        quotaService.verifier(TypeQuota.BRIQUE, nb24h);

        // RG3 — titre unique par auteur
        if (briqueRepo.existsByIdAuteurAndTitreIgnoreCase(moi.id(), dto.titre())) {
            throw new ApiException(CodeErreur.TITRE_DEJA_UTILISE,
                "Vous avez déjà une brique avec ce titre.");
        }

        Categorie cat  = catRepo.findById(dto.idCategorie())
            .orElseThrow(() -> new ApiException(CodeErreur.BRIQUE_LIEE_INVALIDE, "Catégorie introuvable."));
        Langage   lang = langRepo.findById(dto.idLangage())
            .orElseThrow(() -> new ApiException(CodeErreur.BRIQUE_LIEE_INVALIDE, "Langage introuvable."));

        Brique brique = new Brique(
            dto.titre(), dto.resume(), dto.code(), dto.modeEmploi(),
            dto.prerequis(), dto.licence(), moi.id(), cat, lang
        );
        brique = briqueRepo.save(brique);

        ReferenceUtilisateur auteur = utilisateurService.references(List.of(moi.id()))
            .getOrDefault(moi.id(), refInconnue(moi.id()));
        return BriqueDetailDto.de(brique, auteur);
    }

    // ── M03 — Modifier ────────────────────────────────────────────────────────

    @Override
    public BriqueDetailDto modifier(Long id, BriqueModificationDto dto) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();
        Brique brique = obtenirPourModification(id, moi);

        // RG3 — unicité titre/auteur (hors brique courante)
        if (briqueRepo.existsByIdAuteurAndTitreIgnoreCaseAndIdNot(moi.id(), dto.titre(), id)) {
            throw new ApiException(CodeErreur.TITRE_DEJA_UTILISE,
                "Vous avez déjà une brique avec ce titre.");
        }

        Categorie cat  = catRepo.findById(dto.idCategorie())
            .orElseThrow(() -> new ApiException(CodeErreur.BRIQUE_LIEE_INVALIDE, "Catégorie introuvable."));
        Langage   lang = langRepo.findById(dto.idLangage())
            .orElseThrow(() -> new ApiException(CodeErreur.BRIQUE_LIEE_INVALIDE, "Langage introuvable."));

        brique.modifier(dto.titre(), dto.resume(), dto.code(), dto.modeEmploi(),
                        dto.prerequis(), dto.licence(), cat, lang);

        ReferenceUtilisateur auteur = utilisateurService.references(List.of(moi.id()))
            .getOrDefault(moi.id(), refInconnue(moi.id()));
        return BriqueDetailDto.de(brique, auteur);
    }

    // ── M05 — Mes briques ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageReponse<BriqueResumeInterne> mesBriques(Pageable pageable) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();
        Page<Brique> page = briqueRepo.findByIdAuteurOrderByDateCreationDesc(moi.id(), pageable);
        return PageReponse.de(page.map(BriqueResumeInterne::de));
    }

    // ── M05 — Supprimer ───────────────────────────────────────────────────────

    @Override
    public void supprimer(Long id) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();
        Brique brique = briqueRepo.findById(id)
            .orElseThrow(() -> new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Brique introuvable : " + id));

        // RG19 — auteur ou admin
        if (!moi.id().equals(brique.getIdAuteur()) && !moi.estAdmin()) {
            throw new ApiException(CodeErreur.ACCES_REFUSE,
                "Seul l'auteur ou un administrateur peut supprimer cette brique.");
        }
        briqueRepo.delete(brique);
    }

    // ── Admin — Masquer / Republier ───────────────────────────────────────────

    @Override
    public void masquer(Long id) {
        Brique brique = briqueRepo.findById(id)
            .orElseThrow(() -> new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Brique introuvable : " + id));
        brique.masquer();
    }

    @Override
    public void republier(Long id) {
        Brique brique = briqueRepo.findById(id)
            .orElseThrow(() -> new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Brique introuvable : " + id));
        brique.republier();
    }

    // ── Référence légère ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BriqueReference reference(Long id) {
        Brique b = briqueRepo.findById(id)
            .orElseThrow(() -> new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Brique introuvable : " + id));
        return BriqueReference.de(b);
    }

    // ── Utilitaires privés ────────────────────────────────────────────────────

    private Brique obtenirPourModification(Long id, IdentiteUtilisateur moi) {
        Brique brique = briqueRepo.findById(id)
            .orElseThrow(() -> new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Brique introuvable : " + id));
        // RG15 — seul l'auteur peut modifier
        if (!moi.id().equals(brique.getIdAuteur())) {
            throw new ApiException(CodeErreur.ACCES_REFUSE,
                "Seul l'auteur peut modifier cette brique.");
        }
        return brique;
    }

    private ReferenceUtilisateur refInconnue(Long id) {
        return new ReferenceUtilisateur(id, "utilisateur_" + id, "Utilisateur inconnu");
    }
}

package sn.devbrik.backend.brique.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.avis.dto.StatsBrique;
import sn.devbrik.backend.avis.service.StatsBriqueService;
import sn.devbrik.backend.brique.dto.*;
import sn.devbrik.backend.brique.entity.Brique;
import sn.devbrik.backend.brique.entity.Categorie;
import sn.devbrik.backend.brique.entity.Langage;
import sn.devbrik.backend.brique.entity.StatutBrique;
import sn.devbrik.backend.brique.repository.BriqueRecherche;
import sn.devbrik.backend.brique.repository.BriqueRecherche.Critere;
import sn.devbrik.backend.brique.repository.BriqueRecherche.Tri;
import sn.devbrik.backend.brique.repository.BriqueRepository;
import sn.devbrik.backend.brique.repository.CategorieRepository;
import sn.devbrik.backend.brique.repository.LangageRepository;
import sn.devbrik.backend.common.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BriqueServiceImpl implements BriqueService {

    /** RG16 : page à partir de 0, taille par défaut 20, maximum 50. */
    private static final int TAILLE_PAR_DEFAUT = 20;
    private static final int TAILLE_MAX = 50;
    private static final int RECHERCHE_MIN = 2;
    private static final int RECHERCHE_MAX = 100;

    private final BriqueRepository    briqueRepo;
    private final BriqueRecherche     recherche;
    private final CategorieRepository catRepo;
    private final LangageRepository   langRepo;
    private final UtilisateurService  utilisateurService;
    private final StatsBriqueService  statsService;
    private final QuotaService        quotaService;

    public BriqueServiceImpl(BriqueRepository briqueRepo,
                             BriqueRecherche recherche,
                             CategorieRepository catRepo,
                             LangageRepository langRepo,
                             UtilisateurService utilisateurService,
                             StatsBriqueService statsService,
                             QuotaService quotaService) {
        this.briqueRepo         = briqueRepo;
        this.recherche          = recherche;
        this.catRepo            = catRepo;
        this.langRepo           = langRepo;
        this.utilisateurService = utilisateurService;
        this.statsService       = statsService;
        this.quotaService       = quotaService;
    }

    // ── Listes ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageReponse<BriqueResumeDto> listerPubliees(String q, String categorie, String langage, Long auteur,
                                                       String tri, Integer page, Integer taille) {
        Critere critere = critere(q, categorie, langage, auteur, StatutBrique.PUBLIEE, tri);
        return lister(critere, page, taille);
    }

    @Override
    @Transactional(readOnly = true)
    public PageReponse<BriqueResumeDto> mesBriques(String q, String categorie, String langage, String tri,
                                                   String statut, Integer page, Integer taille) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();
        Critere critere = critere(q, categorie, langage, moi.id(), lireStatut(statut), tri);
        return lister(critere, page, taille);
    }

    @Override
    @Transactional(readOnly = true)
    public PageReponse<BriqueResumeDto> listerPourAdmin(String q, String categorie, String langage, Long auteur,
                                                        String tri, String statut, Integer page, Integer taille) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();
        if (!moi.estAdmin()) {
            throw new ApiException(CodeErreur.ACCES_REFUSE, "Réservé aux administrateurs.");
        }
        // 11.4 : sans paramètre statut, la liste d'administration montre les briques MASQUEE
        // (écran « Briques masquées ») ; statut=PUBLIEE affiche les briques publiées.
        StatutBrique filtre = (statut == null || statut.isBlank()) ? StatutBrique.MASQUEE : lireStatut(statut);
        Critere critere = critere(q, categorie, langage, auteur, filtre, tri);
        return lister(critere, page, taille);
    }

    private PageReponse<BriqueResumeDto> lister(Critere critere, Integer page, Integer taille) {
        int p = page == null ? 0 : page;
        int t = taille == null ? TAILLE_PAR_DEFAUT : taille;
        if (p < 0) {
            throw requeteInvalide("La page doit être supérieure ou égale à 0.");
        }
        if (t < 1) {
            throw requeteInvalide("La taille doit être supérieure ou égale à 1.");
        }
        t = Math.min(t, TAILLE_MAX);

        BriqueRecherche.Resultat resultat = recherche.rechercher(critere, p, t);
        List<Brique> briques = chargerDansLOrdre(resultat.ids());

        Map<Long, ReferenceUtilisateur> auteurs = utilisateurService.references(
            briques.stream().map(Brique::getIdAuteur).collect(Collectors.toSet()));
        Map<Long, StatsBrique> stats = statsService.statistiques(resultat.ids());

        List<BriqueResumeDto> contenu = briques.stream()
            .map(b -> BriqueResumeDto.de(b,
                auteurOuInconnu(auteurs, b.getIdAuteur()),
                stats.get(b.getId())))
            .toList();
        int totalPages = (int) ((resultat.total() + t - 1) / t);
        return new PageReponse<>(contenu, p, t, resultat.total(), totalPages);
    }

    /** Recharge les entités puis les remet dans l'ordre calculé par la recherche. */
    private List<Brique> chargerDansLOrdre(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Brique> parId = new HashMap<>();
        for (Brique b : briqueRepo.chargerAvecReferentiels(ids)) {
            parId.put(b.getId(), b);
        }
        List<Brique> ordonnees = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Brique b = parId.get(id);
            if (b != null) {
                ordonnees.add(b);
            }
        }
        return ordonnees;
    }

    private Critere critere(String q, String categorie, String langage, Long auteur,
                            StatutBrique statut, String tri) {
        Tri leTri = Tri.depuisApi(tri);
        if (leTri == null) {
            throw requeteInvalide("Le tri doit être recentes, mieux-notees ou plus-testees.");
        }
        return new Critere(lireRecherche(q), vide(categorie), vide(langage), auteur, statut, leTri);
    }

    /** RG16 : q de 2 à 100 caractères. Une valeur vide est ignorée. */
    private static String lireRecherche(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String propre = q.strip();
        if (propre.length() < RECHERCHE_MIN || propre.length() > RECHERCHE_MAX) {
            throw requeteInvalide("La recherche doit contenir entre 2 et 100 caractères.");
        }
        return propre;
    }

    private static StatutBrique lireStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return null;
        }
        try {
            return StatutBrique.valueOf(statut.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw requeteInvalide("Le statut doit être PUBLIEE ou MASQUEE.");
        }
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    // ── Fiche ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BriqueDetailDto obtenir(Long id) {
        Brique b = trouver(id);
        if (b.getStatut() == StatutBrique.MASQUEE && !peutVoirMasquee(b)) {
            throw introuvable(id); // RG7 : comme une brique inexistante
        }
        return detail(b);
    }

    private static boolean peutVoirMasquee(Brique b) {
        return IdentiteUtilisateur.optionnelle()
            .map(i -> i.estAdmin() || Objects.equals(i.id(), b.getIdAuteur()))
            .orElse(false);
    }

    // ── Publier ───────────────────────────────────────────────────────────────

    @Override
    public BriqueDetailDto publier(BriqueCreationDto dto) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();

        // RG4 : l'auteur doit confirmer ses droits.
        if (!Boolean.TRUE.equals(dto.droitsConfirmes())) {
            throw new ApiException(CodeErreur.DROITS_NON_CONFIRMES,
                "Vous devez confirmer avoir le droit de publier ce code.");
        }

        // RG15 — quota de 20 briques par 24 h glissantes.
        long nb24h = briqueRepo.compterCreationsDepuis(moi.id(), Instant.now().minus(24, ChronoUnit.HOURS));
        quotaService.verifier(TypeQuota.BRIQUE, nb24h);

        // RG5 — titre unique par auteur, casse ignorée.
        if (briqueRepo.existsByIdAuteurAndTitreIgnoreCase(moi.id(), dto.titre())) {
            throw titreDejaUtilise();
        }

        Categorie cat  = categorie(dto.idCategorie());
        Langage   lang = langage(dto.idLangage());

        Brique brique = new Brique(
            dto.titre(), dto.resume(), dto.code(), dto.modeEmploi(),
            dto.prerequis(), dto.licence(), moi.id(), cat, lang
        );
        try {
            brique = briqueRepo.save(brique);
            briqueRepo.flush();
        } catch (DataIntegrityViolationException e) {
            // Deux requêtes simultanées : l'index uk_brique_auteur_titre a tranché.
            throw titreDejaUtilise();
        }
        return detail(brique);
    }

    // ── Modifier ──────────────────────────────────────────────────────────────

    @Override
    public BriqueDetailDto modifier(Long id, BriqueModificationDto dto) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();
        Brique brique = trouver(id);

        // RG19 — seul l'auteur modifie. Brique MASQUEE d'un autre : 404 (RG7).
        if (!moi.id().equals(brique.getIdAuteur())) {
            if (brique.getStatut() == StatutBrique.MASQUEE && !moi.estAdmin()) {
                throw introuvable(id);
            }
            throw new ApiException(CodeErreur.ACCES_REFUSE, "Seul l'auteur peut modifier cette brique.");
        }

        // RG5 — unicité titre/auteur, hors brique courante.
        if (briqueRepo.existsByIdAuteurAndTitreIgnoreCaseAndIdNot(moi.id(), dto.titre(), id)) {
            throw titreDejaUtilise();
        }

        Categorie cat  = categorie(dto.idCategorie());
        Langage   lang = langage(dto.idLangage());

        // RG6 : la version augmente seulement si le code change (voir Brique.modifier).
        brique.modifier(dto.titre(), dto.resume(), dto.code(), dto.modeEmploi(),
                        dto.prerequis(), dto.licence(), cat, lang);
        try {
            briqueRepo.flush();
        } catch (DataIntegrityViolationException e) {
            throw titreDejaUtilise();
        }
        return detail(brique);
    }

    // ── Supprimer ─────────────────────────────────────────────────────────────

    @Override
    public void supprimer(Long id) {
        IdentiteUtilisateur moi = IdentiteUtilisateur.obligatoire();
        Brique brique = trouver(id);

        // RG19 — auteur ou administrateur.
        boolean auteur = moi.id().equals(brique.getIdAuteur());
        if (!auteur && !moi.estAdmin()) {
            if (brique.getStatut() == StatutBrique.MASQUEE) {
                throw introuvable(id); // RG7
            }
            throw new ApiException(CodeErreur.ACCES_REFUSE,
                "Seul l'auteur ou un administrateur peut supprimer cette brique.");
        }
        briqueRepo.delete(brique);
    }

    // ── Administration du statut ──────────────────────────────────────────────

    @Override
    public BriqueDetailDto changerStatut(Long id, StatutBrique statut) {
        Brique brique = trouver(id);
        if (statut == StatutBrique.MASQUEE) {
            brique.masquer();
        } else {
            brique.republier();
        }
        briqueRepo.flush();
        return detail(brique);
    }

    @Override
    public void masquer(Long id) {
        trouver(id).masquer();
    }

    @Override
    public void republier(Long id) {
        trouver(id).republier();
    }

    // ── Méthodes publiques (13.4) ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<BriqueResumeInterne> resume(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return briqueRepo.findById(id).map(BriqueResumeInterne::de);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, BriqueReference> references(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        Set<Long> distincts = new LinkedHashSet<>(ids);
        distincts.remove(null);
        if (distincts.isEmpty()) {
            return Map.of();
        }
        Map<Long, BriqueReference> resultat = new LinkedHashMap<>();
        for (Brique b : briqueRepo.chargerAvecReferentiels(distincts)) {
            resultat.put(b.getId(), BriqueReference.de(b));
        }
        return Collections.unmodifiableMap(resultat);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterPubliees(Long idAuteur) {
        if (idAuteur == null) {
            return 0L;
        }
        return briqueRepo.countByIdAuteurAndStatut(idAuteur, StatutBrique.PUBLIEE);
    }

    // ── Utilitaires privés ────────────────────────────────────────────────────

    private Brique trouver(Long id) {
        return briqueRepo.findById(id).orElseThrow(() -> introuvable(id));
    }

    private Categorie categorie(Long id) {
        return catRepo.findById(id).orElseThrow(
            () -> new ApiException(CodeErreur.REQUETE_INVALIDE, "Catégorie introuvable : " + id));
    }

    private Langage langage(Long id) {
        return langRepo.findById(id).orElseThrow(
            () -> new ApiException(CodeErreur.REQUETE_INVALIDE, "Langage introuvable : " + id));
    }

    private BriqueDetailDto detail(Brique b) {
        ReferenceUtilisateur auteur = auteurOuInconnu(
            utilisateurService.references(List.of(b.getIdAuteur())), b.getIdAuteur());
        StatsBrique stats = b.getId() == null ? null
            : statsService.statistiques(List.of(b.getId())).get(b.getId());
        return BriqueDetailDto.de(b, auteur, stats);
    }

    private static ReferenceUtilisateur auteurOuInconnu(Map<Long, ReferenceUtilisateur> refs, Long id) {
        ReferenceUtilisateur ref = refs.get(id);
        return ref != null ? ref : new ReferenceUtilisateur(id, "utilisateur_" + id, "Utilisateur inconnu");
    }

    private static ApiException introuvable(Long id) {
        return new ApiException(CodeErreur.RESSOURCE_INTROUVABLE, "Brique introuvable : " + id);
    }

    private static ApiException titreDejaUtilise() {
        return new ApiException(CodeErreur.TITRE_DEJA_UTILISE, "Vous avez déjà une brique avec ce titre.");
    }

    private static ApiException requeteInvalide(String message) {
        return new ApiException(CodeErreur.REQUETE_INVALIDE, message);
    }
}

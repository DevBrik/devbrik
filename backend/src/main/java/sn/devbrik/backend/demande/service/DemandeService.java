package sn.devbrik.backend.demande.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.service.UtilisateurService;          // membre 5 (hypothèse de paquet)
import sn.devbrik.backend.brique.service.BriqueService;              // membre 2 (hypothèse de paquet)
import sn.devbrik.backend.brique.dto.BriqueReference;            // membre 2 (hypothèse de paquet)
import sn.devbrik.backend.common.PageReponse;
import sn.devbrik.backend.common.QuotaService;                       // membre 1 (hypothèse de paquet)
import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.common.TypeQuota;                          // membre 1 (hypothèse de paquet)
import sn.devbrik.backend.demande.dto.BriqueLiee;
import sn.devbrik.backend.demande.dto.DemandeDetail;
import sn.devbrik.backend.demande.dto.DemandeResume;
import sn.devbrik.backend.demande.dto.ElementReference;
import sn.devbrik.backend.demande.dto.NouvelleDemande;
import sn.devbrik.backend.demande.dto.ReponseDto;
import sn.devbrik.backend.demande.entity.Demande;
import sn.devbrik.backend.demande.entity.Reponse;
import sn.devbrik.backend.demande.entity.StatutDemande;
import sn.devbrik.backend.demande.repository.DemandeRepository;
import sn.devbrik.backend.demande.repository.DemandeSpecifications;
import sn.devbrik.backend.demande.repository.ReponseRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DemandeService {

    private static final Duration FENETRE_QUOTA = Duration.ofHours(24);
    private static final int TAILLE_PAR_DEFAUT = 20;
    private static final int TAILLE_MAX = 50;
    private static final int Q_MIN = 2;
    private static final int Q_MAX = 100;
    private static final int EXTRAIT_MAX = 160;
    /** Identifiant qui n'existe jamais : un slug inconnu donne une page vide. */
    private static final long ID_INEXISTANT = -1L;

    private final DemandeRepository demandeRepository;
    private final ReponseRepository reponseRepository;
    private final ReferentielLecture referentiel;
    private final UtilisateurService utilisateurService;
    private final BriqueService briqueService;
    private final QuotaService quotaService;

    public DemandeService(DemandeRepository demandeRepository,
                          ReponseRepository reponseRepository,
                          ReferentielLecture referentiel,
                          UtilisateurService utilisateurService,
                          BriqueService briqueService,
                          QuotaService quotaService) {
        this.demandeRepository = demandeRepository;
        this.reponseRepository = reponseRepository;
        this.referentiel = referentiel;
        this.utilisateurService = utilisateurService;
        this.briqueService = briqueService;
        this.quotaService = quotaService;
    }

    // ---------------------------------------------------------------- création

    /** RG3 (validé par le DTO), RG15 (10 demandes par 24 h glissantes). */
    @Transactional
    public DemandeDetail creer(Long idUtilisateur, NouvelleDemande corps) {
        long dejaCreees = demandeRepository.countByIdAuteurAndDateCreationAfter(
                idUtilisateur, Instant.now().minus(FENETRE_QUOTA));
        quotaService.verifier(TypeQuota.DEMANDE, dejaCreees);

        if (corps.idCategorie() != null && !referentiel.categorieExiste(corps.idCategorie())) {
            throw ErreursDemande.requeteInvalide("La catégorie indiquée n'existe pas.");
        }
        if (corps.idLangage() != null && !referentiel.langageExiste(corps.idLangage())) {
            throw ErreursDemande.requeteInvalide("Le langage indiqué n'existe pas.");
        }

        Demande demande = demandeRepository.save(new Demande(
                idUtilisateur, corps.titre(), corps.description(),
                corps.idCategorie(), corps.idLangage()));

        DemandeResume resume = versResumes(List.of(demande)).getFirst();
        return DemandeDetail.de(resume, demande.getDescription(), List.of());
    }

    // ------------------------------------------------------------------- liste

    /** RG16 : pagination (page à partir de 0, taille 20 par défaut, 50 max) et recherche q. */
    @Transactional(readOnly = true)
    public PageReponse<DemandeResume> lister(String statut, String categorie, String langage,
                                             String q, Integer page, Integer taille) {
        int numeroPage = page == null ? 0 : page;
        int taillePage = taille == null ? TAILLE_PAR_DEFAUT : taille;
        if (numeroPage < 0) {
            throw ErreursDemande.requeteInvalide("Le numéro de page doit être supérieur ou égal à 0.");
        }
        if (taillePage < 1) {
            throw ErreursDemande.requeteInvalide("La taille de page doit être supérieure ou égale à 1.");
        }
        taillePage = Math.min(taillePage, TAILLE_MAX);

        StatutDemande statutFiltre = lireStatut(statut);
        String motCle = lireRecherche(q);
        Long idCategorie = estRenseigne(categorie)
                ? referentiel.idCategorieParSlug(categorie.strip()).orElse(ID_INEXISTANT) : null;
        Long idLangage = estRenseigne(langage)
                ? referentiel.idLangageParSlug(langage.strip()).orElse(ID_INEXISTANT) : null;

        Pageable pageable = PageRequest.of(numeroPage, taillePage,
                Sort.by(Sort.Direction.DESC, "dateCreation").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Demande> resultat = demandeRepository.findAll(
                DemandeSpecifications.filtre(statutFiltre, idCategorie, idLangage, motCle), pageable);

        List<DemandeResume> contenu = versResumes(resultat.getContent());
        return new PageReponse<>(contenu, resultat.getNumber(), resultat.getSize(),
                resultat.getTotalElements(), resultat.getTotalPages());
    }

    // ------------------------------------------------------------------ détail

    @Transactional(readOnly = true)
    public DemandeDetail detail(Long idDemande) {
        Demande demande = trouver(idDemande);
        List<Reponse> reponses = reponseRepository.findByIdDemandeOrderByDateCreationAsc(idDemande);
        DemandeResume resume = versResumes(List.of(demande)).getFirst();
        return DemandeDetail.de(resume, demande.getDescription(), construireReponses(reponses));
    }

    // -------------------------------------------------------------- suppression

    /** RG19 : auteur ou administrateur. Les réponses sont supprimées avec la demande. */
    @Transactional
    public void supprimer(Long idDemande, Long idUtilisateur, boolean administrateur) {
        Demande demande = trouver(idDemande);
        if (!administrateur && !demande.getIdAuteur().equals(idUtilisateur)) {
            throw ErreursDemande.accesRefuse();
        }
        reponseRepository.supprimerParDemande(idDemande);
        demandeRepository.delete(demande);
    }

    // ---------------------------------------------------- service public (RG14)

    /** Publié pour ReputationService (13.4). */
    @Transactional(readOnly = true)
    public long compterReponsesAcceptees(Long idAuteur) {
        return reponseRepository.countByIdAuteurAndAccepteeTrue(idAuteur);
    }
    /**
     * Publié pour le membre 5 (à annoncer, 13.4) : à appeler après la suppression
     * d'un compte (RG17). Rouvre les demandes RESOLUE qui ont perdu leur réponse
     * acceptée par la cascade en base. Renvoie le nombre de demandes rouvertes.
     */
    @Transactional
    public int rouvrirDemandesSansReponseAcceptee() {
        return demandeRepository.rouvrirSansReponseAcceptee(StatutDemande.OUVERTE, StatutDemande.RESOLUE);
    }

    // ---------------------------------------------------------------- internes

    Demande trouver(Long idDemande) {
        return demandeRepository.findById(idDemande)
                .orElseThrow(() -> ErreursDemande.introuvable("Demande"));
    }

    /** Construit des DemandeResume avec une seule requête par source (pas de N+1). */
    private List<DemandeResume> versResumes(List<Demande> demandes) {
        if (demandes.isEmpty()) {
            return List.of();
        }
        Set<Long> idsDemandes = demandes.stream().map(Demande::getId).collect(Collectors.toSet());
        Set<Long> idsAuteurs = demandes.stream().map(Demande::getIdAuteur).collect(Collectors.toSet());
        Set<Long> idsCategories = demandes.stream().map(Demande::getIdCategorie)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Set<Long> idsLangages = demandes.stream().map(Demande::getIdLangage)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());

        Map<Long, ReferenceUtilisateur> auteurs = utilisateurService.references(idsAuteurs);
        Map<Long, ElementReference> categories = referentiel.categories(idsCategories);
        Map<Long, ElementReference> langages = referentiel.langages(idsLangages);
        Map<Long, Long> nombres = reponseRepository.compterParDemande(idsDemandes).stream()
                .collect(Collectors.toMap(
                        ReponseRepository.CompteReponses::getIdDemande,
                        ReponseRepository.CompteReponses::getNombre));

        List<DemandeResume> resumes = new ArrayList<>();
        for (Demande d : demandes) {
            resumes.add(new DemandeResume(
                    d.getId(), d.getTitre(), extrait(d.getDescription()), d.getStatut(), d.getDateCreation(),
                    d.getIdCategorie() == null ? null : categories.get(d.getIdCategorie()),
                    d.getIdLangage() == null ? null : langages.get(d.getIdLangage()),
                    auteurs.get(d.getIdAuteur()),
                    nombres.getOrDefault(d.getId(), 0L)));
        }
        return resumes;
    }

    /**
     * RG13 : le lien vers une brique n'est renvoyé que si la brique existe et est
     * PUBLIEE ; sinon brique = null. Réutilisée à l'étape 3 (création d'une réponse).
     */
    List<ReponseDto> construireReponses(List<Reponse> reponses) {
        if (reponses.isEmpty()) {
            return List.of();
        }
        Set<Long> idsAuteurs = reponses.stream().map(Reponse::getIdAuteur).collect(Collectors.toSet());
        Set<Long> idsBriques = reponses.stream().map(Reponse::getIdBrique)
                .filter(java.util.Objects::nonNull).collect(Collectors.toCollection(HashSet::new));

        Map<Long, ReferenceUtilisateur> auteurs = utilisateurService.references(idsAuteurs);
        Map<Long, BriqueReference> briques = idsBriques.isEmpty()
                ? Map.of() : briqueService.references(idsBriques);

        return reponses.stream()
                .map(r -> new ReponseDto(
                        r.getId(), r.getContenu(), r.isAcceptee(), r.getDateCreation(),
                        auteurs.get(r.getIdAuteur()),
                        lienValide(r.getIdBrique(), briques)))
                .toList();
    }

    private static BriqueLiee lienValide(Long idBrique, Map<Long, BriqueReference> briques) {
        if (idBrique == null) {
            return null;
        }
        BriqueReference brique = briques.get(idBrique);
        return (brique != null && brique.publiee()) ? new BriqueLiee(brique.id(), brique.titre()) : null;
    }

    /**
     * Extrait de la liste (11.6) : 160 premiers caractères de la description, espaces réduits,
     * texte brut, terminé par « … » si la description est coupée.
     */
    static String extrait(String description) {
        String net = description == null ? "" : description.replaceAll("\\s+", " ").strip();
        if (net.length() <= EXTRAIT_MAX) {
            return net;
        }
        int fin = EXTRAIT_MAX;
        if (Character.isHighSurrogate(net.charAt(fin - 1))) {
            fin--; // ne jamais couper un caractère codé sur deux unités
        }
        return net.substring(0, fin) + "…";
    }

    private static StatutDemande lireStatut(String statut) {
        if (!estRenseigne(statut)) {
            return null;
        }
        try {
            return StatutDemande.valueOf(statut.strip());
        } catch (IllegalArgumentException e) {
            throw ErreursDemande.requeteInvalide("Le statut doit valoir OUVERTE ou RESOLUE.");
        }
    }

    private static String lireRecherche(String q) {
        if (!estRenseigne(q)) {
            return null;
        }
        String motCle = q.strip();
        if (motCle.length() < Q_MIN || motCle.length() > Q_MAX) {
            throw ErreursDemande.requeteInvalide(
                    "La recherche doit contenir entre " + Q_MIN + " et " + Q_MAX + " caractères.");
        }
        return motCle;
    }

    private static boolean estRenseigne(String valeur) {
        return valeur != null && !valeur.isBlank();
    }
}
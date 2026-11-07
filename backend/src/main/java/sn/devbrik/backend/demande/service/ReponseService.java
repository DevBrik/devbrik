package sn.devbrik.backend.demande.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.Brique.service.BriqueReference;   // membre 2 (hypothèse de paquet)
import sn.devbrik.backend.Brique.service.BriqueService;     // membre 2 (hypothèse de paquet)
import sn.devbrik.backend.common.QuotaService;              // membre 1 (hypothèse de paquet)
import sn.devbrik.backend.common.TypeQuota;                 // membre 1 (hypothèse de paquet)
import sn.devbrik.backend.demande.dto.DemandeDetail;
import sn.devbrik.backend.demande.dto.NouvelleReponse;
import sn.devbrik.backend.demande.dto.ReponseDto;
import sn.devbrik.backend.demande.entity.Demande;
import sn.devbrik.backend.demande.entity.Reponse;
import sn.devbrik.backend.demande.repository.DemandeRepository;
import sn.devbrik.backend.demande.repository.ReponseRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ReponseService {

    private static final Duration FENETRE_QUOTA = Duration.ofHours(24);

    private final DemandeRepository demandeRepository;
    private final ReponseRepository reponseRepository;
    private final DemandeService demandeService;
    private final BriqueService briqueService;
    private final QuotaService quotaService;

    public ReponseService(DemandeRepository demandeRepository,
                          ReponseRepository reponseRepository,
                          DemandeService demandeService,
                          BriqueService briqueService,
                          QuotaService quotaService) {
        this.demandeRepository = demandeRepository;
        this.reponseRepository = reponseRepository;
        this.demandeService = demandeService;
        this.briqueService = briqueService;
        this.quotaService = quotaService;
    }

    // ---------------------------------------------------------------- création

    /** RG3 (validé par le DTO), RG13 (lien vers une brique publiée), RG15 (50 réponses par 24 h). */
    @Transactional
    public ReponseDto creer(Long idDemande, Long idUtilisateur, NouvelleReponse corps) {
        demandeService.trouver(idDemande);

        long dejaCreees = reponseRepository.countByIdAuteurAndDateCreationAfter(
                idUtilisateur, Instant.now().minus(FENETRE_QUOTA));
        quotaService.verifier(TypeQuota.REPONSE, dejaCreees);

        verifierBriqueLiee(corps.idBrique());

        Reponse reponse = reponseRepository.save(
                new Reponse(idDemande, idUtilisateur, corps.contenu(), corps.idBrique()));
        return demandeService.construireReponses(List.of(reponse)).getFirst();
    }

    // -------------------------------------------------------------- suppression

    /**
     * RG19 : auteur de la réponse ou administrateur.
     * RG12 : supprimer la réponse acceptée rouvre la demande, dans la même transaction.
     */
    @Transactional
    public void supprimer(Long idReponse, Long idUtilisateur, boolean administrateur) {
        Reponse reponse = trouverReponse(idReponse);
        if (!administrateur && !reponse.getIdAuteur().equals(idUtilisateur)) {
            throw ErreursDemande.accesRefuse();
        }
        if (reponse.isAcceptee()) {
            Demande demande = verrouillerDemande(reponse.getIdDemande());
            demande.rouvrir();
        }
        reponseRepository.delete(reponse);
    }

    // ------------------------------------------------- acceptation et retrait

    /**
     * RG12 : réponse.acceptee et demande.statut changent dans une seule transaction.
     * Seul l'auteur de la demande accepte, jamais sa propre réponse.
     */
    @Transactional
    public DemandeDetail accepter(Long idReponse, Long idUtilisateur) {
        Reponse reponse = trouverReponse(idReponse);
        Demande demande = verrouillerDemandeDeLAuteur(reponse, idUtilisateur);

        if (reponse.getIdAuteur().equals(idUtilisateur)) {
            throw ErreursDemande.acceptationPropreReponse();
        }

        Optional<Reponse> dejaAcceptee = reponseRepository.findByIdDemandeAndAccepteeTrue(demande.getId());
        if (dejaAcceptee.isPresent() && !dejaAcceptee.get().getId().equals(reponse.getId())) {
            throw ErreursDemande.reponseDejaAcceptee();
        }

        reponse.setAcceptee(true);
        demande.resoudre();
        try {
            reponseRepository.saveAndFlush(reponse);
            demandeRepository.saveAndFlush(demande);
        } catch (DataIntegrityViolationException e) {
            // Filet de sécurité : l'index uk_reponse_acceptee a refusé une deuxième acceptation.
            throw ErreursDemande.reponseDejaAcceptee();
        }
        return demandeService.detail(demande.getId());
    }

    /** RG12 : retirer l'acceptation rouvre la demande, dans une seule transaction. */
    @Transactional
    public DemandeDetail retirer(Long idReponse, Long idUtilisateur) {
        Reponse reponse = trouverReponse(idReponse);
        Demande demande = verrouillerDemandeDeLAuteur(reponse, idUtilisateur);

        if (reponse.isAcceptee()) {
            reponse.setAcceptee(false);
            demande.rouvrir();
            reponseRepository.saveAndFlush(reponse);
            demandeRepository.saveAndFlush(demande);
        }
        return demandeService.detail(demande.getId());
    }

    // ---------------------------------------------------------------- internes

    /** RG13 : la brique doit exister et être PUBLIEE (absente de la map = inexistante). */
    private void verifierBriqueLiee(Long idBrique) {
        if (idBrique == null) {
            return;
        }
        BriqueReference brique = briqueService.references(Set.of(idBrique)).get(idBrique);
        if (brique == null || !brique.publiee()) {
            throw ErreursDemande.briqueLieeInvalide();
        }
    }

    private Reponse trouverReponse(Long idReponse) {
        return reponseRepository.findById(idReponse)
                .orElseThrow(() -> ErreursDemande.introuvable("Réponse"));
    }

    private Demande verrouillerDemande(Long idDemande) {
        return demandeRepository.trouverPourMiseAJour(idDemande)
                .orElseThrow(() -> ErreursDemande.introuvable("Demande"));
    }

    /** Verrouille la demande puis vérifie que l'appelant en est l'auteur (même un administrateur : section 4). */
    private Demande verrouillerDemandeDeLAuteur(Reponse reponse, Long idUtilisateur) {
        Demande demande = verrouillerDemande(reponse.getIdDemande());
        if (!demande.getIdAuteur().equals(idUtilisateur)) {
            throw ErreursDemande.accesRefuse(
                    "Seul l'auteur de la demande peut accepter ou retirer une réponse.");
        }
        return demande;
    }
}
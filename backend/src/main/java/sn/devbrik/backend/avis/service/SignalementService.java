package sn.devbrik.backend.avis.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.avis.dto.SignalementReponse;
import sn.devbrik.backend.avis.dto.SignalementReponse.BriqueSignalee;
import sn.devbrik.backend.avis.dto.SignalementRequete;
import sn.devbrik.backend.avis.dto.TraitementRequete;
import sn.devbrik.backend.avis.entity.ActionTraitement;
import sn.devbrik.backend.avis.entity.SignalementBrique;
import sn.devbrik.backend.avis.entity.StatutSignalement;
import sn.devbrik.backend.avis.repository.SignalementBriqueRepository;
import sn.devbrik.backend.brique.service.BriqueService;
import sn.devbrik.backend.common.PageReponse;
import sn.devbrik.backend.common.QuotaService;
import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.common.TypeQuota;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SignalementService {

    private static final Duration FENETRE_QUOTA = Duration.ofHours(24);

    private final SignalementBriqueRepository repository;
    private final AccesBrique accesBrique;
    private final BriqueLecture briques;
    private final BriqueService briqueService;
    private final UtilisateurService utilisateurService;
    private final QuotaService quotaService;

    public SignalementService(SignalementBriqueRepository repository, AccesBrique accesBrique,
                              BriqueLecture briques, BriqueService briqueService,
                              UtilisateurService utilisateurService, QuotaService quotaService) {
        this.repository = repository;
        this.accesBrique = accesBrique;
        this.briques = briques;
        this.briqueService = briqueService;
        this.utilisateurService = utilisateurService;
        this.quotaService = quotaService;
    }

    /** RG11 : un signalement par utilisateur et par brique. */
    @Transactional
    public SignalementReponse signaler(Long idBrique, Long idAppelant, SignalementRequete requete) {
        accesBrique.briqueEvaluable(idBrique, idAppelant);

        if (repository.existsByIdBriqueAndIdAuteur(idBrique, idAppelant)) {
            throw ErreursAvis.signalementDejaEnvoye();
        }

        // RG15 : 10 signalements par 24 h glissantes.
        Instant depuis = Instant.now().minus(FENETRE_QUOTA);
        long actions = repository.countByIdAuteurAndDateCreationAfter(idAppelant, depuis);
        quotaService.verifier(TypeQuota.SIGNALEMENT, actions);

        SignalementBrique enregistre;
        try {
            enregistre = repository.saveAndFlush(
                    new SignalementBrique(idBrique, idAppelant, requete.motif(), requete.details()));
        } catch (DataIntegrityViolationException e) {
            // Deux requêtes simultanées : la contrainte uk_signalement_brique_auteur a tranché.
            throw ErreursAvis.signalementDejaEnvoye();
        }
        return versReponses(List.of(enregistre)).get(0);
    }

    /** Administrateur seulement. statut absent ou vide : OUVERT. */
    @Transactional(readOnly = true)
    public PageReponse<SignalementReponse> lister(boolean admin, String statut, Integer page, Integer taille) {
        verifierAdministrateur(admin);
        StatutSignalement filtre = lireStatut(statut);
        Page<SignalementBrique> resultat = repository.findByStatutOrderByDateCreationDescIdDesc(
                filtre, Pagination.pageable(page, taille));
        return Pagination.reponse(resultat, versReponses(resultat.getContent()));
    }

    /**
     * RG11 : seul un administrateur traite. MASQUER_BRIQUE appelle BriqueService.masquer
     * puis passe le signalement à TRAITE, dans la même transaction.
     */
    @Transactional
    public SignalementReponse traiter(Long idSignalement, boolean admin, TraitementRequete requete) {
        verifierAdministrateur(admin);
        SignalementBrique signalement = repository.findById(idSignalement)
                .orElseThrow(ErreursAvis::signalementIntrouvable);
        if (signalement.getStatut() == StatutSignalement.TRAITE) {
            throw ErreursAvis.signalementDejaTraite();
        }

        if (requete.action() == ActionTraitement.MASQUER_BRIQUE) {
            InfoBrique brique = briques.trouver(signalement.getIdBrique())
                    .orElseThrow(ErreursAvis::briqueIntrouvable);
            // Une brique déjà masquée (autre signalement traité avant) n'a pas besoin d'être masquée deux fois.
            if (!brique.estMasquee()) {
                briqueService.masquer(signalement.getIdBrique());
            }
        }

        signalement.traiter(requete.action());
        // saveAndFlush écrit aussi le statut de la brique modifié par BriqueService.masquer (même transaction).
        return versReponses(List.of(repository.saveAndFlush(signalement))).get(0);
    }

    private static void verifierAdministrateur(boolean admin) {
        if (!admin) {
            throw ErreursAvis.reserveAdministrateur();
        }
    }

    private static StatutSignalement lireStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return StatutSignalement.OUVERT;
        }
        try {
            return StatutSignalement.valueOf(statut.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ErreursAvis.requeteInvalide("Le statut doit être OUVERT ou TRAITE.");
        }
    }

    private List<SignalementReponse> versReponses(List<SignalementBrique> signalements) {
        if (signalements.isEmpty()) {
            return List.of();
        }
        // Un seul chargement pour toutes les briques de la page.
        Set<Long> idsBriques = signalements.stream().map(SignalementBrique::getIdBrique)
                .collect(Collectors.toSet());
        Map<Long, InfoBrique> infos = briques.parLot(idsBriques);

        // Un seul appel pour tous les utilisateurs : ceux qui ont signalé et les auteurs des briques.
        Set<Long> idsUtilisateurs = new HashSet<>();
        signalements.forEach(s -> idsUtilisateurs.add(s.getIdAuteur()));
        infos.values().forEach(b -> idsUtilisateurs.add(b.idAuteur()));
        Map<Long, ReferenceUtilisateur> utilisateurs = utilisateurService.references(idsUtilisateurs);

        return signalements.stream().map(s -> {
            InfoBrique b = infos.get(s.getIdBrique());
            BriqueSignalee brique = b == null ? null
                    : new BriqueSignalee(b.id(), b.titre(), b.statut(), utilisateurs.get(b.idAuteur()));
            return new SignalementReponse(s.getId(), s.getMotif(), s.getDetails(), s.getStatut(),
                    s.getActionTraitement(), s.getDateCreation(), s.getDateTraitement(),
                    brique, utilisateurs.get(s.getIdAuteur()));
        }).toList();
    }
}
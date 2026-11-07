package sn.devbrik.backend.avis.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.avis.dto.AvisReponse;
import sn.devbrik.backend.avis.dto.AvisRequete;
import sn.devbrik.backend.avis.dto.ResultatEcriture;
import sn.devbrik.backend.avis.entity.Avis;
import sn.devbrik.backend.avis.repository.AvisRepository;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.common.PageReponse;
import sn.devbrik.backend.common.QuotaService;
import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.common.TypeQuota;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvisService {

    private static final Duration FENETRE_QUOTA = Duration.ofHours(24);

    private final AvisRepository avisRepository;
    private final AccesBrique accesBrique;
    private final UtilisateurService utilisateurService;
    private final QuotaService quotaService;

    public AvisService(AvisRepository avisRepository, AccesBrique accesBrique,
                       UtilisateurService utilisateurService, QuotaService quotaService) {
        this.avisRepository = avisRepository;
        this.accesBrique = accesBrique;
        this.utilisateurService = utilisateurService;
        this.quotaService = quotaService;
    }

    /** RG8 : crée ou met à jour l'avis de l'appelant sur la brique. */
    @Transactional
    public ResultatEcriture<AvisReponse> enregistrer(Long idBrique, Long idAppelant, AvisRequete requete) {
        accesBrique.briqueEvaluable(idBrique, idAppelant);
        Instant depuis = Instant.now().minus(FENETRE_QUOTA);

        Optional<Avis> existant = avisRepository.findByIdBriqueAndIdAuteur(idBrique, idAppelant);
        if (existant.isPresent()) {
            Avis avis = existant.get();
            // Un avis déjà modifié dans les dernières 24 h est déjà compté : pas de quota en plus.
            if (avis.getDateModification().isBefore(depuis)) {
                verifierQuota(idAppelant, depuis);
            }
            avis.modifier(requete.note(), requete.commentaire());
            return new ResultatEcriture<>(versReponse(avisRepository.saveAndFlush(avis)), false);
        }

        verifierQuota(idAppelant, depuis);
        Avis cree = avisRepository.saveAndFlush(
                new Avis(idBrique, idAppelant, requete.note(), requete.commentaire()));
        return new ResultatEcriture<>(versReponse(cree), true);
    }

    @Transactional(readOnly = true)
    public PageReponse<AvisReponse> lister(Long idBrique, Long idAppelant, boolean admin,
                                           Integer page, Integer taille) {
        accesBrique.briqueVisible(idBrique, idAppelant, admin);
        Page<Avis> resultat = avisRepository.findByIdBriqueOrderByDateCreationDescIdDesc(
                idBrique, Pagination.pageable(page, taille));
        return Pagination.reponse(resultat, versReponses(resultat.getContent()));
    }

    /** RG19 : auteur ou administrateur. */
    @Transactional
    public void supprimer(Long idAvis, Long idAppelant, boolean admin) {
        Avis avis = avisRepository.findById(idAvis).orElseThrow(ErreursAvis::avisIntrouvable);
        if (!admin && !avis.getIdAuteur().equals(idAppelant)) {
            throw ErreursAvis.accesRefuse();
        }
        avisRepository.delete(avis);
    }

    @Transactional(readOnly = true)
    public Optional<AvisReponse> monAvis(Long idBrique, Long idAppelant) {
        return avisRepository.findByIdBriqueAndIdAuteur(idBrique, idAppelant).map(this::versReponse);
    }

    private void verifierQuota(Long idAppelant, Instant depuis) {
        long actions = avisRepository.countByIdAuteurAndDateModificationAfter(idAppelant, depuis);
        quotaService.verifier(TypeQuota.AVIS, actions);
    }

    private AvisReponse versReponse(Avis avis) {
        return versReponses(List.of(avis)).get(0);
    }

    private List<AvisReponse> versReponses(List<Avis> avis) {
        if (avis.isEmpty()) {
            return List.of();
        }
        Set<Long> idsAuteurs = avis.stream().map(Avis::getIdAuteur).collect(Collectors.toSet());
        Map<Long, ReferenceUtilisateur> auteurs = utilisateurService.references(idsAuteurs);
        return avis.stream()
                .map(a -> new AvisReponse(a.getId(), a.getNote(), a.getCommentaire(),
                        auteurs.get(a.getIdAuteur()), a.getDateCreation(), a.getDateModification()))
                .toList();
    }
}
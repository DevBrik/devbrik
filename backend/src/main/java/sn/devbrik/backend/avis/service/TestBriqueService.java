package sn.devbrik.backend.avis.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.avis.dto.ResultatEcriture;
import sn.devbrik.backend.avis.dto.TestReponse;
import sn.devbrik.backend.avis.dto.TestRequete;
import sn.devbrik.backend.avis.entity.TestBrique;
import sn.devbrik.backend.avis.repository.TestBriqueRepository;
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
public class TestBriqueService {

    private static final Duration FENETRE_QUOTA = Duration.ofHours(24);

    private final TestBriqueRepository testRepository;
    private final AccesBrique accesBrique;
    private final UtilisateurService utilisateurService;
    private final QuotaService quotaService;

    public TestBriqueService(TestBriqueRepository testRepository, AccesBrique accesBrique,
                             UtilisateurService utilisateurService, QuotaService quotaService) {
        this.testRepository = testRepository;
        this.accesBrique = accesBrique;
        this.utilisateurService = utilisateurService;
        this.quotaService = quotaService;
    }

    /** RG9 : un test par utilisateur et par brique ; la version enregistrée est la version actuelle. */
    @Transactional
    public ResultatEcriture<TestReponse> enregistrer(Long idBrique, Long idAppelant, TestRequete requete) {
        InfoBrique brique = accesBrique.briqueEvaluable(idBrique, idAppelant);
        Integer versionActuelle = brique.versionCode();
        Instant depuis = Instant.now().minus(FENETRE_QUOTA);

        Optional<TestBrique> existant = testRepository.findByIdBriqueAndIdAuteur(idBrique, idAppelant);
        if (existant.isPresent()) {
            TestBrique test = existant.get();
            if (test.getDateTest().isBefore(depuis)) {
                verifierQuota(idAppelant, depuis);
            }
            test.changer(requete.resultat(), requete.environnement(), versionActuelle);
            return new ResultatEcriture<>(versReponse(testRepository.saveAndFlush(test)), false);
        }

        verifierQuota(idAppelant, depuis);
        TestBrique cree = testRepository.saveAndFlush(new TestBrique(
                idBrique, idAppelant, requete.resultat(), requete.environnement(), versionActuelle));
        return new ResultatEcriture<>(versReponse(cree), true);
    }

    /** RG9 : seuls les tests de la version actuelle sont listés. */
    @Transactional(readOnly = true)
    public PageReponse<TestReponse> lister(Long idBrique, Long idAppelant, boolean admin,
                                           Integer page, Integer taille) {
        InfoBrique brique = accesBrique.briqueVisible(idBrique, idAppelant, admin);
        Page<TestBrique> resultat = testRepository.findByIdBriqueAndVersionCodeOrderByDateTestDescIdDesc(
                idBrique, brique.versionCode(), Pagination.pageable(page, taille));
        return Pagination.reponse(resultat, versReponses(resultat.getContent()));
    }

    /** Retire le test de l'appelant (une ligne par utilisateur et par brique). */
    @Transactional
    public void supprimerMonTest(Long idBrique, Long idAppelant) {
        TestBrique test = testRepository.findByIdBriqueAndIdAuteur(idBrique, idAppelant)
                .orElseThrow(ErreursAvis::testIntrouvable);
        testRepository.delete(test);
    }

    /** Renvoie le test de l'appelant, même s'il porte sur une ancienne version (versionCode dans la réponse). */
    @Transactional(readOnly = true)
    public Optional<TestReponse> monTest(Long idBrique, Long idAppelant) {
        return testRepository.findByIdBriqueAndIdAuteur(idBrique, idAppelant).map(this::versReponse);
    }

    private void verifierQuota(Long idAppelant, Instant depuis) {
        long actions = testRepository.countByIdAuteurAndDateTestAfter(idAppelant, depuis);
        quotaService.verifier(TypeQuota.TEST, actions);
    }

    private TestReponse versReponse(TestBrique test) {
        return versReponses(List.of(test)).get(0);
    }

    private List<TestReponse> versReponses(List<TestBrique> tests) {
        if (tests.isEmpty()) {
            return List.of();
        }
        Set<Long> idsAuteurs = tests.stream().map(TestBrique::getIdAuteur).collect(Collectors.toSet());
        Map<Long, ReferenceUtilisateur> auteurs = utilisateurService.references(idsAuteurs);
        return tests.stream()
                .map(t -> new TestReponse(t.getId(), t.getResultat(), t.getEnvironnement(),
                        t.getVersionCode(), auteurs.get(t.getIdAuteur()), t.getDateTest()))
                .toList();
    }
}
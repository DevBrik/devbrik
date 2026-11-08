package sn.devbrik.backend.avis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.avis.dto.SignalementRequete;
import sn.devbrik.backend.avis.dto.TraitementRequete;
import sn.devbrik.backend.avis.entity.ActionTraitement;
import sn.devbrik.backend.avis.entity.MotifSignalement;
import sn.devbrik.backend.avis.entity.SignalementBrique;
import sn.devbrik.backend.avis.entity.StatutSignalement;
import sn.devbrik.backend.avis.repository.SignalementBriqueRepository;
import sn.devbrik.backend.brique.entity.StatutBrique;
import sn.devbrik.backend.brique.service.BriqueService;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.QuotaService;
import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.common.TypeQuota;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalementServiceTest {

    private static final long BRIQUE = 1L;
    private static final long AUTEUR_BRIQUE = 99L;
    private static final long MOI = 10L;
    private static final long SIGNALEMENT = 7L;

    @Mock SignalementBriqueRepository repository;
    @Mock BriqueService briqueService; // seulement pour masquer
    @Mock BriqueLecture briques;
    @Mock UtilisateurService utilisateurService;
    @Mock QuotaService quotaService;

    SignalementService service;

    @BeforeEach
    void init() {
        service = new SignalementService(repository, new AccesBrique(briques), briques, briqueService,
                utilisateurService, quotaService);
    }

    private static InfoBrique brique(long idAuteur, StatutBrique statut) {
        return new InfoBrique(BRIQUE, idAuteur, statut.name(), 1, "Titre");
    }

    private void briqueAvecStatut(long idAuteur, StatutBrique statut) {
        InfoBrique info = brique(idAuteur, statut);
        lenient().when(briques.trouver(BRIQUE)).thenReturn(Optional.of(info));
        lenient().when(briques.parLot(anyCollection())).thenReturn(Map.of(BRIQUE, info));
    }

    private static SignalementBrique signalementOuvert() {
        return new SignalementBrique(BRIQUE, MOI, MotifSignalement.SPAM, "Publicité");
    }

    private static void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, String code) {
        assertThatThrownBy(action)
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode().name())
                .isEqualTo(code);
    }

    // ---------- POST : signaler ----------

    @Test
    void test_RG11_un_signalement_est_cree_a_l_etat_ouvert() {
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.PUBLIEE);
        when(repository.countByIdAuteurAndDateCreationAfter(eq(MOI), any(Instant.class))).thenReturn(3L);
        when(repository.saveAndFlush(any(SignalementBrique.class))).thenAnswer(i -> i.getArgument(0));

        var reponse = service.signaler(BRIQUE, MOI, new SignalementRequete(MotifSignalement.SPAM, "Publicité"));

        assertThat(reponse.statut()).isEqualTo(StatutSignalement.OUVERT);
        assertThat(reponse.motif()).isEqualTo(MotifSignalement.SPAM);
        assertThat(reponse.actionTraitement()).isNull();
        assertThat(reponse.brique().titre()).isEqualTo("Titre");
        verify(quotaService).verifier(TypeQuota.SIGNALEMENT, 3L);
    }

    @Test
    void test_RG11_le_motif_INAPPROPRIE_est_accepte() {
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.PUBLIEE);
        when(repository.saveAndFlush(any(SignalementBrique.class))).thenAnswer(i -> i.getArgument(0));

        var reponse = service.signaler(BRIQUE, MOI, new SignalementRequete(MotifSignalement.INAPPROPRIE, null));

        assertThat(reponse.motif()).isEqualTo(MotifSignalement.INAPPROPRIE);
    }

    @Test
    void test_RG11_deuxieme_signalement_du_meme_utilisateur_donne_409() {
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.PUBLIEE);
        when(repository.existsByIdBriqueAndIdAuteur(BRIQUE, MOI)).thenReturn(true);

        assertCode(() -> service.signaler(BRIQUE, MOI, new SignalementRequete(MotifSignalement.INEXACT, null)),
                "SIGNALEMENT_DEJA_ENVOYE");
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(quotaService);
    }

    @Test
    void test_RG11_deux_requetes_simultanees_la_contrainte_de_base_donne_409() {
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.PUBLIEE);
        when(repository.saveAndFlush(any(SignalementBrique.class)))
                .thenThrow(new DataIntegrityViolationException("uk_signalement_brique_auteur"));

        assertCode(() -> service.signaler(BRIQUE, MOI, new SignalementRequete(MotifSignalement.AUTRE, null)),
                "SIGNALEMENT_DEJA_ENVOYE");
    }

    @Test
    void test_RG15_quota_de_signalements_depasse_bloque_la_creation() {
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.PUBLIEE);
        doThrow(new IllegalStateException("quota")).when(quotaService)
                .verifier(any(TypeQuota.class), anyLong());

        assertThatThrownBy(() -> service.signaler(BRIQUE, MOI,
                new SignalementRequete(MotifSignalement.CODE_DANGEREUX, null)))
                .isInstanceOf(IllegalStateException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void test_RG11_signaler_sa_propre_brique_donne_403() {
        briqueAvecStatut(MOI, StatutBrique.PUBLIEE);

        assertCode(() -> service.signaler(BRIQUE, MOI, new SignalementRequete(MotifSignalement.SPAM, null)),
                "AUTO_EVALUATION_INTERDITE");
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(quotaService);
    }

    @Test
    void test_RG7_signaler_une_brique_masquee_d_un_autre_donne_404() {
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.MASQUEE);

        assertCode(() -> service.signaler(BRIQUE, MOI, new SignalementRequete(MotifSignalement.SPAM, null)),
                "RESSOURCE_INTROUVABLE");
    }

    @Test
    void test_RG11_signaler_une_brique_inexistante_donne_404() {
        assertCode(() -> service.signaler(BRIQUE, MOI, new SignalementRequete(MotifSignalement.SPAM, null)),
                "RESSOURCE_INTROUVABLE");
    }

    // ---------- GET admin : lister ----------

    @Test
    void test_RG11_un_non_administrateur_ne_peut_pas_lister_les_signalements() {
        assertCode(() -> service.lister(false, null, 0, 20), "ACCES_REFUSE");
        verifyNoInteractions(repository);
    }

    @Test
    void test_RG11_la_liste_administrateur_est_ouverte_par_defaut_avec_apercu_de_la_brique() {
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.PUBLIEE);
        when(repository.findByStatutOrderByDateCreationDescIdDesc(
                eq(StatutSignalement.OUVERT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(signalementOuvert())));

        var page = service.lister(true, null, 0, 20);

        assertThat(page.contenu()).hasSize(1);
        assertThat(page.contenu().get(0).brique().id()).isEqualTo(BRIQUE);
        assertThat(page.contenu().get(0).brique().statut()).isEqualTo("PUBLIEE");
    }

    @Test
    void test_RG11_la_liste_accepte_le_statut_traite() {
        when(repository.findByStatutOrderByDateCreationDescIdDesc(
                eq(StatutSignalement.TRAITE), any(Pageable.class))).thenReturn(Page.empty());

        service.lister(true, "TRAITE", 0, 20);

        verify(repository).findByStatutOrderByDateCreationDescIdDesc(
                eq(StatutSignalement.TRAITE), any(Pageable.class));
    }

    @Test
    void test_RG11_un_statut_inconnu_donne_400() {
        assertCode(() -> service.lister(true, "BIZARRE", 0, 20), "REQUETE_INVALIDE");
    }

    // ---------- contrat v1.1 : brique.auteur et chargement en lot ----------

    @Test
    void test_contrat_la_liste_porte_l_auteur_de_la_brique_et_celui_du_signalement() {
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.PUBLIEE);
        when(repository.findByStatutOrderByDateCreationDescIdDesc(
                eq(StatutSignalement.OUVERT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(signalementOuvert())));
        when(utilisateurService.references(anyCollection())).thenReturn(Map.of(
                AUTEUR_BRIQUE, new ReferenceUtilisateur(AUTEUR_BRIQUE, "papa.diao", "Papa Birane Diao"),
                MOI, new ReferenceUtilisateur(MOI, "ndieme", "Ndiémé Ndiaye")));

        var element = service.lister(true, null, 0, 20).contenu().get(0);

        assertThat(element.brique().auteur().pseudo()).isEqualTo("papa.diao");
        assertThat(element.auteur().pseudo()).isEqualTo("ndieme");
    }

    @Test
    void test_contrat_les_utilisateurs_sont_charges_en_un_seul_appel_pour_toute_la_page() {
        SignalementBrique s1 = new SignalementBrique(1L, 10L, MotifSignalement.SPAM, null);
        SignalementBrique s2 = new SignalementBrique(2L, 11L, MotifSignalement.AUTRE, null);
        when(briques.parLot(anyCollection())).thenReturn(Map.of(
                1L, new InfoBrique(1L, 99L, "PUBLIEE", 1, "A"),
                2L, new InfoBrique(2L, 98L, "PUBLIEE", 1, "B")));
        when(repository.findByStatutOrderByDateCreationDescIdDesc(
                eq(StatutSignalement.OUVERT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(s1, s2)));

        service.lister(true, null, 0, 20);

        verify(briques, times(1)).parLot(anyCollection());
        verify(utilisateurService, times(1)).references(
                argThat(ids -> ids.containsAll(List.of(10L, 11L, 98L, 99L))));
    }

    @Test
    void test_contrat_une_brique_introuvable_donne_brique_nulle_sans_planter() {
        when(repository.findByStatutOrderByDateCreationDescIdDesc(
                eq(StatutSignalement.OUVERT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(signalementOuvert())));

        var element = service.lister(true, null, 0, 20).contenu().get(0);

        assertThat(element.brique()).isNull();
    }

    // ---------- PUT admin : traitement ----------

    @Test
    void test_RG11_un_non_administrateur_ne_peut_pas_traiter() {
        assertCode(() -> service.traiter(SIGNALEMENT, false, new TraitementRequete(ActionTraitement.CONSERVER)),
                "ACCES_REFUSE");
        verifyNoInteractions(repository, briqueService);
    }

    @Test
    void test_RG11_masquer_brique_appelle_masquer_puis_passe_le_signalement_a_traite() {
        SignalementBrique signalement = signalementOuvert();
        when(repository.findById(SIGNALEMENT)).thenReturn(Optional.of(signalement));
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.PUBLIEE);
        when(repository.saveAndFlush(signalement)).thenReturn(signalement);

        var reponse = service.traiter(SIGNALEMENT, true, new TraitementRequete(ActionTraitement.MASQUER_BRIQUE));

        InOrder ordre = inOrder(briqueService, repository);
        ordre.verify(briqueService).masquer(BRIQUE);
        ordre.verify(repository).saveAndFlush(signalement);
        assertThat(signalement.getStatut()).isEqualTo(StatutSignalement.TRAITE);
        assertThat(signalement.getDateTraitement()).isNotNull();
        assertThat(reponse.actionTraitement()).isEqualTo(ActionTraitement.MASQUER_BRIQUE);
    }

    @Test
    void test_RG11_conserver_ne_masque_pas_la_brique() {
        SignalementBrique signalement = signalementOuvert();
        when(repository.findById(SIGNALEMENT)).thenReturn(Optional.of(signalement));
        when(repository.saveAndFlush(signalement)).thenReturn(signalement);

        var reponse = service.traiter(SIGNALEMENT, true, new TraitementRequete(ActionTraitement.CONSERVER));

        verify(briqueService, never()).masquer(anyLong());
        assertThat(signalement.getStatut()).isEqualTo(StatutSignalement.TRAITE);
        assertThat(reponse.actionTraitement()).isEqualTo(ActionTraitement.CONSERVER);
    }

    @Test
    void test_RG11_masquer_une_brique_deja_masquee_ne_rappelle_pas_masquer() {
        SignalementBrique signalement = signalementOuvert();
        when(repository.findById(SIGNALEMENT)).thenReturn(Optional.of(signalement));
        briqueAvecStatut(AUTEUR_BRIQUE, StatutBrique.MASQUEE);
        when(repository.saveAndFlush(signalement)).thenReturn(signalement);

        service.traiter(SIGNALEMENT, true, new TraitementRequete(ActionTraitement.MASQUER_BRIQUE));

        verify(briqueService, never()).masquer(anyLong());
        assertThat(signalement.getStatut()).isEqualTo(StatutSignalement.TRAITE);
    }

    @Test
    void test_RG11_un_signalement_deja_traite_ne_se_traite_pas_deux_fois() {
        SignalementBrique signalement = signalementOuvert();
        signalement.traiter(ActionTraitement.CONSERVER);
        when(repository.findById(SIGNALEMENT)).thenReturn(Optional.of(signalement));

        assertCode(() -> service.traiter(SIGNALEMENT, true, new TraitementRequete(ActionTraitement.MASQUER_BRIQUE)),
                "REQUETE_INVALIDE");
        verify(briqueService, never()).masquer(anyLong());
    }

    @Test
    void test_RG11_traiter_un_signalement_inconnu_donne_404() {
        assertCode(() -> service.traiter(404L, true, new TraitementRequete(ActionTraitement.CONSERVER)),
                "RESSOURCE_INTROUVABLE");
    }
}
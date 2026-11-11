package sn.devbrik.backend.demande.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.brique.dto.BriqueReference;
import sn.devbrik.backend.brique.service.BriqueService;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;
import sn.devbrik.backend.common.QuotaService;
import sn.devbrik.backend.common.TypeQuota;
import sn.devbrik.backend.demande.dto.NouvelleReponse;
import sn.devbrik.backend.demande.dto.ReponseDto;
import sn.devbrik.backend.demande.entity.Demande;
import sn.devbrik.backend.demande.entity.Reponse;
import sn.devbrik.backend.demande.entity.StatutDemande;
import sn.devbrik.backend.demande.repository.DemandeRepository;
import sn.devbrik.backend.demande.repository.ReponseRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReponseServiceTest {

    private static final long AUTEUR_DEMANDE = 5L;
    private static final long REPONDANT = 6L;
    private static final long TIERS = 7L;
    private static final long ADMIN = 99L;

    @Mock private DemandeRepository demandeRepository;
    @Mock private ReponseRepository reponseRepository;
    @Mock private DemandeService demandeService;
    @Mock private BriqueService briqueService;
    @Mock private QuotaService quotaService;
    @InjectMocks private ReponseService service;

    // ----------------------------------------------------------------- outils

    private static CodeErreur codeDe(Throwable e) {
        return ((ApiException) e).getCode();
    }

    private static Demande demande(long id, long idAuteur, StatutDemande statut) {
        Demande d = new Demande(idAuteur, "Valider un numéro sénégalais",
                "Je cherche une brique qui valide et normalise les numéros.", null, null);
        ReflectionTestUtils.setField(d, "id", id);
        ReflectionTestUtils.setField(d, "dateCreation", Instant.now());
        if (statut == StatutDemande.RESOLUE) {
            d.resoudre();
        }
        return d;
    }

    private static Reponse reponse(long id, long idDemande, long idAuteur, boolean acceptee) {
        Reponse r = new Reponse(idDemande, idAuteur, "Voici une brique qui fait cela.", null);
        ReflectionTestUtils.setField(r, "id", id);
        ReflectionTestUtils.setField(r, "dateCreation", Instant.now());
        r.setAcceptee(acceptee);
        return r;
    }

    private void donnerReponseEtDemande(Reponse reponse, Demande demande) {
        when(reponseRepository.findById(reponse.getId())).thenReturn(Optional.of(reponse));
        when(demandeRepository.trouverPourMiseAJour(demande.getId())).thenReturn(Optional.of(demande));
    }

    // ---------------------------------------------------------------- création

    @Test
    void creer_enregistre_la_reponse_et_verifie_le_quota() {
        when(demandeService.trouver(1L)).thenReturn(demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE));
        when(reponseRepository.save(any(Reponse.class))).thenAnswer(inv -> inv.getArgument(0));
        ReponseDto attendu = new ReponseDto(1L, "c", false, Instant.now(), null, null);
        when(demandeService.construireReponses(anyList())).thenReturn(List.of(attendu));

        ReponseDto resultat = service.creer(1L, REPONDANT, new NouvelleReponse("Voici une brique pour cela.", null));

        assertThat(resultat).isSameAs(attendu);
        verify(quotaService).verifier(TypeQuota.REPONSE, 0L);
        verify(briqueService, never()).references(anyCollection());
        ArgumentCaptor<Reponse> captor = ArgumentCaptor.forClass(Reponse.class);
        verify(reponseRepository).save(captor.capture());
        assertThat(captor.getValue().getIdDemande()).isEqualTo(1L);
        assertThat(captor.getValue().getIdAuteur()).isEqualTo(REPONDANT);
        assertThat(captor.getValue().isAcceptee()).isFalse();
    }

    @Test
    void test_RG15_creer_est_refuse_sans_rien_enregistrer_quand_le_quota_est_depasse() {
        when(demandeService.trouver(1L)).thenReturn(demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE));
        when(reponseRepository.countByIdAuteurAndDateCreationAfter(anyLong(), any(Instant.class))).thenReturn(50L);
        doThrow(new RuntimeException("quota")).when(quotaService).verifier(TypeQuota.REPONSE, 50L);

        assertThatThrownBy(() -> service.creer(1L, REPONDANT, new NouvelleReponse("Voici une brique pour cela.", null)))
                .hasMessage("quota");
        verify(reponseRepository, never()).save(any());
    }

    @Test
    void test_RG13_un_lien_vers_une_brique_inexistante_est_refuse() {
        when(demandeService.trouver(1L)).thenReturn(demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE));
        when(briqueService.references(anyCollection())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.creer(1L, REPONDANT, new NouvelleReponse("Voici une brique pour cela.", 42L)))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.BRIQUE_LIEE_INVALIDE));
        verify(reponseRepository, never()).save(any());
    }

    @Test
    void test_RG13_un_lien_vers_une_brique_masquee_est_refuse() {
        when(demandeService.trouver(1L)).thenReturn(demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE));
        when(briqueService.references(anyCollection()))
                .thenReturn(Map.of(42L, new BriqueReference(42L, "Titre", false)));

        assertThatThrownBy(() -> service.creer(1L, REPONDANT, new NouvelleReponse("Voici une brique pour cela.", 42L)))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.BRIQUE_LIEE_INVALIDE));
        verify(reponseRepository, never()).save(any());
    }

    @Test
    void test_RG13_un_lien_vers_une_brique_publiee_est_accepte() {
        when(demandeService.trouver(1L)).thenReturn(demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE));
        when(briqueService.references(anyCollection()))
                .thenReturn(Map.of(42L, new BriqueReference(42L, "Titre", true)));
        when(reponseRepository.save(any(Reponse.class))).thenAnswer(inv -> inv.getArgument(0));
        when(demandeService.construireReponses(anyList()))
        .thenReturn(List.of(new ReponseDto(1L, "c", false, Instant.now(), null, null)));
        
        service.creer(1L, REPONDANT, new NouvelleReponse("Voici une brique pour cela.", 42L));

        ArgumentCaptor<Reponse> captor = ArgumentCaptor.forClass(Reponse.class);
        verify(reponseRepository).save(captor.capture());
        assertThat(captor.getValue().getIdBrique()).isEqualTo(42L);
    }

    // -------------------------------------------------------------- acceptation

    @Test
    void test_RG12_accepter_marque_la_reponse_et_resout_la_demande() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE);
        Reponse reponse = reponse(10L, 1L, REPONDANT, false);
        donnerReponseEtDemande(reponse, demande);

        service.accepter(10L, AUTEUR_DEMANDE);

        assertThat(reponse.isAcceptee()).isTrue();
        assertThat(demande.getStatut()).isEqualTo(StatutDemande.RESOLUE);
        verify(reponseRepository).saveAndFlush(reponse);
        verify(demandeRepository).saveAndFlush(demande);
        verify(demandeService).detail(1L);
    }

    @Test
    void test_RG12_l_auto_acceptation_est_refusee() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE);
        Reponse saPropreReponse = reponse(10L, 1L, AUTEUR_DEMANDE, false);
        donnerReponseEtDemande(saPropreReponse, demande);

        assertThatThrownBy(() -> service.accepter(10L, AUTEUR_DEMANDE))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.ACCEPTATION_PROPRE_REPONSE));
        assertThat(saPropreReponse.isAcceptee()).isFalse();
        assertThat(demande.getStatut()).isEqualTo(StatutDemande.OUVERTE);
        verify(reponseRepository, never()).saveAndFlush(any());
    }

    @Test
    void test_RG12_seul_l_auteur_de_la_demande_accepte_meme_un_administrateur() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE);
        Reponse reponse = reponse(10L, 1L, REPONDANT, false);
        donnerReponseEtDemande(reponse, demande);

        assertThatThrownBy(() -> service.accepter(10L, TIERS))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.ACCES_REFUSE));
        assertThatThrownBy(() -> service.accepter(10L, ADMIN))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.ACCES_REFUSE));
        assertThat(reponse.isAcceptee()).isFalse();
    }

    @Test
    void test_RG12_deux_reponses_acceptees_pour_une_demande_sont_impossibles() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.RESOLUE);
        Reponse dejaAcceptee = reponse(11L, 1L, TIERS, true);
        Reponse nouvelle = reponse(10L, 1L, REPONDANT, false);
        donnerReponseEtDemande(nouvelle, demande);
        when(reponseRepository.findByIdDemandeAndAccepteeTrue(1L)).thenReturn(Optional.of(dejaAcceptee));

        assertThatThrownBy(() -> service.accepter(10L, AUTEUR_DEMANDE))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.REPONSE_DEJA_ACCEPTEE));
        assertThat(nouvelle.isAcceptee()).isFalse();
        verify(reponseRepository, never()).saveAndFlush(any());
    }

    @Test
    void test_RG12_un_conflit_concurrent_de_l_index_d_unicite_donne_409() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.OUVERTE);
        Reponse reponse = reponse(10L, 1L, REPONDANT, false);
        donnerReponseEtDemande(reponse, demande);
        when(reponseRepository.saveAndFlush(reponse))
                .thenThrow(new DataIntegrityViolationException("uk_reponse_acceptee"));

        assertThatThrownBy(() -> service.accepter(10L, AUTEUR_DEMANDE))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.REPONSE_DEJA_ACCEPTEE));
    }

    @Test
    void accepter_deux_fois_la_meme_reponse_est_sans_effet_et_sans_erreur() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.RESOLUE);
        Reponse reponse = reponse(10L, 1L, REPONDANT, true);
        donnerReponseEtDemande(reponse, demande);
        when(reponseRepository.findByIdDemandeAndAccepteeTrue(1L)).thenReturn(Optional.of(reponse));

        assertThatCode(() -> service.accepter(10L, AUTEUR_DEMANDE)).doesNotThrowAnyException();
        assertThat(reponse.isAcceptee()).isTrue();
        assertThat(demande.getStatut()).isEqualTo(StatutDemande.RESOLUE);
    }

    @Test
    void accepter_une_reponse_inconnue_donne_ressource_introuvable() {
        when(reponseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.accepter(404L, AUTEUR_DEMANDE))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.RESSOURCE_INTROUVABLE));
    }

    // ------------------------------------------------------------------ retrait

    @Test
    void test_RG12_retirer_l_acceptation_rouvre_la_demande() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.RESOLUE);
        Reponse reponse = reponse(10L, 1L, REPONDANT, true);
        donnerReponseEtDemande(reponse, demande);

        service.retirer(10L, AUTEUR_DEMANDE);

        assertThat(reponse.isAcceptee()).isFalse();
        assertThat(demande.getStatut()).isEqualTo(StatutDemande.OUVERTE);
        verify(reponseRepository).saveAndFlush(reponse);
        verify(demandeRepository).saveAndFlush(demande);
        verify(demandeService).detail(1L);
    }

    @Test
    void test_RG12_seul_l_auteur_de_la_demande_retire_l_acceptation() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.RESOLUE);
        Reponse reponse = reponse(10L, 1L, REPONDANT, true);
        donnerReponseEtDemande(reponse, demande);

        assertThatThrownBy(() -> service.retirer(10L, TIERS))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.ACCES_REFUSE));
        assertThat(reponse.isAcceptee()).isTrue();
        assertThat(demande.getStatut()).isEqualTo(StatutDemande.RESOLUE);
    }

    @Test
    void test_RG12_accepter_et_retirer_s_executent_dans_une_seule_transaction() throws NoSuchMethodException {
        assertThat(ReponseService.class.getMethod("accepter", Long.class, Long.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(ReponseService.class.getMethod("retirer", Long.class, Long.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }

    // -------------------------------------------------------------- suppression

    @Test
    void test_RG19_un_non_auteur_ne_peut_pas_supprimer_une_reponse() {
        when(reponseRepository.findById(10L)).thenReturn(Optional.of(reponse(10L, 1L, REPONDANT, false)));

        assertThatThrownBy(() -> service.supprimer(10L, TIERS, false))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.ACCES_REFUSE));
        verify(reponseRepository, never()).delete(any());
    }

    @Test
    void test_RG19_l_auteur_peut_supprimer_sa_reponse() {
        Reponse reponse = reponse(10L, 1L, REPONDANT, false);
        when(reponseRepository.findById(10L)).thenReturn(Optional.of(reponse));

        service.supprimer(10L, REPONDANT, false);

        verify(reponseRepository).delete(reponse);
        verify(demandeRepository, never()).trouverPourMiseAJour(anyLong());
    }

    @Test
    void test_RG19_un_administrateur_peut_supprimer_la_reponse_d_un_autre() {
        Reponse reponse = reponse(10L, 1L, REPONDANT, false);
        when(reponseRepository.findById(10L)).thenReturn(Optional.of(reponse));

        service.supprimer(10L, ADMIN, true);

        verify(reponseRepository).delete(reponse);
    }

    @Test
    void test_RG12_supprimer_la_reponse_acceptee_rouvre_la_demande() {
        Demande demande = demande(1L, AUTEUR_DEMANDE, StatutDemande.RESOLUE);
        Reponse reponse = reponse(10L, 1L, REPONDANT, true);
        donnerReponseEtDemande(reponse, demande);

        service.supprimer(10L, REPONDANT, false);

        assertThat(demande.getStatut()).isEqualTo(StatutDemande.OUVERTE);
        verify(reponseRepository).delete(reponse);
    }

    @Test
    void supprimer_une_reponse_inconnue_donne_ressource_introuvable() {
        when(reponseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.supprimer(404L, REPONDANT, false))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(codeDe(e)).isEqualTo(CodeErreur.RESSOURCE_INTROUVABLE));
    }
}
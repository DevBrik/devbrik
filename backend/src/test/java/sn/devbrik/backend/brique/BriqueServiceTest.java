package sn.devbrik.backend.brique;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.avis.service.StatsBriqueService;
import sn.devbrik.backend.brique.dto.*;
import sn.devbrik.backend.brique.entity.*;
import sn.devbrik.backend.brique.repository.*;
import sn.devbrik.backend.brique.repository.BriqueRecherche.Critere;
import sn.devbrik.backend.brique.repository.BriqueRecherche.Tri;
import sn.devbrik.backend.brique.service.BriqueServiceImpl;
import sn.devbrik.backend.common.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du BriqueService — RG3 à RG7, RG15, RG16, RG19 et services publics (13.4).
 */
@ExtendWith(MockitoExtension.class)
class BriqueServiceTest {

    @Mock BriqueRepository    briqueRepo;
    @Mock BriqueRecherche     recherche;
    @Mock CategorieRepository catRepo;
    @Mock LangageRepository   langRepo;
    @Mock UtilisateurService  utilisateurService;
    @Mock StatsBriqueService  statsService;
    @Mock QuotaService        quotaService;

    @InjectMocks BriqueServiceImpl service;

    private Categorie cat;
    private Langage   lang;

    @BeforeEach
    void setUp() {
        cat  = new Categorie("outils", "Outils", "");
        lang = new Langage("java", "Java");
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private void connecterComme(Long id, String role) {
        // On mock IdentiteUtilisateur.obligatoire() via SecurityContext — ici simplifié :
        // dans un vrai test Spring Security on utilise @WithMockUser.
        // Pour ce test unitaire pur, on injecte via réflexion ou on teste à travers mocks.
    }

    private BriqueCreationDto dtoCreation(boolean droitsOk) {
        return new BriqueCreationDto(
            "Ma brique", "Un résumé", "public class A {}", "Exemple d'utilisation.",
            null, Licence.MIT, 1L, 1L, droitsOk
        );
    }

    private BriqueModificationDto dtoModification() {
        return new BriqueModificationDto(
            "Ma brique v2", "Résumé modifié", "public class B {}",
            "Nouveau mode d'emploi.", null, Licence.APACHE_2_0, 1L, 1L
        );
    }

    // ── RG5 — Unicité titre/auteur ────────────────────────────────────────────

    @Nested
    @DisplayName("RG5 — Titre unique par auteur")
    class Rg5Titre {

        @Test
        @DisplayName("titre déjà utilisé → TITRE_DEJA_UTILISE")
        void titreDuplique_lanceException() {
            when(briqueRepo.existsByIdAuteurAndTitreIgnoreCase(anyLong(), anyString())).thenReturn(true);
            when(briqueRepo.compterCreationsDepuis(anyLong(), any())).thenReturn(0L);

            // IdentiteUtilisateur.obligatoire() est statique — on mocke via mockStatic
            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                assertThatThrownBy(() -> service.publier(dtoCreation(true)))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.TITRE_DEJA_UTILISE);
            }
        }

        @Test
        @DisplayName("titre unique → pas d'exception")
        void titreUnique_OK() {
            when(briqueRepo.existsByIdAuteurAndTitreIgnoreCase(anyLong(), anyString())).thenReturn(false);
            when(catRepo.findById(1L)).thenReturn(Optional.of(cat));
            when(langRepo.findById(1L)).thenReturn(Optional.of(lang));
            when(briqueRepo.compterCreationsDepuis(anyLong(), any())).thenReturn(0L);
            when(briqueRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(utilisateurService.references(anyList()))
                .thenReturn(Map.of(1L, new ReferenceUtilisateur(1L, "dev1", "Dev One")));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                BriqueDetailDto result = service.publier(dtoCreation(true));
                assertThat(result.titre()).isEqualTo("Ma brique");
            }
        }
    }

    // ── RG4 — Licences permissives ────────────────────────────────────────────

    @Nested
    @DisplayName("RG4 — Licences permissives")
    class Rg4Licence {

        @Test
        @DisplayName("MIT accepté → création OK")
        void licenceMIT_acceptee() {
            when(briqueRepo.existsByIdAuteurAndTitreIgnoreCase(anyLong(), anyString())).thenReturn(false);
            when(catRepo.findById(1L)).thenReturn(Optional.of(cat));
            when(langRepo.findById(1L)).thenReturn(Optional.of(lang));
            when(briqueRepo.compterCreationsDepuis(anyLong(), any())).thenReturn(0L);
            when(briqueRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(utilisateurService.references(anyList()))
                .thenReturn(Map.of(1L, new ReferenceUtilisateur(1L, "dev1", "Dev One")));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                BriqueDetailDto result = service.publier(dtoCreation(true));
                assertThat(result.licence()).isEqualTo("MIT");
            }
        }

        @Test
        @DisplayName("Toutes les licences permissives sont valides")
        void toutesLicencesPermissives_sontValides() {
            // Vérifie que l'enum Licence ne contient pas GPL/LGPL/AGPL
            for (Licence lic : Licence.values()) {
                assertThat(lic.name()).doesNotContain("GPL");
            }
            assertThat(Licence.values()).hasSize(6);
        }
    }

    // ── RG15 — Quota 20 créations/24 h ────────────────────────────────────────

    @Nested
    @DisplayName("RG15 — Quota créations")
    class Rg15Quota {

        @Test
        @DisplayName("quota atteint → LIMITE_QUOTIDIENNE")
        void quotaAtteint_lanceException() {
            when(briqueRepo.compterCreationsDepuis(anyLong(), any())).thenReturn(20L);
            doThrow(new ApiException(CodeErreur.LIMITE_QUOTIDIENNE, "Quota atteint"))
                .when(quotaService).verifier(eq(TypeQuota.BRIQUE), eq(20L));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                assertThatThrownBy(() -> service.publier(dtoCreation(true)))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.LIMITE_QUOTIDIENNE);
            }
        }

        @Test
        @DisplayName("19 créations → pas de quota")
        void quotaNonAtteint_OK() {
            when(briqueRepo.compterCreationsDepuis(anyLong(), any())).thenReturn(19L);
            when(briqueRepo.existsByIdAuteurAndTitreIgnoreCase(anyLong(), anyString())).thenReturn(false);
            when(catRepo.findById(1L)).thenReturn(Optional.of(cat));
            when(langRepo.findById(1L)).thenReturn(Optional.of(lang));
            when(briqueRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(utilisateurService.references(anyList()))
                .thenReturn(Map.of(1L, new ReferenceUtilisateur(1L, "dev1", "Dev One")));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                assertThatNoException().isThrownBy(() -> service.publier(dtoCreation(true)));
            }
        }
    }

    // ── RG19 — Auteur seul peut modifier ─────────────────────────────────────

    @Nested
    @DisplayName("RG19 — Modification par l'auteur uniquement")
    class Rg19Modification {

        @Test
        @DisplayName("non-auteur → ACCES_REFUSE")
        void nonAuteur_modifierRefuse() {
            Brique b = new Brique("titre", "resume", "code", "mode", null,
                                  Licence.MIT, 2L, cat, lang); // auteur = 2

            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR")); // ≠ auteur

                assertThatThrownBy(() -> service.modifier(1L, dtoModification()))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.ACCES_REFUSE);
            }
        }

        @Test
        @DisplayName("auteur légitime → modification OK")
        void auteur_modifierAccepte() {
            Brique b = new Brique("titre", "resume", "code", "mode", null,
                                  Licence.MIT, 1L, cat, lang);

            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));
            when(briqueRepo.existsByIdAuteurAndTitreIgnoreCaseAndIdNot(anyLong(), anyString(), anyLong()))
                .thenReturn(false);
            when(catRepo.findById(1L)).thenReturn(Optional.of(cat));
            when(langRepo.findById(1L)).thenReturn(Optional.of(lang));
            when(utilisateurService.references(anyList()))
                .thenReturn(Map.of(1L, new ReferenceUtilisateur(1L, "dev1", "Dev One")));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                BriqueDetailDto result = service.modifier(1L, dtoModification());
                assertThat(result.titre()).isEqualTo("Ma brique v2");
                assertThat(result.versionCode()).isEqualTo(2);
            }
        }
    }

    // ── RG7 — Masquage admin ─────────────────────────────────────────────────

    @Nested
    @DisplayName("RG7 — Masquage par admin")
    class Rg7Masquage {

        @Test
        @DisplayName("masquer → statut MASQUEE")
        void masquer_changeStatut() {
            Brique b = new Brique("titre", "r", "c", "m", null, Licence.MIT, 1L, cat, lang);
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));

            service.masquer(1L);

            assertThat(b.getStatut()).isEqualTo(StatutBrique.MASQUEE);
        }

        @Test
        @DisplayName("republier → statut PUBLIEE")
        void republier_changeStatut() {
            Brique b = new Brique("titre", "r", "c", "m", null, Licence.MIT, 1L, cat, lang);
            b.masquer();
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));

            service.republier(1L);

            assertThat(b.getStatut()).isEqualTo(StatutBrique.PUBLIEE);
        }
    }

    // ── RG19 — Suppression auteur ou admin ───────────────────────────────────

    @Nested
    @DisplayName("RG19 — Suppression auteur ou admin")
    class Rg19Suppression {

        private Brique briqueDAuteur2() {
            return new Brique("titre", "r", "c", "m", null, Licence.MIT, 2L, cat, lang);
        }

        @Test
        @DisplayName("auteur → suppression OK")
        void auteur_suppressionOK() {
            Brique b = new Brique("titre", "r", "c", "m", null, Licence.MIT, 1L, cat, lang);
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                assertThatNoException().isThrownBy(() -> service.supprimer(1L));
                verify(briqueRepo).delete(b);
            }
        }

        @Test
        @DisplayName("admin → suppression OK même si pas auteur")
        void admin_suppressionOK() {
            Brique b = briqueDAuteur2();
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(99L, "ADMIN"));

                assertThatNoException().isThrownBy(() -> service.supprimer(1L));
                verify(briqueRepo).delete(b);
            }
        }

        @Test
        @DisplayName("non-auteur non-admin → ACCES_REFUSE")
        void nonAuteurNonAdmin_refuse() {
            Brique b = briqueDAuteur2(); // auteur = 2
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                assertThatThrownBy(() -> service.supprimer(1L))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.ACCES_REFUSE);
            }
        }
    }

    // ── RG4 — Droits confirmés ────────────────────────────────────────────────

    @Nested
    @DisplayName("RG4 — Confirmation des droits")
    class Rg4Droits {

        @Test
        @DisplayName("droitsConfirmes faux → DROITS_NON_CONFIRMES, rien n'est enregistré")
        void droitsFaux_refuse() {
            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                assertThatThrownBy(() -> service.publier(dtoCreation(false)))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.DROITS_NON_CONFIRMES);
                verify(briqueRepo, never()).save(any());
            }
        }

        @Test
        @DisplayName("droitsConfirmes absent → DROITS_NON_CONFIRMES")
        void droitsAbsents_refuse() {
            BriqueCreationDto sansDroits = new BriqueCreationDto(
                "Ma brique", "Un résumé", "public class A {}", "Exemple d'utilisation.",
                null, Licence.MIT, 1L, 1L, null);

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(1L, "DEVELOPPEUR"));

                assertThatThrownBy(() -> service.publier(sansDroits))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.DROITS_NON_CONFIRMES);
            }
        }
    }

    // ── RG3 — Textes bruts rognés ─────────────────────────────────────────────

    @Nested
    @DisplayName("RG3 — Textes rognés avant validation")
    class Rg3Textes {

        @Test
        @DisplayName("les espaces autour des champs sont retirés, un prérequis vide devient null")
        void texteRogne() {
            BriqueCreationDto dto = new BriqueCreationDto(
                "  Ma brique  ", "  Un résumé assez long  ", "  code  ", "  Mode d'emploi  ",
                "   ", Licence.MIT, 1L, 1L, true);

            assertThat(dto.titre()).isEqualTo("Ma brique");
            assertThat(dto.resume()).isEqualTo("Un résumé assez long");
            assertThat(dto.code()).isEqualTo("code");
            assertThat(dto.modeEmploi()).isEqualTo("Mode d'emploi");
            assertThat(dto.prerequis()).isNull();
        }
    }

    // ── RG6 — Version du code ─────────────────────────────────────────────────

    @Nested
    @DisplayName("RG6 — Version du code")
    class Rg6Version {

        @Test
        @DisplayName("le code change → la version passe de 1 à 2")
        void codeChange_versionAugmente() {
            Brique b = new Brique("titre", "r", "code v1", "m", null, Licence.MIT, 1L, cat, lang);

            b.modifier("titre", "r", "code v2", "m", null, Licence.MIT, cat, lang);

            assertThat(b.getVersionCode()).isEqualTo(2);
        }

        @Test
        @DisplayName("seuls les autres champs changent → la version reste 1")
        void autresChamps_versionInchangee() {
            Brique b = new Brique("titre", "r", "code v1", "m", null, Licence.MIT, 1L, cat, lang);

            b.modifier("autre titre", "autre résumé", "code v1", "autre mode", "Java 21",
                       Licence.APACHE_2_0, cat, lang);

            assertThat(b.getVersionCode()).isEqualTo(1);
            assertThat(b.getTitre()).isEqualTo("autre titre");
        }
    }

    // ── RG7 — Visibilité ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("RG7 — Visibilité d'une brique MASQUEE")
    class Rg7Visibilite {

        private Brique masquee() {
            Brique b = new Brique("titre", "r", "c", "m", null, Licence.MIT, 2L, cat, lang);
            b.masquer();
            return b;
        }

        private void obtenirEn(Optional<IdentiteUtilisateur> identite, Runnable verification) {
            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::optionnelle).thenReturn(identite);
                verification.run();
            }
        }

        @Test
        @DisplayName("visiteur → RESSOURCE_INTROUVABLE")
        void visiteur_404() {
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(masquee()));

            obtenirEn(Optional.empty(), () ->
                assertThatThrownBy(() -> service.obtenir(1L))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.RESSOURCE_INTROUVABLE));
        }

        @Test
        @DisplayName("autre développeur → RESSOURCE_INTROUVABLE")
        void autreDeveloppeur_404() {
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(masquee()));

            obtenirEn(Optional.of(new IdentiteUtilisateur(1L, "DEVELOPPEUR")), () ->
                assertThatThrownBy(() -> service.obtenir(1L))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.RESSOURCE_INTROUVABLE));
        }

        @Test
        @DisplayName("auteur → fiche visible")
        void auteur_voit() {
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(masquee()));
            when(utilisateurService.references(anyList())).thenReturn(Map.of());

            obtenirEn(Optional.of(new IdentiteUtilisateur(2L, "DEVELOPPEUR")), () ->
                assertThat(service.obtenir(1L).statut()).isEqualTo("MASQUEE"));
        }

        @Test
        @DisplayName("administrateur → fiche visible")
        void admin_voit() {
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(masquee()));
            when(utilisateurService.references(anyList())).thenReturn(Map.of());

            obtenirEn(Optional.of(new IdentiteUtilisateur(99L, "ADMIN")), () ->
                assertThat(service.obtenir(1L).statut()).isEqualTo("MASQUEE"));
        }

        @Test
        @DisplayName("changerStatut(MASQUEE) puis (PUBLIEE) renvoie la brique à jour")
        void changerStatut() {
            Brique b = new Brique("titre", "r", "c", "m", null, Licence.MIT, 2L, cat, lang);
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));
            when(utilisateurService.references(anyList())).thenReturn(Map.of());

            assertThat(service.changerStatut(1L, StatutBrique.MASQUEE).statut()).isEqualTo("MASQUEE");
            assertThat(service.changerStatut(1L, StatutBrique.PUBLIEE).statut()).isEqualTo("PUBLIEE");
        }
    }

    // ── RG16 — Recherche, tris, pagination ────────────────────────────────────

    @Nested
    @DisplayName("RG16 — Recherche et pagination")
    class Rg16Recherche {

        @Test
        @DisplayName("recherche q d'un seul caractère → REQUETE_INVALIDE")
        void rechercheTropCourte() {
            assertThatThrownBy(() -> service.listerPubliees("a", null, null, null, null, null, null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode())
                .isEqualTo(CodeErreur.REQUETE_INVALIDE);
        }

        @Test
        @DisplayName("recherche q de plus de 100 caractères → REQUETE_INVALIDE")
        void rechercheTropLongue() {
            assertThatThrownBy(() -> service.listerPubliees("a".repeat(101), null, null, null, null, null, null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode())
                .isEqualTo(CodeErreur.REQUETE_INVALIDE);
        }

        @Test
        @DisplayName("tri inconnu → REQUETE_INVALIDE")
        void triInconnu() {
            assertThatThrownBy(() -> service.listerPubliees(null, null, null, null, "au-hasard", null, null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode())
                .isEqualTo(CodeErreur.REQUETE_INVALIDE);
        }

        @Test
        @DisplayName("page négative → REQUETE_INVALIDE")
        void pageNegative() {
            assertThatThrownBy(() -> service.listerPubliees(null, null, null, null, null, -1, null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode())
                .isEqualTo(CodeErreur.REQUETE_INVALIDE);
        }

        @Test
        @DisplayName("la liste publique ne cherche que les PUBLIEES, 20 par page par défaut")
        void listePubliqueParDefaut() {
            when(recherche.rechercher(any(), anyInt(), anyInt()))
                .thenReturn(new BriqueRecherche.Resultat(List.of(), 0));

            PageReponse<BriqueResumeDto> page =
                service.listerPubliees("téléphone", "validation-donnees", "java", null, "mieux-notees", null, null);

            ArgumentCaptor<Critere> critere = ArgumentCaptor.forClass(Critere.class);
            verify(recherche).rechercher(critere.capture(), eq(0), eq(20));
            assertThat(critere.getValue().statut()).isEqualTo(StatutBrique.PUBLIEE);
            assertThat(critere.getValue().q()).isEqualTo("téléphone");
            assertThat(critere.getValue().categorie()).isEqualTo("validation-donnees");
            assertThat(critere.getValue().langage()).isEqualTo("java");
            assertThat(critere.getValue().tri()).isEqualTo(Tri.MIEUX_NOTEES);
            assertThat(page.taille()).isEqualTo(20);
            assertThat(page.contenu()).isEmpty();
        }

        @Test
        @DisplayName("la taille est plafonnée à 50")
        void tailleMax() {
            when(recherche.rechercher(any(), anyInt(), anyInt()))
                .thenReturn(new BriqueRecherche.Resultat(List.of(), 0));

            service.listerPubliees(null, null, null, null, null, 0, 500);

            verify(recherche).rechercher(any(), eq(0), eq(50));
        }

        @Test
        @DisplayName("mes briques : filtre sur l'auteur connecté, MASQUEES comprises")
        void mesBriques() {
            when(recherche.rechercher(any(), anyInt(), anyInt()))
                .thenReturn(new BriqueRecherche.Resultat(List.of(), 0));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(7L, "DEVELOPPEUR"));

                service.mesBriques(null, null, null, null, null, null, null);
            }

            ArgumentCaptor<Critere> critere = ArgumentCaptor.forClass(Critere.class);
            verify(recherche).rechercher(critere.capture(), eq(0), eq(20));
            assertThat(critere.getValue().auteur()).isEqualTo(7L);
            assertThat(critere.getValue().statut()).isNull();
        }

        @Test
        @DisplayName("liste d'administration : un non-administrateur est refusé")
        void listeAdminRefusee() {
            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(7L, "DEVELOPPEUR"));

                assertThatThrownBy(() -> service.listerPourAdmin(null, null, null, null, null, null, null, null))
                    .isInstanceOf(ApiException.class)
                    .extracting(e -> ((ApiException) e).getCode())
                    .isEqualTo(CodeErreur.ACCES_REFUSE);
            }
        }

        @Test
        @DisplayName("liste d'administration : sans statut, les briques MASQUEE sont listées par défaut (11.4)")
        void listeAdminStatutParDefaut() {
            when(recherche.rechercher(any(), anyInt(), anyInt()))
                .thenReturn(new BriqueRecherche.Resultat(List.of(), 0));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(99L, "ADMIN"));

                service.listerPourAdmin(null, null, null, null, null, null, null, null);
            }

            ArgumentCaptor<Critere> critere = ArgumentCaptor.forClass(Critere.class);
            verify(recherche).rechercher(critere.capture(), eq(0), eq(20));
            assertThat(critere.getValue().statut()).isEqualTo(StatutBrique.MASQUEE);
        }

        @Test
        @DisplayName("liste d'administration : statut=PUBLIEE transmis à la recherche")
        void listeAdminStatutPubliee() {
            when(recherche.rechercher(any(), anyInt(), anyInt()))
                .thenReturn(new BriqueRecherche.Resultat(List.of(), 0));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(99L, "ADMIN"));

                service.listerPourAdmin(null, null, null, null, null, "PUBLIEE", null, null);
            }

            ArgumentCaptor<Critere> critere = ArgumentCaptor.forClass(Critere.class);
            verify(recherche).rechercher(critere.capture(), eq(0), eq(20));
            assertThat(critere.getValue().statut()).isEqualTo(StatutBrique.PUBLIEE);
        }

        @Test
        @DisplayName("liste d'administration : filtre statut=MASQUEE transmis à la recherche")
        void listeAdminFiltreStatut() {
            when(recherche.rechercher(any(), anyInt(), anyInt()))
                .thenReturn(new BriqueRecherche.Resultat(List.of(), 0));

            try (MockedStatic<IdentiteUtilisateur> ms = mockStatic(IdentiteUtilisateur.class)) {
                ms.when(IdentiteUtilisateur::obligatoire)
                  .thenReturn(new IdentiteUtilisateur(99L, "ADMIN"));

                service.listerPourAdmin(null, null, null, null, null, "MASQUEE", null, null);
            }

            ArgumentCaptor<Critere> critere = ArgumentCaptor.forClass(Critere.class);
            verify(recherche).rechercher(critere.capture(), eq(0), eq(20));
            assertThat(critere.getValue().statut()).isEqualTo(StatutBrique.MASQUEE);
        }
    }

    // ── Services publics du module (13.4) ─────────────────────────────────────

    @Nested
    @DisplayName("13.4 — Services publics aux autres modules")
    class ServicesPublics {

        @Test
        @DisplayName("resume(id) donne id, auteur, statut, version et titre ; vide si inconnue")
        void resume() {
            Brique b = new Brique("titre", "r", "c", "m", null, Licence.MIT, 5L, cat, lang);
            when(briqueRepo.findById(1L)).thenReturn(Optional.of(b));
            when(briqueRepo.findById(2L)).thenReturn(Optional.empty());

            BriqueResumeInterne r = service.resume(1L).orElseThrow();
            assertThat(r.idAuteur()).isEqualTo(5L);
            assertThat(r.statut()).isEqualTo("PUBLIEE");
            assertThat(r.versionCode()).isEqualTo(1);
            assertThat(r.titre()).isEqualTo("titre");
            assertThat(service.resume(2L)).isEmpty();
        }

        @Test
        @DisplayName("compterPubliees ne compte que les briques PUBLIEES de l'auteur")
        void compterPubliees() {
            when(briqueRepo.countByIdAuteurAndStatut(5L, StatutBrique.PUBLIEE)).thenReturn(3L);

            assertThat(service.compterPubliees(5L)).isEqualTo(3L);
            assertThat(service.compterPubliees(null)).isZero();
        }

        @Test
        @DisplayName("references(ids) : liste vide ou null → map vide")
        void referencesVides() {
            assertThat(service.references(null)).isEmpty();
            assertThat(service.references(List.of())).isEmpty();
        }
    }
}

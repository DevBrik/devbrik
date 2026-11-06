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
import sn.devbrik.backend.brique.dto.*;
import sn.devbrik.backend.brique.entity.*;
import sn.devbrik.backend.brique.repository.*;
import sn.devbrik.backend.brique.service.BriqueServiceImpl;
import sn.devbrik.backend.common.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du BriqueService — RG3, RG4, RG5, RG6, RG7, RG15, RG16, RG19.
 */
@ExtendWith(MockitoExtension.class)
class BriqueServiceTest {

    @Mock BriqueRepository    briqueRepo;
    @Mock CategorieRepository catRepo;
    @Mock LangageRepository   langRepo;
    @Mock UtilisateurService  utilisateurService;
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

    // ── RG3 — Unicité titre/auteur ────────────────────────────────────────────

    @Nested
    @DisplayName("RG3 — Titre unique par auteur")
    class Rg3 {

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

    // ── RG5 — Licences permissives ────────────────────────────────────────────

    @Nested
    @DisplayName("RG5 — Licences permissives")
    class Rg5 {

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

    // ── RG6 — Quota 20 créations/24 h ────────────────────────────────────────

    @Nested
    @DisplayName("RG6 — Quota créations")
    class Rg6 {

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

    // ── RG15 — Auteur seul peut modifier ─────────────────────────────────────

    @Nested
    @DisplayName("RG15 — Modification par l'auteur uniquement")
    class Rg15 {

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

    // ── RG16 — Masquage admin ─────────────────────────────────────────────────

    @Nested
    @DisplayName("RG16 — Masquage par admin")
    class Rg16 {

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
    class Rg19 {

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
}

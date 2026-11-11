package sn.devbrik.backend.developpeur.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.devbrik.backend.common.ApiException;
import sn.devbrik.backend.common.CodeErreur;
import sn.devbrik.backend.common.PageReponse;
import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.developpeur.dto.ClassementEntree;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassementServiceTest {

    @Mock private ClassementLecture lecture;
    @InjectMocks private ClassementService service;

    private static ClassementEntree entree(long id, long points) {
        return new ClassementEntree(new ReferenceUtilisateur(id, "dev" + id, null), "Sénégal", points, 1, 0, 0);
    }

    private static void refuse(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(CodeErreur.REQUETE_INVALIDE));
    }

    @Test
    void test_RG16_la_page_par_defaut_est_0_avec_20_elements() {
        when(lecture.compter()).thenReturn(45L);
        when(lecture.page(0, 20)).thenReturn(List.of(entree(1, 30), entree(2, 13)));

        PageReponse<ClassementEntree> page = service.classement(null, null);

        assertThat(page.page()).isZero();
        assertThat(page.taille()).isEqualTo(20);
        assertThat(page.totalElements()).isEqualTo(45);
        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.contenu()).extracting(e -> e.developpeur().id()).containsExactly(1L, 2L);
    }

    @Test
    void test_RG16_la_taille_est_limitee_a_50() {
        when(lecture.compter()).thenReturn(120L);
        when(lecture.page(2, 50)).thenReturn(List.of());

        PageReponse<ClassementEntree> page = service.classement(2, 500);

        assertThat(page.taille()).isEqualTo(50);
        assertThat(page.totalPages()).isEqualTo(3);
        verify(lecture).page(2, 50);
    }

    @Test
    void test_RG16_une_page_negative_ou_une_taille_nulle_est_refusee() {
        refuse(() -> service.classement(-1, null));
        refuse(() -> service.classement(0, 0));
        refuse(() -> service.classement(0, -5));
        verify(lecture, never()).page(anyInt(), anyInt());
    }

    @Test
    void un_classement_vide_n_interroge_pas_la_page() {
        when(lecture.compter()).thenReturn(0L);

        PageReponse<ClassementEntree> page = service.classement(null, null);

        assertThat(page.contenu()).isEmpty();
        assertThat(page.totalElements()).isZero();
        assertThat(page.totalPages()).isZero();
        verify(lecture, never()).page(anyInt(), anyInt());
    }

    @Test
    void le_nombre_de_pages_est_arrondi_a_l_entier_superieur() {
        when(lecture.compter()).thenReturn(40L);
        when(lecture.page(0, 20)).thenReturn(List.of());

        assertThat(service.classement(0, 20).totalPages()).isEqualTo(2);
    }
}

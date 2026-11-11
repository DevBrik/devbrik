package sn.devbrik.backend.developpeur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.common.PageReponse;
import sn.devbrik.backend.developpeur.dto.ClassementEntree;

import java.util.List;

/** GET /api/developpeurs/classement (P2) : page de développeurs triée par réputation décroissante. */
@Service
public class ClassementService {

    static final int TAILLE_PAR_DEFAUT = 20;
    static final int TAILLE_MAX = 50;

    private final ClassementLecture lecture;

    public ClassementService(ClassementLecture lecture) {
        this.lecture = lecture;
    }

    /** RG16 : page à partir de 0, taille 20 par défaut, 50 au plus. */
    @Transactional(readOnly = true)
    public PageReponse<ClassementEntree> classement(Integer page, Integer taille) {
        int numeroPage = page == null ? 0 : page;
        int taillePage = taille == null ? TAILLE_PAR_DEFAUT : taille;
        if (numeroPage < 0) {
            throw ErreursDeveloppeur.requeteInvalide("Le numéro de page doit être supérieur ou égal à 0.");
        }
        if (taillePage < 1) {
            throw ErreursDeveloppeur.requeteInvalide("La taille de page doit être supérieure ou égale à 1.");
        }
        taillePage = Math.min(taillePage, TAILLE_MAX);

        long total = lecture.compter();
        List<ClassementEntree> contenu = total == 0 ? List.of() : lecture.page(numeroPage, taillePage);
        int totalPages = (int) ((total + taillePage - 1) / taillePage);
        return new PageReponse<>(contenu, numeroPage, taillePage, total, totalPages);
    }
}

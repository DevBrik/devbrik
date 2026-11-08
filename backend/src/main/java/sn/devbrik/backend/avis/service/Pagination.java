package sn.devbrik.backend.avis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sn.devbrik.backend.common.PageReponse;

import java.util.List;

/** RG16 : page à partir de 0, taille par défaut 20, maximum 50. */
final class Pagination {

    static final int TAILLE_PAR_DEFAUT = 20;
    static final int TAILLE_MAX = 50;

    private Pagination() {
    }

    static Pageable pageable(Integer page, Integer taille) {
        int p = page == null ? 0 : page;
        int t = taille == null ? TAILLE_PAR_DEFAUT : taille;
        if (p < 0) {
            throw ErreursAvis.requeteInvalide("La page doit être supérieure ou égale à 0.");
        }
        if (t < 1) {
            throw ErreursAvis.requeteInvalide("La taille doit être supérieure ou égale à 1.");
        }
        return PageRequest.of(p, Math.min(t, TAILLE_MAX));
    }

    static <T> PageReponse<T> reponse(Page<?> source, List<T> contenu) {
        return new PageReponse<>(contenu, source.getNumber(), source.getSize(),
                source.getTotalElements(), source.getTotalPages());
    }
}
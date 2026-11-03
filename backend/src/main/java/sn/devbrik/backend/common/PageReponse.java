package sn.devbrik.backend.common;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageReponse<T>(List<T> contenu, int page, int taille, long totalElements, int totalPages) {

    public static <T> PageReponse<T> de(Page<T> p) {
        return new PageReponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
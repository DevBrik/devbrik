package sn.devbrik.backend.brique.service;

import org.springframework.data.domain.Pageable;
import sn.devbrik.backend.brique.dto.*;
import sn.devbrik.backend.common.PageReponse;

/**
 * Contrat du service briques.
 */
public interface BriqueService {

    /** M01 — Liste publique paginée. */
    PageReponse<BriqueResumeDto> listerPubliees(Long idCategorie, Long idLangage, Pageable pageable);

    /** M02 — Fiche publique. */
    BriqueDetailDto obtenirPublique(Long id);

    /** M03 — Publier une nouvelle brique. */
    BriqueDetailDto publier(BriqueCreationDto dto);

    /** M03 — Modifier une brique existante. */
    BriqueDetailDto modifier(Long id, BriqueModificationDto dto);

    /** M05 — Mes briques (auteur connecté). */
    PageReponse<BriqueResumeInterne> mesBriques(Pageable pageable);

    /** M05 — Supprimer une brique (auteur ou admin, RG19). */
    void supprimer(Long id);

    /** Admin — Masquer une brique (RG16). */
    void masquer(Long id);

    /** Admin — Republier une brique masquée. */
    void republier(Long id);

    /** Référence légère (utilisée par d'autres modules). */
    BriqueReference reference(Long id);
}

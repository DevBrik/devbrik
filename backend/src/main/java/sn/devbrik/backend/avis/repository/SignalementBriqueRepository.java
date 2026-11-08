package sn.devbrik.backend.avis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.devbrik.backend.avis.entity.SignalementBrique;
import sn.devbrik.backend.avis.entity.StatutSignalement;

import java.time.Instant;

public interface SignalementBriqueRepository extends JpaRepository<SignalementBrique, Long> {

    boolean existsByIdBriqueAndIdAuteur(Long idBrique, Long idAuteur);

    Page<SignalementBrique> findByStatutOrderByDateCreationDescIdDesc(StatutSignalement statut, Pageable pageable);

    /** Quota RG15. */
    long countByIdAuteurAndDateCreationAfter(Long idAuteur, Instant depuis);
}
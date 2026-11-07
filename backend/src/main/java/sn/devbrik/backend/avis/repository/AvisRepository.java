package sn.devbrik.backend.avis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.devbrik.backend.avis.entity.Avis;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AvisRepository extends JpaRepository<Avis, Long> {

    Optional<Avis> findByIdBriqueAndIdAuteur(Long idBrique, Long idAuteur);

    /** Récents d'abord (11.5). */
    Page<Avis> findByIdBriqueOrderByDateCreationDescIdDesc(Long idBrique, Pageable pageable);

    /** Quota RG15 : avis créés ou modifiés depuis l'instant donné. */
    long countByIdAuteurAndDateModificationAfter(Long idAuteur, Instant depuis);

    @Query(nativeQuery = true, value = """
            SELECT a.id_brique AS "idBrique",
                   COUNT(*)    AS "nombre",
                   SUM(a.note) AS "somme"
            FROM avis a
            WHERE a.id_brique IN (:ids)
            GROUP BY a.id_brique
            """)
    List<AgregatAvis> agreger(@Param("ids") Collection<Long> ids);
}
package sn.devbrik.backend.avis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.devbrik.backend.avis.entity.TestBrique;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TestBriqueRepository extends JpaRepository<TestBrique, Long> {

    Optional<TestBrique> findByIdBriqueAndIdAuteur(Long idBrique, Long idAuteur);

    /** RG9 : tests de la version actuelle, récents d'abord. */
    Page<TestBrique> findByIdBriqueAndVersionCodeOrderByDateTestDescIdDesc(
            Long idBrique, Integer versionCode, Pageable pageable);

    /** Quota RG15 : tests posés ou changés depuis l'instant donné. */
    long countByIdAuteurAndDateTestAfter(Long idAuteur, Instant depuis);

    /** Lecture seule de brique.version_code : seuls les tests de la version actuelle comptent. */
    @Query(nativeQuery = true, value = """
            SELECT t.id_brique AS "idBrique",
                   COUNT(*) FILTER (WHERE t.resultat = 'OK') AS "ok",
                   COUNT(*) FILTER (WHERE t.resultat = 'KO') AS "ko",
                   MAX(t.date_test) AS "derniere"
            FROM test_brique t
            JOIN brique b ON b.id_brique = t.id_brique
                         AND b.version_code = t.version_code
            WHERE t.id_brique IN (:ids)
            GROUP BY t.id_brique
            """)
    List<AgregatTests> agreger(@Param("ids") Collection<Long> ids);

    /** RG14 : tests OK de la version actuelle reçus sur toutes les briques de l'auteur. */
    @Query(nativeQuery = true, value = """
            SELECT COUNT(*)
            FROM test_brique t
            JOIN brique b ON b.id_brique = t.id_brique
                         AND b.version_code = t.version_code
            WHERE b.id_auteur = :idAuteur
              AND t.resultat = 'OK'
            """)
    long compterOkRecusVersionActuelle(@Param("idAuteur") Long idAuteur);
}
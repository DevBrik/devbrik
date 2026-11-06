package sn.devbrik.backend.brique.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.devbrik.backend.brique.entity.Brique;
import sn.devbrik.backend.brique.entity.StatutBrique;

import java.time.Instant;
import java.util.Optional;

public interface BriqueRepository extends JpaRepository<Brique, Long> {

    /** Toutes les briques publiées, avec filtre optionnel catégorie/langage (M01). */
    @Query("""
        SELECT b FROM Brique b
        WHERE b.statut = 'PUBLIEE'
          AND (:idCategorie IS NULL OR b.categorie.id = :idCategorie)
          AND (:idLangage   IS NULL OR b.langage.id   = :idLangage)
        ORDER BY b.dateCreation DESC
        """)
    Page<Brique> listerPubliees(
            @Param("idCategorie") Long idCategorie,
            @Param("idLangage")   Long idLangage,
            Pageable pageable);

    /** Briques d'un auteur donné (toutes visibilités) pour M05. */
    Page<Brique> findByIdAuteurOrderByDateCreationDesc(Long idAuteur, Pageable pageable);

    /** Vérifie l'unicité titre/auteur (RG3). */
    boolean existsByIdAuteurAndTitreIgnoreCase(Long idAuteur, String titre);

    /** Même vérification en excluant la brique en cours de modification. */
    boolean existsByIdAuteurAndTitreIgnoreCaseAndIdNot(Long idAuteur, String titre, Long id);

    /** Compte les briques créées par un auteur dans les dernières 24 h (quota RG6). */
    @Query("""
        SELECT COUNT(b) FROM Brique b
        WHERE b.idAuteur = :idAuteur
          AND b.dateCreation >= :depuis
        """)
    long compterCreationsDepuis(@Param("idAuteur") Long idAuteur, @Param("depuis") Instant depuis);

    /** Brique publique visible par tous (M02). */
    Optional<Brique> findByIdAndStatut(Long id, StatutBrique statut);
}

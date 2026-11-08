package sn.devbrik.backend.brique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.devbrik.backend.brique.entity.Brique;
import sn.devbrik.backend.brique.entity.StatutBrique;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface BriqueRepository extends JpaRepository<Brique, Long> {

    /** Vérifie l'unicité titre/auteur (RG5). */
    boolean existsByIdAuteurAndTitreIgnoreCase(Long idAuteur, String titre);

    /** Même vérification en excluant la brique en cours de modification. */
    boolean existsByIdAuteurAndTitreIgnoreCaseAndIdNot(Long idAuteur, String titre, Long id);

    /** Compte les briques créées par un auteur dans les dernières 24 h (quota RG15). */
    @Query("""
        SELECT COUNT(b) FROM Brique b
        WHERE b.idAuteur = :idAuteur
          AND b.dateCreation >= :depuis
        """)
    long compterCreationsDepuis(@Param("idAuteur") Long idAuteur, @Param("depuis") Instant depuis);

    /** Briques publiées d'un auteur (réputation RG14, BriqueService.compterPubliees). */
    long countByIdAuteurAndStatut(Long idAuteur, StatutBrique statut);

    /** Charge les briques d'une page avec leur catégorie et leur langage (évite les requêtes en cascade). */
    @Query("""
        SELECT b FROM Brique b
        JOIN FETCH b.categorie
        JOIN FETCH b.langage
        WHERE b.id IN :ids
        """)
    List<Brique> chargerAvecReferentiels(@Param("ids") Collection<Long> ids);
}

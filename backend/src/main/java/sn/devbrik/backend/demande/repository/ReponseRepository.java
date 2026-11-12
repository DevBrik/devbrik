package sn.devbrik.backend.demande.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.devbrik.backend.demande.entity.Reponse;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReponseRepository extends JpaRepository<Reponse, Long> {

    /** Projection : nombre de réponses par demande (pour DemandeResume). */
    interface CompteReponses {
        Long getIdDemande();
        Long getNombre();
    }

    List<Reponse> findByIdDemandeOrderByDateCreationAsc(Long idDemande);

    Optional<Reponse> findByIdDemandeAndAccepteeTrue(Long idDemande);

    /** RG15 : réponses de l'auteur sur les 24 dernières heures glissantes. */
    long countByIdAuteurAndDateCreationAfter(Long idAuteur, Instant depuis);

    /** RG14 : base de DemandeService.compterReponsesAcceptees. */
    long countByIdAuteurAndAccepteeTrue(Long idAuteur);

    @Query("""
           select r.idDemande as idDemande, count(r) as nombre
           from Reponse r
           where r.idDemande in :ids
           group by r.idDemande
           """)
    List<CompteReponses> compterParDemande(@Param("ids") Collection<Long> ids);

    @Modifying
    @Query("delete from Reponse r where r.idDemande = :idDemande")
    void supprimerParDemande(@Param("idDemande") Long idDemande);
}
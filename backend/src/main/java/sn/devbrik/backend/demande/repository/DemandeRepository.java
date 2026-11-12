package sn.devbrik.backend.demande.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.devbrik.backend.demande.entity.Demande;
import sn.devbrik.backend.demande.entity.StatutDemande;

import java.time.Instant;
import java.util.Optional;

public interface DemandeRepository extends JpaRepository<Demande, Long>, JpaSpecificationExecutor<Demande> {

    /** RG15 : demandes de l'auteur sur les 24 dernières heures glissantes. */
    long countByIdAuteurAndDateCreationAfter(Long idAuteur, Instant depuis);

    /**
     * Verrou pessimiste (select ... for update) : sérialise les acceptations,
     * retraits et suppressions d'une réponse acceptée pour une même demande (RG12).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Demande d where d.id = :id")
    Optional<Demande> trouverPourMiseAJour(@Param("id") Long id);

    /**
     * RG12 : rouvre les demandes RESOLUE qui n'ont plus aucune réponse acceptée
     * (cas d'un compte supprimé en cascade, RG17).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Demande d set d.statut = :ouverte
           where d.statut = :resolue
             and not exists (select r.id from Reponse r
                             where r.idDemande = d.id and r.acceptee = true)
           """)
    int rouvrirSansReponseAcceptee(@Param("ouverte") StatutDemande ouverte,
                                   @Param("resolue") StatutDemande resolue);
}
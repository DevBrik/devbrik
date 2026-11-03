package sn.devbrik.backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /** Recherche sans tenir compte de la casse (RG1), alignée sur l'index uk_utilisateur_pseudo (lower(pseudo)). */
    Optional<Utilisateur> findByPseudoIgnoreCase(String pseudo);

    boolean existsByPseudoIgnoreCase(String pseudo);

    /** Utilisé par AdminInitialiseur : « seulement s'il n'existe pas ». */
    boolean existsByRole(Role role);
}

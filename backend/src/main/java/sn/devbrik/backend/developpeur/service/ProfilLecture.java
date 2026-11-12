package sn.devbrik.backend.developpeur.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lecture seule de la table utilisateur (module 5), par SQL natif (règle 2 de 9.4).
 * Jamais de colonne mot_de_passe. Requête entièrement paramétrée.
 * À remplacer par UtilisateurService.profil(id) quand le membre 5 le publie.
 */
@Component
public class ProfilLecture {

    public record ProfilUtilisateur(Long id, String pseudo, String nomAffiche, String pays,
                                    String bio, String lienProfil, Instant dateCreation) {
    }

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public Optional<ProfilUtilisateur> parId(Long id) {
        List<Object[]> lignes = em
                .createNativeQuery("select id_utilisateur, pseudo, nom_affiche, pays, bio, lien_profil, "
                        + "date_creation from utilisateur where id_utilisateur = :id")
                .setParameter("id", id)
                .getResultList();
        if (lignes.isEmpty()) {
            return Optional.empty();
        }
        Object[] l = lignes.getFirst();
        return Optional.of(new ProfilUtilisateur(
                ((Number) l[0]).longValue(),
                (String) l[1], (String) l[2], (String) l[3], (String) l[4], (String) l[5],
                versInstant(l[6])));
    }

    /** Selon le pilote, une colonne timestamptz arrive en Instant, OffsetDateTime ou Timestamp. */
    private static Instant versInstant(Object valeur) {
        return switch (valeur) {
            case null -> null;
            case Instant i -> i;
            case OffsetDateTime o -> o.toInstant();
            case Timestamp t -> t.toInstant();
            default -> throw new IllegalStateException("Type de date inattendu : " + valeur.getClass());
        };
    }
}
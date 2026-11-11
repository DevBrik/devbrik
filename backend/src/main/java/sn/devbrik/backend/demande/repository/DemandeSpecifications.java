package sn.devbrik.backend.demande.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import sn.devbrik.backend.demande.entity.Demande;
import sn.devbrik.backend.demande.entity.StatutDemande;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Filtres de la liste des demandes (RG16, S5). Requête entièrement paramétrée :
 * aucune concaténation de texte utilisateur dans du SQL.
 */
public final class DemandeSpecifications {

    private static final char ECHAPPEMENT = '\\';

    private DemandeSpecifications() {
    }

    /** Traite %, _ et le caractère d'échappement comme du texte ordinaire. */
    static String echapper(String texte) {
        return texte
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * Tous les paramètres sont facultatifs (null = pas de filtre).
     * Recherche insensible à la casse, mais pas aux accents.
     */
    public static Specification<Demande> filtre(StatutDemande statut, Long idCategorie,
                                                Long idLangage, String q) {
        return (root, query, cb) -> {
            List<Predicate> predicats = new ArrayList<>();
            if (statut != null) {
                predicats.add(cb.equal(root.get("statut"), statut));
            }
            if (idCategorie != null) {
                predicats.add(cb.equal(root.get("idCategorie"), idCategorie));
            }
            if (idLangage != null) {
                predicats.add(cb.equal(root.get("idLangage"), idLangage));
            }
            if (q != null) {
                String motif = "%" + echapper(q).toLowerCase(Locale.ROOT) + "%";
                predicats.add(cb.or(
                        cb.like(cb.lower(root.get("titre")), motif, ECHAPPEMENT),
                        cb.like(cb.lower(root.get("description")), motif, ECHAPPEMENT)));
            }
            return cb.and(predicats.toArray(new Predicate[0]));
        };
    }
}
package sn.devbrik.backend.brique.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import sn.devbrik.backend.brique.entity.StatutBrique;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recherche paginée des briques (RG16) : filtres, trois tris, lecture seule.
 *
 * Les tris mieux-notees et plus-testees s'appuient sur des sous-requêtes SQL en lecture seule
 * sur avis et test_brique (règle 2 de 9.4) ; cette classe n'écrit jamais dans ces tables.
 * Toutes les valeurs passent par des paramètres liés (S5). Dans la recherche, les caractères
 * %, _ et le caractère d'échappement sont traités comme du texte.
 * La requête renvoie seulement des identifiants, déjà ordonnés ; le service charge les entités.
 */
@Repository
public class BriqueRecherche {

    /** Caractère d'échappement du LIKE : lui-même, % et _ sont échappés dans le motif. */
    private static final char ECHAPPEMENT = '!';

    public enum Tri {
        RECENTES, MIEUX_NOTEES, PLUS_TESTEES;

        /** Valeurs de l'API : recentes (défaut), mieux-notees, plus-testees. Null si inconnue. */
        public static Tri depuisApi(String valeur) {
            if (valeur == null || valeur.isBlank()) {
                return RECENTES;
            }
            return switch (valeur.strip()) {
                case "recentes" -> RECENTES;
                case "mieux-notees" -> MIEUX_NOTEES;
                case "plus-testees" -> PLUS_TESTEES;
                default -> null;
            };
        }
    }

    /** Tous les champs sont facultatifs (null = pas de filtre), sauf tri. */
    public record Critere(String q, String categorie, String langage, Long auteur,
                          StatutBrique statut, Tri tri) {
    }

    public record Resultat(List<Long> ids, long total) {
    }

    @PersistenceContext
    private EntityManager em;

    public Resultat rechercher(Critere c, int page, int taille) {
        StringBuilder base = new StringBuilder(
                " FROM brique b"
                + " JOIN categorie c ON c.id_categorie = b.id_categorie"
                + " JOIN langage l ON l.id_langage = b.id_langage");
        StringBuilder filtres = new StringBuilder(" WHERE 1 = 1");
        Map<String, Object> parametres = new LinkedHashMap<>();

        if (c.statut() != null) {
            filtres.append(" AND b.statut = :statut");
            parametres.put("statut", c.statut().name());
        }
        if (c.categorie() != null) {
            filtres.append(" AND c.slug = :categorie");
            parametres.put("categorie", c.categorie());
        }
        if (c.langage() != null) {
            filtres.append(" AND l.slug = :langage");
            parametres.put("langage", c.langage());
        }
        if (c.auteur() != null) {
            filtres.append(" AND b.id_auteur = :auteur");
            parametres.put("auteur", c.auteur());
        }
        if (c.q() != null) {
            filtres.append(" AND (b.titre ILIKE :motif ESCAPE '").append(ECHAPPEMENT).append("'")
                   .append(" OR b.resume ILIKE :motif ESCAPE '").append(ECHAPPEMENT).append("')");
            parametres.put("motif", "%" + echapper(c.q()) + "%");
        }

        Query comptage = em.createNativeQuery("SELECT COUNT(*)" + base + filtres);
        parametres.forEach(comptage::setParameter);
        long total = ((Number) comptage.getSingleResult()).longValue();

        long debut = (long) page * taille;
        if (total == 0 || debut >= total) {
            return new Resultat(List.of(), total);
        }

        Tri tri = c.tri() == null ? Tri.RECENTES : c.tri();
        String jointure = switch (tri) {
            case MIEUX_NOTEES -> " LEFT JOIN (SELECT id_brique, ROUND(AVG(note), 1) AS moyenne, COUNT(*) AS nb"
                    + " FROM avis GROUP BY id_brique) av ON av.id_brique = b.id_brique";
            case PLUS_TESTEES -> " LEFT JOIN (SELECT t.id_brique, COUNT(*) AS ok"
                    + " FROM test_brique t JOIN brique bv ON bv.id_brique = t.id_brique"
                    + " AND bv.version_code = t.version_code"
                    + " WHERE t.resultat = 'OK' GROUP BY t.id_brique) te ON te.id_brique = b.id_brique";
            case RECENTES -> "";
        };
        String ordre = switch (tri) {
            case MIEUX_NOTEES -> " ORDER BY av.moyenne DESC NULLS LAST, av.nb DESC NULLS LAST,"
                    + " b.date_creation DESC, b.id_brique DESC";
            case PLUS_TESTEES -> " ORDER BY COALESCE(te.ok, 0) DESC, b.date_creation DESC, b.id_brique DESC";
            case RECENTES -> " ORDER BY b.date_creation DESC, b.id_brique DESC";
        };

        Query requete = em.createNativeQuery("SELECT b.id_brique" + base + jointure + filtres + ordre);
        parametres.forEach(requete::setParameter);
        requete.setFirstResult((int) debut);
        requete.setMaxResults(taille);

        List<?> lignes = requete.getResultList();
        List<Long> ids = lignes.stream().map(o -> ((Number) o).longValue()).toList();
        return new Resultat(ids, total);
    }

    /** RG16 : %, _ et le caractère d'échappement sont du texte. */
    static String echapper(String texte) {
        StringBuilder sb = new StringBuilder(texte.length() + 4);
        for (char ch : texte.toCharArray()) {
            if (ch == ECHAPPEMENT || ch == '%' || ch == '_') {
                sb.append(ECHAPPEMENT);
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}

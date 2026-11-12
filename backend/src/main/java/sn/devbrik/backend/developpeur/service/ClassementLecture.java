package sn.devbrik.backend.developpeur.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import sn.devbrik.backend.common.ReferenceUtilisateur;
import sn.devbrik.backend.developpeur.dto.ClassementEntree;

import java.util.ArrayList;
import java.util.List;

/**
 * Lecture seule des tables utilisateur, brique, test_brique et reponse, par SQL natif
 * (règle 2 de 9.4) : le classement doit trier TOUS les développeurs, ce que des appels
 * un par un aux services publics ne permettent pas sans N+1. Les points sont ceux de RG14
 * (mêmes constantes que ReputationService), calculés à la demande et jamais stockés.
 * Aucune colonne mot_de_passe. Seules des constantes entières entrent dans le texte SQL ;
 * la pagination passe par des paramètres liés.
 * Sont classés les développeurs (pas les administrateurs) qui ont au moins un point.
 */
@Component
public class ClassementLecture {

    private static final String BASE = """
            select u.id_utilisateur as id, u.pseudo as pseudo, u.nom_affiche as nom_affiche, u.pays as pays,
                   coalesce(b.nombre, 0) as briques,
                   coalesce(t.nombre, 0) as tests_ok,
                   coalesce(r.nombre, 0) as reponses,
                   coalesce(b.nombre, 0) * %d + coalesce(t.nombre, 0) * %d + coalesce(r.nombre, 0) * %d as points
            from utilisateur u
            left join (select id_auteur, count(*) as nombre
                       from brique where statut = 'PUBLIEE' group by id_auteur) b
                   on b.id_auteur = u.id_utilisateur
            left join (select br.id_auteur, count(*) as nombre
                       from test_brique tb
                       join brique br on br.id_brique = tb.id_brique and br.version_code = tb.version_code
                       where tb.resultat = 'OK' group by br.id_auteur) t
                   on t.id_auteur = u.id_utilisateur
            left join (select id_auteur, count(*) as nombre
                       from reponse where acceptee group by id_auteur) r
                   on r.id_auteur = u.id_utilisateur
            where u.role = 'DEVELOPPEUR'
            """.formatted(ReputationService.POINTS_PAR_BRIQUE_PUBLIEE,
            ReputationService.POINTS_PAR_TEST_OK_RECU,
            ReputationService.POINTS_PAR_REPONSE_ACCEPTEE);

    private static final String PAGE = "select c.* from (" + BASE + ") c where c.points > 0 "
            + "order by c.points desc, c.id asc limit :limite offset :decalage";

    private static final String TOTAL = "select count(*) from (" + BASE + ") c where c.points > 0";

    @PersistenceContext
    private EntityManager em;

    public long compter() {
        return ((Number) em.createNativeQuery(TOTAL).getSingleResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<ClassementEntree> page(int numeroPage, int taille) {
        List<Object[]> lignes = em.createNativeQuery(PAGE)
                .setParameter("limite", taille)
                .setParameter("decalage", (long) numeroPage * taille)
                .getResultList();
        List<ClassementEntree> resultat = new ArrayList<>();
        for (Object[] l : lignes) {
            resultat.add(new ClassementEntree(
                    new ReferenceUtilisateur(((Number) l[0]).longValue(), (String) l[1], (String) l[2]),
                    (String) l[3],
                    ((Number) l[7]).longValue(),
                    ((Number) l[4]).longValue(),
                    ((Number) l[5]).longValue(),
                    ((Number) l[6]).longValue()));
        }
        return resultat;
    }
}

package sn.devbrik.backend.avis.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Lecture seule de la table brique (règle 2 de 9.4) : aucune écriture, aucune relation JPA.
 * À remplacer par BriqueService.resume / resumes quand le membre 2 les publiera.
 */
@Component
public class BriqueLecture {

    private static final String SQL = """
            SELECT id_brique, id_auteur, statut, version_code, titre
            FROM brique
            WHERE id_brique IN (:ids)
            """;

    @PersistenceContext
    private EntityManager em;

    public Optional<InfoBrique> trouver(Long idBrique) {
        if (idBrique == null) {
            return Optional.empty();
        }
        return parLot(List.of(idBrique)).values().stream().findFirst();
    }

    /** Une seule requête pour toutes les briques demandées ; les identifiants inconnus sont absents. */
    @Transactional
    public Map<Long, InfoBrique> parLot(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        Set<Long> distincts = new LinkedHashSet<>(ids);
        distincts.remove(null);
        if (distincts.isEmpty()) {
            return Map.of();
        }
        // Écrit d'abord ce que le module des briques a modifié dans la même transaction (code, statut).
        em.flush();
        List<?> lignes = em.createNativeQuery(SQL).setParameter("ids", distincts).getResultList();

        Map<Long, InfoBrique> resultat = new LinkedHashMap<>();
        for (Object ligne : lignes) {
            Object[] c = (Object[]) ligne;
            long id = ((Number) c[0]).longValue();
            resultat.put(id, new InfoBrique(id, ((Number) c[1]).longValue(), (String) c[2],
                    ((Number) c[3]).intValue(), (String) c[4]));
        }
        return Collections.unmodifiableMap(resultat);
    }
}
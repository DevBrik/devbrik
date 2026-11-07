package sn.devbrik.backend.demande.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import sn.devbrik.backend.demande.dto.ElementReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lecture seule des tables categorie et langage (module 2), par SQL natif
 * (règle 2 de 9.4). Noms de tables et de colonnes en constantes : jamais de
 * texte utilisateur dans la requête, uniquement des paramètres liés.
 * Si le membre 1 impose l'option B, cette classe est remplacée par des appels
 * aux services du membre 2.
 */
@Component
public class ReferentielLecture {

    @PersistenceContext
    private EntityManager em;

    public Optional<Long> idCategorieParSlug(String slug) {
        return idParSlug("categorie", "id_categorie", slug);
    }

    public Optional<Long> idLangageParSlug(String slug) {
        return idParSlug("langage", "id_langage", slug);
    }

    public boolean categorieExiste(Long id) {
        return existe("categorie", "id_categorie", id);
    }

    public boolean langageExiste(Long id) {
        return existe("langage", "id_langage", id);
    }

    public Map<Long, ElementReference> categories(Collection<Long> ids) {
        return elements("categorie", "id_categorie", ids);
    }

    public Map<Long, ElementReference> langages(Collection<Long> ids) {
        return elements("langage", "id_langage", ids);
    }

    private Optional<Long> idParSlug(String table, String colonneId, String slug) {
        List<?> lignes = em
                .createNativeQuery("select " + colonneId + " from " + table + " where slug = :slug")
                .setParameter("slug", slug)
                .getResultList();
        return lignes.isEmpty() ? Optional.empty() : Optional.of(((Number) lignes.get(0)).longValue());
    }

    private boolean existe(String table, String colonneId, Long id) {
        Number nombre = (Number) em
                .createNativeQuery("select count(*) from " + table + " where " + colonneId + " = :id")
                .setParameter("id", id)
                .getSingleResult();
        return nombre.longValue() > 0;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, ElementReference> elements(String table, String colonneId, Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Object[]> lignes = em
                .createNativeQuery("select " + colonneId + ", slug, nom from " + table
                        + " where " + colonneId + " in (:ids)")
                .setParameter("ids", ids)
                .getResultList();
        Map<Long, ElementReference> resultat = new HashMap<>();
        for (Object[] ligne : lignes) {
            resultat.put(((Number) ligne[0]).longValue(),
                    new ElementReference((String) ligne[1], (String) ligne[2]));
        }
        return resultat;
    }
}
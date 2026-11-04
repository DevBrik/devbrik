package sn.devbrik.backend.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Service public du module auth (section 13.4), utilisé par les membres 2, 3 et 4. */
@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Une seule requête SQL. Doublons et null ignorés ; identifiant inconnu absent de la map ;
     * collection null ou vide : map vide ; map non modifiable.
     */
    @Transactional(readOnly = true)
    public Map<Long, ReferenceUtilisateur> references(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        Set<Long> identifiants = new LinkedHashSet<>();
        for (Long id : ids) {
            if (Objects.nonNull(id)) {
                identifiants.add(id);
            }
        }
        if (identifiants.isEmpty()) {
            return Map.of();
        }
        Map<Long, ReferenceUtilisateur> resultat = new LinkedHashMap<>();
        for (Utilisateur u : utilisateurRepository.findAllById(identifiants)) {
            resultat.put(u.getId(), new ReferenceUtilisateur(u.getId(), u.getPseudo(), u.getNomAffiche()));
        }
        return Collections.unmodifiableMap(resultat);
    }

    /** Vrai si un utilisateur existe avec cet identifiant ; faux pour null. */
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return id != null && utilisateurRepository.existsById(id);
    }
}

package sn.devbrik.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.service.UtilisateurService;
import sn.devbrik.backend.common.ReferenceUtilisateur;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Vérifie sur la vraie base que l'entité Utilisateur correspond à V1 (ddl-auto=validate) et que le service public fonctionne. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UtilisateurServiceIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private UtilisateurService service;

    private long creer(String pseudo, String nomAffiche) {
        return jdbc.queryForObject(
                "INSERT INTO utilisateur (pseudo, nom_affiche, mot_de_passe) VALUES (?, ?, 'hache') RETURNING id_utilisateur",
                Long.class, pseudo, nomAffiche);
    }

    @Test
    void references_lit_les_vrais_utilisateurs() {
        long a = creer("it.ref.a", "Awa Test");
        long b = creer("it.ref.b", null);

        Map<Long, ReferenceUtilisateur> r = service.references(List.of(a, b, 999_999_999L));

        assertEquals(2, r.size());
        assertEquals(new ReferenceUtilisateur(a, "it.ref.a", "Awa Test"), r.get(a));
        assertNull(r.get(b).nomAffiche());
    }

    @Test
    void existe_sur_la_vraie_base() {
        long a = creer("it.existe", null);

        assertTrue(service.existe(a));
        assertFalse(service.existe(999_999_999L));
    }
}

package sn.devbrik.backend.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Comptes de démonstration (section 5) : deux développeurs. L'administrateur de démonstration est
 * créé par AdminInitialiseur (ADMIN_PSEUDO=admin.demo, par exemple).
 *
 * Activé seulement si la variable d'environnement DEMO_MOT_DE_PASSE est définie : aucun mot de passe
 * dans le dépôt (S9). Elle doit différer de ADMIN_MOT_DE_PASSE (S12). Idempotent : les comptes
 * existants ne sont jamais modifiés. Ne bloque jamais le démarrage.
 */
@Component
@Order(20)
public class DemoInitialiseur implements ApplicationRunner {

    private static final Logger JOURNAL = LoggerFactory.getLogger(DemoInitialiseur.class);

    record CompteDemo(String pseudo, String nomAffiche, String pays, String bio) {
    }

    static final List<CompteDemo> COMPTES = List.of(
            new CompteDemo("awa.dev", "Awa Diop", "Sénégal",
                    "Développeuse backend à Dakar, passionnée par les paiements mobiles."),
            new CompteDemo("moussa.dev", "Moussa Ndiaye", "Sénégal",
                    "Étudiant en informatique de gestion, je partage mes petits utilitaires."));

    private final String motDePasseDemo;
    private final String motDePasseAdmin;
    private final ComptesInitiaux comptes;

    public DemoInitialiseur(@Value("${DEMO_MOT_DE_PASSE:}") String motDePasseDemo,
                            @Value("${app.admin.mot-de-passe:}") String motDePasseAdmin,
                            ComptesInitiaux comptes) {
        this.motDePasseDemo = motDePasseDemo;
        this.motDePasseAdmin = motDePasseAdmin;
        this.comptes = comptes;
    }

    @Override
    public void run(ApplicationArguments args) {
        initialiser();
    }

    void initialiser() {
        if (motDePasseDemo == null || motDePasseDemo.isEmpty()) {
            JOURNAL.info("DEMO_MOT_DE_PASSE absent : aucun compte de démonstration créé.");
            return;
        }
        if (motDePasseDemo.equals(motDePasseAdmin)) {
            JOURNAL.warn("DEMO_MOT_DE_PASSE doit différer de ADMIN_MOT_DE_PASSE (S12) : "
                    + "aucun compte de démonstration créé.");
            return;
        }
        for (CompteDemo c : COMPTES) {
            try {
                ComptesInitiaux.Resultat r = comptes.creerDeveloppeurSiAbsent(
                        c.pseudo(), c.nomAffiche(), c.pays(), c.bio(), motDePasseDemo);
                JOURNAL.info("Compte de démonstration « {} » : {}.", c.pseudo(), r);
            } catch (IllegalArgumentException e) {
                JOURNAL.warn("Compte de démonstration « {} » non créé : {}", c.pseudo(), e.getMessage());
            }
        }
    }
}

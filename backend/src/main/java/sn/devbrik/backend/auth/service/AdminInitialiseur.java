package sn.devbrik.backend.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Crée le premier administrateur au démarrage à partir des variables d'environnement
 * ADMIN_PSEUDO et ADMIN_MOT_DE_PASSE (18.3), seulement s'il n'existe pas encore d'administrateur.
 *
 * - Variables absentes ou vides : rien n'est créé (démarrage normal).
 * - Configuration invalide : le démarrage échoue avec un message clair, sans jamais afficher le mot de passe.
 * - Pseudo déjà pris par un autre compte : avertissement, aucune promotion automatique.
 * Le mot de passe n'est jamais journalisé.
 */
@Component
@Order(10)
public class AdminInitialiseur implements ApplicationRunner {

    private static final Logger JOURNAL = LoggerFactory.getLogger(AdminInitialiseur.class);

    private final String pseudo;
    private final String motDePasse;
    private final ComptesInitiaux comptes;

    public AdminInitialiseur(@Value("${app.admin.pseudo:}") String pseudo,
                             @Value("${app.admin.mot-de-passe:}") String motDePasse,
                             ComptesInitiaux comptes) {
        this.pseudo = pseudo;
        this.motDePasse = motDePasse;
        this.comptes = comptes;
    }

    @Override
    public void run(ApplicationArguments args) {
        initialiser();
    }

    void initialiser() {
        if (pseudo == null || pseudo.isBlank() || motDePasse == null || motDePasse.isEmpty()) {
            JOURNAL.info("ADMIN_PSEUDO ou ADMIN_MOT_DE_PASSE absent : aucun administrateur créé.");
            return;
        }
        ComptesInitiaux.Resultat resultat;
        try {
            resultat = comptes.creerAdministrateurSiAbsent(pseudo, motDePasse);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Configuration de l'administrateur invalide (ADMIN_PSEUDO / ADMIN_MOT_DE_PASSE) : "
                            + e.getMessage());
        }
        switch (resultat) {
            case CREE -> JOURNAL.info("Administrateur « {} » créé.", pseudo.strip());
            case ADMIN_EXISTANT -> JOURNAL.info("Un administrateur existe déjà : aucune création.");
            case PSEUDO_PRIS -> JOURNAL.warn(
                    "ADMIN_PSEUDO « {} » est déjà utilisé par un autre compte : aucun administrateur créé.",
                    pseudo.strip());
            default -> { }
        }
    }
}

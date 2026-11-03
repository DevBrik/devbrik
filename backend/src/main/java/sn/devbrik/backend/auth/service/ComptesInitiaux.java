package sn.devbrik.backend.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;

import java.util.regex.Pattern;

/**
 * Création idempotente des comptes de départ (administrateur, comptes de démonstration).
 * Utilisé uniquement au démarrage par AdminInitialiseur et DemoInitialiseur.
 *
 * Volontairement SANS @Transactional : chaque enregistrement a sa propre transaction, ce qui permet
 * de rattraper une violation de l'index unique (deux instances qui démarrent ensemble) sans
 * transaction « rollback-only ».
 *
 * Les pseudos réservés (RG1) ne sont pas refusés ici : ils protègent contre l'usurpation par les
 * inscriptions, et l'administrateur peut légitimement s'appeler « admin ».
 * Aucun message d'erreur ne contient de mot de passe.
 */
@Service
public class ComptesInitiaux {

    public enum Resultat {
        CREE,
        /** Un administrateur existe déjà : rien n'est fait. */
        ADMIN_EXISTANT,
        /** Le pseudo demandé appartient déjà à un autre compte : il n'est jamais promu ni modifié. */
        PSEUDO_PRIS,
        /** Le compte de démonstration existe déjà. */
        DEJA_PRESENT
    }

    /** Même format que la contrainte ck_utilisateur_pseudo de V1. */
    private static final Pattern FORMAT_PSEUDO = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{2,29}$");

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public ComptesInitiaux(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Crée l'administrateur seulement si aucun administrateur n'existe. */
    public Resultat creerAdministrateurSiAbsent(String pseudo, String motDePasse) {
        String p = verifier(pseudo, motDePasse);
        if (utilisateurRepository.existsByRole(Role.ADMIN)) {
            return Resultat.ADMIN_EXISTANT;
        }
        if (utilisateurRepository.existsByPseudoIgnoreCase(p)) {
            return Resultat.PSEUDO_PRIS;
        }
        return enregistrer(new Utilisateur(p, null, passwordEncoder.encode(motDePasse), Role.ADMIN),
                Resultat.PSEUDO_PRIS);
    }

    /** Crée un compte développeur de démonstration s'il n'existe pas (pseudo, casse ignorée). */
    public Resultat creerDeveloppeurSiAbsent(String pseudo, String nomAffiche, String pays, String bio,
                                             String motDePasse) {
        String p = verifier(pseudo, motDePasse);
        if (utilisateurRepository.existsByPseudoIgnoreCase(p)) {
            return Resultat.DEJA_PRESENT;
        }
        Utilisateur u = new Utilisateur(p, nomAffiche, passwordEncoder.encode(motDePasse), Role.DEVELOPPEUR);
        u.setPays(pays);
        u.setBio(bio);
        return enregistrer(u, Resultat.DEJA_PRESENT);
    }

    private Resultat enregistrer(Utilisateur utilisateur, Resultat siConflit) {
        try {
            utilisateurRepository.saveAndFlush(utilisateur);
            return Resultat.CREE;
        } catch (DataIntegrityViolationException e) {
            return siConflit;
        }
    }

    private static String verifier(String pseudo, String motDePasse) {
        String p = pseudo == null ? "" : pseudo.strip();
        if (!FORMAT_PSEUDO.matcher(p).matches()) {
            throw new IllegalArgumentException(
                    "Pseudo invalide : 3 à 30 caractères (lettres, chiffres, point, tiret, tiret bas), "
                            + "commençant par une lettre ou un chiffre.");
        }
        if (motDePasse == null || motDePasse.length() < 8 || motDePasse.length() > 72
                || !AuthService.octetsValides(motDePasse)) {
            throw new IllegalArgumentException("Mot de passe invalide : 8 à 72 caractères et 72 octets au plus.");
        }
        return p;
    }
}

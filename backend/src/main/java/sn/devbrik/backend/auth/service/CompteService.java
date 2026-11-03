package sn.devbrik.backend.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.dto.ChangementMotDePasseRequete;
import sn.devbrik.backend.auth.dto.SuppressionRequete;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;

/**
 * Changement de mot de passe et suppression du compte (RG2, RG17).
 * L'identifiant vient toujours du jeton. Ne journalise rien.
 */
@Service
public class CompteService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public CompteService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changerMotDePasse(Long idUtilisateur, ChangementMotDePasseRequete requete) {
        Utilisateur utilisateur = charger(idUtilisateur);
        verifierMotDePasse(utilisateur, requete.ancienMotDePasse());
        if (!AuthService.octetsValides(requete.nouveauMotDePasse())) {
            throw ErreursAuth.requeteInvalide(
                    "Le nouveau mot de passe est trop long (72 octets au plus ; les accents comptent double).");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(requete.nouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }

    /**
     * RG17 : supprime la ligne utilisateur ; le reste disparaît par la cascade de la base.
     * L'administrateur est refusé avant toute vérification du mot de passe.
     */
    @Transactional
    public void supprimer(Long idUtilisateur, SuppressionRequete requete) {
        Utilisateur utilisateur = charger(idUtilisateur);
        if (utilisateur.getRole() == Role.ADMIN) {
            throw ErreursAuth.suppressionAdminInterdite();
        }
        verifierMotDePasse(utilisateur, requete.motDePasse());
        utilisateurRepository.delete(utilisateur);
        // flush : une clé étrangère sans cascade échoue ici, dans la transaction.
        utilisateurRepository.flush();
    }

    private void verifierMotDePasse(Utilisateur utilisateur, String saisi) {
        boolean correct = AuthService.octetsValides(saisi)
                && passwordEncoder.matches(saisi, utilisateur.getMotDePasse());
        if (!correct) {
            throw ErreursAuth.motDePasseIncorrect();
        }
    }

    private Utilisateur charger(Long idUtilisateur) {
        return utilisateurRepository.findById(idUtilisateur).orElseThrow(ErreursAuth::nonAuthentifie);
    }
}

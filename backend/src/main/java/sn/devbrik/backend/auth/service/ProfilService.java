package sn.devbrik.backend.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.dto.ProfilRequete;
import sn.devbrik.backend.auth.dto.UtilisateurDto;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;

/** Lecture et modification du profil de l'utilisateur connecté (GET et PUT /api/moi). */
@Service
public class ProfilService {

    private final UtilisateurRepository utilisateurRepository;

    public ProfilService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public UtilisateurDto moi(Long idUtilisateur) {
        return UtilisateurDto.depuis(charger(idUtilisateur));
    }

    @Transactional
    public UtilisateurDto mettreAJour(Long idUtilisateur, ProfilRequete requete) {
        Utilisateur utilisateur = charger(idUtilisateur);
        utilisateur.setNomAffiche(requete.nomAffiche());
        utilisateur.setPays(requete.pays());
        utilisateur.setBio(requete.bio());
        utilisateur.setLienProfil(requete.lienProfil());
        return UtilisateurDto.depuis(utilisateurRepository.save(utilisateur));
    }

    /** Jeton encore valide mais compte supprimé : 401 pour que le frontend supprime le jeton (11.1). */
    private Utilisateur charger(Long idUtilisateur) {
        return utilisateurRepository.findById(idUtilisateur).orElseThrow(ErreursAuth::nonAuthentifie);
    }
}

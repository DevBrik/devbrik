package sn.devbrik.backend.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.auth.dto.ConnexionRequete;
import sn.devbrik.backend.auth.dto.InscriptionRequete;
import sn.devbrik.backend.auth.dto.ReponseAuth;
import sn.devbrik.backend.auth.dto.UtilisateurDto;
import sn.devbrik.backend.auth.entity.Role;
import sn.devbrik.backend.auth.entity.Utilisateur;
import sn.devbrik.backend.auth.repository.UtilisateurRepository;
import sn.devbrik.backend.config.JwtService;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Inscription et connexion (RG1, RG2). Ne journalise rien : ni mot de passe, ni jeton, ni hachage. */
@Service
public class AuthService {

    /** RG1 : pseudos réservés, comparés en minuscules. */
    static final Set<String> PSEUDOS_RESERVES = Set.of(
            "admin", "administrator", "administrateur", "root", "support",
            "moderateur", "devbrik", "equipe", "systalink", "datacloud");

    /** BCrypt ne lit que les 72 premiers octets. */
    static final int OCTETS_MAX_MOT_DE_PASSE = 72;

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    /** Hachage factice comparé quand le pseudo n'existe pas, pour égaliser le temps de réponse. */
    private final String hacheFactice;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hacheFactice = passwordEncoder.encode("devbrik-hache-factice");
    }

    @Transactional
    public ReponseAuth inscrire(InscriptionRequete requete) {
        if (!octetsValides(requete.motDePasse())) {
            throw ErreursAuth.requeteInvalide(
                    "Le mot de passe est trop long (72 octets au plus ; les accents comptent double).");
        }
        if (PSEUDOS_RESERVES.contains(requete.pseudo().toLowerCase(Locale.ROOT))) {
            throw ErreursAuth.pseudoReserve();
        }
        if (utilisateurRepository.existsByPseudoIgnoreCase(requete.pseudo())) {
            throw ErreursAuth.pseudoDejaUtilise();
        }

        Utilisateur utilisateur = new Utilisateur(
                requete.pseudo(),
                requete.nomAffiche(),
                passwordEncoder.encode(requete.motDePasse()),
                Role.DEVELOPPEUR);
        try {
            // saveAndFlush : une course entre deux inscriptions est rattrapée par l'index unique de V1.
            utilisateur = utilisateurRepository.saveAndFlush(utilisateur);
        } catch (DataIntegrityViolationException e) {
            throw ErreursAuth.pseudoDejaUtilise();
        }
        return reponse(utilisateur);
    }

    @Transactional(readOnly = true)
    public ReponseAuth connecter(ConnexionRequete requete) {
        Optional<Utilisateur> trouve = utilisateurRepository.findByPseudoIgnoreCase(requete.pseudo());
        String hache = trouve.map(Utilisateur::getMotDePasse).orElse(hacheFactice);

        boolean motDePasseCorrect = octetsValides(requete.motDePasse())
                && passwordEncoder.matches(requete.motDePasse(), hache);

        if (trouve.isEmpty() || !motDePasseCorrect) {
            throw ErreursAuth.identifiantsInvalides();
        }
        return reponse(trouve.get());
    }

    private ReponseAuth reponse(Utilisateur utilisateur) {
        return new ReponseAuth(jeton(utilisateur), UtilisateurDto.depuis(utilisateur));
    }

    /** Jeton signé de 12 h (RG2), fabriqué par JwtService (membre 1). Point unique à adapter. */
    private String jeton(Utilisateur utilisateur) {
        return jwtService.generer(utilisateur.getId(), utilisateur.getRole().name());
    }

    static boolean octetsValides(String motDePasse) {
        return motDePasse != null
                && motDePasse.getBytes(StandardCharsets.UTF_8).length <= OCTETS_MAX_MOT_DE_PASSE;
    }
}

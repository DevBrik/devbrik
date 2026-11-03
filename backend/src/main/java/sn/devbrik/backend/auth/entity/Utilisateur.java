package sn.devbrik.backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Correspond à la table "utilisateur" de V1 (ne jamais modifier V1).
 * Aucune relation JPA vers un autre module. Pas de toString() : le hachage ne doit jamais finir dans un journal (RG2).
 */
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long id;

    @Column(name = "pseudo", nullable = false, length = 30)
    private String pseudo;

    @Column(name = "nom_affiche", length = 80)
    private String nomAffiche;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.DEVELOPPEUR;

    @Column(name = "pays", length = 60)
    private String pays;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "lien_profil", length = 255)
    private String lienProfil;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    /** Requis par JPA. */
    protected Utilisateur() {
    }

    public Utilisateur(String pseudo, String nomAffiche, String motDePasseHache, Role role) {
        this.pseudo = pseudo;
        this.nomAffiche = nomAffiche;
        this.motDePasse = motDePasseHache;
        this.role = role;
    }

    @PrePersist
    void avantInsertion() {
        if (dateCreation == null) {
            dateCreation = Instant.now();
        }
        if (role == null) {
            role = Role.DEVELOPPEUR;
        }
    }

    public Long getId() { return id; }
    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public String getNomAffiche() { return nomAffiche; }
    public void setNomAffiche(String nomAffiche) { this.nomAffiche = nomAffiche; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasseHache) { this.motDePasse = motDePasseHache; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getLienProfil() { return lienProfil; }
    public void setLienProfil(String lienProfil) { this.lienProfil = lienProfil; }
    public Instant getDateCreation() { return dateCreation; }
}

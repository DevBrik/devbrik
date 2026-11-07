package sn.devbrik.backend.demande.entity;

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
 * Table demande (V1). Aucune relation JPA vers un autre module :
 * idCategorie, idLangage et idAuteur sont de simples identifiants.
 */
@Entity
@Table(name = "demande")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long id;

    @Column(name = "titre", nullable = false, length = 150)
    private String titre;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 10)
    private StatutDemande statut = StatutDemande.OUVERTE;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "id_categorie")
    private Long idCategorie;

    @Column(name = "id_langage")
    private Long idLangage;

    @Column(name = "id_auteur", nullable = false, updatable = false)
    private Long idAuteur;

    protected Demande() {
        // requis par JPA
    }

    public Demande(Long idAuteur, String titre, String description, Long idCategorie, Long idLangage) {
        this.idAuteur = idAuteur;
        this.titre = titre;
        this.description = description;
        this.idCategorie = idCategorie;
        this.idLangage = idLangage;
    }

    @PrePersist
    void avantInsertion() {
        if (dateCreation == null) {
            dateCreation = Instant.now();
        }
    }

    /** RG12 : appelée uniquement quand une réponse vient d'être acceptée. */
    public void resoudre() {
        this.statut = StatutDemande.RESOLUE;
    }

    /** RG12 : appelée uniquement quand l'acceptation est retirée. */
    public void rouvrir() {
        this.statut = StatutDemande.OUVERTE;
    }

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public StatutDemande getStatut() { return statut; }
    public Instant getDateCreation() { return dateCreation; }
    public Long getIdCategorie() { return idCategorie; }
    public Long getIdLangage() { return idLangage; }
    public Long getIdAuteur() { return idAuteur; }
}
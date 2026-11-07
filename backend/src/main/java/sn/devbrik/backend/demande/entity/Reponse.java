package sn.devbrik.backend.demande.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Table reponse (V1). idBrique est facultatif (RG13) ; l'unicité d'une
 * réponse acceptée par demande est garantie par l'index uk_reponse_acceptee.
 */
@Entity
@Table(name = "reponse")
public class Reponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reponse")
    private Long id;

    @Column(name = "contenu", nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "acceptee", nullable = false)
    private boolean acceptee = false;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "id_demande", nullable = false, updatable = false)
    private Long idDemande;

    @Column(name = "id_brique")
    private Long idBrique;

    @Column(name = "id_auteur", nullable = false, updatable = false)
    private Long idAuteur;

    protected Reponse() {
        // requis par JPA
    }

    public Reponse(Long idDemande, Long idAuteur, String contenu, Long idBrique) {
        this.idDemande = idDemande;
        this.idAuteur = idAuteur;
        this.contenu = contenu;
        this.idBrique = idBrique;
    }

    @PrePersist
    void avantInsertion() {
        if (dateCreation == null) {
            dateCreation = Instant.now();
        }
    }

    public Long getId() { return id; }
    public String getContenu() { return contenu; }
    public boolean isAcceptee() { return acceptee; }
    public void setAcceptee(boolean acceptee) { this.acceptee = acceptee; }
    public Instant getDateCreation() { return dateCreation; }
    public Long getIdDemande() { return idDemande; }
    public Long getIdBrique() { return idBrique; }
    public Long getIdAuteur() { return idAuteur; }
}
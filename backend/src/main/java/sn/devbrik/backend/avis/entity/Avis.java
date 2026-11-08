package sn.devbrik.backend.avis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avis")
    private Long id;

    @Column(name = "note", nullable = false)
    private Integer note;

    @Column(name = "commentaire", length = 2000)
    private String commentaire;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    @Column(name = "id_brique", nullable = false, updatable = false)
    private Long idBrique;

    @Column(name = "id_auteur", nullable = false, updatable = false)
    private Long idAuteur;

    protected Avis() {
        // requis par JPA
    }

    public Avis(Long idBrique, Long idAuteur, Integer note, String commentaire) {
        this.idBrique = idBrique;
        this.idAuteur = idAuteur;
        this.note = note;
        this.commentaire = commentaire;
    }

    @PrePersist
    void avantCreation() {
        Instant maintenant = Instant.now();
        this.dateCreation = maintenant;
        this.dateModification = maintenant;
    }

    @PreUpdate
    void avantModification() {
        this.dateModification = Instant.now();
    }

    /** RG8 : mise à jour de l'avis existant par le même appel. */
    public void modifier(Integer note, String commentaire) {
        this.note = note;
        this.commentaire = commentaire;
    }

    public Long getId() { return id; }
    public Integer getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public Instant getDateCreation() { return dateCreation; }
    public Instant getDateModification() { return dateModification; }
    public Long getIdBrique() { return idBrique; }
    public Long getIdAuteur() { return idAuteur; }
}
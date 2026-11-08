package sn.devbrik.backend.avis.entity;

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

@Entity
@Table(name = "signalement_brique")
public class SignalementBrique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signalement")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "motif", nullable = false, length = 20)
    private MotifSignalement motif;

    @Column(name = "details", length = 1000)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 10)
    private StatutSignalement statut = StatutSignalement.OUVERT;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_traitement", length = 20)
    private ActionTraitement actionTraitement;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_traitement")
    private Instant dateTraitement;

    @Column(name = "id_brique", nullable = false, updatable = false)
    private Long idBrique;

    @Column(name = "id_auteur", nullable = false, updatable = false)
    private Long idAuteur;

    protected SignalementBrique() {
        // requis par JPA
    }

    public SignalementBrique(Long idBrique, Long idAuteur, MotifSignalement motif, String details) {
        this.idBrique = idBrique;
        this.idAuteur = idAuteur;
        this.motif = motif;
        this.details = details;
    }

    @PrePersist
    void avantCreation() {
        this.dateCreation = Instant.now();
    }

    /** RG11 : passe le signalement à TRAITE avec l'action choisie. */
    public void traiter(ActionTraitement action) {
        this.statut = StatutSignalement.TRAITE;
        this.actionTraitement = action;
        this.dateTraitement = Instant.now();
    }

    public Long getId() { return id; }
    public MotifSignalement getMotif() { return motif; }
    public String getDetails() { return details; }
    public StatutSignalement getStatut() { return statut; }
    public ActionTraitement getActionTraitement() { return actionTraitement; }
    public Instant getDateCreation() { return dateCreation; }
    public Instant getDateTraitement() { return dateTraitement; }
    public Long getIdBrique() { return idBrique; }
    public Long getIdAuteur() { return idAuteur; }
}
package sn.devbrik.backend.brique.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entité principale du module briques.
 *
 * Pas de relation JPA vers Utilisateur (inter-modules) : seul l'ID est stocké (idAuteur).
 * Les références @ManyToOne vers Categorie et Langage sont autorisées car ce sont
 * des entités du même module.
 *
 * Contraintes SQL vérifiées côté DB (Flyway V1) ; les contraintes Jakarta Validation
 * servent de filet de sécurité côté service.
 */
@Entity
@Table(name = "brique")
public class Brique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_brique")
    private Long id;

    @Column(nullable = false, length = 120)
    private String titre;

    @Column(nullable = false, length = 300)
    private String resume;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(name = "mode_emploi", nullable = false, columnDefinition = "TEXT")
    private String modeEmploi;

    @Column(length = 500)
    private String prerequis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Licence licence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutBrique statut = StatutBrique.PUBLIEE;

    @Column(name = "version_code", nullable = false)
    private int versionCode = 1;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    @Column(name = "date_modification", nullable = false)
    private Instant dateModification = Instant.now();

    /** ID de l'auteur (pas de JPA cross-module). */
    @Column(name = "id_auteur", nullable = false)
    private Long idAuteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_langage", nullable = false)
    private Langage langage;

    protected Brique() {}

    // ── Constructeur de création ──────────────────────────────────────────────

    public Brique(String titre, String resume, String code, String modeEmploi,
                  String prerequis, Licence licence, Long idAuteur,
                  Categorie categorie, Langage langage) {
        this.titre        = titre;
        this.resume       = resume;
        this.code         = code;
        this.modeEmploi   = modeEmploi;
        this.prerequis    = prerequis;
        this.licence      = licence;
        this.idAuteur     = idAuteur;
        this.categorie    = categorie;
        this.langage      = langage;
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    public void modifier(String titre, String resume, String code, String modeEmploi,
                         String prerequis, Licence licence,
                         Categorie categorie, Langage langage) {
        // RG6 : la version du code augmente de 1 seulement si le champ code change.
        if (!this.code.equals(code)) {
            this.versionCode += 1;
        }
        this.titre          = titre;
        this.resume         = resume;
        this.code           = code;
        this.modeEmploi     = modeEmploi;
        this.prerequis      = prerequis;
        this.licence        = licence;
        this.categorie      = categorie;
        this.langage        = langage;
        this.dateModification = Instant.now();
    }

    public void masquer()  { this.statut = StatutBrique.MASQUEE; this.dateModification = Instant.now(); }
    public void republier() { this.statut = StatutBrique.PUBLIEE; this.dateModification = Instant.now(); }

    // ── Accesseurs ────────────────────────────────────────────────────────────

    public Long        getId()               { return id; }
    public String      getTitre()            { return titre; }
    public String      getResume()           { return resume; }
    public String      getCode()             { return code; }
    public String      getModeEmploi()       { return modeEmploi; }
    public String      getPrerequis()        { return prerequis; }
    public Licence     getLicence()          { return licence; }
    public StatutBrique getStatut()          { return statut; }
    public int         getVersionCode()      { return versionCode; }
    public Instant     getDateCreation()     { return dateCreation; }
    public Instant     getDateModification() { return dateModification; }
    public Long        getIdAuteur()         { return idAuteur; }
    public Categorie   getCategorie()        { return categorie; }
    public Langage     getLangage()          { return langage; }
}

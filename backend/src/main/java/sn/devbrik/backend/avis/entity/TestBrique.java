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
@Table(name = "test_brique")
public class TestBrique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat", nullable = false, length = 2)
    private ResultatTest resultat;

    @Column(name = "environnement", length = 120)
    private String environnement;

    /** RG9 : version du code au moment du vote. */
    @Column(name = "version_code", nullable = false)
    private Integer versionCode;

    @Column(name = "date_test", nullable = false)
    private Instant dateTest;

    @Column(name = "id_brique", nullable = false, updatable = false)
    private Long idBrique;

    @Column(name = "id_auteur", nullable = false, updatable = false)
    private Long idAuteur;

    protected TestBrique() {
        // requis par JPA
    }

    public TestBrique(Long idBrique, Long idAuteur, ResultatTest resultat,
                      String environnement, Integer versionCode) {
        this.idBrique = idBrique;
        this.idAuteur = idAuteur;
        this.resultat = resultat;
        this.environnement = environnement;
        this.versionCode = versionCode;
    }

    @PrePersist
    void avantCreation() {
        this.dateTest = Instant.now();
    }

    /** RG9 : l'utilisateur change son test ; la version et la date sont celles du nouveau vote. */
    public void changer(ResultatTest resultat, String environnement, Integer versionCode) {
        this.resultat = resultat;
        this.environnement = environnement;
        this.versionCode = versionCode;
        this.dateTest = Instant.now();
    }

    public Long getId() { return id; }
    public ResultatTest getResultat() { return resultat; }
    public String getEnvironnement() { return environnement; }
    public Integer getVersionCode() { return versionCode; }
    public Instant getDateTest() { return dateTest; }
    public Long getIdBrique() { return idBrique; }
    public Long getIdAuteur() { return idAuteur; }
}
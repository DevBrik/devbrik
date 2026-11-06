package sn.devbrik.backend.brique.entity;

import jakarta.persistence.*;

/**
 * Référentiel des catégories (géré par admin, inséré par Flyway V2).
 */
@Entity
@Table(name = "categorie")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(nullable = false, unique = true, length = 80)
    private String nom;

    @Column(length = 255)
    private String description;

    protected Categorie() {}

    public Categorie(String slug, String nom, String description) {
        this.slug        = slug;
        this.nom         = nom;
        this.description = description;
    }

    public Long   getId()          { return id; }
    public String getSlug()        { return slug; }
    public String getNom()         { return nom; }
    public String getDescription() { return description; }
}

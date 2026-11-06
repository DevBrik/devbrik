package sn.devbrik.backend.brique.entity;

import jakarta.persistence.*;

/**
 * Référentiel des langages (géré par admin, inséré par Flyway V2).
 */
@Entity
@Table(name = "langage")
public class Langage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_langage")
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String slug;

    @Column(nullable = false, unique = true, length = 50)
    private String nom;

    protected Langage() {}

    public Langage(String slug, String nom) {
        this.slug = slug;
        this.nom  = nom;
    }

    public Long   getId()   { return id; }
    public String getSlug() { return slug; }
    public String getNom()  { return nom; }
}

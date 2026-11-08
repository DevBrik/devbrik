package sn.devbrik.backend.brique.service;

import sn.devbrik.backend.brique.dto.*;
import sn.devbrik.backend.brique.entity.StatutBrique;
import sn.devbrik.backend.common.PageReponse;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Service public du module briques (11.4 et 13.4).
 * Les autres modules n'utilisent que resume, references, masquer et compterPubliees.
 */
public interface BriqueService {

    // ── Routes (11.4) ─────────────────────────────────────────────────────────

    /** GET /api/briques : briques PUBLIEES uniquement. tri : recentes (défaut), mieux-notees, plus-testees. */
    PageReponse<BriqueResumeDto> listerPubliees(String q, String categorie, String langage, Long auteur,
                                                String tri, Integer page, Integer taille);

    /** GET /api/briques/{id} : une brique MASQUEE n'est visible que de son auteur et des administrateurs (RG7). */
    BriqueDetailDto obtenir(Long id);

    /** POST /api/briques. */
    BriqueDetailDto publier(BriqueCreationDto dto);

    /** PUT /api/briques/{id} : auteur seulement (RG19). */
    BriqueDetailDto modifier(Long id, BriqueModificationDto dto);

    /** DELETE /api/briques/{id} : auteur ou administrateur (RG19). */
    void supprimer(Long id);

    /** GET /api/moi/briques : briques de l'appelant, MASQUEES comprises ; statut facultatif. */
    PageReponse<BriqueResumeDto> mesBriques(String q, String categorie, String langage, String tri,
                                            String statut, Integer page, Integer taille);

    /** GET /api/admin/briques : toutes les briques, filtre statut facultatif. */
    PageReponse<BriqueResumeDto> listerPourAdmin(String q, String categorie, String langage, Long auteur,
                                                 String tri, String statut, Integer page, Integer taille);

    /** PUT /api/admin/briques/{id}/statut : renvoie la brique à jour. */
    BriqueDetailDto changerStatut(Long id, StatutBrique statut);

    // ── Méthodes publiques pour les autres modules (13.4) ─────────────────────

    /** id, idAuteur, statut, versionCode, titre ; vide si la brique n'existe pas. */
    Optional<BriqueResumeInterne> resume(Long id);

    /** id, titre, publiée ou non. Les identifiants inconnus sont absents de la map. */
    Map<Long, BriqueReference> references(Collection<Long> ids);

    /** Masque une brique (RG7). 404 RESSOURCE_INTROUVABLE si elle n'existe pas. */
    void masquer(Long id);

    void republier(Long id);

    /** Nombre de briques PUBLIEES de l'auteur (réputation, RG14). */
    long compterPubliees(Long idAuteur);
}

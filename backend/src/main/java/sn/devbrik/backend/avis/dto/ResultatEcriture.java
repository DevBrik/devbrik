package sn.devbrik.backend.avis.dto;

/** Résultat d'un PUT : le corps de la réponse et l'indication « créé » (201) ou « mis à jour » (200). */
public record ResultatEcriture<T>(T corps, boolean cree) {
}
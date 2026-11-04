/**
 * B2 — Paiement Orange Money (simulation TypeScript).
 *
 * Cette brique fournit les types et les fonctions utilitaires pour
 * préparer et valider une requête de paiement Orange Money (Sénégal).
 *
 * Note : les appels réseau réels vers l'API Orange Money nécessitent
 * des credentials. Cette brique expose uniquement la logique métier
 * (validation, formatage du payload, lecture de la réponse).
 */

// ── Types ─────────────────────────────────────────────────────────────────────

export interface PaiementOrangeConfig {
  /** Clé marchande fournie par Orange Money Sénégal. */
  cleMarchande: string;
  /** Identifiant du marchand. */
  idMarchand: string;
  /** URL de rappel (webhook) appelée après paiement. */
  urlRappel: string;
}

export interface DemandePaiement {
  /** Numéro de téléphone du payeur (format : 77XXXXXXX ou +221XXXXXXXXX). */
  telephone: string;
  /** Montant en FCFA (entier, minimum 100). */
  montant: number;
  /** Référence unique de la transaction côté marchand. */
  reference: string;
  /** Description optionnelle. */
  description?: string;
}

export interface ReponsePaiement {
  succes: boolean;
  codeTransaction?: string;
  message: string;
}

export type ErreurPaiement =
  | 'TELEPHONE_INVALIDE'
  | 'MONTANT_INVALIDE'
  | 'REFERENCE_VIDE'
  | 'CONFIG_INVALIDE';

// ── Validation ────────────────────────────────────────────────────────────────

const REGEX_TEL_SN = /^(?:\+221|00221)?7[5678]\d{7}$/;

/**
 * Valide un numéro de téléphone Orange Sénégal.
 * Accepte : 77XXXXXXX, 76XXXXXXX, 75XXXXXXX, 78XXXXXXX,
 *           +22177XXXXXXX, 0022178XXXXXXX.
 */
export function validerTelephone(tel: string): boolean {
  if (!tel || typeof tel !== 'string') return false;
  return REGEX_TEL_SN.test(tel.trim().replace(/\s/g, ''));
}

/**
 * Valide un montant en FCFA (entier ≥ 100).
 */
export function validerMontant(montant: number): boolean {
  return Number.isInteger(montant) && montant >= 100;
}

/**
 * Normalise un numéro de téléphone au format international (+221XXXXXXXXX).
 */
export function normaliserTelephone(tel: string): string {
  const propre = tel.trim().replace(/\s/g, '');
  if (propre.startsWith('+221')) return propre;
  if (propre.startsWith('00221')) return '+221' + propre.slice(5);
  return '+221' + propre;
}

// ── Payload ───────────────────────────────────────────────────────────────────

/**
 * Construit le payload JSON prêt à envoyer à l'API Orange Money.
 * Lance une erreur si les paramètres sont invalides.
 */
export function construirePayload(
  config: PaiementOrangeConfig,
  demande: DemandePaiement,
): Record<string, unknown> {
  const erreurs: ErreurPaiement[] = [];

  if (!config.cleMarchande || !config.idMarchand || !config.urlRappel) {
    erreurs.push('CONFIG_INVALIDE');
  }
  if (!validerTelephone(demande.telephone)) {
    erreurs.push('TELEPHONE_INVALIDE');
  }
  if (!validerMontant(demande.montant)) {
    erreurs.push('MONTANT_INVALIDE');
  }
  if (!demande.reference?.trim()) {
    erreurs.push('REFERENCE_VIDE');
  }

  if (erreurs.length > 0) {
    throw new Error(`Paiement invalide : ${erreurs.join(', ')}`);
  }

  return {
    merchant_key:   config.cleMarchande,
    currency:       'OUV',
    order_id:       demande.reference,
    amount:         demande.montant,
    return_url:     config.urlRappel,
    cancel_url:     config.urlRappel,
    notif_url:      config.urlRappel,
    lang:           'fr',
    reference:      demande.reference,
    merchant_name:  config.idMarchand,
    description:    demande.description ?? '',
    // Le numéro est normalisé au format international
    tel:            normaliserTelephone(demande.telephone),
  };
}

/**
 * Interprète la réponse de l'API Orange Money.
 */
export function lireReponse(reponseApi: Record<string, unknown>): ReponsePaiement {
  const statut = String(reponseApi['status'] ?? '').toLowerCase();
  const succes = statut === 'initiated' || statut === 'success';

  return {
    succes,
    codeTransaction: reponseApi['pay_token'] as string | undefined,
    message:         succes
      ? 'Paiement initié avec succès.'
      : `Paiement échoué : ${reponseApi['message'] ?? 'Raison inconnue'}`,
  };
}

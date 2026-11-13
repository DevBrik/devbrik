/**
 * B2 · Formatage d'un montant en FCFA (franc CFA, codes ISO XOF et XAF).
 *
 * Le franc CFA n'a pas de subdivision utilisée : les montants sont arrondis à l'unité.
 * Séparateur des milliers : espace (par défaut une espace insécable fine, U+202F, comme en français).
 */

export interface OptionsFcfa {
  /** Séparateur des milliers. Par défaut : espace fine insécable (U+202F). */
  separateur?: string;
  /** Suffixe affiché après le montant. Par défaut : « FCFA ». Chaîne vide pour aucun suffixe. */
  suffixe?: string;
}

const ESPACE_FINE = ' ';

/** Formate un montant : 1500000 → « 1 500 000 FCFA ». Arrondi à l'unité (demi vers le haut en valeur absolue). */
export function formaterFcfa(montant: number, options: OptionsFcfa = {}): string {
  if (typeof montant !== 'number' || !Number.isFinite(montant)) {
    throw new TypeError(`Montant invalide : ${String(montant)}`);
  }
  const separateur = options.separateur ?? ESPACE_FINE;
  const suffixe = options.suffixe ?? 'FCFA';
  const arrondi = Math.sign(montant) * Math.round(Math.abs(montant));
  const signe = arrondi < 0 ? '-' : '';
  const chiffres = String(Math.abs(arrondi));
  const groupes: string[] = [];
  for (let fin = chiffres.length; fin > 0; fin -= 3) {
    groupes.unshift(chiffres.slice(Math.max(0, fin - 3), fin));
  }
  const corps = signe + groupes.join(separateur);
  return suffixe ? `${corps} ${suffixe}` : corps;
}

/**
 * Lit un montant saisi par un utilisateur : « 15 000 », « 15.000 FCFA », « 15 000 F CFA ».
 * Renvoie un entier, ou null si la saisie n'est pas un montant.
 */
export function lireFcfa(saisie: string): number | null {
  if (typeof saisie !== 'string') {
    return null;
  }
  const nettoye = saisie
    .replace(/f\s*cfa|xof|xaf|francs?\s*cfa/gi, '')
    .replace(/[\s  .]/g, '')
    .trim();
  if (!/^-?\d+$/.test(nettoye)) {
    return null;
  }
  const valeur = Number(nettoye);
  return Number.isSafeInteger(valeur) ? valeur : null;
}

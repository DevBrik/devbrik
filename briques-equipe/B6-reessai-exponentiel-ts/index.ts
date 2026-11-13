/**
 * B6 · Réessai avec attente exponentielle (réseaux instables).
 *
 * Relance une opération asynchrone qui échoue, en attendant de plus en plus longtemps :
 * delai(n) = min(delaiMax, delaiInitial × facteur^(n-1)), avec une part aléatoire facultative (« jitter »)
 * pour éviter que tous les clients réessaient au même instant.
 * L'horloge (attendre) et le hasard (aleatoire) sont injectables : les tests n'attendent jamais vraiment.
 */

export interface OptionsReessai {
  /** Nombre total de tentatives (première comprise). Défaut : 5. */
  tentatives?: number;
  /** Premier délai d'attente en millisecondes. Défaut : 500. */
  delaiInitial?: number;
  /** Multiplicateur entre deux attentes. Défaut : 2. */
  facteur?: number;
  /** Délai maximal en millisecondes. Défaut : 30 000. */
  delaiMax?: number;
  /** Part aléatoire de 0 (aucune) à 1 (« full jitter »). Défaut : 0.5. */
  jitter?: number;
  /** Décide si une erreur mérite un nouvel essai. Défaut : toujours. */
  doitReessayer?: (erreur: unknown, tentative: number) => boolean;
  /** Appelé avant chaque attente (journalisation, interface « nouvel essai dans … »). */
  surReessai?: (erreur: unknown, tentative: number, delai: number) => void;
  /** Signal d'annulation : arrête les essais dès qu'il est déclenché. */
  signal?: AbortSignal;
  /** Fonction d'attente (injectable pour les tests). */
  attendre?: (ms: number) => Promise<void>;
  /** Source de hasard dans [0, 1) (injectable pour les tests). */
  aleatoire?: () => number;
}

export class EchecReessai extends Error {
  readonly tentatives: number;
  readonly derniereErreur: unknown;

  constructor(tentatives: number, derniereErreur: unknown) {
    super(`Échec après ${tentatives} tentative(s)`);
    this.name = 'EchecReessai';
    this.tentatives = tentatives;
    this.derniereErreur = derniereErreur;
  }
}

const attendreParDefaut = (ms: number): Promise<void> => new Promise((r) => setTimeout(r, ms));

/** Délai avant la tentative suivante (après l'échec numéro « tentative », à partir de 1). */
export function calculerDelai(
  tentative: number,
  options: Pick<OptionsReessai, 'delaiInitial' | 'facteur' | 'delaiMax' | 'jitter' | 'aleatoire'> = {},
): number {
  const { delaiInitial = 500, facteur = 2, delaiMax = 30_000, jitter = 0.5, aleatoire = Math.random } = options;
  const base = Math.min(delaiMax, delaiInitial * Math.pow(facteur, tentative - 1));
  const part = Math.min(1, Math.max(0, jitter));
  return Math.round(base * (1 - part) + base * part * aleatoire());
}

/** Exécute « operation » et la relance en cas d'échec. Lève EchecReessai quand les essais sont épuisés. */
export async function reessayer<T>(operation: (tentative: number) => Promise<T>, options: OptionsReessai = {}): Promise<T> {
  const tentatives = Math.max(1, Math.floor(options.tentatives ?? 5));
  const attendre = options.attendre ?? attendreParDefaut;
  const doitReessayer = options.doitReessayer ?? (() => true);
  let derniere: unknown;
  let effectuees = 0;
  for (let n = 1; n <= tentatives; n++) {
    if (options.signal?.aborted) {
      throw new EchecReessai(n - 1, options.signal.reason ?? derniere);
    }
    effectuees = n;
    try {
      return await operation(n);
    } catch (erreur) {
      derniere = erreur;
      if (n === tentatives || !doitReessayer(erreur, n)) {
        break;
      }
      const delai = calculerDelai(n, options);
      options.surReessai?.(erreur, n, delai);
      await attendre(delai);
    }
  }
  throw new EchecReessai(effectuees, derniere);
}

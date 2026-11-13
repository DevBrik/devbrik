/**
 * B8 · File d'envoi hors ligne avec reprise.
 *
 * Les messages (formulaires, mesures, commandes…) sont mis en file et conservés dans un stockage
 * (localStorage, IndexedDB, fichier, mémoire). Quand le réseau revient, la file est vidée dans l'ordre ;
 * un envoi qui échoue arrête le vidage et le message reste en tête de file pour le prochain essai.
 * Aucune dépendance. Module ES.
 */

/** Stockage en mémoire (tests, Node.js). */
export function stockageMemoire() {
  const donnees = new Map();
  return {
    lire: (cle) => (donnees.has(cle) ? donnees.get(cle) : null),
    ecrire: (cle, valeur) => void donnees.set(cle, valeur),
  };
}

/** Adaptateur pour window.localStorage (ou sessionStorage). */
export function stockageNavigateur(stockage = globalThis.localStorage) {
  return {
    lire: (cle) => stockage.getItem(cle),
    ecrire: (cle, valeur) => stockage.setItem(cle, valeur),
  };
}

let compteur = 0;
const nouvelId = () => `${Date.now().toString(36)}-${(compteur++).toString(36)}-${Math.random().toString(36).slice(2, 8)}`;

export class FileHorsLigne {
  /**
   * @param {object} options
   * @param {(message: unknown) => Promise<void>} options.envoyer  envoie un message ; rejette en cas d'échec
   * @param {{lire(cle: string): string|null, ecrire(cle: string, valeur: string): void}} [options.stockage]
   * @param {string} [options.cle]  clé de stockage (défaut « devbrik.file-hors-ligne »)
   * @param {number} [options.maxElements]  taille maximale de la file (défaut 500) ; au-delà, ajouter() lève une erreur
   */
  constructor({ envoyer, stockage = stockageMemoire(), cle = 'devbrik.file-hors-ligne', maxElements = 500 }) {
    if (typeof envoyer !== 'function') {
      throw new TypeError('envoyer doit être une fonction');
    }
    this._envoyer = envoyer;
    this._stockage = stockage;
    this._cle = cle;
    this._max = maxElements;
    this._vidage = null;
  }

  /** Éléments en attente, du plus ancien au plus récent. */
  elements() {
    try {
      const brut = this._stockage.lire(this._cle);
      const liste = brut ? JSON.parse(brut) : [];
      return Array.isArray(liste) ? liste : [];
    } catch {
      return []; // stockage corrompu : on repart d'une file vide plutôt que de bloquer l'application
    }
  }

  get taille() {
    return this.elements().length;
  }

  _sauver(liste) {
    this._stockage.ecrire(this._cle, JSON.stringify(liste));
  }

  /** Ajoute un message (sérialisable en JSON) et renvoie son identifiant. */
  ajouter(message) {
    const liste = this.elements();
    if (liste.length >= this._max) {
      throw new Error(`File pleine (${this._max} éléments)`);
    }
    const element = { id: nouvelId(), message, ajouteLe: new Date().toISOString(), essais: 0 };
    liste.push(element);
    this._sauver(liste);
    return element.id;
  }

  /** Retire un message de la file sans l'envoyer. */
  retirer(id) {
    this._sauver(this.elements().filter((e) => e.id !== id));
  }

  /**
   * Envoie les messages dans l'ordre. S'arrête au premier échec (le message reste en tête).
   * Un seul vidage à la fois : un appel pendant un vidage renvoie le vidage en cours.
   * @returns {Promise<{envoyes: number, restants: number, erreur: unknown}>}
   */
  vider() {
    if (!this._vidage) {
      this._vidage = this._viderMaintenant().finally(() => {
        this._vidage = null;
      });
    }
    return this._vidage;
  }

  async _viderMaintenant() {
    let envoyes = 0;
    for (;;) {
      const liste = this.elements();
      if (liste.length === 0) {
        return { envoyes, restants: 0, erreur: null };
      }
      const [tete] = liste;
      try {
        await this._envoyer(tete.message);
      } catch (erreur) {
        const maj = this.elements().map((e) => (e.id === tete.id ? { ...e, essais: e.essais + 1 } : e));
        this._sauver(maj);
        return { envoyes, restants: maj.length, erreur };
      }
      this._sauver(this.elements().filter((e) => e.id !== tete.id));
      envoyes++;
    }
  }

  /** Navigateur : vide la file au retour du réseau. Renvoie une fonction pour arrêter l'écoute. */
  ecouterReseau(cible = globalThis) {
    const surRetour = () => void this.vider();
    cible.addEventListener('online', surRetour);
    if (cible.navigator?.onLine !== false) {
      surRetour();
    }
    return () => cible.removeEventListener('online', surRetour);
  }
}

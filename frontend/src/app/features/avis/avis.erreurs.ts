import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';
import { SocleAvis } from './avis.socle';

export interface DetailChamp {
  champ: string;
  message: string;
}

export interface ErreurApi {
  statut: number;
  code: string;
  message: string;
  details: DetailChamp[];
}

const MESSAGE_GENERIQUE = 'Une erreur est survenue. Réessayez dans un instant.';

/** Erreur au format de la section 11.2 : { statut, code, message, details?, horodatage }. */
export function lireErreur(e: unknown): ErreurApi {
  if (e instanceof HttpErrorResponse) {
    if (e.status === 0) {
      return {
        statut: 0,
        code: 'RESEAU',
        message: 'Connexion impossible. Vérifiez votre connexion Internet puis réessayez.',
        details: [],
      };
    }
    const corps = (e.error ?? {}) as Partial<ErreurApi>;
    return {
      statut: e.status,
      code: typeof corps.code === 'string' ? corps.code : 'ERREUR_INCONNUE',
      message: typeof corps.message === 'string' ? corps.message : MESSAGE_GENERIQUE,
      details: Array.isArray(corps.details) ? corps.details : [],
    };
  }
  return { statut: -1, code: 'ERREUR_INCONNUE', message: MESSAGE_GENERIQUE, details: [] };
}

/** Pose les messages du serveur sur les champs concernés. Renvoie true si au moins un champ est touché. */
export function appliquerDetails(erreur: ErreurApi, formulaire: FormGroup): boolean {
  let applique = false;
  for (const detail of erreur.details) {
    const controle = formulaire.get(detail.champ);
    if (controle) {
      controle.setErrors({ serveur: detail.message });
      controle.markAsTouched();
      applique = true;
    }
  }
  return applique;
}

/** Message à afficher sous un champ, ou null. Le message du serveur passe avant les messages locaux. */
export function messageChamp(controle: AbstractControl | null, messages: Record<string, string>): string | null {
  if (!controle || controle.valid || !(controle.touched || controle.dirty)) {
    return null;
  }
  const erreurs = controle.errors ?? {};
  if (typeof erreurs['serveur'] === 'string') {
    return erreurs['serveur'];
  }
  for (const cle of Object.keys(erreurs)) {
    const message = messages[cle];
    if (message) {
      return message;
    }
  }
  return null;
}

/** Met le focus sur le premier champ marqué invalide (attend que le gabarit soit rendu). */
export function focaliserPremierInvalide(racine: HTMLElement): void {
  setTimeout(() => racine.querySelector<HTMLElement>('[data-invalide="true"]')?.focus());
}

const CODES_SANS_REESSAI = ['AUTO_EVALUATION_INTERDITE', 'SIGNALEMENT_DEJA_ENVOYE'];

export interface ContexteEchec {
  formulaire: FormGroup;
  hote: HTMLElement;
  socle: SocleAvis;
  fermer: () => void;
}

/** Règles d'erreur communes aux trois dialogues (dossier 12.5). */
export function traiterEchec(e: unknown, contexte: ContexteEchec): void {
  const erreur = lireErreur(e);
  if (erreur.code === 'NON_AUTHENTIFIE') {
    contexte.fermer();
    contexte.socle.versConnexion();
    return;
  }
  if (erreur.code === 'REQUETE_INVALIDE' && appliquerDetails(erreur, contexte.formulaire)) {
    focaliserPremierInvalide(contexte.hote);
    return;
  }
  contexte.socle.erreur(erreur.message);
  if (CODES_SANS_REESSAI.includes(erreur.code)) {
    contexte.fermer();
  }
}

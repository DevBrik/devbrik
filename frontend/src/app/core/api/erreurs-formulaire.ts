import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';

/** Erreur lue au format de la section 11.2, avec un repli pour le réseau (statut 0). */
export interface ErreurLue {
  statut: number;
  code: string;
  message: string;
  details: { champ: string; message: string }[];
}

const MESSAGE_GENERIQUE = 'Une erreur est survenue. Réessayez dans un instant.';

export function lireErreur(e: unknown): ErreurLue {
  if (e instanceof HttpErrorResponse) {
    if (e.status === 0) {
      return {
        statut: 0,
        code: 'RESEAU',
        message: 'Connexion impossible. Vérifiez votre connexion Internet puis réessayez.',
        details: [],
      };
    }
    const corps = (e.error ?? {}) as Partial<ErreurLue>;
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
export function appliquerDetails(erreur: ErreurLue, formulaire: FormGroup): boolean {
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

/** Pose un message du serveur sur un champ précis (ex. TITRE_DEJA_UTILISE sous le titre). */
export function erreurSurChamp(formulaire: FormGroup, champ: string, message: string): void {
  const controle = formulaire.get(champ);
  controle?.setErrors({ serveur: message });
  controle?.markAsTouched();
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

/** Ne garde que les paramètres définis et non vides (pour HttpParams). */
export function parametres(objet: object): Record<string, string | number> {
  const resultat: Record<string, string | number> = {};
  for (const [cle, valeur] of Object.entries(objet)) {
    if (valeur !== undefined && valeur !== null && valeur !== '') {
      resultat[cle] = valeur as string | number;
    }
  }
  return resultat;
}

/** Validateur : la valeur rognée (trim) respecte une longueur, comme le serveur (11.1). */
export function longueurRognee(min: number, max: number) {
  return (c: AbstractControl): Record<string, true> | null => {
    const v = typeof c.value === 'string' ? c.value.trim() : '';
    if (min > 0 && v.length === 0) {
      return { required: true };
    }
    if (v.length > 0 && v.length < min) {
      return { minlength: true };
    }
    if (v.length > max) {
      return { maxlength: true };
    }
    return null;
  };
}

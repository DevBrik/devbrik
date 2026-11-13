import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** RG1 : 3 à 30 caractères, lettres, chiffres, point, tiret, tiret bas ; commence par une lettre ou un chiffre. */
export const MOTIF_PSEUDO = /^[A-Za-z0-9][A-Za-z0-9._-]{2,29}$/;

/** RG1 : pseudos réservés (le serveur fait foi : 400 PSEUDO_RESERVE). */
export const PSEUDOS_RESERVES = [
  'admin', 'administrator', 'administrateur', 'root', 'support', 'moderateur', 'devbrik', 'equipe', 'systalink', 'datacloud',
];

export const validerPseudo: ValidatorFn = (c: AbstractControl): ValidationErrors | null => {
  const v = String(c.value ?? '').trim();
  if (!v) {
    return { required: true };
  }
  if (!MOTIF_PSEUDO.test(v)) {
    return { motif: true };
  }
  return PSEUDOS_RESERVES.includes(v.toLowerCase()) ? { reserve: true } : null;
};

/** RG2 : 8 à 72 caractères. */
export const validerMotDePasse: ValidatorFn = (c: AbstractControl): ValidationErrors | null => {
  const v = String(c.value ?? '');
  if (!v) {
    return { required: true };
  }
  return v.length < 8 || v.length > 72 ? { longueur: true } : null;
};

/** Confirmation vérifiée dans le navigateur (seul le mot de passe est envoyé). */
export function confirmationIdentique(champ: string, confirmation: string): ValidatorFn {
  return (groupe: AbstractControl): ValidationErrors | null => {
    const a = groupe.get(champ)?.value;
    const b = groupe.get(confirmation);
    if (!b) {
      return null;
    }
    const erreurs = { ...(b.errors ?? {}) };
    delete erreurs['differente'];
    if (b.value && a !== b.value) {
      b.setErrors({ ...erreurs, differente: true });
    } else {
      b.setErrors(Object.keys(erreurs).length ? erreurs : null);
    }
    return null;
  };
}

/** Adresse de retour interne uniquement (pas de redirection ouverte). */
export function retourSur(retour: string | null): string {
  return retour && retour.startsWith('/') && !retour.startsWith('//') && !retour.startsWith('/compte/') ? retour : '/briques';
}

export const MESSAGES_PSEUDO = {
  required: 'Le pseudo est obligatoire.',
  motif: 'De 3 à 30 caractères : lettres, chiffres, point, tiret ou tiret bas ; commence par une lettre ou un chiffre.',
  reserve: 'Ce pseudo est réservé. Choisissez-en un autre.',
};

export const MESSAGES_MDP = {
  required: 'Le mot de passe est obligatoire.',
  longueur: 'Le mot de passe doit contenir entre 8 et 72 caractères.',
};

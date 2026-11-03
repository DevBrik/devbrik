import { HttpErrorResponse } from '@angular/common/http';
import { ErreurApi } from './modeles';

function corpsErreur(err: unknown): Partial<ErreurApi> | null {
  if (err instanceof HttpErrorResponse && err.error && typeof err.error === 'object') {
    return err.error as Partial<ErreurApi>;
  }
  return null;
}

/** Message en français à afficher à l'utilisateur. */
export function messageErreur(err: unknown): string {
  if (err instanceof HttpErrorResponse && err.status === 0) {
    return 'Impossible de joindre le serveur. Vérifiez votre connexion.';
  }
  const corps = corpsErreur(err);
  if (corps && typeof corps.message === 'string') {
    return corps.message;
  }
  return 'Une erreur inattendue est survenue.';
}

/** Code du catalogue 11.7 (ex. TITRE_DEJA_UTILISE), ou null. */
export function codeErreur(err: unknown): string | null {
  const corps = corpsErreur(err);
  return corps && typeof corps.code === 'string' ? corps.code : null;
}
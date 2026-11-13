import { FormGroup } from '@angular/forms';
import {
  appliquerDetails,
  focaliserPremierInvalide,
  lireErreur,
} from '../../core/api/erreurs-formulaire';
import { SocleAvis } from './avis.socle';

export type { ErreurLue as ErreurApi } from '../../core/api/erreurs-formulaire';
export {
  appliquerDetails,
  focaliserPremierInvalide,
  lireErreur,
  messageChamp,
} from '../../core/api/erreurs-formulaire';

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

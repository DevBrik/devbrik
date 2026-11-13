import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import {
  DialogueConfirmationComponent,
  DonneesConfirmation,
  ResultatConfirmation,
} from './dialogue-confirmation.component';

@Injectable({ providedIn: 'root' })
export class DialogueService {
  private readonly dialog = inject(MatDialog);

  private ouvrir(donnees: DonneesConfirmation): Observable<ResultatConfirmation | undefined> {
    return this.dialog
      .open<DialogueConfirmationComponent, DonneesConfirmation, ResultatConfirmation>(DialogueConfirmationComponent, {
        data: donnees,
        width: '30rem',
        maxWidth: '100vw',
        autoFocus: 'first-tabbable',
      })
      .afterClosed();
  }

  /** Émet true si l'utilisateur confirme. */
  confirmer(donnees: DonneesConfirmation): Observable<boolean> {
    return this.ouvrir(donnees).pipe(map((r) => r === true || typeof r === 'string'));
  }

  /** Émet le mot de passe saisi, ou null si annulé. */
  confirmerAvecMotDePasse(donnees: Omit<DonneesConfirmation, 'avecMotDePasse'>): Observable<string | null> {
    return this.ouvrir({ ...donnees, avecMotDePasse: true }).pipe(map((r) => (typeof r === 'string' ? r : null)));
  }
}

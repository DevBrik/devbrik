import { Component, Injectable, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';

export interface DonneesConfirmation {
  titre: string;
  message: string;
  libelleConfirmer?: string;
  libelleAnnuler?: string;
}

@Component({
  selector: 'app-dialogue-confirmation',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ donnees.titre }}</h2>
    <mat-dialog-content>{{ donnees.message }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" [mat-dialog-close]="false" cdkFocusInitial>
        {{ donnees.libelleAnnuler ?? 'Annuler' }}
      </button>
      <button mat-flat-button type="button" [mat-dialog-close]="true">
        {{ donnees.libelleConfirmer ?? 'Confirmer' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class DialogueConfirmation {
  protected readonly donnees = inject<DonneesConfirmation>(MAT_DIALOG_DATA);
}

@Injectable({ providedIn: 'root' })
export class DialogueService {
  private readonly dialog = inject(MatDialog);

  /** Renvoie true si l'utilisateur confirme. Le focus est sur « Annuler » par sécurité. */
  confirmer(donnees: DonneesConfirmation): Observable<boolean> {
    return this.dialog
      .open(DialogueConfirmation, { data: donnees, width: '420px', maxWidth: '92vw' })
      .afterClosed()
      .pipe(map((resultat) => resultat === true));
  }
}
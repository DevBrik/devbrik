import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { map, Observable } from 'rxjs';
import { STYLES_COMMUNS } from '../avis.styles';

export interface DonneesConfirmation {
  titre: string;
  message: string;
  libelleConfirmer: string;
}

/** Gabarit local, à remplacer par db-dialogue-confirmation quand il sera fusionné. */
@Component({
  selector: 'avis-dialogue-confirmation',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatDialogModule],
  template: `
    <h2 mat-dialog-title>{{ donnees.titre }}</h2>
    <mat-dialog-content>
      <p>{{ donnees.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions class="actions">
      <button type="button" class="btn btn-destructif" [mat-dialog-close]="true">{{ donnees.libelleConfirmer }}</button>
      <button type="button" class="btn btn-contour-erreur" cdkFocusInitial [mat-dialog-close]="false">Annuler</button>
    </mat-dialog-actions>
  `,
  styles: [STYLES_COMMUNS],
})
export class DialogueConfirmationLocalComponent {
  protected readonly donnees = inject<DonneesConfirmation>(MAT_DIALOG_DATA);
}

/** Ouvre la confirmation ; émet true si l'utilisateur confirme. */
export function confirmer(dialog: MatDialog, donnees: DonneesConfirmation): Observable<boolean> {
  return dialog
    .open<DialogueConfirmationLocalComponent, DonneesConfirmation, boolean>(DialogueConfirmationLocalComponent, {
      data: donnees,
      width: '28rem',
      maxWidth: '100vw',
    })
    .afterClosed()
    .pipe(map((reponse) => reponse === true));
}

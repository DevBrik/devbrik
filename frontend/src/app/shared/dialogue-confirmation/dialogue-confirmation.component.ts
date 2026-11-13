import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

export interface DonneesConfirmation {
  titre: string;
  message: string;
  /** Second paragraphe (ex. rappel de la suppression en cascade). */
  rappel?: string;
  libelleConfirmer: string;
  libelleAnnuler?: string;
  /** Variante avec mot de passe (suppression du compte). */
  avecMotDePasse?: boolean;
  /** Icône d'avertissement (charte 6.9, dialogues destructifs). */
  destructif?: boolean;
}

/** Résultat : null si annulé ; true si confirmé ; le mot de passe saisi pour la variante avec mot de passe. */
export type ResultatConfirmation = true | string | null;

/** Confirmation d'une action (charte 6.9) : focus piégé, Échap ferme, « Annuler » toujours visible. */
@Component({
  selector: 'db-dialogue-confirmation',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatDialogModule, FormsModule],
  templateUrl: './dialogue-confirmation.component.html',
  styleUrl: './dialogue-confirmation.component.scss',
})
export class DialogueConfirmationComponent {
  protected readonly donnees = inject<DonneesConfirmation>(MAT_DIALOG_DATA);
  private readonly ref = inject<MatDialogRef<DialogueConfirmationComponent, ResultatConfirmation>>(MatDialogRef);

  protected readonly motDePasse = signal('');
  protected readonly visible = signal(false);
  protected readonly tente = signal(false);

  protected confirmer(): void {
    if (this.donnees.avecMotDePasse && this.motDePasse().length === 0) {
      this.tente.set(true);
      return;
    }
    this.ref.close(this.donnees.avecMotDePasse ? this.motDePasse() : true);
  }
}

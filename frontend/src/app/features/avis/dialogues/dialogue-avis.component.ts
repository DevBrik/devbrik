import { ChangeDetectionStrategy, Component, ElementRef, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { map } from 'rxjs';
import { focaliserPremierInvalide, messageChamp, traiterEchec } from '../avis.erreurs';
import { Avis, Ecriture } from '../avis.modeles';
import { AvisService } from '../avis.service';
import { SocleAvis } from '../avis.socle';
import { STYLES_COMMUNS } from '../avis.styles';
import { ChampNoteLocalComponent } from '../champ-note-local/champ-note-local.component';

export interface DonneesDialogueAvis {
  idBrique: number;
  titreBrique?: string;
  /** Avis existant à modifier, ou null. */
  avis: Avis | null;
}

/** Donner votre avis (RG8) : le même dialogue crée ou met à jour. */
@Component({
  selector: 'avis-dialogue-avis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule, ChampNoteLocalComponent],
  template: `
    <h2 mat-dialog-title>Donner votre avis</h2>
    <form [formGroup]="formulaire" (ngSubmit)="envoyer()" novalidate>
      <mat-dialog-content>
        <p>Partagez votre expérience avec cette brique.</p>
        @if (donnees.titreBrique) {
          <p class="objet">Brique : <strong>{{ donnees.titreBrique }}</strong></p>
        }

        <div class="champ">
          <span class="etiquette" id="etiquette-note">
            Votre note <span class="obligatoire" aria-hidden="true">*</span>
          </span>
          <avis-champ-note
            formControlName="note"
            libelleId="etiquette-note"
            decritPar="erreur-note"
            [invalide]="noteInvalide()"
          />
          @if (noteInvalide()) {
            <p class="erreur" id="erreur-note" role="alert">Choisissez une note de 1 à 5.</p>
          }
        </div>

        <div class="champ">
          <label class="etiquette" for="avis-commentaire">Votre commentaire (facultatif)</label>
          <textarea
            id="avis-commentaire"
            formControlName="commentaire"
            rows="5"
            placeholder="Décrivez ce que vous avez pensé de cette brique…"
            aria-describedby="compteur-commentaire erreur-commentaire"
            [attr.aria-invalid]="erreurCommentaire() ? 'true' : null"
            [attr.data-invalide]="erreurCommentaire() ? 'true' : null"
          ></textarea>
          <p class="compteur" id="compteur-commentaire">{{ longueur() }} / 2 000</p>
          @if (erreurCommentaire(); as message) {
            <p class="erreur" id="erreur-commentaire" role="alert">{{ message }}</p>
          }
        </div>
      </mat-dialog-content>
      <mat-dialog-actions class="actions">
        <button type="submit" class="btn btn-primaire" [disabled]="envoi()">
          {{ envoi() ? 'Envoi en cours…' : 'Envoyer mon avis' }}
        </button>
        <button type="button" class="btn btn-destructif" mat-dialog-close>Annuler</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [STYLES_COMMUNS],
})
export class DialogueAvisComponent {
  private readonly service = inject(AvisService);
  private readonly socle = inject(SocleAvis);
  private readonly ref = inject<MatDialogRef<DialogueAvisComponent, Ecriture<Avis>>>(MatDialogRef);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);
  protected readonly donnees = inject<DonneesDialogueAvis>(MAT_DIALOG_DATA);

  protected readonly envoi = signal(false);

  protected readonly formulaire = new FormGroup({
    note: new FormControl<number | null>(this.donnees.avis?.note ?? null, [
      Validators.required,
      Validators.min(1),
      Validators.max(5),
    ]),
    commentaire: new FormControl(this.donnees.avis?.commentaire ?? '', {
      nonNullable: true,
      validators: [Validators.maxLength(2000)],
    }),
  });

  protected readonly longueur = toSignal(
    this.formulaire.controls.commentaire.valueChanges.pipe(map((v) => v.length)),
    { initialValue: (this.donnees.avis?.commentaire ?? '').length },
  );

  protected noteInvalide(): boolean {
    const c = this.formulaire.controls.note;
    return c.invalid && (c.touched || c.dirty);
  }

  protected erreurCommentaire(): string | null {
    return messageChamp(this.formulaire.controls.commentaire, { maxlength: 'Au plus 2 000 caractères.' });
  }

  protected envoyer(): void {
    if (this.envoi()) {
      return;
    }
    this.formulaire.markAllAsTouched();
    const { note, commentaire } = this.formulaire.getRawValue();
    if (this.formulaire.invalid || note === null) {
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    const texte = commentaire.trim();
    this.envoi.set(true);
    this.service
      .enregistrerAvis(this.donnees.idBrique, { note, ...(texte ? { commentaire: texte } : {}) })
      .subscribe({
        next: (resultat) => this.ref.close(resultat),
        error: (e: unknown) => {
          this.envoi.set(false);
          traiterEchec(e, {
            formulaire: this.formulaire,
            hote: this.hote.nativeElement,
            socle: this.socle,
            fermer: () => this.ref.close(),
          });
        },
      });
  }
}

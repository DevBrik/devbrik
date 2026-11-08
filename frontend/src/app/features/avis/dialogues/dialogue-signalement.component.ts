import { ChangeDetectionStrategy, Component, ElementRef, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { map } from 'rxjs';
import { focaliserPremierInvalide, messageChamp, traiterEchec } from '../avis.erreurs';
import { MOTIFS, MotifSignalement } from '../avis.modeles';
import { AvisService } from '../avis.service';
import { SocleAvis } from '../avis.socle';
import { STYLES_COMMUNS } from '../avis.styles';

export interface DonneesDialogueSignalement {
  idBrique: number;
  titreBrique?: string;
}

/** Signaler ce contenu (RG11) : six motifs en boutons radio, description facultative de 1 000 caractères. */
@Component({
  selector: 'avis-dialogue-signalement',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule],
  template: `
    <h2 mat-dialog-title>Signaler ce contenu</h2>
    <form [formGroup]="formulaire" (ngSubmit)="envoyer()" novalidate>
      <mat-dialog-content>
        <p>Pourquoi souhaitez-vous signaler ce contenu ?</p>
        @if (donnees.titreBrique) {
          <p class="objet">Brique : <strong>{{ donnees.titreBrique }}</strong></p>
        }

        <fieldset class="champ">
          <legend class="etiquette">
            Motif du signalement <span class="obligatoire" aria-hidden="true">*</span>
          </legend>
          @for (m of motifs; track m.valeur) {
            <label class="option">
              <input
                type="radio"
                formControlName="motif"
                [value]="m.valeur"
                [attr.aria-invalid]="motifInvalide() ? 'true' : null"
                [attr.data-invalide]="motifInvalide() ? 'true' : null"
                [attr.aria-describedby]="motifInvalide() ? 'erreur-motif' : null"
              />
              <span>{{ m.libelle }}</span>
            </label>
          }
          @if (motifInvalide()) {
            <p class="erreur" id="erreur-motif" role="alert">Choisissez un motif.</p>
          }
        </fieldset>

        <div class="champ">
          <label class="etiquette" for="signalement-details">Description complémentaire (facultatif)</label>
          <textarea
            id="signalement-details"
            formControlName="details"
            rows="4"
            placeholder="Donnez davantage de précisions (facultatif)…"
            aria-describedby="compteur-details erreur-details"
            [attr.aria-invalid]="erreurDetails() ? 'true' : null"
            [attr.data-invalide]="erreurDetails() ? 'true' : null"
          ></textarea>
          <p class="compteur" id="compteur-details">{{ longueur() }} / 1 000</p>
          @if (erreurDetails(); as message) {
            <p class="erreur" id="erreur-details" role="alert">{{ message }}</p>
          }
        </div>
      </mat-dialog-content>
      <mat-dialog-actions class="actions">
        <button type="submit" class="btn btn-primaire" [disabled]="envoi()">
          {{ envoi() ? 'Enregistrement en cours…' : 'Enregistrer' }}
        </button>
        <button type="button" class="btn btn-destructif" mat-dialog-close>Annuler</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [STYLES_COMMUNS],
})
export class DialogueSignalementComponent {
  private readonly service = inject(AvisService);
  private readonly socle = inject(SocleAvis);
  private readonly ref = inject<MatDialogRef<DialogueSignalementComponent, boolean>>(MatDialogRef);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);
  protected readonly donnees = inject<DonneesDialogueSignalement>(MAT_DIALOG_DATA);

  protected readonly motifs = MOTIFS;
  protected readonly envoi = signal(false);

  protected readonly formulaire = new FormGroup({
    motif: new FormControl<MotifSignalement | null>(null, [Validators.required]),
    details: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(1000)] }),
  });

  protected readonly longueur = toSignal(
    this.formulaire.controls.details.valueChanges.pipe(map((v) => v.length)),
    { initialValue: 0 },
  );

  protected motifInvalide(): boolean {
    const c = this.formulaire.controls.motif;
    return c.invalid && (c.touched || c.dirty);
  }

  protected erreurDetails(): string | null {
    return messageChamp(this.formulaire.controls.details, { maxlength: 'Au plus 1 000 caractères.' });
  }

  protected envoyer(): void {
    if (this.envoi()) {
      return;
    }
    this.formulaire.markAllAsTouched();
    const { motif, details } = this.formulaire.getRawValue();
    if (this.formulaire.invalid || motif === null) {
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    const texte = details.trim();
    this.envoi.set(true);
    this.service
      .signaler(this.donnees.idBrique, { motif, ...(texte ? { details: texte } : {}) })
      .subscribe({
        next: () => this.ref.close(true),
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

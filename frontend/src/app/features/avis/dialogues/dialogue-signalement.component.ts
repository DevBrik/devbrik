import { ChangeDetectionStrategy, Component, ElementRef, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { map } from 'rxjs';
import { focaliserPremierInvalide, messageChamp, traiterEchec } from '../avis.erreurs';
import { MOTIFS, MotifSignalement } from '../avis.modeles';
import { AvisService } from '../avis.service';
import { SocleAvis } from '../avis.socle';

export interface DonneesDialogueSignalement {
  idBrique: number;
  titreBrique?: string;
}

/** Signaler ce contenu (RG11) : six motifs en boutons radio, description facultative de 1 000 caractères. */
@Component({
  selector: 'avis-dialogue-signalement',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule],
  templateUrl: './dialogue-signalement.component.html',
  styleUrl: './dialogue-signalement.component.scss',
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

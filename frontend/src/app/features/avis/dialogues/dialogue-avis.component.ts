import { ChangeDetectionStrategy, Component, ElementRef, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { map } from 'rxjs';
import { focaliserPremierInvalide, messageChamp, traiterEchec } from '../avis.erreurs';
import { Avis, Ecriture } from '../avis.modeles';
import { AvisService } from '../avis.service';
import { SocleAvis } from '../avis.socle';
import { ChampNoteComponent } from '../../../shared/champ-note/champ-note.component';

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
  imports: [ReactiveFormsModule, MatDialogModule, ChampNoteComponent],
  templateUrl: './dialogue-avis.component.html',
  styleUrl: './dialogue-avis.component.scss',
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

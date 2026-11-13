import { ChangeDetectionStrategy, Component, ElementRef, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { map } from 'rxjs';
import { focaliserPremierInvalide, messageChamp, traiterEchec } from '../avis.erreurs';
import { Ecriture, ResultatTest, Test } from '../avis.modeles';
import { AvisService } from '../avis.service';
import { SocleAvis } from '../avis.socle';

export interface DonneesDialogueTest {
  idBrique: number;
  titreBrique?: string;
  /** Version actuelle du code (RG9). */
  versionCode: number;
  /** Test existant, ou null. */
  test: Test | null;
}

/** Enregistrer un test (RG9) : OK ou KO, environnement facultatif de 120 caractères. */
@Component({
  selector: 'avis-dialogue-test',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule],
  templateUrl: './dialogue-test.component.html',
  styleUrl: './dialogue-test.component.scss',
})
export class DialogueTestComponent {
  private readonly service = inject(AvisService);
  private readonly socle = inject(SocleAvis);
  private readonly ref = inject<MatDialogRef<DialogueTestComponent, Ecriture<Test>>>(MatDialogRef);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);
  protected readonly donnees = inject<DonneesDialogueTest>(MAT_DIALOG_DATA);

  protected readonly resultats: readonly { valeur: ResultatTest; libelle: string; icone: string }[] = [
    { valeur: 'OK', libelle: 'Testé OK', icone: '✓' },
    { valeur: 'KO', libelle: 'Testé KO', icone: '!' },
  ];
  protected readonly envoi = signal(false);

  protected readonly formulaire = new FormGroup({
    resultat: new FormControl<ResultatTest | null>(this.donnees.test?.resultat ?? null, [Validators.required]),
    environnement: new FormControl(this.donnees.test?.environnement ?? '', {
      nonNullable: true,
      validators: [Validators.maxLength(120)],
    }),
  });

  protected readonly longueur = toSignal(
    this.formulaire.controls.environnement.valueChanges.pipe(map((v) => v.length)),
    { initialValue: (this.donnees.test?.environnement ?? '').length },
  );

  protected resultatInvalide(): boolean {
    const c = this.formulaire.controls.resultat;
    return c.invalid && (c.touched || c.dirty);
  }

  protected erreurEnvironnement(): string | null {
    return messageChamp(this.formulaire.controls.environnement, { maxlength: 'Au plus 120 caractères.' });
  }

  protected envoyer(): void {
    if (this.envoi()) {
      return;
    }
    this.formulaire.markAllAsTouched();
    const { resultat, environnement } = this.formulaire.getRawValue();
    if (this.formulaire.invalid || resultat === null) {
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    const texte = environnement.trim();
    this.envoi.set(true);
    this.service
      .enregistrerTest(this.donnees.idBrique, { resultat, ...(texte ? { environnement: texte } : {}) })
      .subscribe({
        next: (retour) => this.ref.close(retour),
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

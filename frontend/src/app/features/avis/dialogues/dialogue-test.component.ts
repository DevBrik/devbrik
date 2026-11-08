import { ChangeDetectionStrategy, Component, ElementRef, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { map } from 'rxjs';
import { focaliserPremierInvalide, messageChamp, traiterEchec } from '../avis.erreurs';
import { Ecriture, ResultatTest, Test } from '../avis.modeles';
import { AvisService } from '../avis.service';
import { SocleAvis } from '../avis.socle';
import { STYLES_COMMUNS } from '../avis.styles';

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
  template: `
    <h2 mat-dialog-title>Enregistrer un test</h2>
    <form [formGroup]="formulaire" (ngSubmit)="envoyer()" novalidate>
      <mat-dialog-content>
        <p>Indiquez le résultat de votre test sur la version actuelle du code.</p>
        @if (donnees.titreBrique) {
          <p class="objet">Brique : <strong>{{ donnees.titreBrique }}</strong></p>
        }
        <p class="aide">Votre test sera enregistré pour la version {{ donnees.versionCode }} du code.</p>
        @if (donnees.test && donnees.test.versionCode !== donnees.versionCode) {
          <p class="info" role="note">
            Votre test actuel porte sur la version {{ donnees.test.versionCode }}, qui n’est plus la version
            actuelle. Il sera remplacé par ce nouveau test.
          </p>
        }

        <fieldset class="champ">
          <legend class="etiquette">
            Résultat du test <span class="obligatoire" aria-hidden="true">*</span>
          </legend>
          @for (r of resultats; track r.valeur) {
            <label class="option">
              <input
                type="radio"
                formControlName="resultat"
                [value]="r.valeur"
                [attr.aria-invalid]="resultatInvalide() ? 'true' : null"
                [attr.data-invalide]="resultatInvalide() ? 'true' : null"
                [attr.aria-describedby]="resultatInvalide() ? 'erreur-resultat' : null"
              />
              <span class="pastille" [class.ok]="r.valeur === 'OK'" [class.ko]="r.valeur === 'KO'">
                <span aria-hidden="true">{{ r.icone }}</span> {{ r.libelle }}
              </span>
            </label>
          }
          @if (resultatInvalide()) {
            <p class="erreur" id="erreur-resultat" role="alert">Indiquez si la brique fonctionne ou non.</p>
          }
        </fieldset>

        <div class="champ">
          <label class="etiquette" for="test-environnement">Environnement (facultatif)</label>
          <input
            id="test-environnement"
            type="text"
            formControlName="environnement"
            autocomplete="off"
            placeholder="Exemple : Java 21, Windows 11"
            aria-describedby="compteur-environnement erreur-environnement"
            [attr.aria-invalid]="erreurEnvironnement() ? 'true' : null"
            [attr.data-invalide]="erreurEnvironnement() ? 'true' : null"
          />
          <p class="compteur" id="compteur-environnement">{{ longueur() }} / 120</p>
          @if (erreurEnvironnement(); as message) {
            <p class="erreur" id="erreur-environnement" role="alert">{{ message }}</p>
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

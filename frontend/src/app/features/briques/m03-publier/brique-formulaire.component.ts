import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  FormBuilder, FormGroup, ReactiveFormsModule,
  Validators, AbstractControl,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { BriqueService } from '../services/brique.service';
import { CategorieDto, LangageDto, LICENCES_PERMISSIVES } from '../models/brique.model';
import { EtatPage } from '../../../shared/etat-page/etat-page';
import { messageErreur, codeErreur } from '../../../core/api/erreur';

/**
 * M03 — Formulaire de publication et modification d'une brique.
 * RG4 : droitsConfirmes obligatoire.
 * RG18 : code en texte brut (textarea standard).
 */
@Component({
  selector: 'app-brique-formulaire',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule, MatCardModule, MatCheckboxModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatProgressSpinnerModule,
    MatSelectModule, MatSnackBarModule,
    EtatPage,
  ],
  templateUrl: './brique-formulaire.component.html',
  styleUrl: './brique-formulaire.component.scss',
})
export class BriqueFormulaireComponent implements OnInit {

  private readonly route         = inject(ActivatedRoute);
  private readonly router        = inject(Router);
  private readonly fb            = inject(FormBuilder);
  private readonly briqueService = inject(BriqueService);
  private readonly snack         = inject(MatSnackBar);

  readonly chargement = signal(false);
  readonly soumission = signal(false);
  readonly mode       = signal<'creation' | 'modification'>('creation');

  categories: CategorieDto[] = [];
  langages:   LangageDto[]   = [];
  readonly licences = LICENCES_PERMISSIVES;

  private briqueId: number | null = null;

  form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      titre:           ['', [Validators.required, Validators.maxLength(120)]],
      resume:          ['', [Validators.required, Validators.maxLength(300)]],
      code:            ['', [Validators.required, Validators.maxLength(30000)]],
      modeEmploi:      ['', [Validators.required, Validators.minLength(10), Validators.maxLength(10000)]],
      prerequis:       ['', Validators.maxLength(500)],
      licence:         [null, Validators.required],
      idCategorie:     [null, Validators.required],
      idLangage:       [null, Validators.required],
      droitsConfirmes: [false],
    });

    this.briqueService.categories().subscribe(cats => this.categories = cats);
    this.briqueService.langages().subscribe(langs => this.langages = langs);

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.briqueId = Number(idParam);
      this.mode.set('modification');
      this.chargerBrique(this.briqueId);
      // En mode modification : droitsConfirmes n'est pas requis
      this.form.controls['droitsConfirmes'].clearValidators();
    } else {
      this.form.controls['droitsConfirmes'].setValidators(Validators.requiredTrue);
    }
    this.form.controls['droitsConfirmes'].updateValueAndValidity();
  }

  private chargerBrique(id: number): void {
    this.chargement.set(true);
    this.briqueService.obtenir(id).subscribe({
      next: b => {
        this.form.patchValue({
          titre: b.titre, resume: b.resume, code: b.code,
          modeEmploi: b.modeEmploi, prerequis: b.prerequis ?? '',
          licence: b.licence,
          idCategorie: b.categorie.id,
          idLangage: b.langage.id,
        });
        this.chargement.set(false);
      },
      error: () => {
        this.snack.open('Impossible de charger la brique.', 'Fermer', { duration: 4000 });
        this.chargement.set(false);
      },
    });
  }

  soumettre(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.soumission.set(true);

    const v = this.form.value;

    const obs = this.mode() === 'creation'
      ? this.briqueService.publier({ ...v, prerequis: v.prerequis || null })
      : this.briqueService.modifier(this.briqueId!, {
          titre: v.titre, resume: v.resume, code: v.code,
          modeEmploi: v.modeEmploi, prerequis: v.prerequis || null,
          licence: v.licence, idCategorie: v.idCategorie, idLangage: v.idLangage,
        });

    obs.subscribe({
      next: b => {
        this.soumission.set(false);
        const msg = this.mode() === 'creation' ? 'Brique publiée !' : 'Brique modifiée !';
        this.snack.open(msg, 'Fermer', { duration: 3000 });
        this.router.navigate(['/briques', b.id]);
      },
      error: err => {
        this.soumission.set(false);
        const code = codeErreur(err);
        const msg = code === 'TITRE_DEJA_UTILISE'
          ? 'Vous avez déjà une brique avec ce titre.'
          : messageErreur(err);
        this.snack.open(msg, 'Fermer', { duration: 5000 });
      },
    });
  }

  annuler(): void {
    if (this.briqueId) {
      this.router.navigate(['/briques', this.briqueId]);
    } else {
      this.router.navigate(['/briques']);
    }
  }

  longueur(ctrl: AbstractControl): number {
    return (ctrl.value as string)?.length ?? 0;
  }
}

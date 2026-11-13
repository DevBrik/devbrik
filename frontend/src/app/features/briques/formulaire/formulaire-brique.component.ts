import { Location } from '@angular/common';
import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { startWith } from 'rxjs';
import {
  appliquerDetails,
  erreurSurChamp,
  focaliserPremierInvalide,
  lireErreur,
  longueurRognee,
  messageChamp,
} from '../../../core/api/erreurs-formulaire';
import { BriqueDetail, CorpsBrique, Licence } from '../../../core/api/modeles';
import { ReferentielService } from '../../../core/api/referentiel.service';
import { AuthService } from '../../../core/auth/auth.service';
import { LICENCES, lignes } from '../../../core/libelles';
import { ChampCodeComponent } from '../../../shared/champ-code/champ-code.component';
import { ChampLignesComponent } from '../../../shared/champ-lignes/champ-lignes.component';
import { EtatPageComponent, TypeEtat } from '../../../shared/etat-page/etat-page.component';
import { NotificationService } from '../../../shared/notification/notification.service';
import { BriquesService } from '../briques.service';

/** Longueur du texte envoyé (lignes séparées par \n) comprise entre min et max. */
function longueurLignes(min: number, max: number) {
  return (c: AbstractControl): Record<string, true> | null => {
    const texte = ((c.value as string[] | null) ?? []).join('\n');
    if (min > 0 && texte.length === 0) {
      return { required: true };
    }
    if (texte.length > 0 && texte.length < min) {
      return { minlength: true };
    }
    return texte.length > max ? { maxlength: true } : null;
  };
}

const MESSAGES: Record<string, Record<string, string>> = {
  titre: { required: 'Le titre est obligatoire.', minlength: 'Le titre doit contenir au moins 5 caractères.', maxlength: 'Le titre doit contenir au plus 120 caractères.' },
  resume: { required: 'La description est obligatoire.', minlength: 'La description doit contenir au moins 10 caractères.', maxlength: 'La description doit contenir au plus 300 caractères.' },
  idCategorie: { required: 'Choisissez une catégorie.' },
  idLangage: { required: 'Choisissez un langage.' },
  code: { required: 'Le code source est obligatoire.', maxlength: 'Le code doit contenir au plus 30 000 caractères.' },
  modeEmploi: { required: "Ajoutez au moins une étape au mode d'emploi.", minlength: "Le mode d'emploi doit contenir au moins 10 caractères.", maxlength: "Le mode d'emploi doit contenir au plus 10 000 caractères." },
  prerequis: { maxlength: 'Les prérequis doivent contenir au plus 500 caractères.' },
  licence: { required: 'Choisissez une licence.' },
  droitsConfirmes: { required: 'Vous devez confirmer détenir les droits nécessaires pour publier ce code.' },
};

/** M03 : publier (/briques/nouvelle) ou modifier (/briques/:id/modifier) une brique. */
@Component({
  selector: 'db-formulaire-brique',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, ChampCodeComponent, ChampLignesComponent, EtatPageComponent],
  templateUrl: './formulaire-brique.component.html',
  styleUrl: './formulaire-brique.component.scss',
})
export class FormulaireBriqueComponent {
  private readonly service = inject(BriquesService);
  private readonly referentiels = inject(ReferentielService);
  private readonly auth = inject(AuthService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);

  /** Présent en modification (paramètre de route). */
  readonly id = input<string>();

  protected readonly modification = computed(() => this.id() !== undefined);
  protected readonly categories = toSignal(this.referentiels.categories(), { initialValue: [] });
  protected readonly langages = toSignal(this.referentiels.langages(), { initialValue: [] });
  protected readonly licences = LICENCES;
  protected readonly etat = signal<TypeEtat | null>(null);
  protected readonly envoi = signal(false);
  protected readonly versionActuelle = signal<number | null>(null);

  protected readonly formulaire = new FormGroup({
    titre: new FormControl('', { nonNullable: true, validators: [longueurRognee(5, 120)] }),
    resume: new FormControl('', { nonNullable: true, validators: [longueurRognee(10, 300)] }),
    idCategorie: new FormControl<number | null>(null, Validators.required),
    idLangage: new FormControl<number | null>(null, Validators.required),
    code: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(30000)] }),
    modeEmploi: new FormControl<string[]>([], { nonNullable: true, validators: [longueurLignes(10, 10000)] }),
    prerequis: new FormControl<string[]>([], { nonNullable: true, validators: [longueurLignes(0, 500)] }),
    licence: new FormControl<Licence | null>('MIT', Validators.required),
    droitsConfirmes: new FormControl(false, { nonNullable: true, validators: [Validators.requiredTrue] }),
  });

  private readonly valeurs = toSignal(this.formulaire.valueChanges.pipe(startWith(null)));
  protected readonly longueurs = computed(() => {
    this.valeurs();
    const v = this.formulaire.getRawValue();
    return {
      titre: v.titre.length,
      resume: v.resume.length,
      modeEmploi: v.modeEmploi.join('\n').length,
      prerequis: v.prerequis.join('\n').length,
    };
  });

  constructor() {
    effect(() => {
      const id = this.id();
      if (id !== undefined) {
        untracked(() => this.chargerBrique(Number(id)));
      }
    });
  }

  private chargerBrique(id: number): void {
    if (!Number.isInteger(id) || id <= 0) {
      this.etat.set('404');
      return;
    }
    this.etat.set('chargement');
    this.service.detail(id).subscribe({
      next: (b) => {
        if (b.auteur?.id !== this.auth.utilisateur()?.id) {
          this.etat.set('403'); // RG19 : seul l'auteur modifie
          return;
        }
        this.remplir(b);
        this.etat.set(null);
      },
      error: (e: unknown) => {
        const s = lireErreur(e).statut;
        this.etat.set(s === 404 ? '404' : s === 403 ? '403' : 'erreur');
      },
    });
  }

  private remplir(b: BriqueDetail): void {
    this.versionActuelle.set(b.versionCode);
    this.formulaire.setValue({
      titre: b.titre,
      resume: b.resume,
      idCategorie: b.idCategorie,
      idLangage: b.idLangage,
      code: b.code,
      modeEmploi: lignes(b.modeEmploi),
      prerequis: lignes(b.prerequis),
      licence: b.licence,
      droitsConfirmes: false,
    });
  }

  protected recharger(): void {
    this.chargerBrique(Number(this.id()));
  }

  protected erreur(champ: string): string | null {
    return messageChamp(this.formulaire.get(champ), MESSAGES[champ] ?? {});
  }

  protected envoyer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    const v = this.formulaire.getRawValue();
    const prerequis = v.prerequis.join('\n');
    const corps: CorpsBrique = {
      titre: v.titre.trim(),
      resume: v.resume.trim(),
      code: v.code,
      modeEmploi: v.modeEmploi.join('\n'),
      prerequis: prerequis.length > 0 ? prerequis : null,
      licence: v.licence as Licence,
      idCategorie: v.idCategorie as number,
      idLangage: v.idLangage as number,
      droitsConfirmes: v.droitsConfirmes,
    };
    this.envoi.set(true);
    const appel = this.modification() ? this.service.modifier(Number(this.id()), corps) : this.service.creer(corps);
    appel.subscribe({
      next: (b) => {
        this.notifications.succes(this.modification() ? 'Modifications enregistrées' : 'Brique publiée');
        void this.router.navigate(['/briques', b.id]);
      },
      error: (e: unknown) => {
        this.envoi.set(false);
        this.traiterErreur(e);
      },
    });
  }

  private traiterErreur(e: unknown): void {
    const err = lireErreur(e);
    switch (err.code) {
      case 'TITRE_DEJA_UTILISE':
        erreurSurChamp(this.formulaire, 'titre', err.message);
        break;
      case 'DROITS_NON_CONFIRMES':
        erreurSurChamp(this.formulaire, 'droitsConfirmes', err.message);
        break;
      case 'REQUETE_INVALIDE':
        if (!appliquerDetails(err, this.formulaire)) {
          this.notifications.erreur(err.message);
        }
        break;
      case 'NON_AUTHENTIFIE':
        return; // l'intercepteur renvoie vers la connexion
      default:
        this.notifications.erreur(err.message);
        return;
    }
    focaliserPremierInvalide(this.hote.nativeElement);
  }

  protected annuler(): void {
    if (window.history.length > 1) {
      this.location.back();
    } else {
      void this.router.navigateByUrl('/briques');
    }
  }
}

import { Location } from '@angular/common';
import { ChangeDetectionStrategy, Component, ElementRef, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { startWith } from 'rxjs';
import {
  appliquerDetails,
  focaliserPremierInvalide,
  lireErreur,
  longueurRognee,
  messageChamp,
} from '../../../core/api/erreurs-formulaire';
import { ReferentielService } from '../../../core/api/referentiel.service';
import { NotificationService } from '../../../shared/notification/notification.service';
import { DemandesService } from '../demandes.service';

const MESSAGES: Record<string, Record<string, string>> = {
  titre: { required: 'Le titre est obligatoire.', minlength: 'Le titre doit contenir au moins 10 caractères.', maxlength: 'Le titre doit contenir au plus 150 caractères.' },
  description: { required: 'La description est obligatoire.', minlength: 'La description doit contenir au moins 20 caractères.', maxlength: 'La description doit contenir au plus 5 000 caractères.' },
  idCategorie: { required: 'Choisissez une catégorie.' },
  idLangage: { required: 'Choisissez un langage.' },
};

/** M08 : nouvelle demande (/demandes/nouvelle). Catégorie et langage obligatoires dans l'interface. */
@Component({
  selector: 'db-nouvelle-demande',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './nouvelle-demande.component.html',
  styleUrl: './nouvelle-demande.component.scss',
})
export class NouvelleDemandeComponent {
  private readonly service = inject(DemandesService);
  private readonly referentiels = inject(ReferentielService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly categories = toSignal(this.referentiels.categories(), { initialValue: [] });
  protected readonly langages = toSignal(this.referentiels.langages(), { initialValue: [] });
  protected readonly envoi = signal(false);

  protected readonly formulaire = new FormGroup({
    titre: new FormControl('', { nonNullable: true, validators: [longueurRognee(10, 150)] }),
    description: new FormControl('', { nonNullable: true, validators: [longueurRognee(20, 5000)] }),
    idCategorie: new FormControl<number | null>(null, Validators.required),
    idLangage: new FormControl<number | null>(null, Validators.required),
  });
  private readonly valeurs = toSignal(this.formulaire.valueChanges.pipe(startWith(null)));
  protected readonly longueurs = computed(() => {
    this.valeurs();
    const v = this.formulaire.getRawValue();
    return { titre: v.titre.length, description: v.description.length };
  });

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
    this.envoi.set(true);
    const enCours = this.notifications.chargement('Publication en cours…');
    this.service
      .creer({ titre: v.titre.trim(), description: v.description.trim(), idCategorie: v.idCategorie, idLangage: v.idLangage })
      .subscribe({
        next: (d) => {
          enCours.fermer();
          this.notifications.succes('Demande publiée');
          void this.router.navigate(['/demandes', d.id]);
        },
        error: (e: unknown) => {
          enCours.fermer();
          this.envoi.set(false);
          const err = lireErreur(e);
          if (err.code === 'REQUETE_INVALIDE' && appliquerDetails(err, this.formulaire)) {
            focaliserPremierInvalide(this.hote.nativeElement);
          } else if (err.code !== 'NON_AUTHENTIFIE') {
            this.notifications.erreur(err.message);
          }
        },
      });
  }

  protected annuler(): void {
    if (window.history.length > 1) {
      this.location.back();
    } else {
      void this.router.navigateByUrl('/demandes');
    }
  }
}

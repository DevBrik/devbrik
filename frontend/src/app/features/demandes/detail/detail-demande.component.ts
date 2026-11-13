import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { Title } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { Subject, Subscription, debounceTime, distinctUntilChanged, of, startWith, switchMap, catchError, map } from 'rxjs';
import {
  appliquerDetails,
  erreurSurChamp,
  focaliserPremierInvalide,
  lireErreur,
  longueurRognee,
  messageChamp,
} from '../../../core/api/erreurs-formulaire';
import { BriqueResume, DemandeDetail, Reponse } from '../../../core/api/modeles';
import { AuthService } from '../../../core/auth/auth.service';
import { nomAuteur } from '../../../core/libelles';
import { DialogueService } from '../../../shared/dialogue-confirmation/dialogue.service';
import { EtatPageComponent, TypeEtat } from '../../../shared/etat-page/etat-page.component';
import { NotificationService } from '../../../shared/notification/notification.service';
import { PastilleComponent } from '../../../shared/pastille/pastille.component';
import { BriquesService } from '../../briques/briques.service';
import { DemandesService } from '../demandes.service';

/** M07 : détail d'une demande (/demandes/:id), réponses, acceptation (RG12), formulaire de réponse (RG13). */
@Component({
  selector: 'db-detail-demande',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink, ReactiveFormsModule, MatAutocompleteModule, EtatPageComponent, PastilleComponent],
  templateUrl: './detail-demande.component.html',
  styleUrl: './detail-demande.component.scss',
})
export class DetailDemandeComponent {
  private readonly service = inject(DemandesService);
  private readonly briques = inject(BriquesService);
  private readonly auth = inject(AuthService);
  private readonly dialogues = inject(DialogueService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly titre = inject(Title);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly id = input.required<string>();

  protected readonly demande = signal<DemandeDetail | null>(null);
  protected readonly etat = signal<TypeEtat | null>('chargement');
  protected readonly enCours = signal<number | 'demande' | null>(null);
  protected readonly envoi = signal(false);
  protected readonly connecte = this.auth.estConnecte;
  protected readonly estAdmin = this.auth.estAdmin;
  protected readonly nomAuteur = nomAuteur;
  private appel?: Subscription;

  protected readonly estAuteurDemande = computed(() => {
    const u = this.auth.utilisateur();
    return !!u && this.demande()?.auteur?.id === u.id;
  });
  protected readonly reponseAcceptee = computed(() => this.demande()?.reponses.some((r) => r.acceptee) ?? false);

  // ----- formulaire de réponse -----
  protected readonly formulaire = new FormGroup({
    contenu: new FormControl('', { nonNullable: true, validators: [longueurRognee(5, 5000)] }),
    idBrique: new FormControl<number | null>(null),
  });
  /** Texte saisi dans « Lier une brique » ; la brique choisie est dans idBrique. */
  protected readonly rechercheBrique = new FormControl<string | BriqueResume>('', { nonNullable: true });
  protected readonly briqueChoisie = signal<BriqueResume | null>(null);
  private readonly longueurContenu = toSignal(this.formulaire.controls.contenu.valueChanges.pipe(startWith('')), {
    initialValue: '',
  });
  protected readonly compteur = computed(() => this.longueurContenu().length);
  protected readonly suggestions = toSignal(
    this.rechercheBrique.valueChanges.pipe(
      map((v) => (typeof v === 'string' ? v.trim() : '')),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((q) =>
        q.length >= 2 && q.length <= 100
          ? this.briques.lister({ q, taille: 8 }).pipe(
              map((p) => p.contenu),
              catchError(() => of([] as BriqueResume[])),
            )
          : of([] as BriqueResume[]),
      ),
      takeUntilDestroyed(),
    ),
    { initialValue: [] as BriqueResume[] },
  );
  protected readonly afficherBrique = (b: BriqueResume | string | null): string =>
    typeof b === 'string' ? b : (b?.titre ?? '');

  constructor() {
    effect(() => {
      const id = Number(this.id());
      untracked(() => this.charger(id, true));
    });
    // Si l'utilisateur modifie le texte après un choix, le lien est retiré.
    this.rechercheBrique.valueChanges.pipe(takeUntilDestroyed()).subscribe((v) => {
      if (typeof v === 'string' && this.briqueChoisie() && v !== this.briqueChoisie()?.titre) {
        this.briqueChoisie.set(null);
        this.formulaire.controls.idBrique.setValue(null);
      }
    });
  }

  protected recharger(): void {
    this.charger(Number(this.id()), false);
  }

  private charger(id: number, afficherChargement: boolean): void {
    if (!Number.isInteger(id) || id <= 0) {
      this.etat.set('404');
      return;
    }
    this.appel?.unsubscribe();
    if (afficherChargement) {
      this.etat.set('chargement');
    }
    this.appel = this.service.detail(id).subscribe({
      next: (d) => {
        this.demande.set(d);
        this.etat.set(null);
        this.titre.setTitle(`${d.titre} – DevBrik`);
      },
      error: (e: unknown) => {
        const s = lireErreur(e).statut;
        this.etat.set(s === 404 || s === 400 ? '404' : 'erreur');
      },
    });
  }

  protected estMienne(r: Reponse): boolean {
    const u = this.auth.utilisateur();
    return !!u && r.auteur?.id === u.id;
  }

  protected peutAccepter(r: Reponse): boolean {
    return this.estAuteurDemande() && !this.reponseAcceptee() && !this.estMienne(r);
  }

  protected accepter(r: Reponse): void {
    this.enCours.set(r.id);
    this.service.accepter(r.id).subscribe({
      next: (d) => {
        this.enCours.set(null);
        this.demande.set(d);
        this.notifications.succes('Réponse acceptée : la demande est résolue.');
      },
      error: (e: unknown) => this.echec(e),
    });
  }

  protected retirer(r: Reponse): void {
    this.enCours.set(r.id);
    this.service.retirerAcceptation(r.id).subscribe({
      next: (d) => {
        this.enCours.set(null);
        this.demande.set(d);
        this.notifications.succes('Acceptation retirée : la demande est rouverte.');
      },
      error: (e: unknown) => this.echec(e),
    });
  }

  protected supprimerReponse(r: Reponse): void {
    this.dialogues
      .confirmer({
        titre: 'Supprimer cette réponse ?',
        message: this.estMienne(r)
          ? 'Votre réponse sera supprimée. Cette action est irréversible.'
          : `La réponse de ${nomAuteur(r.auteur)} sera supprimée. Cette action est irréversible.`,
        libelleConfirmer: 'Supprimer',
      })
      .subscribe((ok) => {
        if (!ok) {
          return;
        }
        this.enCours.set(r.id);
        this.service.supprimerReponse(r.id).subscribe({
          next: () => {
            this.enCours.set(null);
            this.notifications.succes('Réponse supprimée');
            this.recharger();
          },
          error: (e: unknown) => this.echec(e),
        });
      });
  }

  protected supprimerDemande(): void {
    const d = this.demande();
    if (!d) {
      return;
    }
    this.dialogues
      .confirmer({
        titre: 'Supprimer cette demande ?',
        message: `« ${d.titre} » et toutes ses réponses seront supprimées. Cette action est irréversible.`,
        libelleConfirmer: 'Supprimer',
      })
      .subscribe((ok) => {
        if (!ok) {
          return;
        }
        this.enCours.set('demande');
        this.service.supprimer(d.id).subscribe({
          next: () => {
            this.notifications.succes('Demande supprimée');
            void this.router.navigateByUrl('/demandes');
          },
          error: (e: unknown) => this.echec(e),
        });
      });
  }

  private echec(e: unknown): void {
    this.enCours.set(null);
    const err = lireErreur(e);
    if (err.code !== 'NON_AUTHENTIFIE') {
      this.notifications.erreur(err.message);
    }
    if (err.code === 'REPONSE_DEJA_ACCEPTEE' || err.statut === 404) {
      this.recharger();
    }
  }

  // ----- réponse -----
  protected choisirBrique(e: MatAutocompleteSelectedEvent): void {
    const b = e.option.value as BriqueResume;
    this.briqueChoisie.set(b);
    this.formulaire.controls.idBrique.setValue(b.id);
    this.formulaire.controls.idBrique.setErrors(null);
  }

  protected retirerBrique(): void {
    this.briqueChoisie.set(null);
    this.formulaire.controls.idBrique.setValue(null);
    this.rechercheBrique.setValue('');
  }

  protected erreurContenu(): string | null {
    return messageChamp(this.formulaire.controls.contenu, {
      required: 'La réponse est obligatoire.',
      minlength: 'La réponse doit contenir au moins 5 caractères.',
      maxlength: 'La réponse doit contenir au plus 5 000 caractères.',
    });
  }

  protected erreurBrique(): string | null {
    const c = this.formulaire.controls.idBrique;
    return typeof c.errors?.['serveur'] === 'string' ? c.errors['serveur'] : null;
  }

  protected versConnexion(): void {
    void this.router.navigate(['/compte/connexion'], { queryParams: { retour: this.router.url } });
  }

  protected repondre(): void {
    const d = this.demande();
    if (!d) {
      return;
    }
    if (this.formulaire.controls.contenu.invalid) {
      this.formulaire.markAllAsTouched();
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    this.envoi.set(true);
    this.service
      .repondre(d.id, {
        contenu: this.formulaire.controls.contenu.value.trim(),
        idBrique: this.formulaire.controls.idBrique.value,
      })
      .subscribe({
        next: () => {
          this.envoi.set(false);
          this.formulaire.reset({ contenu: '', idBrique: null });
          this.retirerBrique();
          this.notifications.succes('Réponse publiée');
          this.recharger();
        },
        error: (e: unknown) => {
          this.envoi.set(false);
          const err = lireErreur(e);
          if (err.code === 'BRIQUE_LIEE_INVALIDE') {
            erreurSurChamp(this.formulaire, 'idBrique', err.message);
          } else if (err.code === 'REQUETE_INVALIDE' && appliquerDetails(err, this.formulaire)) {
            focaliserPremierInvalide(this.hote.nativeElement);
          } else if (err.code !== 'NON_AUTHENTIFIE') {
            this.notifications.erreur(err.message);
          }
        },
      });
  }
}

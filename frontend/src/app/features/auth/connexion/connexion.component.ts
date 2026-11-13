import { ChangeDetectionStrategy, Component, ElementRef, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { focaliserPremierInvalide, lireErreur, messageChamp } from '../../../core/api/erreurs-formulaire';
import { AuthService } from '../../../core/auth/auth.service';
import { ChampMotDePasseComponent } from '../../../shared/champ-mot-de-passe/champ-mot-de-passe.component';
import { LogoComponent } from '../../../shared/logo/logo.component';
import { NotificationService } from '../../../shared/notification/notification.service';
import { PanneauAuthComponent } from '../panneau-auth/panneau-auth.component';
import { retourSur } from '../regles-compte';

/** M04 : connexion (/compte/connexion). Retour vers ?retour= après succès. */
@Component({
  selector: 'db-connexion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, LogoComponent, ChampMotDePasseComponent, PanneauAuthComponent],
  templateUrl: './connexion.component.html',
  styleUrl: './connexion.component.scss',
})
export class ConnexionComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notifications = inject(NotificationService);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly envoi = signal(false);
  protected readonly erreurGlobale = signal<string | null>(null);
  protected readonly formulaire = new FormGroup({
    pseudo: new FormControl('', { nonNullable: true, validators: [(c) => (String(c.value).trim() ? null : { required: true })] }),
    motDePasse: new FormControl('', { nonNullable: true, validators: [(c) => (c.value ? null : { required: true })] }),
  });

  protected get retour(): string | null {
    return this.route.snapshot.queryParamMap.get('retour');
  }

  protected erreur(champ: 'pseudo' | 'motDePasse'): string | null {
    return messageChamp(this.formulaire.get(champ), {
      required: champ === 'pseudo' ? 'Saisissez votre pseudo.' : 'Saisissez votre mot de passe.',
    });
  }

  protected envoyer(): void {
    this.erreurGlobale.set(null);
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    const { pseudo, motDePasse } = this.formulaire.getRawValue();
    this.envoi.set(true);
    this.auth.connexion(pseudo.trim(), motDePasse).subscribe({
      next: (r) => {
        this.notifications.succes(`Bienvenue, ${r.utilisateur.nomAffiche?.trim() || r.utilisateur.pseudo} !`);
        void this.router.navigateByUrl(retourSur(this.retour));
      },
      error: (e: unknown) => {
        this.envoi.set(false);
        const err = lireErreur(e);
        // IDENTIFIANTS_INVALIDES : sans dire si le pseudo existe (11.3).
        this.erreurGlobale.set(err.code === 'IDENTIFIANTS_INVALIDES' ? 'Pseudo ou mot de passe incorrect.' : err.message);
        setTimeout(() => this.hote.nativeElement.querySelector<HTMLElement>('#erreur-connexion')?.focus());
      },
    });
  }
}

import { ChangeDetectionStrategy, Component, ElementRef, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  appliquerDetails,
  erreurSurChamp,
  focaliserPremierInvalide,
  lireErreur,
  messageChamp,
} from '../../../core/api/erreurs-formulaire';
import { AuthService } from '../../../core/auth/auth.service';
import { ChampMotDePasseComponent } from '../../../shared/champ-mot-de-passe/champ-mot-de-passe.component';
import { LogoComponent } from '../../../shared/logo/logo.component';
import { NotificationService } from '../../../shared/notification/notification.service';
import { PanneauAuthComponent } from '../panneau-auth/panneau-auth.component';
import {
  MESSAGES_MDP,
  MESSAGES_PSEUDO,
  confirmationIdentique,
  retourSur,
  validerMotDePasse,
  validerPseudo,
} from '../regles-compte';

/** M04 : inscription (/compte/inscription). Règles RG1 et RG2 visibles sous les champs. */
@Component({
  selector: 'db-inscription',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, LogoComponent, ChampMotDePasseComponent, PanneauAuthComponent],
  templateUrl: './inscription.component.html',
  styleUrl: './inscription.component.scss',
})
export class InscriptionComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notifications = inject(NotificationService);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly envoi = signal(false);
  protected readonly formulaire = new FormGroup(
    {
      pseudo: new FormControl('', { nonNullable: true, validators: [validerPseudo] }),
      motDePasse: new FormControl('', { nonNullable: true, validators: [validerMotDePasse] }),
      confirmation: new FormControl('', { nonNullable: true, validators: [(c) => (c.value ? null : { required: true })] }),
    },
    { validators: [confirmationIdentique('motDePasse', 'confirmation')] },
  );

  protected get retour(): string | null {
    return this.route.snapshot.queryParamMap.get('retour');
  }

  protected erreur(champ: 'pseudo' | 'motDePasse' | 'confirmation'): string | null {
    const messages: Record<string, Record<string, string>> = {
      pseudo: MESSAGES_PSEUDO,
      motDePasse: MESSAGES_MDP,
      confirmation: { required: 'Confirmez le mot de passe.', differente: 'Les deux mots de passe ne sont pas identiques.' },
    };
    return messageChamp(this.formulaire.get(champ), messages[champ]);
  }

  protected envoyer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    const { pseudo, motDePasse } = this.formulaire.getRawValue();
    this.envoi.set(true);
    this.auth.inscription(pseudo.trim(), motDePasse).subscribe({
      next: () => {
        this.notifications.succes('Bienvenue sur DevBrik ! Votre compte est créé.');
        void this.router.navigateByUrl(retourSur(this.retour));
      },
      error: (e: unknown) => {
        this.envoi.set(false);
        const err = lireErreur(e);
        if (err.code === 'PSEUDO_DEJA_UTILISE' || err.code === 'PSEUDO_RESERVE') {
          erreurSurChamp(this.formulaire, 'pseudo', err.message);
          focaliserPremierInvalide(this.hote.nativeElement);
        } else if (err.code === 'REQUETE_INVALIDE' && appliquerDetails(err, this.formulaire)) {
          focaliserPremierInvalide(this.hote.nativeElement);
        } else {
          this.notifications.erreur(err.message);
        }
      },
    });
  }
}

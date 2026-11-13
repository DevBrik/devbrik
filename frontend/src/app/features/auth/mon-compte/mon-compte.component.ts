import { ChangeDetectionStrategy, Component, ElementRef, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { startWith } from 'rxjs';
import {
  appliquerDetails,
  erreurSurChamp,
  focaliserPremierInvalide,
  lireErreur,
  messageChamp,
} from '../../../core/api/erreurs-formulaire';
import { Utilisateur } from '../../../core/api/modeles';
import { AuthService } from '../../../core/auth/auth.service';
import { PAYS } from '../../../core/libelles';
import { ChampMotDePasseComponent } from '../../../shared/champ-mot-de-passe/champ-mot-de-passe.component';
import { DialogueService } from '../../../shared/dialogue-confirmation/dialogue.service';
import { EtatPageComponent } from '../../../shared/etat-page/etat-page.component';
import { NotificationService } from '../../../shared/notification/notification.service';
import { CompteService } from '../compte.service';
import { MESSAGES_MDP, confirmationIdentique, validerMotDePasse } from '../regles-compte';

/** Adresse http ou https (lienProfil), vide autorisée. */
function adresseWeb(c: AbstractControl): Record<string, true> | null {
  const v = String(c.value ?? '').trim();
  if (!v) {
    return null;
  }
  try {
    const u = new URL(v);
    return u.protocol === 'http:' || u.protocol === 'https:' ? null : { url: true };
  } catch {
    return { url: true };
  }
}

const vide = (v: string): string | null => (v.trim() ? v.trim() : null);

/** M10 : mon compte (/compte/profil) : profil, mot de passe, export, suppression (RG17). */
@Component({
  selector: 'db-mon-compte',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, ChampMotDePasseComponent, EtatPageComponent],
  templateUrl: './mon-compte.component.html',
  styleUrl: './mon-compte.component.scss',
})
export class MonCompteComponent {
  private readonly compte = inject(CompteService);
  private readonly auth = inject(AuthService);
  private readonly dialogues = inject(DialogueService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly utilisateur = signal<Utilisateur | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreurChargement = signal(false);
  protected readonly envoiProfil = signal(false);
  protected readonly envoiMdp = signal(false);
  protected readonly export = signal(false);
  protected readonly suppression = signal(false);
  protected readonly estAdmin = computed(() => this.utilisateur()?.role === 'ADMIN');
  protected readonly listePays = computed(() => {
    const p = this.utilisateur()?.pays;
    return p && !PAYS.includes(p) ? [p, ...PAYS] : PAYS;
  });

  protected readonly profil = new FormGroup({
    nomAffiche: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(80)] }),
    bio: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    pays: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(60)] }),
    lienProfil: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(255), adresseWeb] }),
  });
  private readonly bio = toSignal(this.profil.controls.bio.valueChanges.pipe(startWith('')), { initialValue: '' });
  protected readonly longueurBio = computed(() => this.bio().length);

  protected readonly securite = new FormGroup(
    {
      ancienMotDePasse: new FormControl('', { nonNullable: true, validators: [(c) => (c.value ? null : { required: true })] }),
      nouveauMotDePasse: new FormControl('', { nonNullable: true, validators: [validerMotDePasse] }),
      confirmation: new FormControl('', { nonNullable: true, validators: [(c) => (c.value ? null : { required: true })] }),
    },
    { validators: [confirmationIdentique('nouveauMotDePasse', 'confirmation')] },
  );

  constructor() {
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreurChargement.set(false);
    this.compte.moi().subscribe({
      next: (u) => {
        this.utilisateur.set(u);
        this.profil.reset({ nomAffiche: u.nomAffiche ?? '', bio: u.bio ?? '', pays: u.pays ?? '', lienProfil: u.lienProfil ?? '' });
        this.chargement.set(false);
      },
      error: () => {
        this.chargement.set(false);
        this.erreurChargement.set(true);
      },
    });
  }

  protected erreurProfil(champ: string): string | null {
    const messages: Record<string, Record<string, string>> = {
      nomAffiche: { maxlength: 'Le nom affiché doit contenir au plus 80 caractères.' },
      bio: { maxlength: 'La bio doit contenir au plus 500 caractères.' },
      pays: { maxlength: 'Le pays doit contenir au plus 60 caractères.' },
      lienProfil: { url: 'Saisissez une adresse commençant par http:// ou https://.', maxlength: "L'adresse doit contenir au plus 255 caractères." },
    };
    return messageChamp(this.profil.get(champ), messages[champ] ?? {});
  }

  protected erreurSecurite(champ: string): string | null {
    const messages: Record<string, Record<string, string>> = {
      ancienMotDePasse: { required: 'Saisissez votre mot de passe actuel.' },
      nouveauMotDePasse: MESSAGES_MDP,
      confirmation: { required: 'Confirmez le nouveau mot de passe.', differente: 'Les deux mots de passe ne sont pas identiques.' },
    };
    return messageChamp(this.securite.get(champ), messages[champ] ?? {});
  }

  protected enregistrerProfil(): void {
    if (this.profil.invalid) {
      this.profil.markAllAsTouched();
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    const v = this.profil.getRawValue();
    this.envoiProfil.set(true);
    this.compte
      .modifierProfil({ nomAffiche: vide(v.nomAffiche), bio: vide(v.bio), pays: vide(v.pays), lienProfil: vide(v.lienProfil) })
      .subscribe({
        next: (u) => {
          this.envoiProfil.set(false);
          this.utilisateur.set(u);
          this.profil.markAsPristine();
          this.notifications.succes('Profil enregistré');
        },
        error: (e: unknown) => {
          this.envoiProfil.set(false);
          const err = lireErreur(e);
          if (err.code === 'REQUETE_INVALIDE' && appliquerDetails(err, this.profil)) {
            focaliserPremierInvalide(this.hote.nativeElement);
          } else if (err.code !== 'NON_AUTHENTIFIE') {
            this.notifications.erreur(err.message);
          }
        },
      });
  }

  protected changerMotDePasse(): void {
    if (this.securite.invalid) {
      this.securite.markAllAsTouched();
      focaliserPremierInvalide(this.hote.nativeElement);
      return;
    }
    const v = this.securite.getRawValue();
    this.envoiMdp.set(true);
    this.compte.changerMotDePasse(v.ancienMotDePasse, v.nouveauMotDePasse).subscribe({
      next: () => {
        this.envoiMdp.set(false);
        this.securite.reset();
        this.notifications.succes('Mot de passe modifié');
      },
      error: (e: unknown) => {
        this.envoiMdp.set(false);
        const err = lireErreur(e);
        if (err.code === 'MOT_DE_PASSE_INCORRECT') {
          erreurSurChamp(this.securite, 'ancienMotDePasse', err.message);
          focaliserPremierInvalide(this.hote.nativeElement);
        } else if (err.code === 'REQUETE_INVALIDE' && appliquerDetails(err, this.securite)) {
          focaliserPremierInvalide(this.hote.nativeElement);
        } else if (err.code !== 'NON_AUTHENTIFIE') {
          this.notifications.erreur(err.message);
        }
      },
    });
  }

  /** Téléchargement du JSON de GET /api/moi/export (fichier devbrik-export-AAAA-MM-JJ.json, par Blob). */
  protected exporter(): void {
    this.export.set(true);
    this.compte.exporter().subscribe({
      next: (donnees) => {
        this.export.set(false);
        const blob = new Blob([JSON.stringify(donnees, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const lien = document.createElement('a');
        lien.href = url;
        lien.download = `devbrik-export-${new Date().toISOString().slice(0, 10)}.json`;
        document.body.appendChild(lien);
        lien.click();
        lien.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
        this.notifications.succes('Export téléchargé');
      },
      error: (e: unknown) => {
        this.export.set(false);
        this.notifications.erreur(lireErreur(e).message);
      },
    });
  }

  protected supprimerCompte(): void {
    this.dialogues
      .confirmerAvecMotDePasse({
        titre: 'Supprimer mon compte ?',
        message: 'Cette opération est définitive. Confirmez votre identité en saisissant votre mot de passe.',
        rappel: 'Vos briques, avis, tests, demandes, réponses et signalements seront supprimés.',
        libelleConfirmer: 'Supprimer mon compte',
      })
      .subscribe((motDePasse) => {
        if (motDePasse === null) {
          return;
        }
        this.suppression.set(true);
        this.compte.supprimerCompte(motDePasse).subscribe({
          next: () => {
            this.auth.deconnexion();
            this.notifications.succes('Votre compte a été supprimé.');
            void this.router.navigateByUrl('/briques');
          },
          error: (e: unknown) => {
            this.suppression.set(false);
            const err = lireErreur(e);
            this.notifications.erreur(
              err.code === 'MOT_DE_PASSE_INCORRECT' ? 'Mot de passe incorrect : votre compte n’a pas été supprimé.' : err.message,
            );
          },
        });
      });
  }
}

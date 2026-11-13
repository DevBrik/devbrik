import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal,
  untracked,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { PageReponse } from '../../../core/api/modeles';
import { ErreurApi, lireErreur } from '../avis.erreurs';
import { AvisEvenements } from '../avis.evenements';
import { Avis, Ecriture, etoiles, nomAuteur } from '../avis.modeles';
import { AvisService } from '../avis.service';
import { SocleAvis } from '../avis.socle';
import { DialogueAvisComponent, DonneesDialogueAvis } from '../dialogues/dialogue-avis.component';
import { DialogueService } from '../../../shared/dialogue-confirmation/dialogue.service';

/**
 * Section « Avis des utilisateurs » de la fiche M02 : récents d'abord, étoiles avec texte, pagination.
 * Usage (membre 2) : <db-avis-bloc [briqueId]="brique().id" />
 */
@Component({
  selector: 'db-avis-bloc',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
  templateUrl: './avis-bloc.component.html',
  styleUrl: './avis-bloc.component.scss',
})
export class AvisBlocComponent {
  protected readonly socle = inject(SocleAvis);
  private readonly service = inject(AvisService);
  private readonly evenements = inject(AvisEvenements);
  private readonly dialog = inject(MatDialog);
  private readonly dialogues = inject(DialogueService);
  private readonly destroyRef = inject(DestroyRef);

  readonly briqueId = input.required<number>();
  /** Émis après modification ou suppression de son propre avis (ou suppression par un administrateur). */
  readonly changed = output<void>();

  protected readonly page = signal<PageReponse<Avis> | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal<ErreurApi | null>(null);
  protected readonly etoiles = etoiles;
  protected readonly nomAuteur = nomAuteur;

  private indexPage = 0;
  private readonly taille = 10;
  private idCharge: number | null = null;
  private sequence = 0;

  constructor() {
    effect(() => {
      const id = this.briqueId();
      this.evenements.version();
      untracked(() => {
        if (id !== this.idCharge) {
          this.idCharge = id;
          this.indexPage = 0;
        }
        this.charger();
      });
    });
  }

  protected charger(): void {
    const courant = ++this.sequence;
    this.chargement.set(true);
    this.erreur.set(null);
    this.service
      .listerAvis(this.briqueId(), this.indexPage, this.taille)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultat) => {
          if (courant !== this.sequence) {
            return;
          }
          // Dernier avis d'une page supprimé : retour sur la dernière page qui existe encore.
          if (resultat.contenu.length === 0 && this.indexPage > 0 && resultat.totalPages > 0) {
            this.indexPage = resultat.totalPages - 1;
            this.charger();
            return;
          }
          this.page.set(resultat);
          this.chargement.set(false);
        },
        error: (e: unknown) => {
          if (courant !== this.sequence) {
            return;
          }
          this.erreur.set(lireErreur(e));
          this.chargement.set(false);
        },
      });
  }

  protected aller(delta: number): void {
    this.indexPage = Math.max(0, this.indexPage + delta);
    this.charger();
  }

  protected estMien(avis: Avis): boolean {
    const id = this.socle.idUtilisateur();
    return id !== null && avis.auteur?.id === id;
  }

  private apresChangement(): void {
    this.evenements.signaler();
    this.changed.emit();
  }

  protected modifier(avis: Avis): void {
    this.dialog
      .open<DialogueAvisComponent, DonneesDialogueAvis, Ecriture<Avis>>(DialogueAvisComponent, {
        data: { idBrique: this.briqueId(), avis },
        width: '36rem',
        maxWidth: '100vw',
      })
      .afterClosed()
      .subscribe((resultat) => {
        if (resultat) {
          this.socle.succes('Votre avis est mis à jour.');
          this.apresChangement();
        }
      });
  }

  /** RG19 : l'auteur de l'avis ou un administrateur. */
  protected supprimer(avis: Avis): void {
    const mien = this.estMien(avis);
    this.dialogues.confirmer({
      titre: mien ? 'Supprimer mon avis ?' : `Supprimer l’avis de ${nomAuteur(avis.auteur)} ?`,
      message: 'Cette action est irréversible.',
      libelleConfirmer: 'Supprimer',
    }).subscribe((accord) => {
      if (!accord) {
        return;
      }
      this.service.supprimerAvis(avis.id).subscribe({
        next: () => {
          this.socle.succes('Avis supprimé.');
          this.apresChangement();
        },
        error: (e: unknown) => this.socle.erreur(lireErreur(e).message),
      });
    });
  }
}

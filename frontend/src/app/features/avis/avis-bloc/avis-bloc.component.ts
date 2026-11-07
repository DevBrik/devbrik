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
import { STYLES_COMMUNS } from '../avis.styles';
import { DialogueAvisComponent, DonneesDialogueAvis } from '../dialogues/dialogue-avis.component';
import { confirmer } from '../dialogues/dialogue-confirmation-local.component';

/**
 * Section « Avis des utilisateurs » de la fiche M02 : récents d'abord, étoiles avec texte, pagination.
 * Usage (membre 2) : <db-avis-bloc [briqueId]="brique().id" />
 */
@Component({
  selector: 'db-avis-bloc',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
  template: `
    <section aria-labelledby="titre-avis">
      <h2 id="titre-avis">Avis des utilisateurs</h2>

      @if (chargement() && !page()) {
        <div class="squelette" aria-hidden="true"><span></span><span></span><span></span></div>
        <p class="sr-only" role="status">Chargement des avis…</p>
      } @else if (erreur(); as e) {
        <div class="etat" role="alert">
          <p>{{ e.statut === 404 ? 'Les avis de cette brique ne sont pas disponibles.' : e.message }}</p>
          @if (e.statut !== 404) {
            <button type="button" class="btn btn-primaire" (click)="charger()">Réessayer</button>
          }
        </div>
      } @else if (page(); as p) {
        @if (p.totalElements === 0) {
          <p class="etat">Aucun avis pour le moment.</p>
        } @else {
          <ul class="cartes" [attr.aria-busy]="chargement()">
            @for (a of p.contenu; track a.id) {
              <li class="carte">
                <div class="entete">
                  <h3 class="nom">{{ nomAuteur(a.auteur) }}</h3>
                  <p class="note">
                    <span class="etoiles" aria-hidden="true">{{ etoiles(a.note) }}</span>
                    <span class="sr-only">{{ a.note }} sur 5</span>
                  </p>
                </div>
                @if (a.commentaire) {
                  <p class="commentaire">{{ a.commentaire }}</p>
                }
                <p class="date">
                  <time [attr.datetime]="a.dateCreation">{{ a.dateCreation | date: 'd MMMM y' }}</time>
                  @if (a.dateModification !== a.dateCreation) {
                    <span>· modifié</span>
                  }
                </p>
                @if (estMien(a)) {
                  <div class="actions">
                    <button type="button" class="btn btn-accent" (click)="modifier(a)">Modifier mon avis</button>
                    <button type="button" class="btn btn-destructif" (click)="supprimer(a)">Supprimer mon avis</button>
                  </div>
                } @else if (socle.estAdmin()) {
                  <div class="actions">
                    <button
                      type="button"
                      class="btn btn-destructif"
                      [attr.aria-label]="'Supprimer l’avis de ' + nomAuteur(a.auteur)"
                      (click)="supprimer(a)"
                    >
                      Supprimer l’avis
                    </button>
                  </div>
                }
              </li>
            }
          </ul>

          @if (p.totalPages > 1) {
            <nav class="pagination" aria-label="Pagination des avis">
              <button type="button" class="btn btn-primaire" [disabled]="p.page === 0" (click)="aller(-1)">
                <span aria-hidden="true">←</span> Précédent
              </button>
              <span>Page {{ p.page + 1 }} sur {{ p.totalPages }}</span>
              <button type="button" class="btn btn-primaire" [disabled]="p.page + 1 >= p.totalPages" (click)="aller(1)">
                Suivant <span aria-hidden="true">→</span>
              </button>
            </nav>
          }
        }
      }
    </section>
  `,
  styles: [
    STYLES_COMMUNS,
    `
      :host { display: block; }
      .cartes {
        list-style: none; margin: 0; padding: 0; display: grid; gap: 1rem;
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
      @media (max-width: 640px) { .cartes { grid-template-columns: 1fr; } }
      .carte {
        padding: 1rem; border-radius: 0.75rem; background: #fff; display: grid; gap: 0.5rem;
        align-content: start;
      }
      .entete { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 0.5rem; align-items: baseline; }
      .nom { margin: 0; font-size: 1.125rem; }
      .note { margin: 0; }
      .etoiles { color: var(--db-orange); letter-spacing: 0.1em; font-size: 1.125rem; }
      .commentaire { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; }
      .date { margin: 0; font-size: 0.875rem; text-align: right; }
      .etat { padding: 1rem 0; }
      .pagination { display: flex; flex-wrap: wrap; justify-content: center; align-items: center; gap: 1rem; margin-top: 1rem; }
      .squelette { display: grid; gap: 0.75rem; }
      .squelette span { display: block; height: 4rem; border-radius: 0.75rem; background: #e2e8f0; animation: pulsation 1.4s ease-in-out infinite; }
      @keyframes pulsation { 50% { opacity: 0.5; } }
      @media (prefers-reduced-motion: reduce) { .squelette span { animation: none; } }
    `,
  ],
})
export class AvisBlocComponent {
  protected readonly socle = inject(SocleAvis);
  private readonly service = inject(AvisService);
  private readonly evenements = inject(AvisEvenements);
  private readonly dialog = inject(MatDialog);
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
    confirmer(this.dialog, {
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

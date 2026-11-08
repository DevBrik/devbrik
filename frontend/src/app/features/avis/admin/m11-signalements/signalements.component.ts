import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { PageReponse } from '../../../../core/api/modeles';
import { ErreurApi, lireErreur } from '../../avis.erreurs';
import { ActionTraitement, BriqueResumeAdmin, Signalement } from '../../avis.modeles';
import { AvisService } from '../../avis.service';
import { SocleAvis } from '../../avis.socle';
import { STYLES_COMMUNS } from '../../avis.styles';
import { confirmer } from '../../dialogues/dialogue-confirmation-local.component';
import { AdminEnteteComponent } from '../admin-entete.component';
import { STYLES_ADMIN } from '../admin.styles';
import { PaginationLocaleComponent } from '../pagination-locale.component';
import { CarteBriqueMasqueeComponent } from './carte-brique-masquee.component';
import { CarteSignalementComponent } from './carte-signalement.component';
import { DetailSignalementComponent } from './detail-signalement.component';

const TAILLE = 20;

/**
 * M11 : Administration, signalements (/admin/signalements, administrateur).
 * Liste des signalements ouverts, panneau de détails, section « Briques masquées ».
 * Les droits sont vérifiés par le serveur (RG19) ; adminGuard ne sert qu'à l'affichage.
 */
@Component({
  selector: 'avis-signalements',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    AdminEnteteComponent,
    CarteSignalementComponent,
    DetailSignalementComponent,
    CarteBriqueMasqueeComponent,
    PaginationLocaleComponent,
  ],
  template: `
    <div class="conteneur">
      <avis-admin-entete />

      <!-- ===== Signalements à examiner ===== -->
      <section aria-labelledby="titre-liste">
        <h2 id="titre-liste">Signalements à examiner</h2>

        @if (chargement() && !signalements()) {
          <div class="squelette" aria-hidden="true"><span></span><span></span><span></span></div>
          <p class="sr-only" role="status">Chargement des signalements…</p>
        } @else if (erreur(); as e) {
          <div class="etat" role="alert">
            <p class="erreur">{{ e.message }}</p>
            @if (e.statut !== 403 && e.statut !== 404) {
              <button type="button" class="btn btn-primaire" (click)="chargerSignalements()">Réessayer</button>
            }
          </div>
        } @else if (signalements(); as p) {
          @if (p.totalElements === 0) {
            <p class="etat vide">Aucun signalement à examiner.</p>
          } @else {
            <ul class="cartes" [attr.aria-busy]="chargement()">
              @for (s of p.contenu; track s.id) {
                <li>
                  <avis-carte-signalement
                    [signalement]="s"
                    [selectionnee]="s.id === selectionId()"
                    (examiner)="examiner($event)"
                  />
                </li>
              }
            </ul>
            <avis-pagination
              libelle="Pagination des signalements"
              [page]="p.page"
              [totalPages]="p.totalPages"
              (pageChange)="allerSignalements($event)"
            />
          }
        }
      </section>

      <!-- ===== Détails du signalement ===== -->
      <section aria-labelledby="titre-details" class="details">
        <h2 id="titre-details" #titreDetails tabindex="-1">Détails du signalement</h2>
        @if (selection(); as s) {
          <avis-detail-signalement
            [signalement]="s"
            [enCours]="enCours()"
            (conserver)="traiter('CONSERVER')"
            (masquer)="traiter('MASQUER_BRIQUE')"
          />
        } @else {
          <p class="etat vide">Choisissez un signalement dans la liste pour voir ses détails.</p>
        }
      </section>

      <!-- ===== Briques masquées ===== -->
      <section aria-labelledby="titre-masquees">
        <h2 id="titre-masquees">Briques masquées</h2>

        @if (chargementMasquees() && !masquees()) {
          <div class="squelette" aria-hidden="true"><span></span><span></span></div>
          <p class="sr-only" role="status">Chargement des briques masquées…</p>
        } @else if (erreurMasquees(); as e) {
          <div class="etat" role="alert">
            <p class="erreur">{{ e.message }}</p>
            @if (e.statut !== 403 && e.statut !== 404) {
              <button type="button" class="btn btn-primaire" (click)="chargerMasquees()">Réessayer</button>
            }
          </div>
        } @else if (masquees(); as p) {
          @if (p.totalElements === 0) {
            <p class="etat vide">Aucune brique masquée.</p>
          } @else {
            <ul class="cartes" [attr.aria-busy]="chargementMasquees()">
              @for (b of p.contenu; track b.id) {
                <li>
                  <avis-carte-brique-masquee
                    [brique]="b"
                    [enCours]="republication() === b.id"
                    (republier)="republier(b)"
                  />
                </li>
              }
            </ul>
            <avis-pagination
              libelle="Pagination des briques masquées"
              [page]="p.page"
              [totalPages]="p.totalPages"
              (pageChange)="allerMasquees($event)"
            />
          }
        }
      </section>
    </div>
  `,
  styles: [
    STYLES_COMMUNS,
    STYLES_ADMIN,
    `
      .conteneur { max-width: 1200px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
      section { margin-bottom: 2rem; }
      h2 { margin: 0 0 1rem; font-size: 1.5rem; font-weight: 700; }
      #titre-details:focus { outline: none; }
      #titre-details:focus-visible { outline: 3px solid var(--db-focus); outline-offset: 4px; }
      .cartes {
        list-style: none; margin: 0; padding: 0; display: grid; gap: 1rem;
        grid-template-columns: repeat(3, minmax(0, 1fr));
      }
      @media (max-width: 960px) { .cartes { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
      @media (max-width: 640px) { .cartes { grid-template-columns: 1fr; } }
    `,
  ],
})
export class SignalementsComponent {
  private readonly service = inject(AvisService);
  private readonly socle = inject(SocleAvis);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  private readonly titreDetails = viewChild<ElementRef<HTMLElement>>('titreDetails');

  // ----- signalements ouverts -----
  protected readonly signalements = signal<PageReponse<Signalement> | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal<ErreurApi | null>(null);
  protected readonly selectionId = signal<number | null>(null);
  protected readonly selection = computed(
    () => this.signalements()?.contenu.find((s) => s.id === this.selectionId()) ?? null,
  );
  protected readonly enCours = signal<ActionTraitement | null>(null);

  // ----- briques masquées -----
  protected readonly masquees = signal<PageReponse<BriqueResumeAdmin> | null>(null);
  protected readonly chargementMasquees = signal(true);
  protected readonly erreurMasquees = signal<ErreurApi | null>(null);
  protected readonly republication = signal<number | null>(null);

  private indexSignalements = 0;
  private indexMasquees = 0;
  private sequenceSignalements = 0;
  private sequenceMasquees = 0;

  constructor() {
    this.chargerSignalements();
    this.chargerMasquees();
  }

  // ---------- chargements ----------

  protected chargerSignalements(): void {
    const courant = ++this.sequenceSignalements;
    this.chargement.set(true);
    this.erreur.set(null);
    this.service
      .listerSignalements('OUVERT', this.indexSignalements, TAILLE)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultat) => {
          if (courant !== this.sequenceSignalements) {
            return;
          }
          // Dernier signalement d'une page traité : retour sur la dernière page qui existe encore.
          if (resultat.contenu.length === 0 && this.indexSignalements > 0 && resultat.totalPages > 0) {
            this.indexSignalements = resultat.totalPages - 1;
            this.chargerSignalements();
            return;
          }
          this.signalements.set(resultat);
          this.chargement.set(false);
          // Un signalement qui n'est plus dans la page (traité, autre page) n'est plus sélectionné.
          if (!resultat.contenu.some((s) => s.id === this.selectionId())) {
            this.selectionId.set(null);
          }
        },
        error: (e: unknown) => {
          if (courant !== this.sequenceSignalements) {
            return;
          }
          this.echecChargement(e, this.erreur);
          this.chargement.set(false);
        },
      });
  }

  protected chargerMasquees(): void {
    const courant = ++this.sequenceMasquees;
    this.chargementMasquees.set(true);
    this.erreurMasquees.set(null);
    this.service
      .listerBriquesMasquees(this.indexMasquees, TAILLE)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultat) => {
          if (courant !== this.sequenceMasquees) {
            return;
          }
          // Dernière brique d'une page republiée : retour sur la dernière page qui existe encore.
          if (resultat.contenu.length === 0 && this.indexMasquees > 0 && resultat.totalPages > 0) {
            this.indexMasquees = resultat.totalPages - 1;
            this.chargerMasquees();
            return;
          }
          this.masquees.set(resultat);
          this.chargementMasquees.set(false);
        },
        error: (e: unknown) => {
          if (courant !== this.sequenceMasquees) {
            return;
          }
          this.echecChargement(e, this.erreurMasquees);
          this.chargementMasquees.set(false);
        },
      });
  }

  private echecChargement(e: unknown, cible: { set(valeur: ErreurApi): void }): void {
    const erreur = lireErreur(e);
    if (erreur.code === 'NON_AUTHENTIFIE') {
      this.socle.versConnexion();
      return;
    }
    cible.set(erreur);
  }

  protected allerSignalements(page: number): void {
    this.indexSignalements = Math.max(0, page);
    this.chargerSignalements();
  }

  protected allerMasquees(page: number): void {
    this.indexMasquees = Math.max(0, page);
    this.chargerMasquees();
  }

  // ---------- sélection ----------

  protected examiner(id: number): void {
    this.selectionId.set(id);
    this.focaliserDetails();
  }

  /** Amène le clavier et le lecteur d'écran sur le panneau, sans parcourir toute la liste. */
  private focaliserDetails(): void {
    setTimeout(() => this.titreDetails()?.nativeElement.focus());
  }

  // ---------- traitement d'un signalement (RG11) ----------

  protected traiter(action: ActionTraitement): void {
    const signalement = this.selection();
    if (!signalement || this.enCours() !== null) {
      return;
    }
    if (action === 'CONSERVER') {
      this.envoyer(signalement, action);
      return;
    }
    const titre = signalement.brique?.titre ?? 'cette brique';
    confirmer(this.dialog, {
      titre: 'Masquer cette brique ?',
      message: `La brique « ${titre} » disparaîtra des listes publiques et des recherches. Son auteur et les administrateurs pourront toujours la consulter.`,
      libelleConfirmer: 'Masquer la brique',
    }).subscribe((accord) => {
      if (accord) {
        this.envoyer(signalement, action);
      }
    });
  }

  private envoyer(signalement: Signalement, action: ActionTraitement): void {
    this.enCours.set(action);
    this.service.traiterSignalement(signalement.id, action).subscribe({
      next: () => {
        this.enCours.set(null);
        this.socle.succes(
          action === 'CONSERVER'
            ? 'Signalement traité : la brique est conservée.'
            : 'Signalement traité : la brique est masquée.',
        );
        this.selectionId.set(null);
        this.chargerSignalements();
        this.chargerMasquees();
        this.focaliserDetails();
      },
      error: (e: unknown) => {
        this.enCours.set(null);
        this.echecAction(e);
      },
    });
  }

  // ---------- republication d'une brique masquée ----------

  protected republier(brique: BriqueResumeAdmin): void {
    if (this.republication() !== null) {
      return;
    }
    this.republication.set(brique.id);
    this.service.republierBrique(brique.id).subscribe({
      next: () => {
        this.republication.set(null);
        this.socle.succes(`La brique « ${brique.titre} » est republiée.`);
        this.chargerMasquees();
      },
      error: (e: unknown) => {
        this.republication.set(null);
        this.echecAction(e);
      },
    });
  }

  /** Erreurs d'action : notification avec le message du serveur ; 404 = la liste est périmée. */
  private echecAction(e: unknown): void {
    const erreur = lireErreur(e);
    if (erreur.code === 'NON_AUTHENTIFIE') {
      this.socle.versConnexion();
      return;
    }
    this.socle.erreur(erreur.message);
    if (erreur.statut === 404) {
      this.chargerSignalements();
      this.chargerMasquees();
    }
  }
}

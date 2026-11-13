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
import { PageReponse } from '../../../../core/api/modeles';
import { ErreurApi, lireErreur } from '../../avis.erreurs';
import { ActionTraitement, BriqueResumeAdmin, Signalement } from '../../avis.modeles';
import { AvisService } from '../../avis.service';
import { SocleAvis } from '../../avis.socle';
import { DialogueService } from '../../../../shared/dialogue-confirmation/dialogue.service';
import { AdminEnteteComponent } from '../admin-entete.component';
import { BriqueCarteComponent } from '../../../../shared/brique-carte/brique-carte.component';
import { PaginationComponent } from '../../../../shared/pagination/pagination.component';
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
    BriqueCarteComponent,
    PaginationComponent,
  ],
  templateUrl: './signalements.component.html',
  styleUrl: './signalements.component.scss',
})
export class SignalementsComponent {
  private readonly service = inject(AvisService);
  private readonly socle = inject(SocleAvis);
  private readonly dialogues = inject(DialogueService);
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
    this.dialogues.confirmer({
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

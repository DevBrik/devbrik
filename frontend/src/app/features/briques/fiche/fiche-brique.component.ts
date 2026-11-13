import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { lireErreur } from '../../../core/api/erreurs-formulaire';
import { BriqueDetail } from '../../../core/api/modeles';
import { AuthService } from '../../../core/auth/auth.service';
import { formaterNote, libelleLicence, lignes, nomAuteur, pastilleTest } from '../../../core/libelles';
import { DialogueService } from '../../../shared/dialogue-confirmation/dialogue.service';
import { EtatPageComponent, TypeEtat } from '../../../shared/etat-page/etat-page.component';
import { LangageBadgeComponent } from '../../../shared/langage-badge/langage-badge.component';
import { NotificationService } from '../../../shared/notification/notification.service';
import { PastilleComponent, TypePastille } from '../../../shared/pastille/pastille.component';
import { ZoneCodeComponent } from '../../../shared/zone-code/zone-code.component';
import { ActionsEvaluationComponent } from '../../avis/actions-evaluation/actions-evaluation.component';
import { AvisBlocComponent } from '../../avis/avis-bloc/avis-bloc.component';
import { BriquesService } from '../briques.service';

/**
 * M02 : fiche d'une brique (/briques/:id). En-tête, tuiles Évaluation et Tests, code, mode d'emploi,
 * prérequis, actions (membre 3 : avis, test, signalement), actions de l'auteur et de l'administrateur.
 */
@Component({
  selector: 'db-fiche-brique',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DatePipe,
    RouterLink,
    LangageBadgeComponent,
    PastilleComponent,
    ZoneCodeComponent,
    EtatPageComponent,
    ActionsEvaluationComponent,
    AvisBlocComponent,
  ],
  templateUrl: './fiche-brique.component.html',
  styleUrl: './fiche-brique.component.scss',
})
export class FicheBriqueComponent {
  private readonly service = inject(BriquesService);
  private readonly auth = inject(AuthService);
  private readonly dialogues = inject(DialogueService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly titre = inject(Title);

  /** Paramètre de route (withComponentInputBinding). */
  readonly id = input.required<string>();

  protected readonly brique = signal<BriqueDetail | null>(null);
  protected readonly etat = signal<TypeEtat | null>('chargement');
  protected readonly action = signal<'suppression' | 'statut' | null>(null);
  private appel?: Subscription;

  protected readonly estAuteur = computed(() => {
    const u = this.auth.utilisateur();
    return !!u && this.brique()?.auteur?.id === u.id;
  });
  protected readonly estAdmin = this.auth.estAdmin;
  protected readonly licence = computed(() => libelleLicence(this.brique()?.licence ?? ''));
  protected readonly auteur = computed(() => nomAuteur(this.brique()?.auteur));
  protected readonly etapes = computed(() => lignes(this.brique()?.modeEmploi));
  protected readonly prerequis = computed(() => lignes(this.brique()?.prerequis));
  protected readonly note = computed(() => {
    const n = this.brique()?.noteMoyenne;
    return n === null || n === undefined ? null : formaterNote(n);
  });
  protected readonly typeTest = computed<TypePastille>(() => {
    const b = this.brique();
    if (!b) {
      return 'non-testee';
    }
    const t = pastilleTest(b);
    if (t.classe === 'ok') {
      return 'test-ok';
    }
    if (t.classe === 'ko') {
      return 'test-ko';
    }
    return t.libelle === 'Avis partagés' ? 'avis-partages' : 'non-testee';
  });

  constructor() {
    effect(() => {
      const id = Number(this.id());
      untracked(() => this.charger(id, true));
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
      next: (b) => {
        this.brique.set(b);
        this.etat.set(null);
        this.titre.setTitle(`${b.titre} – DevBrik`);
      },
      error: (e: unknown) => {
        const err = lireErreur(e);
        this.brique.set(null);
        this.etat.set(err.statut === 404 || err.statut === 400 ? '404' : err.statut === 403 ? '403' : 'erreur');
      },
    });
  }

  protected supprimer(): void {
    const b = this.brique();
    if (!b) {
      return;
    }
    this.dialogues
      .confirmer({
        titre: 'Supprimer cette brique ?',
        message: `« ${b.titre} » sera supprimée avec ses avis, ses tests et ses signalements. Cette action est irréversible.`,
        libelleConfirmer: 'Supprimer',
      })
      .subscribe((ok) => {
        if (!ok) {
          return;
        }
        this.action.set('suppression');
        this.service.supprimer(b.id).subscribe({
          next: () => {
            this.notifications.succes('Brique supprimée');
            void this.router.navigateByUrl(this.estAuteur() ? '/briques/mes-briques' : '/briques');
          },
          error: (e: unknown) => {
            this.action.set(null);
            this.notifications.erreur(lireErreur(e).message);
          },
        });
      });
  }

  /** Administrateur : masquer ou republier (PUT /api/admin/briques/{id}/statut). */
  protected basculerStatut(): void {
    const b = this.brique();
    if (!b) {
      return;
    }
    const masquer = b.statut === 'PUBLIEE';
    const executer = () => {
      this.action.set('statut');
      this.service.changerStatut(b.id, masquer ? 'MASQUEE' : 'PUBLIEE').subscribe({
        next: (maj) => {
          this.action.set(null);
          this.brique.set(maj);
          this.notifications.succes(masquer ? 'Brique masquée' : 'Brique republiée');
        },
        error: (e: unknown) => {
          this.action.set(null);
          this.notifications.erreur(lireErreur(e).message);
        },
      });
    };
    if (!masquer) {
      executer();
      return;
    }
    this.dialogues
      .confirmer({
        titre: 'Masquer cette brique ?',
        message: `« ${b.titre} » disparaîtra des listes publiques et des recherches. Son auteur et les administrateurs pourront toujours la consulter.`,
        libelleConfirmer: 'Masquer la brique',
      })
      .subscribe((ok) => ok && executer());
  }
}

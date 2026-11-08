import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  untracked,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { lireErreur } from '../avis.erreurs';
import { AvisEvenements } from '../avis.evenements';
import { BriqueEvaluable, Ecriture, Avis, libelleResultat, MoiBrique, Test } from '../avis.modeles';
import { AvisService } from '../avis.service';
import { SocleAvis } from '../avis.socle';
import { STYLES_COMMUNS } from '../avis.styles';
import { DialogueAvisComponent, DonneesDialogueAvis } from '../dialogues/dialogue-avis.component';
import {
  DialogueSignalementComponent,
  DonneesDialogueSignalement,
} from '../dialogues/dialogue-signalement.component';
import { DialogueTestComponent, DonneesDialogueTest } from '../dialogues/dialogue-test.component';

/**
 * Boutons « Tester cette brique », « Donner mon avis », « Signaler » de la fiche M02, avec leurs dialogues.
 * Usage (membre 2) : <db-actions-evaluation [brique]="brique()" (changed)="recharger()" />
 * « changed » est émis après un avis ou un test enregistré : la fiche relit les statistiques.
 */
@Component({
  selector: 'db-actions-evaluation',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="evaluation" aria-label="Évaluer cette brique">
      <div class="actions">
        <button
          type="button"
          class="btn btn-primaire"
          [disabled]="evaluationBloquee()"
          [attr.aria-describedby]="explication() ? 'explication-evaluation' : null"
          (click)="tester()"
        >
          Tester cette brique
        </button>
        <button
          type="button"
          class="btn btn-accent"
          [disabled]="evaluationBloquee()"
          [attr.aria-describedby]="explication() ? 'explication-evaluation' : null"
          (click)="donnerAvis()"
        >
          Donner mon avis
        </button>
        <button type="button" class="btn btn-destructif" [disabled]="signalementBloque()" (click)="signaler()">
          Signaler
        </button>
      </div>

      @if (explication(); as texte) {
        <p class="explication" id="explication-evaluation">{{ texte }}</p>
      }

      @if (erreurMoi(); as message) {
        <div role="alert">
          <p class="erreur">{{ message }}</p>
          <button type="button" class="btn btn-primaire" (click)="chargerMoi()">Réessayer</button>
        </div>
      }

      @if (moi(); as m) {
        @if (m.monAvis || m.monTest) {
          <ul class="mes-evaluations">
            @if (m.monAvis; as a) {
              <li>Votre avis : <strong>{{ a.note }} sur 5</strong></li>
            }
            @if (m.monTest; as t) {
              <li>
                Votre test : <strong>{{ libelleResultat(t.resultat) }}</strong>
                @if (t.environnement) {
                  <span>({{ t.environnement }})</span>
                }
              </li>
            }
          </ul>
        }
        @if (testObsolete()) {
          <p class="info" role="note">
            <span aria-hidden="true">!</span>
            Votre test porte sur la version {{ m.monTest?.versionCode }} du code ; la version actuelle est la
            {{ brique().versionCode }}. Il ne compte plus : refaites-le avec « Tester cette brique ».
          </p>
        }
      }
    </section>
  `,
  styles: [
    STYLES_COMMUNS,
    `
      :host { display: block; }
      .evaluation { display: grid; gap: 0.75rem; }
      .explication { margin: 0; font-size: 1rem; }
      .mes-evaluations { margin: 0; padding-left: 1.25rem; }
    `,
  ],
})
export class ActionsEvaluationComponent {
  private readonly service = inject(AvisService);
  private readonly socle = inject(SocleAvis);
  private readonly evenements = inject(AvisEvenements);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  readonly brique = input.required<BriqueEvaluable>();
  readonly changed = output<void>();

  protected readonly moi = signal<MoiBrique | null>(null);
  protected readonly erreurMoi = signal<string | null>(null);
  protected readonly libelleResultat = libelleResultat;

  protected readonly estAuteur = computed(() => {
    const id = this.socle.idUtilisateur();
    return id !== null && this.brique().auteur?.id === id;
  });
  protected readonly masquee = computed(() => this.brique().statut === 'MASQUEE');
  protected readonly evaluationBloquee = computed(() => this.estAuteur() || this.masquee());
  protected readonly signalementBloque = computed(() => this.masquee());
  protected readonly explication = computed(() => {
    if (this.estAuteur()) {
      return 'Vous ne pouvez pas évaluer votre propre brique.';
    }
    if (this.masquee()) {
      return 'Cette brique est masquée : elle ne peut pas être évaluée.';
    }
    return null;
  });
  protected readonly testObsolete = computed(() => {
    const test = this.moi()?.monTest;
    return !!test && test.versionCode !== this.brique().versionCode;
  });

  private sequence = 0;

  constructor() {
    effect(() => {
      const id = this.brique().id;
      this.brique().versionCode; // la version change : mon test peut devenir obsolète
      this.evenements.version();
      const peutCharger = this.socle.connecte() && !this.estAuteur() && !this.masquee();
      untracked(() => {
        if (peutCharger) {
          this.charger(id);
        } else {
          this.moi.set(null);
          this.erreurMoi.set(null);
        }
      });
    });
  }

  protected chargerMoi(): void {
    this.charger(this.brique().id);
  }

  private charger(idBrique: number): void {
    const courant = ++this.sequence;
    this.erreurMoi.set(null);
    this.service
      .moi(idBrique)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultat) => {
          if (courant === this.sequence) {
            this.moi.set(resultat);
          }
        },
        error: (e: unknown) => {
          if (courant === this.sequence) {
            this.erreurMoi.set(lireErreur(e).message);
          }
        },
      });
  }

  /** Un visiteur est renvoyé vers la connexion, puis revient sur la fiche. */
  private connecteOuRedirige(): boolean {
    if (!this.socle.connecte()) {
      this.socle.versConnexion();
      return false;
    }
    return true;
  }

  private apresChangement(): void {
    this.evenements.signaler();
    this.changed.emit();
  }

  protected tester(): void {
    if (!this.connecteOuRedirige()) {
      return;
    }
    const b = this.brique();
    this.dialog
      .open<DialogueTestComponent, DonneesDialogueTest, Ecriture<Test>>(DialogueTestComponent, {
        data: { idBrique: b.id, titreBrique: b.titre, versionCode: b.versionCode, test: this.moi()?.monTest ?? null },
        width: '36rem',
        maxWidth: '100vw',
      })
      .afterClosed()
      .subscribe((resultat) => {
        if (resultat) {
          this.socle.succes(resultat.cree ? 'Merci, votre test est enregistré.' : 'Votre test est mis à jour.');
          this.apresChangement();
        }
      });
  }

  protected donnerAvis(): void {
    if (!this.connecteOuRedirige()) {
      return;
    }
    const b = this.brique();
    this.dialog
      .open<DialogueAvisComponent, DonneesDialogueAvis, Ecriture<Avis>>(DialogueAvisComponent, {
        data: { idBrique: b.id, titreBrique: b.titre, avis: this.moi()?.monAvis ?? null },
        width: '36rem',
        maxWidth: '100vw',
      })
      .afterClosed()
      .subscribe((resultat) => {
        if (resultat) {
          this.socle.succes(resultat.cree ? 'Merci, votre avis est publié.' : 'Votre avis est mis à jour.');
          this.apresChangement();
        }
      });
  }

  protected signaler(): void {
    if (!this.connecteOuRedirige()) {
      return;
    }
    const b = this.brique();
    this.dialog
      .open<DialogueSignalementComponent, DonneesDialogueSignalement, boolean>(DialogueSignalementComponent, {
        data: { idBrique: b.id, titreBrique: b.titre },
        width: '36rem',
        maxWidth: '100vw',
      })
      .afterClosed()
      .subscribe((envoye) => {
        if (envoye === true) {
          this.socle.succes('Merci, votre signalement a été transmis aux administrateurs.');
        }
      });
  }
}

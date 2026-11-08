import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ActionTraitement, libelleMotif, nomAuteur, Signalement } from '../../avis.modeles';
import { STYLES_COMMUNS } from '../../avis.styles';
import { STYLES_ADMIN } from '../admin.styles';

/** Contenu du panneau « Détails du signalement » (le titre de section est dans la page). */
@Component({
  selector: 'avis-detail-signalement',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe],
  template: `
    <div class="panneau">
      <p class="ligne">
        <span class="motif"
          ><span class="material-icons" aria-hidden="true">flag</span> {{ libelleMotif(signalement().motif) }}</span
        >
        @if (signalement().brique?.statut === 'MASQUEE') {
          <span class="pastille-masquee"
            ><span class="material-icons" aria-hidden="true">circle</span> Masquée</span
          >
        }
      </p>

      <p class="brique"><strong>Brique :</strong> {{ titreBrique() }}</p>
      @if (signalement().brique; as b) {
        <p class="info"><strong>Auteur :</strong> {{ nomAuteur(b.auteur) }}</p>
      }
      <p class="info"><strong>Signalé par :</strong> {{ nomAuteur(signalement().auteur) }}</p>
      <p class="info">
        <strong>Date :</strong>
        <time [attr.datetime]="signalement().dateCreation">{{ signalement().dateCreation | date: 'd MMMM y' }}</time>
      </p>
      <div class="description">
        <strong>Description :</strong>
        <p class="texte">{{ signalement().details || 'Aucune description' }}</p>
      </div>

      @if (signalement().brique; as b) {
        <a class="btn btn-primaire lien-btn consulter" [routerLink]="['/briques', b.id]">Consulter la brique</a>
      }

      <div class="actions">
        <button
          type="button"
          class="btn btn-accent"
          [disabled]="enCours() !== null"
          (click)="conserver.emit()"
        >
          {{ enCours() === 'CONSERVER' ? 'Traitement en cours…' : 'Conserver la brique' }}
        </button>
        <button
          type="button"
          class="btn btn-destructif"
          [disabled]="enCours() !== null"
          (click)="masquer.emit()"
        >
          {{ enCours() === 'MASQUER_BRIQUE' ? 'Masquage en cours…' : 'Masquer la brique' }}
        </button>
      </div>
    </div>
  `,
  styles: [
    STYLES_COMMUNS,
    STYLES_ADMIN,
    `
      .panneau {
        display: grid; gap: 0.75rem; padding: 1.25rem; border: 2px dashed var(--db-primaire);
        border-radius: 1rem; background: #fff;
      }
      .ligne { margin: 0; display: flex; flex-wrap: wrap; gap: 0.5rem; }
      .brique { margin: 0; font-size: 1.25rem; overflow-wrap: anywhere; }
      .info { margin: 0; font-size: 1rem; overflow-wrap: anywhere; }
      .description { display: grid; gap: 0.25rem; }
      .texte { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; }
      .consulter { justify-self: start; }
      .actions { justify-content: flex-end; }
      @media (max-width: 480px) { .consulter { justify-self: stretch; } }
    `,
  ],
})
export class DetailSignalementComponent {
  readonly signalement = input.required<Signalement>();
  /** Action en cours d'envoi : désactive les deux boutons et change le libellé du bouton cliqué. */
  readonly enCours = input<ActionTraitement | null>(null);
  readonly conserver = output<void>();
  readonly masquer = output<void>();

  protected readonly libelleMotif = libelleMotif;
  protected readonly nomAuteur = nomAuteur;
  protected readonly titreBrique = computed(() => this.signalement().brique?.titre ?? 'Brique indisponible');
}

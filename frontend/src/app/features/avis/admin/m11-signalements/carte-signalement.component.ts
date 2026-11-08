import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { libelleMotif, nomAuteur, Signalement } from '../../avis.modeles';
import { STYLES_COMMUNS } from '../../avis.styles';
import { STYLES_ADMIN } from '../admin.styles';

/**
 * Carte compacte d'un signalement (la maquette montre une carte de brique complète, mais l'API
 * ne renvoie que brique.{id, titre, statut}). La carte choisie est entourée ET annoncée.
 */
@Component({
  selector: 'avis-carte-signalement',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
  template: `
    <article class="carte" [class.selectionnee]="selectionnee()">
      <h3 class="titre">{{ titreBrique() }}</h3>
      <p class="ligne">
        <span class="motif"
          ><span class="material-icons" aria-hidden="true">flag</span> {{ libelleMotif(signalement().motif) }}</span
        >
      </p>
      <p class="meta">Signalé par {{ nomAuteur(signalement().auteur) }}</p>
      <p class="meta">
        <time [attr.datetime]="signalement().dateCreation">{{ signalement().dateCreation | date: 'd MMMM y' }}</time>
      </p>
      <div class="pied">
        @if (selectionnee()) {
          <span class="etat-selection"
            ><span class="material-icons" aria-hidden="true">check_circle</span> Sélectionné</span
          >
        }
        <button
          type="button"
          class="btn btn-primaire"
          [attr.aria-current]="selectionnee() ? 'true' : null"
          [attr.aria-label]="'Examiner le signalement de la brique ' + titreBrique()"
          (click)="examiner.emit(signalement().id)"
        >
          Examiner
        </button>
      </div>
    </article>
  `,
  styles: [
    STYLES_COMMUNS,
    STYLES_ADMIN,
    `
      .carte {
        display: grid; gap: 0.5rem; align-content: start; height: 100%; box-sizing: border-box;
        padding: 1rem; border: 3px solid transparent; border-radius: 1rem; background: #fff;
      }
      .carte.selectionnee { border-color: var(--db-primaire); }
      .titre { margin: 0; font-size: 1.25rem; font-weight: 600; overflow-wrap: anywhere; }
      .ligne, .meta { margin: 0; }
      .meta { font-size: 0.875rem; }
      .pied { display: flex; flex-wrap: wrap; align-items: center; justify-content: flex-end; gap: 0.75rem; margin-top: 0.25rem; }
      .etat-selection { margin-right: auto; font-size: 0.875rem; font-weight: 600; color: var(--db-primaire); }
    `,
  ],
})
export class CarteSignalementComponent {
  readonly signalement = input.required<Signalement>();
  readonly selectionnee = input(false);
  /** Émet l'identifiant du signalement à examiner. */
  readonly examiner = output<number>();

  protected readonly libelleMotif = libelleMotif;
  protected readonly nomAuteur = nomAuteur;
  protected readonly titreBrique = computed(() => this.signalement().brique?.titre ?? 'Brique indisponible');
}

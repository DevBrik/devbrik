import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  BriqueResumeAdmin,
  formaterNote,
  libelleLicence,
  nomAuteur,
  pastilleTest,
} from '../../avis.modeles';
import { STYLES_COMMUNS } from '../../avis.styles';
import { STYLES_ADMIN } from '../admin.styles';

/**
 * Gabarit local de carte de brique avec les actions Voir et Republier,
 * à remplacer par db-brique-carte quand il sera fusionné dans main.
 */
@Component({
  selector: 'avis-carte-brique-masquee',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe],
  template: `
    <article class="carte">
      <div class="entete">
        <span class="langage">
          <img
            [src]="'/langages/' + brique().langage.slug + '.svg'"
            alt=""
            width="24"
            height="24"
            aria-hidden="true"
            (error)="cacher($event)"
          />
          <span class="langage-nom">{{ brique().langage.nom }}</span>
        </span>
        <span class="pastille" [class.ok]="test().classe === 'ok'" [class.ko]="test().classe === 'ko'" [class.pastille-neutre]="test().classe === 'neutre'">
          <span class="material-icons" aria-hidden="true">{{ test().icone }}</span> {{ test().libelle }}
        </span>
      </div>
      <p class="ligne">
        <span class="categorie">{{ brique().categorie.nom }}</span>
        <span class="pastille-masquee"><span class="material-icons" aria-hidden="true">circle</span> Masquée</span>
      </p>
      <h3 class="titre">{{ brique().titre }}</h3>
      <p class="resume">{{ brique().resume }}</p>
      <p class="meta">{{ brique().categorie.nom }} · {{ licence() }}</p>
      <p class="meta">
        {{ nomAuteur(brique().auteur) }} ·
        @if (brique().noteMoyenne !== null) {
          <span aria-hidden="true">★</span> {{ formaterNote(brique().noteMoyenne ?? 0) }} sur 5 ({{ brique().nombreAvis }} avis)
        } @else {
          Pas encore d’avis
        }
      </p>
      <p class="meta">
        Version {{ brique().versionCode }} · Créée le
        <time [attr.datetime]="brique().dateCreation">{{ brique().dateCreation | date: 'd MMMM y' }}</time>
      </p>
      <div class="actions">
        <a
          class="btn btn-primaire lien-btn"
          [routerLink]="['/briques', brique().id]"
          [attr.aria-label]="'Voir la brique ' + brique().titre"
          >Voir</a
        >
        <button
          type="button"
          class="btn btn-accent"
          [disabled]="enCours()"
          [attr.aria-label]="'Republier la brique ' + brique().titre"
          (click)="republier.emit()"
        >
          {{ enCours() ? 'Republication en cours…' : 'Republier' }}
        </button>
      </div>
    </article>
  `,
  styles: [
    STYLES_COMMUNS,
    STYLES_ADMIN,
    `
      .carte { display: grid; gap: 0.5rem; align-content: start; height: 100%; box-sizing: border-box; padding: 1rem; border-radius: 1rem; background: #fff; }
      .entete { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 0.5rem; }
      .langage { display: inline-flex; align-items: center; gap: 0.5rem; font-weight: 600; }
      .langage-nom { text-transform: uppercase; }
      .ligne { margin: 0; display: flex; flex-wrap: wrap; gap: 0.5rem; }
      .titre { margin: 0; font-size: 1.25rem; font-weight: 600; overflow-wrap: anywhere; }
      .resume { margin: 0; overflow-wrap: anywhere; }
      .meta { margin: 0; font-size: 0.875rem; }
      .actions { justify-content: flex-end; margin-top: 0.25rem; }
    `,
  ],
})
export class CarteBriqueMasqueeComponent {
  readonly brique = input.required<BriqueResumeAdmin>();
  readonly enCours = input(false);
  readonly republier = output<void>();

  protected readonly nomAuteur = nomAuteur;
  protected readonly formaterNote = formaterNote;
  protected readonly test = computed(() => pastilleTest(this.brique()));
  protected readonly licence = computed(() => libelleLicence(this.brique().licence));

  /** Icône absente (SQL, Shell, fichier manquant) : le nom du langage reste affiché. */
  protected cacher(evenement: Event): void {
    (evenement.target as HTMLElement).hidden = true;
  }
}

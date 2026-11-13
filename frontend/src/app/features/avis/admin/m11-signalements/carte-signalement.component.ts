import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { libelleMotif, nomAuteur, Signalement } from '../../avis.modeles';

/**
 * Carte compacte d'un signalement (la maquette montre une carte de brique complète, mais l'API
 * ne renvoie que brique.{id, titre, statut}). La carte choisie est entourée ET annoncée.
 */
@Component({
  selector: 'avis-carte-signalement',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
  templateUrl: './carte-signalement.component.html',
  styleUrl: './carte-signalement.component.scss',
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

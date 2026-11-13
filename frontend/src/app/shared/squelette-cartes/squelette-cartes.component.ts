import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Squelettes aux formes de la carte de brique (charte 8, chargement). */
@Component({
  selector: 'db-squelette-cartes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './squelette-cartes.component.html',
  styleUrl: './squelette-cartes.component.scss',
})
export class SqueletteCartesComponent {
  readonly nombre = input(6);
  readonly libelle = input('Chargement en cours…');
  protected readonly elements = computed(() => Array.from({ length: this.nombre() }, (_, i) => i));
}

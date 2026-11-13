import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Tuile de chiffre (charte 6.8) : valeur orange en grand, libellé en 16 px. */
@Component({
  selector: 'db-tuile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tuile.component.html',
  styleUrl: './tuile.component.scss',
})
export class TuileComponent {
  readonly valeur = input.required<string | number>();
  readonly libelle = input.required<string>();
  readonly detail = input<string | null>(null);
  /** Valeur au-dessus du libellé (par défaut) ou libellé au-dessus (statistiques). */
  readonly libelleEnHaut = input(false);
  readonly active = input(false);
}

import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Panneau décoratif de droite des pages de connexion et d'inscription (masqué sous 900 px). */
@Component({
  selector: 'db-panneau-auth',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './panneau-auth.component.html',
  styleUrl: './panneau-auth.component.scss',
})
export class PanneauAuthComponent {
  readonly message = input.required<string>();
}

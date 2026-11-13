import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Monogramme DB + mot-symbole « DevBrik » en texte réel (charte 10). */
@Component({
  selector: 'db-logo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './logo.component.html',
  styleUrl: './logo.component.scss',
})
export class LogoComponent {
  /** Côté du monogramme en pixels (40 dans l'en-tête). */
  readonly taille = input(40);
  readonly avecTexte = input(true);
  /** Texte blanc (pied de page sur fond encre). */
  readonly clair = input(false);
}

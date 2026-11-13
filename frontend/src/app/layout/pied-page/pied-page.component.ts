import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LogoComponent } from '../../shared/logo/logo.component';

/** Pied de page (charte 6.1). Les liens réservés passent par les gardes (connexion puis retour). */
@Component({
  selector: 'db-pied-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, LogoComponent],
  templateUrl: './pied-page.component.html',
  styleUrl: './pied-page.component.scss',
})
export class PiedPageComponent {
  protected readonly annee = new Date().getFullYear();
}

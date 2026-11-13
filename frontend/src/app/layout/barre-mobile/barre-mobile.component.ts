import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { MenuCompteComponent } from '../menu-compte/menu-compte.component';

/** Barre de navigation basse (charte 7, 360 px) : Briques, Demandes, Classement, Compte. */
@Component({
  selector: 'db-barre-mobile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, MatMenuModule, MenuCompteComponent],
  templateUrl: './barre-mobile.component.html',
  styleUrl: './barre-mobile.component.scss',
})
export class BarreMobileComponent {
  protected readonly auth = inject(AuthService);
}

import { ChangeDetectionStrategy, Component, inject, input, viewChild } from '@angular/core';
import { MatMenu, MatMenuModule } from '@angular/material/menu';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../shared/notification/notification.service';

/** Menu du compte (charte 6.1) : Mon profil, Mes briques, Mon compte, Administration, Se déconnecter. */
@Component({
  selector: 'db-menu-compte',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatMenuModule, RouterLink],
  exportAs: 'menuCompte',
  templateUrl: './menu-compte.component.html',
  styleUrl: './menu-compte.component.scss',
})
export class MenuCompteComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly yPosition = input<'above' | 'below'>('below');
  readonly menu = viewChild.required<MatMenu>('menu');

  protected deconnecter(): void {
    this.auth.deconnexion();
    this.notifications.succes('Vous êtes déconnecté.');
    void this.router.navigateByUrl('/briques');
  }
}

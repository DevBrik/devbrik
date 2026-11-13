import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { initiales, nomAuteur } from '../../core/libelles';
import { LogoComponent } from '../../shared/logo/logo.component';
import { MenuCompteComponent } from '../menu-compte/menu-compte.component';

/** En-tête (charte 6.1) : logo, navigation Briques · Demandes · Classement, connexion ou menu du compte. */
@Component({
  selector: 'db-entete',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, MatMenuModule, LogoComponent, MenuCompteComponent],
  templateUrl: './entete.component.html',
  styleUrl: './entete.component.scss',
})
export class EnteteComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly nom = computed(() => {
    const u = this.auth.utilisateur();
    return u ? nomAuteur(u) : '';
  });
  protected readonly initiales = computed(() => initiales(this.nom()));

  /** Connexion avec retour sur la page courante. */
  protected connexion(): void {
    const url = this.router.url;
    const retour = url.startsWith('/compte/') ? undefined : url;
    void this.router.navigate(['/compte/connexion'], { queryParams: retour ? { retour } : {} });
  }
}

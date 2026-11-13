import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatMenuModule } from '@angular/material/menu';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { MenuCompteComponent } from '../menu-compte/menu-compte.component';

type Entree = 'briques' | 'demandes' | 'classement' | 'profil' | null;

/**
 * Barre de navigation basse (360 px), maquette « Navigation mobile » :
 * Briques, Demandes, bouton central « + », Classement, Profil.
 * L'entrée active est orange avec son libellé ; les autres n'affichent que l'icône
 * (leur libellé reste lu par les lecteurs d'écran). Le « + » publie une brique,
 * ou crée une demande depuis les pages Demandes.
 */
@Component({
  selector: 'db-barre-mobile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, MatMenuModule, MenuCompteComponent],
  templateUrl: './barre-mobile.component.html',
  styleUrl: './barre-mobile.component.scss',
})
export class BarreMobileComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  protected readonly active = computed<Entree>(() => {
    const url = this.url().split('?')[0];
    const moi = this.auth.utilisateur()?.id;
    if (url.startsWith('/compte') || url === '/briques/mes-briques' || (moi && url === `/developpeurs/${moi}`)) {
      return 'profil';
    }
    if (url.startsWith('/briques') && url !== '/briques/nouvelle') {
      return 'briques';
    }
    if (url.startsWith('/demandes') && url !== '/demandes/nouvelle') {
      return 'demandes';
    }
    if (url.startsWith('/developpeurs')) {
      return 'classement';
    }
    return null;
  });

  /** Action du bouton central selon la section. */
  protected readonly ajout = computed(() =>
    this.url().startsWith('/demandes')
      ? { lien: '/demandes/nouvelle', libelle: 'Nouvelle demande' }
      : { lien: '/briques/nouvelle', libelle: 'Publier une brique' },
  );
}

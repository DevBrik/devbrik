import { LiveAnnouncer } from '@angular/cdk/a11y';
import { Component, DestroyRef, ElementRef, inject, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { BarreMobileComponent } from './layout/barre-mobile/barre-mobile.component';
import { EnteteComponent } from './layout/entete/entete.component';
import { PiedPageComponent } from './layout/pied-page/pied-page.component';

/** Coquille de l'application (dossier 12.6) : lien d'évitement, en-tête, contenu, pied de page, barre mobile. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, EnteteComponent, PiedPageComponent, BarreMobileComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  private readonly titre = inject(Title);
  private readonly annonceur = inject(LiveAnnouncer);
  private readonly contenu = viewChild<ElementRef<HTMLElement>>('contenu');
  private premiere = true;

  constructor() {
    // Changement de route : annonce du titre aux lecteurs d'écran et focus sur le contenu.
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe(() => {
        if (this.premiere) {
          this.premiere = false;
          return;
        }
        setTimeout(() => {
          void this.annonceur.announce(this.titre.getTitle(), 'polite');
          this.contenu()?.nativeElement.focus({ preventScroll: true });
          window.scrollTo({ top: 0 });
        });
      });
  }
}

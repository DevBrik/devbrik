import { ChangeDetectionStrategy, Component, DestroyRef, inject, input, signal } from '@angular/core';

interface Diapositive {
  /** Suite de la phrase après « Connectez-vous » ou « Inscrivez-vous ». */
  suite: string;
  /** Aperçu flouté de la page correspondante (captures de l'application, public/assets/apercus). */
  image: string;
}

const DIAPOSITIVES: Diapositive[] = [
  { suite: 'et commencez à publier vos briques', image: '/assets/apercus/publier.webp' },
  { suite: 'et réutilisez les briques de la communauté', image: '/assets/apercus/briques.webp' },
  { suite: 'et testez les briques de la communauté', image: '/assets/apercus/tester.webp' },
  { suite: 'et demandez la brique qui vous manque', image: '/assets/apercus/demandes.webp' },
  { suite: 'et grimpez dans le classement des contributeurs', image: '/assets/apercus/classement.webp' },
];

const INTERVALLE_MS = 5000;

/**
 * Panneau de droite de la connexion et de l'inscription (maquette M04) : aperçu flouté de la page
 * qui correspond à la phrase, les phrases défilant toutes les 5 secondes.
 * Défilement coupé si l'utilisateur a demandé moins d'animations ; bouton Pause (WCAG 2.2.2).
 * Masqué sous 900 px (charte 7).
 */
@Component({
  selector: 'db-panneau-auth',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './panneau-auth.component.html',
  styleUrl: './panneau-auth.component.scss',
})
export class PanneauAuthComponent {
  /** Début de la phrase : « Connectez-vous » ou « Inscrivez-vous ». */
  readonly verbe = input.required<string>();

  protected readonly diapositives = DIAPOSITIVES;
  protected readonly index = signal(0);
  protected readonly enPause = signal(false);
  protected readonly mouvementReduit =
    typeof window !== 'undefined' && !!window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
  private minuteur?: ReturnType<typeof setInterval>;

  constructor() {
    if (!this.mouvementReduit) {
      this.demarrer();
    }
    inject(DestroyRef).onDestroy(() => this.arreter());
  }

  protected basculer(): void {
    if (this.enPause()) {
      this.enPause.set(false);
      this.demarrer();
    } else {
      this.enPause.set(true);
      this.arreter();
    }
  }

  private demarrer(): void {
    this.arreter();
    this.minuteur = setInterval(() => this.index.update((i) => (i + 1) % DIAPOSITIVES.length), INTERVALLE_MS);
  }

  private arreter(): void {
    clearInterval(this.minuteur);
  }
}

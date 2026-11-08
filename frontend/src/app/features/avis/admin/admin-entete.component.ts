import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

/** Titre « Administration » et les deux grands onglets (M11 et M12). */
@Component({
  selector: 'avis-admin-entete',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header>
      <h1>Administration</h1>
      <nav class="onglets" aria-label="Sections de l’administration">
        <a
          class="onglet"
          routerLink="/admin/signalements"
          routerLinkActive="actif"
          ariaCurrentWhenActive="page"
        >
          Signalements
        </a>
        <a
          class="onglet"
          routerLink="/admin/statistiques"
          routerLinkActive="actif"
          ariaCurrentWhenActive="page"
        >
          Statistiques
        </a>
      </nav>
    </header>
  `,
  styles: [
    `
      :host { display: block; margin-bottom: 1.5rem; }
      h1 { margin: 0 0 1rem; font-size: 2rem; font-weight: 700; text-align: center; }
      .onglets { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; }
      .onglet {
        display: flex; align-items: center; justify-content: center; min-height: 56px; padding: 0 1rem;
        border: 2px solid var(--db-primaire); border-radius: 0.75rem; text-decoration: none;
        font-size: 1.25rem; font-weight: 600; color: var(--db-primaire); background: transparent;
      }
      .onglet.actif { color: #fff; background: var(--db-primaire); }
      :focus-visible { outline: 3px solid var(--db-focus); outline-offset: 2px; }
      @media (max-width: 480px) { .onglet { font-size: 1rem; } }
    `,
  ],
})
export class AdminEnteteComponent {}

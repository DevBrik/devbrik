import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ErreurApi, lireErreur } from '../../avis.erreurs';
import { Statistiques } from '../../avis.modeles';
import { AvisService } from '../../avis.service';
import { SocleAvis } from '../../avis.socle';
import { STYLES_COMMUNS } from '../../avis.styles';
import { AdminEnteteComponent } from '../admin-entete.component';
import { STYLES_ADMIN } from '../admin.styles';
import { tuilesStatistiques } from './statistiques.tuiles';

/**
 * M12 : Administration, statistiques (/admin/statistiques, administrateur).
 * Six tuiles ; pas de graphique (l'API ne fournit pas de série).
 */
@Component({
  selector: 'avis-statistiques',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AdminEnteteComponent],
  template: `
    <div class="conteneur">
      <avis-admin-entete />

      <section aria-labelledby="titre-tableau">
        <h2 id="titre-tableau">Tableau de bord</h2>
        <p class="sous-titre">Vue globale de l'activité de la plateforme.</p>

        @if (chargement()) {
          <div class="tuiles squelette-tuiles" aria-hidden="true">
            @for (i of [1, 2, 3, 4, 5, 6]; track i) {
              <span></span>
            }
          </div>
          <p class="sr-only" role="status">Chargement des statistiques…</p>
        } @else if (erreur(); as e) {
          <div class="etat" role="alert">
            <p class="erreur">{{ e.message }}</p>
            @if (e.statut !== 403 && e.statut !== 404) {
              <button type="button" class="btn btn-primaire" (click)="charger()">Réessayer</button>
            }
          </div>
        } @else {
          <ul class="tuiles">
            @for (t of tuiles(); track t.cle) {
              <li class="tuile">
                <h3 class="libelle">{{ t.libelle }}</h3>
                <p class="valeur">{{ t.valeur.toLocaleString('fr-FR') }}</p>
                @if (t.detail) {
                  <p class="detail">{{ t.detail }}</p>
                }
              </li>
            }
          </ul>
        }
      </section>
    </div>
  `,
  styles: [
    STYLES_COMMUNS,
    STYLES_ADMIN,
    `
      .conteneur { max-width: 1200px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
      h2 { margin: 0; font-size: 1.5rem; font-weight: 700; }
      .sous-titre { margin: 0.25rem 0 1.5rem; color: var(--db-primaire); font-size: 1.125rem; }
      .tuiles {
        list-style: none; margin: 0; padding: 0; display: grid; gap: 1rem;
        grid-template-columns: repeat(3, minmax(0, 1fr));
      }
      /* Mobile (12.8) : les statistiques restent sur deux colonnes. */
      @media (max-width: 640px) { .tuiles { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.75rem; } }
      .tuile { display: grid; gap: 0.25rem; justify-items: center; text-align: center; padding: 1.25rem 0.75rem; border-radius: 1rem; background: #fff; }
      .libelle { margin: 0; font-size: 1.25rem; font-weight: 600; overflow-wrap: anywhere; }
      .valeur { margin: 0; font-size: 2.25rem; font-weight: 700; line-height: 1.2; color: var(--db-orange-texte); }
      .detail { margin: 0; font-size: 0.875rem; }
      .squelette-tuiles span { display: block; height: 7rem; border-radius: 1rem; background: #e2e8f0; animation: pulsation 1.4s ease-in-out infinite; }
      @media (prefers-reduced-motion: reduce) { .squelette-tuiles span { animation: none; } }
    `,
  ],
})
export class StatistiquesComponent {
  private readonly service = inject(AvisService);
  private readonly socle = inject(SocleAvis);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly statistiques = signal<Statistiques | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal<ErreurApi | null>(null);
  protected readonly tuiles = computed(() => {
    const s = this.statistiques();
    return s ? tuilesStatistiques(s) : [];
  });

  private sequence = 0;

  constructor() {
    this.charger();
  }

  protected charger(): void {
    const courant = ++this.sequence;
    this.chargement.set(true);
    this.erreur.set(null);
    this.service
      .statistiques()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultat) => {
          if (courant !== this.sequence) {
            return;
          }
          this.statistiques.set(resultat);
          this.chargement.set(false);
        },
        error: (e: unknown) => {
          if (courant !== this.sequence) {
            return;
          }
          const erreur = lireErreur(e);
          if (erreur.code === 'NON_AUTHENTIFIE') {
            this.socle.versConnexion();
          } else {
            this.erreur.set(erreur);
          }
          this.chargement.set(false);
        },
      });
  }
}

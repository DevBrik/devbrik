import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ErreurApi, lireErreur } from '../../avis.erreurs';
import { Statistiques } from '../../avis.modeles';
import { AvisService } from '../../avis.service';
import { SocleAvis } from '../../avis.socle';
import { AdminEnteteComponent } from '../admin-entete.component';
import { tuilesStatistiques } from './statistiques.tuiles';

/**
 * M12 : Administration, statistiques (/admin/statistiques, administrateur).
 * Six tuiles ; pas de graphique (l'API ne fournit pas de série).
 */
@Component({
  selector: 'avis-statistiques',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AdminEnteteComponent],
  templateUrl: './statistiques.component.html',
  styleUrl: './statistiques.component.scss',
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

import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription, forkJoin, map, of, switchMap } from 'rxjs';
import { ClassementEntree, PageReponse } from '../../../core/api/modeles';
import { nomAuteur } from '../../../core/libelles';
import { EtatPageComponent } from '../../../shared/etat-page/etat-page.component';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { TuileComponent } from '../../../shared/tuile/tuile.component';
import { BriquesService } from '../../briques/briques.service';
import { DemandesService } from '../demandes.service';

const TAILLE = 20;
/** Taille maximale d'une page de l'API (11.1), utilisée pour additionner les points. */
const TAILLE_MAX = 50;

interface Synthese {
  contributeurs: number;
  briquesPubliees: number;
  pointsCumules: number;
}

/** Classement des développeurs (/developpeurs/classement, P2), trié par réputation décroissante. */
@Component({
  selector: 'db-classement',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TuileComponent, PaginationComponent, EtatPageComponent],
  templateUrl: './classement.component.html',
  styleUrl: './classement.component.scss',
})
export class ClassementComponent {
  private readonly service = inject(DemandesService);
  private readonly briques = inject(BriquesService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly resultat = signal<PageReponse<ClassementEntree> | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal(false);
  protected readonly synthese = signal<Synthese | null>(null);
  protected readonly nom = nomAuteur;
  private page = 0;
  private appel?: Subscription;

  constructor() {
    this.chargerSynthese();
    this.route.queryParamMap.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe((p) => {
      const page = Number(p.get('page') ?? '0');
      this.page = Number.isInteger(page) && page >= 0 ? page : 0;
      this.charger();
    });
  }

  protected charger(): void {
    this.appel?.unsubscribe();
    this.chargement.set(true);
    this.erreur.set(false);
    this.appel = this.service.classement(this.page, TAILLE).subscribe({
      next: (r) => {
        this.resultat.set(r);
        this.chargement.set(false);
      },
      error: () => {
        this.chargement.set(false);
        this.erreur.set(true);
      },
    });
  }

  /**
   * Tuiles (maquette M15) : contributeurs (taille du classement), briques publiées (GET /api/briques)
   * et points cumulés (somme des réputations, en parcourant le classement par pages de 50).
   */
  private chargerSynthese(): void {
    const points$ = this.service.classement(0, TAILLE_MAX).pipe(
      switchMap((premiere) => {
        const suivantes = Array.from({ length: Math.max(0, premiere.totalPages - 1) }, (_, i) =>
          this.service.classement(i + 1, TAILLE_MAX),
        );
        return (suivantes.length ? forkJoin(suivantes) : of([])).pipe(map((autres) => [premiere, ...autres]));
      }),
      map((pages) => ({
        contributeurs: pages[0].totalElements,
        points: pages.flatMap((p) => p.contenu).reduce((somme, e) => somme + e.reputation, 0),
      })),
    );
    forkJoin({ points: points$, briques: this.briques.lister({ taille: 1 }) }).subscribe({
      next: (r) =>
        this.synthese.set({
          contributeurs: r.points.contributeurs,
          briquesPubliees: r.briques.totalElements,
          pointsCumules: r.points.points,
        }),
      error: () => this.synthese.set(null),
    });
  }

  /** Rang = page × taille + position + 1 (dossier 12.7). */
  protected rang(r: PageReponse<ClassementEntree>, index: number): number {
    return r.page * r.taille + index + 1;
  }

  protected medaille(rang: number): string {
    return rang === 1 ? 'or' : rang === 2 ? 'argent' : rang === 3 ? 'bronze' : '';
  }

  protected libelleMedaille(rang: number): string {
    return rang === 1 ? ' (médaille d’or)' : rang === 2 ? ' (médaille d’argent)' : rang === 3 ? ' (médaille de bronze)' : '';
  }

  protected changerPage(page: number): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: { page: page > 0 ? page : null } });
  }
}

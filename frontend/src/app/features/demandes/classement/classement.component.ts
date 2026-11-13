import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ClassementEntree, PageReponse } from '../../../core/api/modeles';
import { nomAuteur } from '../../../core/libelles';
import { EtatPageComponent } from '../../../shared/etat-page/etat-page.component';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { TuileComponent } from '../../../shared/tuile/tuile.component';
import { DemandesService } from '../demandes.service';

const TAILLE = 20;

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
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly resultat = signal<PageReponse<ClassementEntree> | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal(false);
  protected readonly nom = nomAuteur;
  private page = 0;
  private appel?: Subscription;

  constructor() {
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

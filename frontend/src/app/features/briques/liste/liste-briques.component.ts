import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { lireErreur } from '../../../core/api/erreurs-formulaire';
import { BriqueResume, PageReponse } from '../../../core/api/modeles';
import { BarreFiltresComponent, Filtres, FILTRES_VIDES } from '../../../shared/barre-filtres/barre-filtres.component';
import { BriqueCarteComponent } from '../../../shared/brique-carte/brique-carte.component';
import { EtatPageComponent } from '../../../shared/etat-page/etat-page.component';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { SqueletteCartesComponent } from '../../../shared/squelette-cartes/squelette-cartes.component';
import { BriquesService } from '../briques.service';
import { lireFiltres, versAdresse, versApi } from '../filtres-adresse';

/** M01 : accueil et liste des briques (/briques). Filtres et page conservés dans l'adresse. */
@Component({
  selector: 'db-liste-briques',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    BarreFiltresComponent,
    BriqueCarteComponent,
    PaginationComponent,
    EtatPageComponent,
    SqueletteCartesComponent,
  ],
  templateUrl: './liste-briques.component.html',
  styleUrl: './liste-briques.component.scss',
})
export class ListeBriquesComponent {
  private readonly service = inject(BriquesService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly filtres = signal<Filtres>(FILTRES_VIDES);
  protected readonly page = signal(0);
  protected readonly resultat = signal<PageReponse<BriqueResume> | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal(false);
  private appel?: Subscription;

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe((p) => {
      const { filtres, page } = lireFiltres(p);
      this.filtres.set(filtres);
      this.page.set(page);
      this.charger();
    });
  }

  protected charger(): void {
    this.appel?.unsubscribe();
    this.chargement.set(true);
    this.erreur.set(false);
    this.appel = this.service.lister(versApi(this.filtres(), this.page())).subscribe({
      next: (r) => {
        this.resultat.set(r);
        this.chargement.set(false);
      },
      error: (e: unknown) => {
        this.chargement.set(false);
        this.erreur.set(lireErreur(e).statut !== 400);
        if (lireErreur(e).statut === 400) {
          this.resultat.set({ contenu: [], page: 0, taille: 20, totalElements: 0, totalPages: 0 });
        }
      },
    });
  }

  protected changerFiltres(f: Filtres): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: versAdresse(f, 0) });
  }

  protected changerPage(page: number): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: versAdresse(this.filtres(), page) });
  }

  protected filtresActifs(): boolean {
    const f = this.filtres();
    return !!(f.q || f.categorie || f.langage);
  }

  protected reinitialiser(): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: {} });
  }
}

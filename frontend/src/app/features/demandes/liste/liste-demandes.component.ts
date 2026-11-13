import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, inject, signal, viewChildren } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { lireErreur } from '../../../core/api/erreurs-formulaire';
import { DemandeResume, PageReponse, StatutDemande } from '../../../core/api/modeles';
import { BarreFiltresComponent, Filtres, FILTRES_VIDES } from '../../../shared/barre-filtres/barre-filtres.component';
import { EtatPageComponent } from '../../../shared/etat-page/etat-page.component';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { DemandeCarteComponent } from '../demande-carte/demande-carte.component';
import { DemandesService } from '../demandes.service';

const ONGLETS: { statut: StatutDemande; libelle: string }[] = [
  { statut: 'OUVERTE', libelle: 'Demandes ouvertes' },
  { statut: 'RESOLUE', libelle: 'Demandes résolues' },
];

/** M06 : liste des demandes (/demandes). Onglets Ouvertes / Résolues, recherche, filtres, pas de tri. */
@Component({
  selector: 'db-liste-demandes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, BarreFiltresComponent, EtatPageComponent, PaginationComponent, DemandeCarteComponent],
  templateUrl: './liste-demandes.component.html',
  styleUrl: './liste-demandes.component.scss',
})
export class ListeDemandesComponent {
  private readonly service = inject(DemandesService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly onglets = viewChildren<ElementRef<HTMLButtonElement>>('onglet');

  protected readonly listeOnglets = ONGLETS;
  protected readonly statut = signal<StatutDemande>('OUVERTE');
  protected readonly filtres = signal<Filtres>(FILTRES_VIDES);
  protected readonly page = signal(0);
  protected readonly resultat = signal<PageReponse<DemandeResume> | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal(false);
  private appel?: Subscription;

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe((p) => {
      const page = Number(p.get('page') ?? '0');
      this.statut.set(p.get('statut') === 'RESOLUE' ? 'RESOLUE' : 'OUVERTE');
      this.filtres.set({ q: p.get('q') ?? '', categorie: p.get('categorie') ?? '', langage: p.get('langage') ?? '', tri: '' });
      this.page.set(Number.isInteger(page) && page >= 0 ? page : 0);
      this.charger();
    });
  }

  protected charger(): void {
    this.appel?.unsubscribe();
    this.chargement.set(true);
    this.erreur.set(false);
    const f = this.filtres();
    const q = f.q.trim();
    this.appel = this.service
      .lister({
        statut: this.statut(),
        q: q.length >= 2 ? q : undefined,
        categorie: f.categorie || undefined,
        langage: f.langage || undefined,
        page: this.page(),
        taille: 20,
      })
      .subscribe({
        next: (r) => {
          this.resultat.set(r);
          this.chargement.set(false);
        },
        error: (e: unknown) => {
          this.chargement.set(false);
          if (lireErreur(e).statut === 400) {
            this.resultat.set({ contenu: [], page: 0, taille: 20, totalElements: 0, totalPages: 0 });
          } else {
            this.erreur.set(true);
          }
        },
      });
  }

  private naviguer(statut: StatutDemande, f: Filtres, page: number): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        statut: statut === 'RESOLUE' ? 'RESOLUE' : null,
        q: f.q || null,
        categorie: f.categorie || null,
        langage: f.langage || null,
        page: page > 0 ? page : null,
      },
    });
  }

  protected choisirOnglet(statut: StatutDemande): void {
    if (statut !== this.statut()) {
      this.naviguer(statut, this.filtres(), 0);
    }
  }

  /** Flèches gauche/droite, Début, Fin : navigation entre onglets (role="tablist"). */
  protected clavierOnglets(e: KeyboardEvent, index: number): void {
    const n = ONGLETS.length;
    let cible: number | null = null;
    if (e.key === 'ArrowRight') cible = (index + 1) % n;
    if (e.key === 'ArrowLeft') cible = (index - 1 + n) % n;
    if (e.key === 'Home') cible = 0;
    if (e.key === 'End') cible = n - 1;
    if (cible === null) {
      return;
    }
    e.preventDefault();
    this.onglets()[cible]?.nativeElement.focus();
    this.choisirOnglet(ONGLETS[cible].statut);
  }

  protected changerFiltres(f: Filtres): void {
    this.naviguer(this.statut(), f, 0);
  }

  protected changerPage(page: number): void {
    this.naviguer(this.statut(), this.filtres(), page);
  }

  protected filtresActifs(): boolean {
    const f = this.filtres();
    return !!(f.q || f.categorie || f.langage);
  }
}

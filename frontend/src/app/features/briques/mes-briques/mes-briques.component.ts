import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription, forkJoin } from 'rxjs';
import { lireErreur } from '../../../core/api/erreurs-formulaire';
import { BriqueResume, PageReponse, StatutBrique } from '../../../core/api/modeles';
import { BarreFiltresComponent, Filtres, FILTRES_VIDES } from '../../../shared/barre-filtres/barre-filtres.component';
import { BriqueCarteComponent } from '../../../shared/brique-carte/brique-carte.component';
import { DialogueService } from '../../../shared/dialogue-confirmation/dialogue.service';
import { EtatPageComponent } from '../../../shared/etat-page/etat-page.component';
import { NotificationService } from '../../../shared/notification/notification.service';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { SqueletteCartesComponent } from '../../../shared/squelette-cartes/squelette-cartes.component';
import { TuileComponent } from '../../../shared/tuile/tuile.component';
import { BriquesService } from '../briques.service';
import { lireFiltres, versAdresse, versApi } from '../filtres-adresse';

interface Compteurs {
  total: number;
  publiees: number;
  masquees: number;
}

/** M05 : mes briques (/briques/mes-briques), y compris masquées. Actions visibles en permanence. */
@Component({
  selector: 'db-mes-briques',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    BarreFiltresComponent,
    BriqueCarteComponent,
    PaginationComponent,
    EtatPageComponent,
    SqueletteCartesComponent,
    TuileComponent,
  ],
  templateUrl: './mes-briques.component.html',
  styleUrl: './mes-briques.component.scss',
})
export class MesBriquesComponent {
  private readonly service = inject(BriquesService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialogues = inject(DialogueService);
  private readonly notifications = inject(NotificationService);

  protected readonly filtres = signal<Filtres>(FILTRES_VIDES);
  protected readonly statut = signal<StatutBrique | null>(null);
  protected readonly page = signal(0);
  protected readonly resultat = signal<PageReponse<BriqueResume> | null>(null);
  protected readonly compteurs = signal<Compteurs | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal(false);
  private appel?: Subscription;

  constructor() {
    this.chargerCompteurs();
    this.route.queryParamMap.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe((p) => {
      const { filtres, page } = lireFiltres(p);
      const statut = p.get('statut');
      this.filtres.set(filtres);
      this.page.set(page);
      this.statut.set(statut === 'PUBLIEE' || statut === 'MASQUEE' ? statut : null);
      this.charger();
    });
  }

  /** Tuiles : GET /api/moi/briques avec taille=1 et statut absent, PUBLIEE, MASQUEE (on lit totalElements). */
  private chargerCompteurs(): void {
    forkJoin({
      total: this.service.mesBriques({ taille: 1 }),
      publiees: this.service.mesBriques({ taille: 1, statut: 'PUBLIEE' }),
      masquees: this.service.mesBriques({ taille: 1, statut: 'MASQUEE' }),
    }).subscribe({
      next: (r) =>
        this.compteurs.set({
          total: r.total.totalElements,
          publiees: r.publiees.totalElements,
          masquees: r.masquees.totalElements,
        }),
      error: () => this.compteurs.set(null),
    });
  }

  protected charger(): void {
    this.appel?.unsubscribe();
    this.chargement.set(true);
    this.erreur.set(false);
    const filtres = { ...versApi(this.filtres(), this.page()), statut: this.statut() ?? undefined };
    this.appel = this.service.mesBriques(filtres).subscribe({
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

  private naviguer(filtres: Filtres, page: number, statut: StatutBrique | null): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: versAdresse(filtres, page, { statut }) });
  }

  protected changerFiltres(f: Filtres): void {
    this.naviguer(f, 0, this.statut());
  }

  protected changerPage(page: number): void {
    this.naviguer(this.filtres(), page, this.statut());
  }

  protected filtrerStatut(statut: StatutBrique | null): void {
    this.naviguer(this.filtres(), 0, statut);
  }

  protected rechercheActive(): boolean {
    const f = this.filtres();
    return !!(f.q || f.categorie || f.langage || this.statut());
  }

  protected modifier(b: BriqueResume): void {
    void this.router.navigate(['/briques', b.id, 'modifier']);
  }

  protected supprimer(b: BriqueResume): void {
    this.dialogues
      .confirmer({
        titre: 'Supprimer cette brique ?',
        message: `« ${b.titre} » sera supprimée avec ses avis, ses tests et ses signalements. Cette action est irréversible.`,
        libelleConfirmer: 'Supprimer',
      })
      .subscribe((ok) => {
        if (!ok) {
          return;
        }
        this.service.supprimer(b.id).subscribe({
          next: () => {
            this.notifications.succes('Brique supprimée');
            this.chargerCompteurs();
            const r = this.resultat();
            // Dernière carte d'une page : on revient à la page précédente.
            if (r && r.contenu.length === 1 && this.page() > 0) {
              this.changerPage(this.page() - 1);
            } else {
              this.charger();
            }
          },
          error: (e: unknown) => this.notifications.erreur(lireErreur(e).message),
        });
      });
  }
}

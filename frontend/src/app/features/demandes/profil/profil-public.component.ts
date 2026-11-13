import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { lireErreur } from '../../../core/api/erreurs-formulaire';
import { BriqueResume, PageReponse, ProfilPublic } from '../../../core/api/modeles';
import { AuthService } from '../../../core/auth/auth.service';
import { initiales, lienSur } from '../../../core/libelles';
import { BarreFiltresComponent, Filtres, FILTRES_VIDES } from '../../../shared/barre-filtres/barre-filtres.component';
import { BriqueCarteComponent } from '../../../shared/brique-carte/brique-carte.component';
import { EtatPageComponent, TypeEtat } from '../../../shared/etat-page/etat-page.component';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { SqueletteCartesComponent } from '../../../shared/squelette-cartes/squelette-cartes.component';
import { TuileComponent } from '../../../shared/tuile/tuile.component';
import { BriquesService } from '../../briques/briques.service';
import { lireFiltres, versAdresse, versApi } from '../../briques/filtres-adresse';
import { DemandesService } from '../demandes.service';

/** M09 : profil public d'un développeur (/developpeurs/:id), réputation détaillée (RG14), ses briques. */
@Component({
  selector: 'db-profil-public',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DatePipe,
    RouterLink,
    TuileComponent,
    BarreFiltresComponent,
    BriqueCarteComponent,
    PaginationComponent,
    EtatPageComponent,
    SqueletteCartesComponent,
  ],
  templateUrl: './profil-public.component.html',
  styleUrl: './profil-public.component.scss',
})
export class ProfilPublicComponent {
  private readonly service = inject(DemandesService);
  private readonly briques = inject(BriquesService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly titre = inject(Title);

  readonly id = input.required<string>();

  protected readonly profil = signal<ProfilPublic | null>(null);
  protected readonly etat = signal<TypeEtat | null>('chargement');
  protected readonly filtres = signal<Filtres>(FILTRES_VIDES);
  protected readonly page = signal(0);
  protected readonly liste = signal<PageReponse<BriqueResume> | null>(null);
  protected readonly chargementListe = signal(true);
  protected readonly erreurListe = signal(false);
  private appelListe?: Subscription;

  protected readonly nom = computed(() => {
    const p = this.profil();
    return p ? p.nomAffiche?.trim() || p.pseudo : '';
  });
  protected readonly initiales = computed(() => initiales(this.nom()));
  protected readonly lien = computed(() => lienSur(this.profil()?.lienProfil));
  protected readonly estMoi = computed(() => this.auth.utilisateur()?.id === this.profil()?.id);

  constructor() {
    effect(() => {
      const id = Number(this.id());
      untracked(() => this.chargerProfil(id));
    });
    // Les paramètres d'adresse arrivent avant l'entrée « id » : la liste est chargée dans un effet.
    const parametres = toSignal(this.route.queryParamMap);
    effect(() => {
      const p = parametres();
      this.id();
      if (!p) {
        return;
      }
      const { filtres, page } = lireFiltres(p);
      untracked(() => {
        this.filtres.set(filtres);
        this.page.set(page);
        this.chargerListe();
      });
    });
  }

  protected chargerProfil(id: number): void {
    if (!Number.isInteger(id) || id <= 0) {
      this.etat.set('404');
      return;
    }
    this.etat.set('chargement');
    this.service.profil(id).subscribe({
      next: (p) => {
        this.profil.set(p);
        this.etat.set(null);
        this.titre.setTitle(`${p.nomAffiche?.trim() || p.pseudo} – DevBrik`);
      },
      error: (e: unknown) => {
        const s = lireErreur(e).statut;
        this.etat.set(s === 404 || s === 400 ? '404' : 'erreur');
      },
    });
  }

  protected chargerListe(): void {
    const id = Number(this.id());
    if (!Number.isInteger(id) || id <= 0) {
      return;
    }
    this.appelListe?.unsubscribe();
    this.chargementListe.set(true);
    this.erreurListe.set(false);
    this.appelListe = this.briques.lister({ ...versApi(this.filtres(), this.page()), auteur: id }).subscribe({
      next: (r) => {
        this.liste.set(r);
        this.chargementListe.set(false);
      },
      error: () => {
        this.chargementListe.set(false);
        this.erreurListe.set(true);
      },
    });
  }

  protected recharger(): void {
    this.chargerProfil(Number(this.id()));
    this.chargerListe();
  }

  protected changerFiltres(f: Filtres): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: versAdresse(f, 0) });
  }

  protected changerPage(page: number): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: versAdresse(this.filtres(), page) });
  }
}

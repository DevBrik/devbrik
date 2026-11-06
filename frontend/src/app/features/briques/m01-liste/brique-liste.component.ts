import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';

import { BriqueService } from '../services/brique.service';
import { BriqueResumeDto, CategorieDto, LangageDto } from '../models/brique.model';
import { PageReponse } from '../../../core/api/modeles';
import { EtatPage } from '../../../shared/etat-page/etat-page';

/**
 * M01 — Liste publique des briques avec filtres catégorie/langage et pagination.
 */
@Component({
  selector: 'app-brique-liste',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule, MatCardModule, MatChipsModule, MatFormFieldModule,
    MatIconModule, MatPaginatorModule, MatSelectModule,
    EtatPage,
  ],
  templateUrl: './brique-liste.component.html',
  styleUrl: './brique-liste.component.scss',
})
export class BriqueListeComponent implements OnInit {

  private readonly briqueService = inject(BriqueService);
  private readonly router        = inject(Router);

  readonly chargement  = signal(false);
  readonly erreur      = signal<string | null>(null);
  readonly page        = signal<PageReponse<BriqueResumeDto> | null>(null);
  readonly categories  = signal<CategorieDto[]>([]);
  readonly langages    = signal<LangageDto[]>([]);

  categorieId: number | null = null;
  langageId: number | null = null;
  pageIndex  = 0;
  pageTaille = 20;

  get briques() { return this.page()?.contenu ?? []; }
  get total()   { return this.page()?.totalElements ?? 0; }
  get modeEtat() {
    if (this.chargement()) return 'chargement' as const;
    if (this.erreur())     return 'erreur' as const;
    if (this.briques.length === 0 && this.page() !== null) return 'vide' as const;
    return null;
  }

  ngOnInit(): void {
    this.chargerReferentiels();
    this.charger();
  }

  private chargerReferentiels(): void {
    this.briqueService.categories().subscribe(cats => this.categories.set(cats));
    this.briqueService.langages().subscribe(langs => this.langages.set(langs));
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.briqueService.listerPubliees({
      categorieId: this.categorieId,
      langageId: this.langageId,
      page: this.pageIndex,
      taille: this.pageTaille,
    }).subscribe({
      next: p => { this.page.set(p); this.chargement.set(false); },
      error: () => { this.erreur.set('Impossible de charger les briques.'); this.chargement.set(false); },
    });
  }

  filtrer(): void {
    this.pageIndex = 0;
    this.charger();
  }

  reinitialiserFiltres(): void {
    this.categorieId = null;
    this.langageId = null;
    this.filtrer();
  }

  surChangementPage(e: PageEvent): void {
    this.pageIndex  = e.pageIndex;
    this.pageTaille = e.pageSize;
    this.charger();
  }

  voirBrique(id: number): void {
    this.router.navigate(['/briques', id]);
  }

  libelleLicence(v: string): string {
    const m: Record<string, string> = {
      MIT:'MIT', APACHE_2_0:'Apache 2.0', BSD_2_CLAUSE:'BSD-2',
      BSD_3_CLAUSE:'BSD-3', ISC:'ISC', CC0_1_0:'CC0',
    };
    return m[v] ?? v;
  }
}

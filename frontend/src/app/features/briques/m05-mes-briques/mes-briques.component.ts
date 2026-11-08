import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { BriqueService } from '../services/brique.service';
import { BriqueResumeInterne } from '../models/brique.model';
import { PageReponse } from '../../../core/api/modeles';
import { EtatPage } from '../../../shared/etat-page/etat-page';
import { DialogueService } from '../../../shared/dialogue/dialogue-confirmation';
import { messageErreur } from '../../../core/api/erreur';

/**
 * M05 — Mes briques : liste personnelle avec suppression confirmée.
 * RG19 : vérification auteur/admin côté serveur.
 * Utilise DialogueService (pas window.confirm).
 */
@Component({
  selector: 'app-mes-briques',
  standalone: true,
  imports: [
    MatButtonModule, MatCardModule, MatChipsModule, MatIconModule,
    MatPaginatorModule, MatProgressSpinnerModule, MatSnackBarModule,
    MatTooltipModule,
    EtatPage,
  ],
  templateUrl: './mes-briques.component.html',
  styleUrl: './mes-briques.component.scss',
})
export class MesBriquesComponent implements OnInit {

  private readonly briqueService = inject(BriqueService);
  private readonly router        = inject(Router);
  private readonly snack         = inject(MatSnackBar);
  private readonly dialogue      = inject(DialogueService);

  readonly chargement    = signal(false);
  readonly erreur        = signal<string | null>(null);
  readonly page          = signal<PageReponse<BriqueResumeInterne> | null>(null);
  readonly suppressionId = signal<number | null>(null);

  readonly briques       = computed(() => this.page()?.contenu ?? []);
  readonly totalElements = computed(() => this.page()?.totalElements ?? 0);

  pageIndex  = 0;
  pageTaille = 20;

  get modeEtat() {
    if (this.chargement()) return 'chargement' as const;
    if (this.erreur())     return 'erreur' as const;
    if (!this.chargement() && !this.erreur() && this.briques().length === 0 && this.page()) return 'vide' as const;
    return null;
  }

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.briqueService.mesBriques(this.pageIndex, this.pageTaille).subscribe({
      next: p  => { this.page.set(p); this.chargement.set(false); },
      error: () => { this.erreur.set('Impossible de charger vos briques.'); this.chargement.set(false); },
    });
  }

  surChangementPage(e: PageEvent): void {
    this.pageIndex  = e.pageIndex;
    this.pageTaille = e.pageSize;
    this.charger();
  }

  voirBrique(id: number): void   { this.router.navigate(['/briques', id]); }
  modifierBrique(id: number): void { this.router.navigate(['/briques', id, 'modifier']); }
  publierNouvelle(): void          { this.router.navigate(['/briques/publier']); }

  supprimerBrique(b: BriqueResumeInterne): void {
    this.dialogue.confirmer({
      titre: 'Supprimer la brique',
      message: `Supprimer définitivement « ${b.titre} » ? Cette action est irréversible.`,
      labelConfirmer: 'Supprimer',
      couleurConfirmer: 'warn',
    }).subscribe(confirme => {
      if (!confirme) return;
      this.suppressionId.set(b.id);

      this.briqueService.supprimer(b.id).subscribe({
        next: () => {
          this.suppressionId.set(null);
          this.snack.open(`Brique « ${b.titre} » supprimée.`, 'Fermer', { duration: 4000 });
          if (this.briques().length === 1 && this.pageIndex > 0) this.pageIndex--;
          this.charger();
        },
        error: err => {
          this.suppressionId.set(null);
          this.snack.open(messageErreur(err), 'Fermer', { duration: 5000 });
        },
      });
    });
  }

  libelleLicence(v: string): string {
    const m: Record<string, string> = {
      MIT:'MIT', APACHE_2_0:'Apache 2.0', BSD_2_CLAUSE:'BSD-2',
      BSD_3_CLAUSE:'BSD-3', ISC:'ISC', CC0_1_0:'CC0',
    };
    return m[v] ?? v;
  }

  formaterDate(iso: string): string {
    return new Intl.DateTimeFormat('fr-SN', {
      day: 'numeric', month: 'short', year: 'numeric',
    }).format(new Date(iso));
  }

  enSuppression(id: number): boolean { return this.suppressionId() === id; }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { BriqueService } from '../services/brique.service';
import { BriqueDetailDto } from '../models/brique.model';
import { EtatPage } from '../../../shared/etat-page/etat-page';
import { AuthService } from '../../../core/auth/auth.service';

/**
 * M02 — Fiche publique d'une brique.
 * RG18 : le code est affiché en texte brut (pas innerHTML).
 */
@Component({
  selector: 'app-brique-fiche',
  standalone: true,
  imports: [
    RouterLink,
    MatButtonModule, MatChipsModule, MatDividerModule,
    MatIconModule, MatSnackBarModule,
    EtatPage,
  ],
  templateUrl: './brique-fiche.component.html',
  styleUrl: './brique-fiche.component.scss',
})
export class BriqueFicheComponent implements OnInit {

  private readonly route         = inject(ActivatedRoute);
  private readonly router        = inject(Router);
  private readonly briqueService = inject(BriqueService);
  private readonly snack         = inject(MatSnackBar);
  readonly auth                  = inject(AuthService);

  readonly chargement = signal(true);
  readonly erreur     = signal<string | null>(null);
  readonly brique     = signal<BriqueDetailDto | null>(null);

  get modeEtat() {
    if (this.chargement()) return 'chargement' as const;
    if (this.erreur())     return 'erreur' as const;
    return null;
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.briqueService.obtenir(id).subscribe({
      next: b => { this.brique.set(b); this.chargement.set(false); },
      error: err => {
        this.chargement.set(false);
        this.erreur.set(err.status === 404 ? 'introuvable' : 'erreur');
      },
    });
  }

  copierCode(): void {
    const code = this.brique()?.code;
    if (!code) return;
    navigator.clipboard.writeText(code).then(() =>
      this.snack.open('Code copié !', 'Fermer', { duration: 2500 })
    );
  }

  modifier(): void {
    this.router.navigate(['/briques', this.brique()?.id, 'modifier']);
  }

  retour(): void {
    this.router.navigate(['/briques']);
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
      day: 'numeric', month: 'long', year: 'numeric',
    }).format(new Date(iso));
  }

  estAuteur(): boolean {
    const u = this.auth.utilisateur();
    return !!u && u.id === this.brique()?.auteur?.id;
  }
}

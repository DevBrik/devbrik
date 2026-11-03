import { Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';

export type ModeEtat = 'chargement' | 'vide' | 'erreur' | 'introuvable' | 'acces-refuse';

const DEFAUTS: Record<ModeEtat, { titre: string; message: string }> = {
  chargement: { titre: 'Chargement en cours', message: '' },
  vide: { titre: 'Rien à afficher pour le moment', message: '' },
  erreur: { titre: 'Une erreur est survenue', message: 'Vérifiez votre connexion puis réessayez.' },
  introuvable: { titre: 'Page introuvable', message: "Cette page n'existe pas ou n'est plus disponible." },
  'acces-refuse': { titre: 'Accès refusé', message: "Vous n'avez pas le droit d'accéder à cette page." },
};

@Component({
  selector: 'app-etat-page',
  imports: [MatButtonModule, MatProgressSpinnerModule, RouterLink],
  template: `
    <section class="etat" [attr.role]="mode() === 'erreur' ? 'alert' : 'status'">
      @if (mode() === 'chargement') {
        <mat-spinner diameter="40" aria-label="Chargement en cours" />
      }
      <h2>{{ titreAffiche() }}</h2>
      @if (messageAffiche()) {
        <p>{{ messageAffiche() }}</p>
      }
      @if (mode() === 'erreur') {
        <button mat-flat-button type="button" (click)="reessayer.emit()">Réessayer</button>
      }
      @if (actionLien() && actionLibelle()) {
        <a mat-stroked-button [routerLink]="actionLien()">{{ actionLibelle() }}</a>
      }
    </section>
  `,
  styles: `
    .etat {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 48px 16px;
      text-align: center;
    }
    h2 { margin: 0; font: var(--mat-sys-headline-small); }
    p { margin: 0; max-width: 48ch; }
  `,
})
export class EtatPage {
  readonly mode = input.required<ModeEtat>();
  readonly titre = input<string>();
  readonly message = input<string>();
  readonly actionLibelle = input<string>();
  readonly actionLien = input<string>();
  readonly reessayer = output<void>();

  protected readonly titreAffiche = computed(() => this.titre() ?? DEFAUTS[this.mode()].titre);
  protected readonly messageAffiche = computed(() => this.message() ?? DEFAUTS[this.mode()].message);
}
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export type TypePastille =
  | 'test-ok'
  | 'test-ko'
  | 'avis-partages'
  | 'non-testee'
  | 'publiee'
  | 'masquee'
  | 'ouverte'
  | 'acceptee'
  | 'resolue'
  | 'categorie'
  | 'motif';

const DEFAUTS: Record<TypePastille, { libelle: string; icone: string | null }> = {
  'test-ok': { libelle: 'Testé OK', icone: 'check' },
  'test-ko': { libelle: 'Testé KO', icone: 'priority_high' },
  'avis-partages': { libelle: 'Avis partagés', icone: 'balance' },
  'non-testee': { libelle: 'Non testée', icone: 'help_outline' },
  publiee: { libelle: 'Publiée', icone: 'radio_button_checked' },
  masquee: { libelle: 'Masquée', icone: 'circle' },
  ouverte: { libelle: 'Ouverte', icone: 'radio_button_unchecked' },
  acceptee: { libelle: 'Acceptée', icone: 'check' },
  resolue: { libelle: 'Résolue', icone: 'check' },
  categorie: { libelle: '', icone: null },
  motif: { libelle: '', icone: 'flag' },
};

/** Pastille de statut (charte 6.4) : libellé + icône + couleur, jamais la couleur seule. */
@Component({
  selector: 'db-pastille',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pastille.component.html',
  styleUrl: './pastille.component.scss',
})
export class PastilleComponent {
  readonly type = input.required<TypePastille>();
  /** Remplace le libellé par défaut (obligatoire pour « categorie » et « motif »). */
  readonly libelle = input<string>();

  protected readonly texte = computed(() => this.libelle() ?? DEFAUTS[this.type()].libelle);
  protected readonly icone = computed(() => DEFAUTS[this.type()].icone);
}

import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { ElementReference } from '../../core/api/modeles';

/** Slugs disposant d'une icône Devicon locale (public/langages, licence MIT). */
const AVEC_ICONE = new Set(['java', 'typescript', 'javascript', 'python', 'php', 'csharp', 'kotlin', 'dart', 'go']);
/** SQL et Shell n'ont pas de logo : icônes Material (charte 6.6 ; « database » est absente de la police locale, « storage » la remplace). */
const ICONES_MATERIAL: Record<string, string> = { sql: 'storage', shell: 'terminal' };

/** Icône du langage (décorative) + nom en texte : l'icône ne porte jamais l'information seule. */
@Component({
  selector: 'db-langage-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './langage-badge.component.html',
  styleUrl: './langage-badge.component.scss',
})
export class LangageBadgeComponent {
  readonly langage = input.required<ElementReference>();

  protected readonly iconeEchouee = signal(false);
  protected readonly image = computed(() =>
    AVEC_ICONE.has(this.langage().slug) && !this.iconeEchouee() ? `/langages/${this.langage().slug}.svg` : null,
  );
  protected readonly iconeMaterial = computed(() => ICONES_MATERIAL[this.langage().slug] ?? null);
}

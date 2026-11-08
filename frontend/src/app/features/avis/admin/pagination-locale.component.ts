import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { STYLES_COMMUNS } from '../avis.styles';

/**
 * Gabarit local, à remplacer par db-pagination quand il sera fusionné dans main.
 * « Précédent », « Page 1 sur 10 », « Suivant » (la forme mobile de la charte 6.10).
 */
@Component({
  selector: 'avis-pagination',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (totalPages() > 1) {
      <nav class="pagination" [attr.aria-label]="libelle()">
        <button type="button" class="btn btn-primaire" [disabled]="page() === 0" (click)="pageChange.emit(page() - 1)">
          <span aria-hidden="true">←</span> Précédent
        </button>
        <span class="position">Page {{ page() + 1 }} sur {{ totalPages() }}</span>
        <button
          type="button"
          class="btn btn-primaire"
          [disabled]="page() + 1 >= totalPages()"
          (click)="pageChange.emit(page() + 1)"
        >
          Suivant <span aria-hidden="true">→</span>
        </button>
      </nav>
    }
  `,
  styles: [
    STYLES_COMMUNS,
    `
      :host { display: block; }
      .pagination { display: flex; flex-wrap: wrap; justify-content: center; align-items: center; gap: 1rem; margin: 1rem 0; }
      .position { font-size: 0.875rem; font-weight: 600; }
    `,
  ],
})
export class PaginationLocaleComponent {
  /** Page courante, à partir de 0 (contrat 11.1). */
  readonly page = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly libelle = input('Pagination');
  readonly pageChange = output<number>();
}

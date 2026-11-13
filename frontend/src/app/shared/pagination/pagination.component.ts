import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

/**
 * Pagination (charte 6.10) : « ← Précédent », numéros avec points de suspension, « Suivant → ».
 * Sur mobile : « Précédent », « Page 1 sur 10 », « Suivant ». Pages à partir de 0 (contrat 11.1).
 */
@Component({
  selector: 'db-pagination',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.scss',
})
export class PaginationComponent {
  readonly page = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly libelle = input('Pagination');
  readonly pageChange = output<number>();

  /** Numéros affichés (à partir de 0) ; null = points de suspension. */
  protected readonly numeros = computed<(number | null)[]>(() => {
    const total = this.totalPages();
    const courante = this.page();
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i);
    }
    const garder = new Set([0, total - 1, courante - 1, courante, courante + 1]);
    if (courante <= 3) {
      [1, 2, 3, 4].forEach((i) => garder.add(i));
    }
    if (courante >= total - 4) {
      [total - 5, total - 4, total - 3, total - 2].forEach((i) => garder.add(i));
    }
    const tries = [...garder].filter((i) => i >= 0 && i < total).sort((a, b) => a - b);
    const resultat: (number | null)[] = [];
    tries.forEach((n, index) => {
      if (index > 0 && n - tries[index - 1] > 1) {
        resultat.push(null);
      }
      resultat.push(n);
    });
    return resultat;
  });

  protected aller(page: number): void {
    if (page >= 0 && page < this.totalPages() && page !== this.page()) {
      this.pageChange.emit(page);
    }
  }
}

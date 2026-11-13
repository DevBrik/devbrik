import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export interface SectionInfo {
  titre: string;
  paragraphes: string[];
  liste?: string[];
}

/** Gabarit des pages statiques (M13) : carte blanche, titre, date, sections numérotées. */
@Component({
  selector: 'db-page-info',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
  templateUrl: './page-info.component.html',
  styleUrl: './page-info.component.scss',
})
export class PageInfoComponent {
  readonly titre = input.required<string>();
  readonly miseAJour = input<string | null>(null);
  readonly introduction = input<string | null>(null);
  readonly sections = input.required<SectionInfo[]>();
}

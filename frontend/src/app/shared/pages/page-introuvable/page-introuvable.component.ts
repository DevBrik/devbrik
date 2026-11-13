import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EtatPageComponent } from '../../etat-page/etat-page.component';

/** État transverse 404 (M14). */
@Component({
  selector: 'db-page-introuvable',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EtatPageComponent],
  templateUrl: './page-introuvable.component.html',
  styleUrl: './page-introuvable.component.scss',
})
export class PageIntrouvableComponent {}

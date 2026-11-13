import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EtatPageComponent } from '../../etat-page/etat-page.component';

/** État transverse 403 (M14). */
@Component({
  selector: 'db-acces-refuse',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EtatPageComponent],
  templateUrl: './acces-refuse.component.html',
  styleUrl: './acces-refuse.component.scss',
})
export class AccesRefuseComponent {}

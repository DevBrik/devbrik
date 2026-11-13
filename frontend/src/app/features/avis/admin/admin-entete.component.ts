import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

/** Titre « Administration » et les deux grands onglets (M11 et M12). */
@Component({
  selector: 'avis-admin-entete',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './admin-entete.component.html',
  styleUrl: './admin-entete.component.scss',
})
export class AdminEnteteComponent {}

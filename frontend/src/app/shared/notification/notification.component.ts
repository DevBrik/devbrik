import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';

export type TypeNotification = 'succes' | 'erreur' | 'chargement';

export interface DonneesNotification {
  type: TypeNotification;
  message: string;
}

/**
 * Contenu d'une notification (maquette « Publication en cours ») : fond blanc,
 * icône à gauche (coche, erreur ou anneau de chargement), texte à droite.
 * Le texte est toujours présent : la couleur ne porte jamais l'information seule.
 */
@Component({
  selector: 'db-notification',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.scss',
})
export class NotificationComponent {
  protected readonly donnees = inject<DonneesNotification>(MAT_SNACK_BAR_DATA);
}

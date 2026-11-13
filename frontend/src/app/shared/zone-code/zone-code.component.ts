import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { NotificationService } from '../notification/notification.service';

/**
 * Bloc de code en lecture (charte 6.7) : texte brut, numéros de ligne, « Copier le code ».
 * Le code n'est jamais interprété ni exécuté (RG18) : il passe par l'interpolation d'Angular.
 */
@Component({
  selector: 'db-zone-code',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './zone-code.component.html',
  styleUrl: './zone-code.component.scss',
})
export class ZoneCodeComponent {
  private readonly notifications = inject(NotificationService);

  readonly code = input.required<string>();
  protected readonly numeros = computed(() => this.code().split('\n').map((_, i) => i + 1).join('\n'));

  protected async copier(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.code());
      this.notifications.succes('Code copié');
    } catch {
      this.notifications.erreur('Copie impossible : sélectionnez le code puis copiez-le manuellement.');
    }
  }
}

import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { NotificationService } from '../notification/notification.service';

/**
 * Bloc de code en lecture (charte 6.7) : texte brut, numéros de ligne, « Copier le code ».
 * Après un clic, le bouton affiche « Code copié » pendant 2 secondes.
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
  protected readonly copie = signal(false);
  private minuteur?: ReturnType<typeof setTimeout>;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.minuteur));
  }

  protected async copier(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.code());
      this.copie.set(true);
      clearTimeout(this.minuteur);
      this.minuteur = setTimeout(() => this.copie.set(false), 2000);
    } catch {
      this.notifications.erreur('Copie impossible : sélectionnez le code puis copiez-le manuellement.');
    }
  }
}

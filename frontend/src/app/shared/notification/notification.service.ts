import { Injectable, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarRef } from '@angular/material/snack-bar';
import { DonneesNotification, NotificationComponent, TypeNotification } from './notification.component';

/** Notification « en cours » à fermer quand l'opération se termine. */
export interface NotificationEnCours {
  fermer(): void;
}

/**
 * Notifications (charte 6.11) : carte blanche en haut au centre, icône + texte,
 * annoncées aux lecteurs d'écran (polite ; assertive pour les erreurs).
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  succes(message: string): void {
    this.ouvrir('succes', message, 4000);
  }

  erreur(message: string): void {
    this.ouvrir('erreur', message, 8000);
  }

  /** Reste affichée jusqu'à fermer() (ou jusqu'à la notification suivante). */
  chargement(message: string): NotificationEnCours {
    const ref = this.ouvrir('chargement', message, undefined);
    return { fermer: () => ref.dismiss() };
  }

  private ouvrir(type: TypeNotification, message: string, duree: number | undefined): MatSnackBarRef<NotificationComponent> {
    return this.snackBar.openFromComponent<NotificationComponent, DonneesNotification>(NotificationComponent, {
      data: { type, message },
      duration: duree,
      verticalPosition: 'top',
      horizontalPosition: 'center',
      politeness: type === 'erreur' ? 'assertive' : 'polite',
      announcementMessage: message,
      panelClass: 'notification-db',
    });
  }
}

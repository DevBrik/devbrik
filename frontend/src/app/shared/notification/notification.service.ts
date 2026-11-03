import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  succes(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 4000, politeness: 'polite' });
  }

  erreur(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 8000,
      politeness: 'assertive',
      panelClass: 'notification-erreur',
    });
  }
}
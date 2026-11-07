import { computed, inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
// HYPOTHÈSES : chemins et formes du socle. À corriger ici seulement.
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../shared/notification/notification.service';

/** Identité, notifications et redirection vers la connexion, vues par le module avis. */
@Injectable({ providedIn: 'root' })
export class SocleAvis {
  private readonly auth = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);

  readonly idUtilisateur = computed<number | null>(() => this.auth.utilisateur()?.id ?? null);
  readonly connecte = computed(() => this.idUtilisateur() !== null);
  readonly estAdmin = computed(() => this.auth.utilisateur()?.role === 'ADMIN');

  succes(message: string): void {
    this.notification.succes(message);
  }

  erreur(message: string): void {
    this.notification.erreur(message);
  }

  /** Un visiteur qui clique sur une action réservée est renvoyé vers la connexion, puis revient ici. */
  versConnexion(): void {
    void this.router.navigate(['/compte/connexion'], { queryParams: { retour: this.router.url } });
  }
}

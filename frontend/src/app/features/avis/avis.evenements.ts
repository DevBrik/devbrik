import { Injectable, signal } from '@angular/core';

/** Signale qu'un avis ou un test a changé : la liste des avis et « mon avis / mon test » se rechargent. */
@Injectable({ providedIn: 'root' })
export class AvisEvenements {
  readonly version = signal(0);

  signaler(): void {
    this.version.update((n) => n + 1);
  }
}

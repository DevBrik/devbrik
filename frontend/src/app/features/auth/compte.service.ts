import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { CorpsProfil, Utilisateur } from '../../core/api/modeles';
import { AuthService } from '../../core/auth/auth.service';

/** Gestion du compte (11.3, membre 5). La connexion et l'inscription sont dans AuthService. */
@Injectable({ providedIn: 'root' })
export class CompteService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  moi(): Observable<Utilisateur> {
    return this.http.get<Utilisateur>('/api/moi').pipe(tap((u) => this.auth.mettreAJourUtilisateur(u)));
  }

  modifierProfil(corps: CorpsProfil): Observable<Utilisateur> {
    return this.http.put<Utilisateur>('/api/moi', corps).pipe(tap((u) => this.auth.mettreAJourUtilisateur(u)));
  }

  changerMotDePasse(ancienMotDePasse: string, nouveauMotDePasse: string): Observable<void> {
    return this.http.put<void>('/api/moi/mot-de-passe', { ancienMotDePasse, nouveauMotDePasse });
  }

  exporter(): Observable<unknown> {
    return this.http.get<unknown>('/api/moi/export');
  }

  supprimerCompte(motDePasse: string): Observable<void> {
    return this.http.delete<void>('/api/moi', { body: { motDePasse } });
  }
}

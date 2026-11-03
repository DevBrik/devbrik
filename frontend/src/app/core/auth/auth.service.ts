import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ReponseAuth, Utilisateur } from '../api/modeles';

const CLE_JETON = 'devbrik.jeton';
const CLE_UTILISATEUR = 'devbrik.utilisateur';

// Le stockage du navigateur peut être indisponible (navigation privée, etc.).
function lire(cle: string): string | null {
  try {
    return localStorage.getItem(cle);
  } catch {
    return null;
  }
}
function ecrire(cle: string, valeur: string | null): void {
  try {
    if (valeur === null) {
      localStorage.removeItem(cle);
    } else {
      localStorage.setItem(cle, valeur);
    }
  } catch {
    // Sans stockage, la session ne survit pas au rechargement : acceptable.
  }
}
function lireUtilisateur(): Utilisateur | null {
  const brut = lire(CLE_UTILISATEUR);
  if (!brut) {
    return null;
  }
  try {
    return JSON.parse(brut) as Utilisateur;
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  readonly jeton = signal<string | null>(lire(CLE_JETON));
  readonly utilisateur = signal<Utilisateur | null>(lireUtilisateur());

  readonly estConnecte = computed(() => this.jeton() !== null);
  // Affichage seulement : le serveur vérifie toujours les droits (RG19).
  readonly estAdmin = computed(() => this.utilisateur()?.role === 'ADMIN');

  connexion(pseudo: string, motDePasse: string): Observable<ReponseAuth> {
    return this.http
      .post<ReponseAuth>('/api/auth/connexion', { pseudo, motDePasse })
      .pipe(tap((reponse) => this.ouvrirSession(reponse)));
  }

  inscription(pseudo: string, motDePasse: string, nomAffiche?: string): Observable<ReponseAuth> {
    return this.http
      .post<ReponseAuth>('/api/auth/inscription', { pseudo, motDePasse, nomAffiche })
      .pipe(tap((reponse) => this.ouvrirSession(reponse)));
  }

  deconnexion(): void {
    this.jeton.set(null);
    this.utilisateur.set(null);
    ecrire(CLE_JETON, null);
    ecrire(CLE_UTILISATEUR, null);
  }

  /** À appeler après la modification du profil (membre 5). */
  mettreAJourUtilisateur(utilisateur: Utilisateur): void {
    this.utilisateur.set(utilisateur);
    ecrire(CLE_UTILISATEUR, JSON.stringify(utilisateur));
  }

  private ouvrirSession(reponse: ReponseAuth): void {
    this.jeton.set(reponse.jeton);
    this.utilisateur.set(reponse.utilisateur);
    ecrire(CLE_JETON, reponse.jeton);
    ecrire(CLE_UTILISATEUR, JSON.stringify(reponse.utilisateur));
  }
}
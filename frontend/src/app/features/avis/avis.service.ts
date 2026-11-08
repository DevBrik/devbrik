import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import { PageReponse } from '../../core/api/modeles';
import {
  ActionTraitement,
  Avis,
  BriqueResumeAdmin,
  CorpsAvis,
  CorpsSignalement,
  CorpsTest,
  Ecriture,
  MoiBrique,
  Signalement,
  Statistiques,
  StatutSignalement,
  Test,
} from './avis.modeles';

/**
 * Appels de la section 11.5 du dossier, plus les deux routes du membre 2 dont M11 a besoin
 * (briques masquées, republication). Le jeton est posé par l'intercepteur du socle.
 */
@Injectable({ providedIn: 'root' })
export class AvisService {
  private readonly http = inject(HttpClient);

  // ---------- avis ----------
  moi(idBrique: number): Observable<MoiBrique> {
    return this.http.get<MoiBrique>(`/api/briques/${idBrique}/moi`);
  }

  listerAvis(idBrique: number, page = 0, taille = 10): Observable<PageReponse<Avis>> {
    return this.http.get<PageReponse<Avis>>(`/api/briques/${idBrique}/avis`, { params: { page, taille } });
  }

  /** RG8 : un PUT crée (201) ou met à jour (200) l'avis de l'utilisateur connecté. */
  enregistrerAvis(idBrique: number, corps: CorpsAvis): Observable<Ecriture<Avis>> {
    return this.http
      .put<Avis>(`/api/briques/${idBrique}/avis`, corps, { observe: 'response' })
      .pipe(map((r) => ({ cree: r.status === 201, valeur: r.body as Avis })));
  }

  /** RG19 : auteur ou administrateur. */
  supprimerAvis(idAvis: number): Observable<void> {
    return this.http.delete<void>(`/api/avis/${idAvis}`);
  }

  // ---------- tests ----------
  /** RG9 : tests de la version actuelle seulement. */
  listerTests(idBrique: number, page = 0, taille = 10): Observable<PageReponse<Test>> {
    return this.http.get<PageReponse<Test>>(`/api/briques/${idBrique}/tests`, { params: { page, taille } });
  }

  enregistrerTest(idBrique: number, corps: CorpsTest): Observable<Ecriture<Test>> {
    return this.http
      .put<Test>(`/api/briques/${idBrique}/test`, corps, { observe: 'response' })
      .pipe(map((r) => ({ cree: r.status === 201, valeur: r.body as Test })));
  }

  /** Retire son propre test. */
  supprimerTest(idBrique: number): Observable<void> {
    return this.http.delete<void>(`/api/briques/${idBrique}/test`);
  }

  // ---------- signalements ----------
  signaler(idBrique: number, corps: CorpsSignalement): Observable<Signalement> {
    return this.http.post<Signalement>(`/api/briques/${idBrique}/signalements`, corps);
  }

  // ---------- administration ----------
  listerSignalements(statut: StatutSignalement = 'OUVERT', page = 0, taille = 20): Observable<PageReponse<Signalement>> {
    return this.http.get<PageReponse<Signalement>>('/api/admin/signalements', {
      params: { statut, page, taille },
    });
  }

  traiterSignalement(idSignalement: number, action: ActionTraitement): Observable<Signalement> {
    return this.http.put<Signalement>(`/api/admin/signalements/${idSignalement}/traitement`, { action });
  }

  statistiques(): Observable<Statistiques> {
    return this.http.get<Statistiques>('/api/admin/statistiques');
  }

  // ---------- routes du membre 2 utilisées par M11 (11.4) ----------
  listerBriquesMasquees(page = 0, taille = 20): Observable<PageReponse<BriqueResumeAdmin>> {
    return this.http.get<PageReponse<BriqueResumeAdmin>>('/api/admin/briques', {
      params: { statut: 'MASQUEE', page, taille },
    });
  }

  republierBrique(idBrique: number): Observable<unknown> {
    return this.http.put(`/api/admin/briques/${idBrique}/statut`, { statut: 'PUBLIEE' });
  }
}

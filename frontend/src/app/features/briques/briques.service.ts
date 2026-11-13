import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { parametres } from '../../core/api/erreurs-formulaire';
import {
  BriqueDetail,
  BriqueResume,
  CorpsBrique,
  FiltresBriques,
  PageReponse,
  StatutBrique,
} from '../../core/api/modeles';

/** Appels de la section 11.4 (module briques, membre 2). */
@Injectable({ providedIn: 'root' })
export class BriquesService {
  private readonly http = inject(HttpClient);

  lister(filtres: FiltresBriques): Observable<PageReponse<BriqueResume>> {
    return this.http.get<PageReponse<BriqueResume>>('/api/briques', { params: parametres(filtres) });
  }

  /** Mes briques, y compris masquées ; statut absent = toutes. */
  mesBriques(filtres: FiltresBriques): Observable<PageReponse<BriqueResume>> {
    return this.http.get<PageReponse<BriqueResume>>('/api/moi/briques', { params: parametres(filtres) });
  }

  detail(id: number): Observable<BriqueDetail> {
    return this.http.get<BriqueDetail>(`/api/briques/${id}`);
  }

  creer(corps: CorpsBrique): Observable<BriqueDetail> {
    return this.http.post<BriqueDetail>('/api/briques', corps);
  }

  modifier(id: number, corps: CorpsBrique): Observable<BriqueDetail> {
    return this.http.put<BriqueDetail>(`/api/briques/${id}`, corps);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`/api/briques/${id}`);
  }

  /** Administrateur : masquer ou republier (RG7). */
  changerStatut(id: number, statut: StatutBrique): Observable<BriqueDetail> {
    return this.http.put<BriqueDetail>(`/api/admin/briques/${id}/statut`, { statut });
  }
}

import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageReponse } from '../../../core/api/modeles';
import {
  BriqueResumeDto, BriqueDetailDto, BriqueResumeInterne,
  BriqueCreationPayload, BriqueModificationPayload,
  CategorieDto, LangageDto,
} from '../models/brique.model';

@Injectable({ providedIn: 'root' })
export class BriqueService {

  private readonly http = inject(HttpClient);

  // ── M01 — Liste publique ──────────────────────────────────────────────────

  listerPubliees(params: {
    categorieId?: number | null;
    langageId?: number | null;
    page?: number;
    taille?: number;
  }): Observable<PageReponse<BriqueResumeDto>> {
    let p = new HttpParams()
      .set('page',   String(params.page   ?? 0))
      .set('taille', String(params.taille ?? 20));
    if (params.categorieId) p = p.set('categorieId', String(params.categorieId));
    if (params.langageId)   p = p.set('langageId',   String(params.langageId));
    return this.http.get<PageReponse<BriqueResumeDto>>('/api/briques', { params: p });
  }

  // ── M02 — Fiche publique ──────────────────────────────────────────────────

  obtenir(id: number): Observable<BriqueDetailDto> {
    return this.http.get<BriqueDetailDto>(`/api/briques/${id}`);
  }

  // ── M03 — Publier / Modifier ──────────────────────────────────────────────

  publier(payload: BriqueCreationPayload): Observable<BriqueDetailDto> {
    return this.http.post<BriqueDetailDto>('/api/briques', payload);
  }

  modifier(id: number, payload: BriqueModificationPayload): Observable<BriqueDetailDto> {
    return this.http.put<BriqueDetailDto>(`/api/briques/${id}`, payload);
  }

  // ── M05 — Mes briques ─────────────────────────────────────────────────────

  mesBriques(page = 0, taille = 20): Observable<PageReponse<BriqueResumeInterne>> {
    const params = new HttpParams()
      .set('page',   String(page))
      .set('taille', String(taille));
    return this.http.get<PageReponse<BriqueResumeInterne>>('/api/moi/briques', { params });
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`/api/briques/${id}`);
  }

  // ── Référentiels ──────────────────────────────────────────────────────────

  categories(): Observable<CategorieDto[]> {
    return this.http.get<CategorieDto[]>('/api/categories');
  }

  langages(): Observable<LangageDto[]> {
    return this.http.get<LangageDto[]>('/api/langages');
  }
}

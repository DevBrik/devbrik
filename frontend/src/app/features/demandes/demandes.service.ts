import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { parametres } from '../../core/api/erreurs-formulaire';
import {
  ClassementEntree,
  DemandeDetail,
  DemandeResume,
  FiltresDemandes,
  PageReponse,
  ProfilPublic,
  Reponse,
} from '../../core/api/modeles';

/** Appels de la section 11.6 (demandes, réponses, développeurs ; membre 4). */
@Injectable({ providedIn: 'root' })
export class DemandesService {
  private readonly http = inject(HttpClient);

  lister(filtres: FiltresDemandes): Observable<PageReponse<DemandeResume>> {
    return this.http.get<PageReponse<DemandeResume>>('/api/demandes', { params: parametres(filtres) });
  }

  detail(id: number): Observable<DemandeDetail> {
    return this.http.get<DemandeDetail>(`/api/demandes/${id}`);
  }

  creer(corps: { titre: string; description: string; idCategorie: number | null; idLangage: number | null }): Observable<DemandeDetail> {
    return this.http.post<DemandeDetail>('/api/demandes', corps);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`/api/demandes/${id}`);
  }

  repondre(idDemande: number, corps: { contenu: string; idBrique: number | null }): Observable<Reponse> {
    return this.http.post<Reponse>(`/api/demandes/${idDemande}/reponses`, corps);
  }

  supprimerReponse(idReponse: number): Observable<void> {
    return this.http.delete<void>(`/api/reponses/${idReponse}`);
  }

  accepter(idReponse: number): Observable<DemandeDetail> {
    return this.http.put<DemandeDetail>(`/api/reponses/${idReponse}/acceptation`, {});
  }

  retirerAcceptation(idReponse: number): Observable<DemandeDetail> {
    return this.http.delete<DemandeDetail>(`/api/reponses/${idReponse}/acceptation`);
  }

  profil(id: number): Observable<ProfilPublic> {
    return this.http.get<ProfilPublic>(`/api/developpeurs/${id}`);
  }

  classement(page: number, taille = 20): Observable<PageReponse<ClassementEntree>> {
    return this.http.get<PageReponse<ClassementEntree>>('/api/developpeurs/classement', { params: { page, taille } });
  }
}

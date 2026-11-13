import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { Categorie, Langage } from './modeles';

/** Catégories et langages (11.4). Mis en cache : ils ne changent pas pendant une session. */
@Injectable({ providedIn: 'root' })
export class ReferentielService {
  private readonly http = inject(HttpClient);

  private categories$?: Observable<Categorie[]>;
  private langages$?: Observable<Langage[]>;

  categories(): Observable<Categorie[]> {
    this.categories$ ??= this.http.get<Categorie[]>('/api/categories').pipe(shareReplay({ bufferSize: 1, refCount: false }));
    return this.categories$;
  }

  langages(): Observable<Langage[]> {
    this.langages$ ??= this.http.get<Langage[]>('/api/langages').pipe(shareReplay({ bufferSize: 1, refCount: false }));
    return this.langages$;
  }
}

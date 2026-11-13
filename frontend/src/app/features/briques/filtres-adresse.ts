import { ParamMap } from '@angular/router';
import { FiltresBriques, TriBriques } from '../../core/api/modeles';
import { Filtres } from '../../shared/barre-filtres/barre-filtres.component';

const TRIS_VALIDES: TriBriques[] = ['recentes', 'mieux-notees', 'plus-testees'];

/** Lit les filtres conservés dans l'adresse (?q=&categorie=&langage=&tri=&page=). */
export function lireFiltres(p: ParamMap): { filtres: Filtres; page: number } {
  const tri = p.get('tri') as TriBriques | null;
  const page = Number(p.get('page') ?? '0');
  return {
    filtres: {
      q: p.get('q') ?? '',
      categorie: p.get('categorie') ?? '',
      langage: p.get('langage') ?? '',
      tri: tri && TRIS_VALIDES.includes(tri) ? tri : 'recentes',
    },
    page: Number.isInteger(page) && page >= 0 ? page : 0,
  };
}

/** Paramètres d'adresse : les valeurs par défaut sont omises pour garder une adresse lisible. */
export function versAdresse(f: Filtres, page: number, extra: Record<string, string | null> = {}): Record<string, string | null> {
  return {
    q: f.q || null,
    categorie: f.categorie || null,
    langage: f.langage || null,
    tri: f.tri && f.tri !== 'recentes' ? f.tri : null,
    page: page > 0 ? String(page) : null,
    ...extra,
  };
}

/** Paramètres de l'API (11.4). La recherche n'est envoyée que si elle fait 2 à 100 caractères (RG16). */
export function versApi(f: Filtres, page: number, taille = 20): FiltresBriques {
  const q = f.q.trim();
  return {
    q: q.length >= 2 && q.length <= 100 ? q : undefined,
    categorie: f.categorie || undefined,
    langage: f.langage || undefined,
    tri: (f.tri || 'recentes') as TriBriques,
    page,
    taille,
  };
}

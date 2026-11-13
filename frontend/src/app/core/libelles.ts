import { Licence, ReferenceUtilisateur } from './api/modeles';

/** Libellés et petites règles d'affichage partagés par tous les écrans. */

export const LICENCES: readonly { valeur: Licence; libelle: string }[] = [
  { valeur: 'MIT', libelle: 'MIT' },
  { valeur: 'APACHE_2_0', libelle: 'Apache 2.0' },
  { valeur: 'BSD_2_CLAUSE', libelle: 'BSD 2-Clause' },
  { valeur: 'BSD_3_CLAUSE', libelle: 'BSD 3-Clause' },
  { valeur: 'ISC', libelle: 'ISC' },
  { valeur: 'CC0_1_0', libelle: 'CC0 1.0' },
];

export function libelleLicence(licence: string): string {
  return LICENCES.find((l) => l.valeur === licence)?.libelle ?? licence;
}

export const TRIS = [
  { valeur: 'recentes', libelle: 'Plus récentes' },
  { valeur: 'mieux-notees', libelle: 'Mieux notées' },
  { valeur: 'plus-testees', libelle: 'Plus testées' },
] as const;

/** Virgule décimale : 4.8 devient « 4,8 ». */
export function formaterNote(note: number): string {
  return String(note).replace('.', ',');
}

/** Nom affiché, sinon pseudo ; « Utilisateur supprimé » si la référence manque. */
export function nomAuteur(auteur: ReferenceUtilisateur | null | undefined): string {
  if (!auteur) {
    return 'Utilisateur supprimé';
  }
  const nom = auteur.nomAffiche?.trim();
  return nom ? nom : auteur.pseudo;
}

/** Deux initiales en majuscules (« Awa Diop » : « AD », « moussa.dev » : « MO »). */
export function initiales(texte: string | null | undefined): string {
  const t = (texte ?? '').trim();
  if (!t) {
    return '?';
  }
  const mots = t.split(/[\s._-]+/).filter(Boolean);
  if (mots.length >= 2) {
    return (mots[0][0] + mots[1][0]).toUpperCase();
  }
  return t.slice(0, 2).toUpperCase();
}

export type ClassePastille = 'ok' | 'ko' | 'neutre';

/** Règle 6.4 de la charte : pastille « Testé » d'une brique (tests de la version actuelle, RG9). */
export function pastilleTest(brique: { testsOk: number; testsKo: number }): {
  libelle: string;
  classe: ClassePastille;
  icone: string;
} {
  if (brique.testsOk > brique.testsKo) {
    return { libelle: 'Testé OK', classe: 'ok', icone: 'check' };
  }
  if (brique.testsKo > brique.testsOk) {
    return { libelle: 'Testé KO', classe: 'ko', icone: 'priority_high' };
  }
  if (brique.testsOk > 0) {
    return { libelle: 'Avis partagés', classe: 'neutre', icone: 'balance' };
  }
  return { libelle: 'Non testée', classe: 'neutre', icone: 'help_outline' };
}

/** Coupe un texte aux sauts de ligne et retire les lignes vides (mode d'emploi, prérequis). */
export function lignes(texte: string | null | undefined): string[] {
  return (texte ?? '')
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l.length > 0);
}

/** Lien externe sûr : http ou https seulement (M09, M10). */
export function lienSur(adresse: string | null | undefined): string | null {
  if (!adresse) {
    return null;
  }
  try {
    const url = new URL(adresse);
    return url.protocol === 'http:' || url.protocol === 'https:' ? url.href : null;
  } catch {
    return null;
  }
}

/** Liste fixe de pays, Afrique en premier (M10). */
export const PAYS: readonly string[] = [
  'Sénégal', 'Afrique du Sud', 'Algérie', 'Bénin', 'Burkina Faso', 'Burundi', 'Cameroun', 'Cap-Vert',
  'Comores', 'Congo', "Côte d'Ivoire", 'Djibouti', 'Égypte', 'Éthiopie', 'Gabon', 'Gambie', 'Ghana',
  'Guinée', 'Guinée-Bissau', 'Guinée équatoriale', 'Kenya', 'Madagascar', 'Mali', 'Maroc', 'Maurice',
  'Mauritanie', 'Niger', 'Nigeria', 'Ouganda', 'République centrafricaine',
  'République démocratique du Congo', 'Rwanda', 'Seychelles', 'Tanzanie', 'Tchad', 'Togo', 'Tunisie',
  'Zambie', 'Zimbabwe', 'Belgique', 'Canada', 'France', 'Suisse', 'Autre',
];

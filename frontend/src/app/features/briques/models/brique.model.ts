// Modèles du module briques (M2).
// PageReponse<T> et ReferenceUtilisateur sont définis dans core/api/modeles.ts.

export type Licence =
  | 'MIT' | 'APACHE_2_0' | 'BSD_2_CLAUSE' | 'BSD_3_CLAUSE' | 'ISC' | 'CC0_1_0';

export type StatutBrique = 'PUBLIEE' | 'MASQUEE';

export interface CategorieDto {
  id: number;
  slug: string;
  nom: string;
  description: string | null;
}

export interface LangageDto {
  id: number;
  slug: string;
  nom: string;
}

export interface BriqueResumeDto {
  id: number;
  titre: string;
  resume: string;
  categorie: CategorieDto;
  langage: LangageDto;
  licence: Licence;
  versionCode: number;
  dateCreation: string;
  auteur: { id: number; pseudo: string; nomAffiche: string };
}

/** Fiche complète (M02) */
export interface BriqueDetailDto extends BriqueResumeDto {
  code: string;
  modeEmploi: string;
  prerequis: string | null;
  dateModification: string;
}

/** Mes briques (M05) — inclut le statut */
export interface BriqueResumeInterne {
  id: number;
  titre: string;
  resume: string;
  categorie: CategorieDto;
  langage: LangageDto;
  licence: Licence;
  statut: StatutBrique;
  versionCode: number;
  dateCreation: string;
  dateModification: string;
}

export interface BriqueCreationPayload {
  titre: string;
  resume: string;
  code: string;
  modeEmploi: string;
  prerequis?: string | null;
  licence: Licence;
  idCategorie: number;
  idLangage: number;
  droitsConfirmes: boolean;
}

export interface BriqueModificationPayload {
  titre: string;
  resume: string;
  code: string;
  modeEmploi: string;
  prerequis?: string | null;
  licence: Licence;
  idCategorie: number;
  idLangage: number;
}

export const LICENCES_PERMISSIVES: { valeur: Licence; libelle: string }[] = [
  { valeur: 'MIT',         libelle: 'MIT' },
  { valeur: 'APACHE_2_0', libelle: 'Apache 2.0' },
  { valeur: 'BSD_2_CLAUSE', libelle: 'BSD-2-Clause' },
  { valeur: 'BSD_3_CLAUSE', libelle: 'BSD-3-Clause' },
  { valeur: 'ISC',         libelle: 'ISC' },
  { valeur: 'CC0_1_0',    libelle: 'CC0 1.0' },
];

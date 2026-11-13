export interface ReferenceUtilisateur {
  id: number;
  pseudo: string;
  nomAffiche: string | null;
}

export interface Utilisateur {
  id: number;
  pseudo: string;
  nomAffiche: string | null;
  role: 'DEVELOPPEUR' | 'ADMIN';
  pays: string | null;
  bio: string | null;
  lienProfil: string | null;
  dateCreation: string;
}

export interface PageReponse<T> {
  contenu: T[];
  page: number;
  taille: number;
  totalElements: number;
  totalPages: number;
}

export interface DetailErreur {
  champ: string;
  message: string;
}

export interface ErreurApi {
  statut: number;
  code: string;
  message: string;
  details?: DetailErreur[];
  horodatage: string;
}

export interface ReponseAuth {
  jeton: string;
  utilisateur: Utilisateur;
}
// ---------- Référentiels (11.4) ----------
export interface Categorie {
  id: number;
  slug: string;
  nom: string;
  description: string | null;
}

export interface Langage {
  id: number;
  slug: string;
  nom: string;
}

export interface ElementReference {
  slug: string;
  nom: string;
}

// ---------- Briques (11.4) ----------
export type Licence = 'MIT' | 'APACHE_2_0' | 'BSD_2_CLAUSE' | 'BSD_3_CLAUSE' | 'ISC' | 'CC0_1_0';
export type StatutBrique = 'PUBLIEE' | 'MASQUEE';
export type TriBriques = 'recentes' | 'mieux-notees' | 'plus-testees';

export interface BriqueResume {
  id: number;
  titre: string;
  resume: string;
  licence: Licence;
  statut: StatutBrique;
  versionCode: number;
  dateCreation: string;
  dateModification: string;
  categorie: ElementReference;
  langage: ElementReference;
  auteur: ReferenceUtilisateur | null;
  noteMoyenne: number | null;
  nombreAvis: number;
  testsOk: number;
  testsKo: number;
  derniereVerification: string | null;
}

export interface BriqueDetail extends BriqueResume {
  code: string;
  modeEmploi: string;
  prerequis: string | null;
  idCategorie: number;
  idLangage: number;
}

export interface CorpsBrique {
  titre: string;
  resume: string;
  code: string;
  modeEmploi: string;
  prerequis: string | null;
  licence: Licence;
  idCategorie: number;
  idLangage: number;
  droitsConfirmes: boolean;
}

export interface FiltresBriques {
  q?: string;
  categorie?: string;
  langage?: string;
  tri?: TriBriques;
  auteur?: number;
  statut?: StatutBrique;
  page?: number;
  taille?: number;
}

// ---------- Demandes (11.6) ----------
export type StatutDemande = 'OUVERTE' | 'RESOLUE';

export interface DemandeResume {
  id: number;
  titre: string;
  extrait: string;
  statut: StatutDemande;
  dateCreation: string;
  categorie: ElementReference | null;
  langage: ElementReference | null;
  auteur: ReferenceUtilisateur | null;
  nombreReponses: number;
}

export interface Reponse {
  id: number;
  contenu: string;
  acceptee: boolean;
  dateCreation: string;
  auteur: ReferenceUtilisateur | null;
  brique: { id: number; titre: string } | null;
}

export interface DemandeDetail extends DemandeResume {
  description: string;
  reponses: Reponse[];
}

export interface FiltresDemandes {
  statut?: StatutDemande;
  categorie?: string;
  langage?: string;
  q?: string;
  page?: number;
  taille?: number;
}

// ---------- Développeurs (11.6) ----------
export interface ProfilPublic {
  id: number;
  pseudo: string;
  nomAffiche: string | null;
  pays: string | null;
  bio: string | null;
  lienProfil: string | null;
  dateCreation: string;
  reputation: number;
  nombreBriques: number;
  testsOkRecus: number;
  nombreReponsesAcceptees: number;
}

export interface ClassementEntree {
  developpeur: ReferenceUtilisateur;
  pays: string | null;
  reputation: number;
  nombreBriques: number;
  testsOkRecus: number;
  nombreReponsesAcceptees: number;
}

// ---------- Compte (11.3) ----------
export interface CorpsProfil {
  nomAffiche: string | null;
  pays: string | null;
  bio: string | null;
  lienProfil: string | null;
}

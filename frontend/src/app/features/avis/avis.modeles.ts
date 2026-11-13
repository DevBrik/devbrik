import { BriqueResume, ReferenceUtilisateur } from '../../core/api/modeles';
export { formaterNote, libelleLicence, nomAuteur, pastilleTest } from '../../core/libelles';

/** Interfaces du contrat d'API, section 11.5 du dossier v1.1. Dates en ISO 8601 UTC. */

// ---------- avis ----------
export interface Avis {
  id: number;
  note: number;
  commentaire: string | null;
  auteur: ReferenceUtilisateur | null;
  dateCreation: string;
  dateModification: string;
}

export interface CorpsAvis {
  note: number;
  commentaire?: string;
}

// ---------- tests ----------
export type ResultatTest = 'OK' | 'KO';

export interface Test {
  id: number;
  resultat: ResultatTest;
  environnement: string | null;
  versionCode: number;
  auteur: ReferenceUtilisateur | null;
  dateTest: string;
}

export interface CorpsTest {
  resultat: ResultatTest;
  environnement?: string;
}

/** GET /api/briques/{id}/moi : chacun peut être null. */
export interface MoiBrique {
  monAvis: Avis | null;
  monTest: Test | null;
}

// ---------- signalements ----------
export type MotifSignalement =
  | 'CODE_DANGEREUX'
  | 'DROITS_AUTEUR'
  | 'SPAM'
  | 'INEXACT'
  | 'INAPPROPRIE'
  | 'AUTRE';
export type StatutSignalement = 'OUVERT' | 'TRAITE';
export type ActionTraitement = 'MASQUER_BRIQUE' | 'CONSERVER';
export type StatutBrique = 'PUBLIEE' | 'MASQUEE';

export interface CorpsSignalement {
  motif: MotifSignalement;
  details?: string;
}

export interface BriqueSignalee {
  id: number;
  titre: string;
  statut: StatutBrique;
  /** Auteur de la brique (pas celui du signalement). */
  auteur: ReferenceUtilisateur | null;
}

export interface Signalement {
  id: number;
  motif: MotifSignalement;
  details: string | null;
  statut: StatutSignalement;
  actionTraitement: ActionTraitement | null;
  dateCreation: string;
  dateTraitement: string | null;
  brique: BriqueSignalee | null;
  /** Personne qui a signalé. */
  auteur: ReferenceUtilisateur | null;
}

// ---------- statistiques ----------
export interface Statistiques {
  utilisateurs: number;
  briquesPubliees: number;
  briquesMasquees: number;
  avis: number;
  tests: number;
  demandesOuvertes: number;
  demandesResolues: number;
  signalementsOuverts: number;
}

// ---------- PUT : 201 = créé, 200 = mis à jour ----------
export interface Ecriture<T> {
  cree: boolean;
  valeur: T;
}

// ---------- libellés (maquette, page 30) ----------
export const MOTIFS: readonly { valeur: MotifSignalement; libelle: string }[] = [
  { valeur: 'INAPPROPRIE', libelle: 'Contenu inapproprié' },
  { valeur: 'CODE_DANGEREUX', libelle: 'Code dangereux' },
  { valeur: 'DROITS_AUTEUR', libelle: 'Violation de droits d’auteur' },
  { valeur: 'INEXACT', libelle: 'Informations incorrectes' },
  { valeur: 'SPAM', libelle: 'Spam ou publicité' },
  { valeur: 'AUTRE', libelle: 'Autre' },
];

export function libelleMotif(motif: MotifSignalement): string {
  return MOTIFS.find((m) => m.valeur === motif)?.libelle ?? motif;
}
/** Ce dont les composants d'évaluation ont besoin ; BriqueDetail (membre 2) est compatible tel quel. */
export interface BriqueEvaluable {
  id: number;
  titre?: string;
  statut: string;
  versionCode: number;
  auteur: { id: number } | null;
}

export function libelleResultat(resultat: ResultatTest): string {
  return resultat === 'OK' ? 'Testé OK' : 'Testé KO';
}

/** Étoiles pleines puis vides (décoratif : toujours accompagné d'un texte « n sur 5 »). */
export function etoiles(note: number): string {
  const n = Math.max(0, Math.min(5, Math.round(note)));
  return '★'.repeat(n) + '☆'.repeat(5 - n);
}


// ---------- briques vues par l'administration (BriqueResume, section 11.4) ----------
export type BriqueResumeAdmin = BriqueResume;

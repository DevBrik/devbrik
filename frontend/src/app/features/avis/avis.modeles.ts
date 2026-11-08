import { ReferenceUtilisateur } from '../../core/api/modeles';

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

export function nomAuteur(auteur: ReferenceUtilisateur | null): string {
  if (!auteur) {
    return 'Utilisateur supprimé';
  }
  const nom = auteur.nomAffiche?.trim();
  return nom ? nom : auteur.pseudo;
}

// ---------- briques vues par l'administration (BriqueResume, section 11.4) ----------
export interface BriqueResumeAdmin {
  id: number;
  titre: string;
  resume: string;
  licence: string;
  statut: StatutBrique;
  versionCode: number;
  dateCreation: string;
  dateModification: string;
  categorie: { slug: string; nom: string };
  langage: { slug: string; nom: string };
  auteur: ReferenceUtilisateur | null;
  noteMoyenne: number | null;
  nombreAvis: number;
  testsOk: number;
  testsKo: number;
  derniereVerification: string | null;
}

const LICENCES: Record<string, string> = {
  MIT: 'MIT',
  APACHE_2_0: 'Apache 2.0',
  BSD_2_CLAUSE: 'BSD 2-Clause',
  BSD_3_CLAUSE: 'BSD 3-Clause',
  ISC: 'ISC',
  CC0_1_0: 'CC0 1.0',
};

export function libelleLicence(licence: string): string {
  return LICENCES[licence] ?? licence;
}

/** Virgule décimale : 4.8 devient « 4,8 ». */
export function formaterNote(note: number): string {
  return String(note).replace('.', ',');
}

/** Règle 6.4 de la charte : pastille « Testé » d'une brique (tests de la version actuelle). */
export function pastilleTest(brique: Pick<BriqueResumeAdmin, 'testsOk' | 'testsKo'>): {
  libelle: string;
  classe: 'ok' | 'ko' | 'neutre';
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

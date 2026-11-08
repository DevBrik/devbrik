import { Statistiques } from '../../avis.modeles';

export interface TuileStatistique {
  cle: string;
  libelle: string;
  valeur: number;
  detail?: string;
}

function accord(n: number, singulier: string, pluriel: string): string {
  return `${n.toLocaleString('fr-FR')} ${n > 1 ? pluriel : singulier}`;
}

/** Six tuiles du tableau de bord (dossier 12.7, M12), construites avec Statistiques (11.5). */
export function tuilesStatistiques(s: Statistiques): TuileStatistique[] {
  return [
    { cle: 'utilisateurs', libelle: 'Utilisateurs', valeur: s.utilisateurs },
    {
      cle: 'briques',
      libelle: 'Briques',
      valeur: s.briquesPubliees,
      detail: accord(s.briquesMasquees, 'masquée', 'masquées'),
    },
    {
      cle: 'demandes',
      libelle: 'Demandes',
      valeur: s.demandesOuvertes + s.demandesResolues,
      detail: `${accord(s.demandesOuvertes, 'ouverte', 'ouvertes')} · ${accord(s.demandesResolues, 'résolue', 'résolues')}`,
    },
    { cle: 'avis', libelle: 'Avis', valeur: s.avis },
    { cle: 'tests', libelle: 'Tests', valeur: s.tests },
    { cle: 'signalements', libelle: 'Signalements ouverts', valeur: s.signalementsOuverts },
  ];
}

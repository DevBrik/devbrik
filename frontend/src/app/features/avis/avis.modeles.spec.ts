import {
  etoiles,
  formaterNote,
  libelleLicence,
  libelleMotif,
  libelleResultat,
  nomAuteur,
  pastilleTest,
} from './avis.modeles';

describe('pastilleTest (charte 6.4, version actuelle seulement)', () => {
  it('plus de tests OK que KO : Testé OK', () => {
    expect(pastilleTest({ testsOk: 3, testsKo: 1 })).toEqual({ libelle: 'Testé OK', classe: 'ok', icone: 'check' });
  });

  it('plus de tests KO que OK : Testé KO', () => {
    expect(pastilleTest({ testsOk: 0, testsKo: 2 }).libelle).toBe('Testé KO');
    expect(pastilleTest({ testsOk: 0, testsKo: 2 }).classe).toBe('ko');
  });

  it('égalité non nulle : Avis partagés (neutre)', () => {
    expect(pastilleTest({ testsOk: 2, testsKo: 2 })).toMatchObject({ libelle: 'Avis partagés', classe: 'neutre' });
  });

  it('aucun test : Non testée (neutre)', () => {
    expect(pastilleTest({ testsOk: 0, testsKo: 0 })).toMatchObject({ libelle: 'Non testée', classe: 'neutre' });
  });
});

describe('libellés et formats', () => {
  it('virgule décimale pour la note', () => {
    expect(formaterNote(4.8)).toBe('4,8');
    expect(formaterNote(5)).toBe('5');
  });

  it('noms de licence lisibles, inconnue renvoyée telle quelle', () => {
    expect(libelleLicence('APACHE_2_0')).toBe('Apache 2.0');
    expect(libelleLicence('BSD_3_CLAUSE')).toBe('BSD 3-Clause');
    expect(libelleLicence('CC0_1_0')).toBe('CC0 1.0');
    expect(libelleLicence('AUTRE_LICENCE')).toBe('AUTRE_LICENCE');
  });

  it('motif et résultat en français', () => {
    expect(libelleMotif('INAPPROPRIE')).toBe('Contenu inapproprié');
    expect(libelleResultat('KO')).toBe('Testé KO');
  });

  it('étoiles : bornées entre 0 et 5', () => {
    expect(etoiles(3)).toBe('★★★☆☆');
    expect(etoiles(9)).toBe('★★★★★');
    expect(etoiles(-2)).toBe('☆☆☆☆☆');
  });

  it('nom affiché, sinon pseudo, sinon « Utilisateur supprimé »', () => {
    expect(nomAuteur({ id: 1, pseudo: 'awa.dev', nomAffiche: 'Awa Diop' })).toBe('Awa Diop');
    expect(nomAuteur({ id: 1, pseudo: 'awa.dev', nomAffiche: '  ' })).toBe('awa.dev');
    expect(nomAuteur(null)).toBe('Utilisateur supprimé');
  });
});

import { formaterNote, initiales, libelleLicence, lienSur, lignes, nomAuteur, pastilleTest } from './libelles';

describe('libellés partagés', () => {
  it('règle 6.4 de la charte : pastille « Testé » de la version actuelle', () => {
    expect(pastilleTest({ testsOk: 3, testsKo: 1 }).libelle).toBe('Testé OK');
    expect(pastilleTest({ testsOk: 1, testsKo: 2 }).libelle).toBe('Testé KO');
    expect(pastilleTest({ testsOk: 2, testsKo: 2 }).libelle).toBe('Avis partagés');
    expect(pastilleTest({ testsOk: 0, testsKo: 0 }).libelle).toBe('Non testée');
  });

  it('virgule décimale et licences lisibles', () => {
    expect(formaterNote(4.8)).toBe('4,8');
    expect(libelleLicence('APACHE_2_0')).toBe('Apache 2.0');
    expect(libelleLicence('CC0_1_0')).toBe('CC0 1.0');
  });

  it('nom affiché, sinon pseudo, et initiales', () => {
    expect(nomAuteur({ id: 1, pseudo: 'awa.dev', nomAffiche: 'Awa Diop' })).toBe('Awa Diop');
    expect(nomAuteur({ id: 1, pseudo: 'awa.dev', nomAffiche: '  ' })).toBe('awa.dev');
    expect(nomAuteur(null)).toBe('Utilisateur supprimé');
    expect(initiales('Awa Diop')).toBe('AD');
    expect(initiales('moussa.dev')).toBe('MD');
  });

  it('mode d’emploi coupé aux sauts de ligne, lignes vides retirées', () => {
    expect(lignes('Étape 1\r\n\n  Étape 2  ')).toEqual(['Étape 1', 'Étape 2']);
    expect(lignes(null)).toEqual([]);
  });

  it('lien de profil : http et https seulement', () => {
    expect(lienSur('https://exemple.sn')).toBe('https://exemple.sn/');
    expect(lienSur('javascript:alert(1)')).toBeNull();
    expect(lienSur('pas une adresse')).toBeNull();
  });
});

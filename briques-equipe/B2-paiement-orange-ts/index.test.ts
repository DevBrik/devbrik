import {
  validerTelephone,
  validerMontant,
  normaliserTelephone,
  construirePayload,
  lireReponse,
  PaiementOrangeConfig,
  DemandePaiement,
} from './index';

const CONFIG: PaiementOrangeConfig = {
  cleMarchande: 'cle-test-123',
  idMarchand:   'marchand-test',
  urlRappel:    'https://exemple.sn/rappel',
};

const DEMANDE: DemandePaiement = {
  telephone: '771234567',
  montant:   5000,
  reference: 'CMD-001',
};

describe('validerTelephone', () => {
  test.each([
    ['771234567',       true],
    ['761234567',       true],
    ['751234567',       true],
    ['781234567',       true],
    ['+221771234567',   true],
    ['00221771234567',  true],
    ['621234567',       false], // Expresso — pas Orange
    ['331234567',       false], // fixe
    ['',                false],
    ['abc',             false],
  ])('%s → %s', (tel, attendu) => {
    expect(validerTelephone(tel)).toBe(attendu);
  });
});

describe('validerMontant', () => {
  test.each([
    [100,   true],
    [500,   true],
    [99,    false],
    [0,     false],
    [-1,    false],
    [100.5, false],
  ])('%d → %s', (montant, attendu) => {
    expect(validerMontant(montant)).toBe(attendu);
  });
});

describe('normaliserTelephone', () => {
  test('ajoute +221 si absent', () => {
    expect(normaliserTelephone('771234567')).toBe('+221771234567');
  });
  test('conserve +221 existant', () => {
    expect(normaliserTelephone('+221771234567')).toBe('+221771234567');
  });
  test('convertit 00221 en +221', () => {
    expect(normaliserTelephone('00221771234567')).toBe('+221771234567');
  });
});

describe('construirePayload', () => {
  test('payload valide contient les champs obligatoires', () => {
    const p = construirePayload(CONFIG, DEMANDE);
    expect(p['order_id']).toBe('CMD-001');
    expect(p['amount']).toBe(5000);
    expect(p['currency']).toBe('OUV');
  });

  test('numéro normalisé au format international', () => {
    const p = construirePayload(CONFIG, DEMANDE);
    expect(p['tel']).toBe('+221771234567');
  });

  test('téléphone invalide → erreur', () => {
    expect(() => construirePayload(CONFIG, { ...DEMANDE, telephone: '621234567' }))
      .toThrow(/TELEPHONE_INVALIDE/);
  });

  test('montant invalide → erreur', () => {
    expect(() => construirePayload(CONFIG, { ...DEMANDE, montant: 50 }))
      .toThrow(/MONTANT_INVALIDE/);
  });

  test('référence vide → erreur', () => {
    expect(() => construirePayload(CONFIG, { ...DEMANDE, reference: '' }))
      .toThrow(/REFERENCE_VIDE/);
  });
});

describe('lireReponse', () => {
  test('statut "initiated" → succes', () => {
    const r = lireReponse({ status: 'initiated', pay_token: 'TOK-XYZ' });
    expect(r.succes).toBe(true);
    expect(r.codeTransaction).toBe('TOK-XYZ');
  });

  test('statut "failed" → echec', () => {
    const r = lireReponse({ status: 'failed', message: 'Fonds insuffisants' });
    expect(r.succes).toBe(false);
    expect(r.message).toMatch(/Fonds insuffisants/);
  });
});

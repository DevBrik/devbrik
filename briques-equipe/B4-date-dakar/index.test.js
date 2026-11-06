'use strict';

const {
  formaterDateDakar,
  formaterHeureDakar,
  formaterDateHeureDakar,
  composantsDakar,
} = require('./index');

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Construit un instant UTC précis. */
const utc = (y, mo, d, h = 0, mi = 0, s = 0) =>
  new Date(Date.UTC(y, mo - 1, d, h, mi, s));

// ── formaterDateDakar ─────────────────────────────────────────────────────────

describe('formaterDateDakar', () => {

  test('1er janvier 2025 → contient "janvier" et "2025"', () => {
    const r = formaterDateDakar(utc(2025, 1, 1));
    expect(r).toMatch(/janvier/i);
    expect(r).toMatch(/2025/);
  });

  test('UTC+0 invariant : même résultat en été et en hiver', () => {
    // 15 juin (été) vs 15 décembre (hiver) — même heure UTC
    const ete   = formaterHeureDakar(utc(2025, 6,  15, 12, 0, 0));
    const hiver = formaterHeureDakar(utc(2025, 12, 15, 12, 0, 0));
    expect(ete).toBe(hiver);   // toujours 12:00:00 à Dakar
  });

  test('contient le nom du jour de la semaine', () => {
    // 2025-01-01 est un mercredi
    const r = formaterDateDakar(utc(2025, 1, 1));
    expect(r).toMatch(/mercredi/i);
  });

  test('accepte un timestamp numérique', () => {
    const ts = utc(2025, 3, 15).getTime();
    expect(() => formaterDateDakar(ts)).not.toThrow();
  });

  test('accepte une chaîne ISO 8601', () => {
    expect(() => formaterDateDakar('2025-07-04T10:00:00Z')).not.toThrow();
  });
});

// ── formaterHeureDakar ────────────────────────────────────────────────────────

describe('formaterHeureDakar', () => {

  test('12:34:56 UTC → 12:34:56 à Dakar (UTC+0)', () => {
    const r = formaterHeureDakar(utc(2025, 1, 1, 12, 34, 56));
    expect(r).toBe('12:34:56');
  });

  test('00:00:00 UTC → 00:00:00 à Dakar (minuit)', () => {
    const r = formaterHeureDakar(utc(2025, 6, 1, 0, 0, 0));
    expect(r).toBe('00:00:00');
  });

  test('23:59:59 UTC → 23:59:59 à Dakar', () => {
    const r = formaterHeureDakar(utc(2025, 12, 31, 23, 59, 59));
    expect(r).toBe('23:59:59');
  });
});

// ── formaterDateHeureDakar ────────────────────────────────────────────────────

describe('formaterDateHeureDakar', () => {

  test('contient "à" entre la date et l\'heure', () => {
    const r = formaterDateHeureDakar(utc(2025, 1, 1, 10, 0, 0));
    expect(r).toMatch(/ à /);
  });

  test('contient l\'heure correcte', () => {
    const r = formaterDateHeureDakar(utc(2025, 1, 1, 15, 30, 0));
    expect(r).toContain('15:30:00');
  });
});

// ── composantsDakar ───────────────────────────────────────────────────────────

describe('composantsDakar', () => {

  test('retourne les bons composants pour 2025-04-20T09:30:45Z', () => {
    const c = composantsDakar(utc(2025, 4, 20, 9, 30, 45));
    expect(c.annee).toBe(2025);
    expect(c.mois).toBe(4);
    expect(c.jour).toBe(20);
    expect(c.heure).toBe(9);
    expect(c.minute).toBe(30);
    expect(c.seconde).toBe(45);
  });

  test('minuit UTC → heure 0 (pas 24)', () => {
    const c = composantsDakar(utc(2025, 1, 1, 0, 0, 0));
    expect(c.heure).toBe(0);
  });

  test('23:59:59 UTC → composants corrects', () => {
    const c = composantsDakar(utc(2025, 12, 31, 23, 59, 59));
    expect(c.heure).toBe(23);
    expect(c.minute).toBe(59);
    expect(c.seconde).toBe(59);
  });
});

// ── Gestion des erreurs ───────────────────────────────────────────────────────

describe('Gestion des entrées invalides', () => {

  const fns = [formaterDateDakar, formaterHeureDakar, formaterDateHeureDakar, composantsDakar];

  test.each(fns.map(f => [f.name, f]))(
    '%s — null → TypeError',
    (_, fn) => { expect(() => fn(null)).toThrow(TypeError); }
  );

  test.each(fns.map(f => [f.name, f]))(
    '%s — undefined → TypeError',
    (_, fn) => { expect(() => fn(undefined)).toThrow(TypeError); }
  );

  test.each(fns.map(f => [f.name, f]))(
    '%s — chaîne invalide → TypeError',
    (_, fn) => { expect(() => fn('pas-une-date')).toThrow(TypeError); }
  );

  test.each(fns.map(f => [f.name, f]))(
    '%s — NaN → TypeError',
    (_, fn) => { expect(() => fn(NaN)).toThrow(TypeError); }
  );
});

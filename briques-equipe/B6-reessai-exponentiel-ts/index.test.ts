import { test } from 'node:test';
import assert from 'node:assert/strict';
import { EchecReessai, calculerDelai, reessayer } from './index.ts';

/** Horloge simulée : enregistre les attentes sans attendre. */
function horloge() {
  const attentes: number[] = [];
  return { attentes, attendre: async (ms: number) => void attentes.push(ms) };
}

test('délais exponentiels plafonnés, sans part aléatoire', () => {
  const o = { delaiInitial: 100, facteur: 2, delaiMax: 1000, jitter: 0 };
  assert.deepEqual([1, 2, 3, 4, 5, 6].map((n) => calculerDelai(n, o)), [100, 200, 400, 800, 1000, 1000]);
});

test('part aléatoire comprise entre la moitié et la totalité du délai (jitter 0,5)', () => {
  assert.equal(calculerDelai(3, { delaiInitial: 100, jitter: 0.5, aleatoire: () => 0 }), 200);
  assert.equal(calculerDelai(3, { delaiInitial: 100, jitter: 0.5, aleatoire: () => 0.999999 }), 400);
});

test('réussit après deux échecs et attend 100 puis 200 ms', async () => {
  const h = horloge();
  let appels = 0;
  const r = await reessayer(
    async () => {
      appels++;
      if (appels < 3) throw new Error('réseau');
      return 'ok';
    },
    { delaiInitial: 100, jitter: 0, attendre: h.attendre },
  );
  assert.equal(r, 'ok');
  assert.equal(appels, 3);
  assert.deepEqual(h.attentes, [100, 200]);
});

test('lève EchecReessai quand les tentatives sont épuisées', async () => {
  const h = horloge();
  await assert.rejects(
    reessayer(async () => { throw new Error('toujours'); }, { tentatives: 3, jitter: 0, delaiInitial: 10, attendre: h.attendre }),
    (e: unknown) => e instanceof EchecReessai && e.tentatives === 3 && (e.derniereErreur as Error).message === 'toujours',
  );
  assert.deepEqual(h.attentes, [10, 20]);
});

test('doitReessayer : une erreur 400 n’est pas relancée', async () => {
  const h = horloge();
  let appels = 0;
  await assert.rejects(
    reessayer(
      async () => { appels++; throw Object.assign(new Error('requête invalide'), { statut: 400 }); },
      { attendre: h.attendre, doitReessayer: (e) => (e as { statut: number }).statut >= 500 },
    ),
    (e: unknown) => e instanceof EchecReessai && e.tentatives === 1,
  );
  assert.equal(appels, 1);
  assert.equal(h.attentes.length, 0);
});

test('surReessai est appelé avant chaque attente et l’annulation arrête tout', async () => {
  const controle = new AbortController();
  const vus: number[] = [];
  await assert.rejects(
    reessayer(async () => { throw new Error('x'); }, {
      jitter: 0,
      delaiInitial: 5,
      signal: controle.signal,
      attendre: async () => controle.abort(),
      surReessai: (_e, n, d) => vus.push(n * 1000 + d),
    }),
    EchecReessai,
  );
  assert.deepEqual(vus, [1005]);
});

import { test } from 'node:test';
import assert from 'node:assert/strict';
import { FileHorsLigne, stockageMemoire } from './index.js';

test('les messages sont envoyés dans l’ordre puis retirés', async () => {
  const recus = [];
  const file = new FileHorsLigne({ envoyer: async (m) => void recus.push(m) });
  file.ajouter({ n: 1 });
  file.ajouter({ n: 2 });
  file.ajouter({ n: 3 });
  const r = await file.vider();
  assert.deepEqual(recus, [{ n: 1 }, { n: 2 }, { n: 3 }]);
  assert.deepEqual(r, { envoyes: 3, restants: 0, erreur: null });
  assert.equal(file.taille, 0);
});

test('un échec arrête le vidage ; le message reste en tête et est repris plus tard', async () => {
  let reseau = false;
  const recus = [];
  const file = new FileHorsLigne({
    envoyer: async (m) => {
      if (!reseau) throw new Error('hors ligne');
      recus.push(m);
    },
  });
  file.ajouter('a');
  file.ajouter('b');
  const r1 = await file.vider();
  assert.equal(r1.envoyes, 0);
  assert.equal(r1.restants, 2);
  assert.equal(file.elements()[0].essais, 1);
  reseau = true;
  const r2 = await file.vider();
  assert.equal(r2.envoyes, 2);
  assert.deepEqual(recus, ['a', 'b']);
});

test('la file survit à un « rechargement » (même stockage, nouvelle instance)', async () => {
  const stockage = stockageMemoire();
  new FileHorsLigne({ envoyer: async () => {}, stockage }).ajouter('persistant');
  const recus = [];
  const apres = new FileHorsLigne({ envoyer: async (m) => void recus.push(m), stockage });
  assert.equal(apres.taille, 1);
  await apres.vider();
  assert.deepEqual(recus, ['persistant']);
});

test('un seul vidage à la fois (pas de double envoi)', async () => {
  let appels = 0;
  const file = new FileHorsLigne({ envoyer: async () => { appels++; await new Promise((r) => setTimeout(r, 5)); } });
  file.ajouter(1);
  const [a, b] = await Promise.all([file.vider(), file.vider()]);
  assert.equal(appels, 1);
  assert.equal(a, b);
});

test('file pleine, retrait et stockage corrompu', () => {
  const stockage = stockageMemoire();
  const file = new FileHorsLigne({ envoyer: async () => {}, stockage, maxElements: 2 });
  const id = file.ajouter('x');
  file.ajouter('y');
  assert.throws(() => file.ajouter('z'), /File pleine/);
  file.retirer(id);
  assert.deepEqual(file.elements().map((e) => e.message), ['y']);
  stockage.ecrire('devbrik.file-hors-ligne', '{pas du json');
  assert.equal(file.taille, 0);
});

test('ecouterReseau vide la file à l’événement « online »', async () => {
  const cible = new EventTarget();
  const recus = [];
  const file = new FileHorsLigne({ envoyer: async (m) => void recus.push(m) });
  const arreter = file.ecouterReseau(Object.assign(cible, { navigator: { onLine: false } }));
  file.ajouter('apres-coupure');
  cible.dispatchEvent(new Event('online'));
  await file.vider();
  assert.deepEqual(recus, ['apres-coupure']);
  arreter();
});

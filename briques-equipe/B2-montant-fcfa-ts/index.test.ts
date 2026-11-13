import { test } from 'node:test';
import assert from 'node:assert/strict';
import { formaterFcfa, lireFcfa } from './index.ts';

const F = ' ';

test('groupes de trois chiffres avec espace fine', () => {
  assert.equal(formaterFcfa(1500000), `1${F}500${F}000 FCFA`);
  assert.equal(formaterFcfa(15000), `15${F}000 FCFA`);
  assert.equal(formaterFcfa(999), '999 FCFA');
  assert.equal(formaterFcfa(0), '0 FCFA');
});

test('séparateur et suffixe personnalisables', () => {
  assert.equal(formaterFcfa(25000, { separateur: ' ' }), '25 000 FCFA');
  assert.equal(formaterFcfa(25000, { separateur: '.', suffixe: 'F CFA' }), '25.000 F CFA');
  assert.equal(formaterFcfa(25000, { separateur: ' ', suffixe: '' }), '25 000');
});

test('arrondi à l’unité et montants négatifs', () => {
  assert.equal(formaterFcfa(1999.5, { separateur: ' ' }), '2 000 FCFA');
  assert.equal(formaterFcfa(-1500, { separateur: ' ' }), '-1 500 FCFA');
  assert.equal(formaterFcfa(-0.4, { separateur: ' ' }), '0 FCFA');
});

test('montant invalide refusé', () => {
  assert.throws(() => formaterFcfa(Number.NaN), TypeError);
  assert.throws(() => formaterFcfa(Infinity), TypeError);
});

test('lecture d’une saisie', () => {
  assert.equal(lireFcfa('15 000'), 15000);
  assert.equal(lireFcfa('15.000 FCFA'), 15000);
  assert.equal(lireFcfa(`1${F}500${F}000 F CFA`), 1500000);
  assert.equal(lireFcfa('-2 500 XOF'), -2500);
  assert.equal(lireFcfa('quinze mille'), null);
  assert.equal(lireFcfa(''), null);
});

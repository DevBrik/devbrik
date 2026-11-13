#!/usr/bin/env node
/**
 * Publie les 10 briques de l'équipe (dossier briques-equipe/) via l'API DevBrik (section 16).
 *
 * - Chaque brique est publiée par un compte développeur (alternance entre les deux comptes),
 *   avec la licence MIT et droitsConfirmes = true. Une brique déjà publiée (TITRE_DEJA_UTILISE) est ignorée.
 * - Avec --tester, l'AUTRE compte enregistre « testé OK » avec l'environnement du manifeste.
 *   N'utilisez --tester qu'après avoir réellement exécuté les tests de chaque brique (voir son README).
 *
 * Usage :
 *   DEVBRIK_URL=https://devbrik.example \
 *   COMPTE_A=awa.dev COMPTE_B=moussa.dev DEMO_MOT_DE_PASSE='…' \
 *   node scripts/publier-briques-equipe.mjs [--tester] [--simulation]
 *
 * Aucune dépendance (Node.js 18 ou plus). Aucun mot de passe n'est affiché.
 */
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const racine = join(dirname(fileURLToPath(import.meta.url)), '..', 'briques-equipe');
const URL_API = (process.env.DEVBRIK_URL ?? 'http://localhost:8080').replace(/\/$/, '');
const COMPTES = [process.env.COMPTE_A ?? 'awa.dev', process.env.COMPTE_B ?? 'moussa.dev'];
const MOT_DE_PASSE = process.env.DEMO_MOT_DE_PASSE;
const TESTER = process.argv.includes('--tester');
const SIMULATION = process.argv.includes('--simulation');

/** Extrait le résumé, le mode d'emploi et les prérequis du README d'une brique. */
export function lireFiche(readme) {
  const resume = /\*\*Résumé\*\*\s*:\s*(.+)/.exec(readme)?.[1].trim() ?? '';
  const section = (titre) => {
    const debut = readme.indexOf(`## ${titre}`);
    if (debut < 0) return [];
    const suite = readme.slice(debut + titre.length + 3);
    const fin = suite.search(/\n## /);
    return (fin < 0 ? suite : suite.slice(0, fin))
      .split('\n')
      .map((l) => l.trim())
      .filter((l) => /^(\d+\.|-)\s+/.test(l))
      .map((l) => l.replace(/^(\d+\.|-)\s+/, '').replace(/\*\*/g, '').replace(/`/g, ''));
  };
  return { resume, modeEmploi: section("Mode d'emploi").join('\n'), prerequis: section('Prérequis').join('\n') };
}

async function appel(methode, chemin, corps, jeton) {
  const reponse = await fetch(URL_API + chemin, {
    method: methode,
    headers: { 'Content-Type': 'application/json', ...(jeton ? { Authorization: `Bearer ${jeton}` } : {}) },
    body: corps === undefined ? undefined : JSON.stringify(corps),
  });
  const texte = await reponse.text();
  const json = texte ? JSON.parse(texte) : null;
  return { statut: reponse.status, json };
}

async function connexion(pseudo) {
  const r = await appel('POST', '/api/auth/connexion', { pseudo, motDePasse: MOT_DE_PASSE });
  if (r.statut !== 200) throw new Error(`Connexion de ${pseudo} impossible (${r.statut} ${r.json?.code ?? ''})`);
  return r.json.jeton;
}

async function principal() {
  const manifeste = JSON.parse(readFileSync(join(racine, 'briques.json'), 'utf8'));
  const fiches = manifeste.map((b) => {
    const readme = readFileSync(join(racine, b.dossier, 'README.md'), 'utf8');
    const code = readFileSync(join(racine, b.dossier, b.fichier), 'utf8');
    return { ...b, ...lireFiche(readme), code };
  });

  if (SIMULATION) {
    for (const f of fiches) {
      console.info(`${f.id} « ${f.titre} » : résumé ${f.resume.length} car., mode d'emploi ${f.modeEmploi.length} car., prérequis ${f.prerequis.length} car., code ${f.code.length} car.`);
    }
    return;
  }
  if (!MOT_DE_PASSE) throw new Error('Définissez DEMO_MOT_DE_PASSE (jamais dans Git).');

  const jetons = await Promise.all(COMPTES.map(connexion));
  const categories = (await appel('GET', '/api/categories')).json;
  const langages = (await appel('GET', '/api/langages')).json;

  for (const [index, f] of fiches.entries()) {
    const auteur = index % 2;
    const corps = {
      titre: f.titre,
      resume: f.resume,
      code: f.code,
      modeEmploi: f.modeEmploi,
      prerequis: f.prerequis || null,
      licence: 'MIT',
      idCategorie: categories.find((c) => c.slug === f.categorie)?.id,
      idLangage: langages.find((l) => l.slug === f.langage)?.id,
      droitsConfirmes: true,
    };
    const r = await appel('POST', '/api/briques', corps, jetons[auteur]);
    let idBrique = r.json?.id;
    if (r.statut === 201) {
      console.info(`${f.id} publiée (id ${idBrique}) par ${COMPTES[auteur]}`);
    } else if (r.json?.code === 'TITRE_DEJA_UTILISE') {
      const existante = await appel('GET', `/api/moi/briques?q=${encodeURIComponent(f.titre.slice(0, 60))}&taille=5`, undefined, jetons[auteur]);
      idBrique = existante.json?.contenu?.find((b) => b.titre === f.titre)?.id;
      console.info(`${f.id} déjà publiée (id ${idBrique ?? '?'})`);
    } else {
      console.error(`${f.id} refusée : ${r.statut} ${r.json?.code} ${r.json?.message ?? ''}`);
      continue;
    }
    if (TESTER && idBrique) {
      const t = await appel('PUT', `/api/briques/${idBrique}/test`, { resultat: 'OK', environnement: f.environnementTest }, jetons[1 - auteur]);
      console.info(`   testée OK par ${COMPTES[1 - auteur]} : ${t.statut}`);
    }
  }
}

principal().catch((e) => {
  console.error(e.message);
  process.exit(1);
});

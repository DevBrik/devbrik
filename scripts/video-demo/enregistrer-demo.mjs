// Enregistre automatiquement une vidéo de démonstration de DevBrik (environ 3 min, 1280×720).
//
// Le script pilote un vrai navigateur (Playwright / Chromium) sur DevBrik en ligne
// (https://devbrik.duckdns.org par défaut) et filme l'écran, avec un curseur visible,
// des légendes et des écrans d'ouverture et de fin aux logos DevBrik.
// Parcours : visiteur (recherche, fiche, copie) → fleury (teste et note la brique d'un autre
// membre) → bousso (ses briques, publication, demandes, classement) → administrateur
// (statistiques, modération) → aperçu mobile Android 360 × 800.
// Par défaut il n'enregistre RIEN : les fenêtres « Tester » et « Donner mon avis » sont
// remplies puis annulées. DEMO_ECRITURE=1 les valide réellement (à n'utiliser que si le
// test a vraiment été fait, car le signal « testé » est visible par tous).
//
// Utilisation (voir README.md) :
//   npm install            (dans ce dossier, une seule fois)
//   npx playwright install chromium
//   node enregistrer-demo.mjs
//
// Mots de passe (obligatoires, jamais écrits dans ce fichier) :
//   DEMO_MDP_FLEURY, DEMO_MDP_BOUSSO, DEMO_MDP_ADMIN
// Variables facultatives :
//   DEMO_URL            adresse du site                  (défaut : https://devbrik.duckdns.org)
//   DEMO_ADMIN          compte administrateur            (défaut : admin.demo)
//   DEMO_RECHERCHE      mot recherché à l'accueil        (défaut : sans accents)
//   DEMO_ENVIRONNEMENT  environnement saisi dans le test (défaut : Java 21, Windows 11)
//   DEMO_ECRITURE=1     valider réellement le test et l'avis (désactivé par défaut)

import { chromium } from 'playwright';
import { mkdirSync, readdirSync, readFileSync, renameSync, rmSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const ici = dirname(fileURLToPath(import.meta.url));
const URL_SITE = (process.env.DEMO_URL ?? 'https://devbrik.duckdns.org').replace(/\/$/, '');
const FLEURY = { pseudo: 'fleury', nom: 'Fleury Ba', mdp: process.env.DEMO_MDP_FLEURY, variable: 'DEMO_MDP_FLEURY' };
const BOUSSO = { pseudo: 'bousso', nom: 'Bousso Diémé', mdp: process.env.DEMO_MDP_BOUSSO, variable: 'DEMO_MDP_BOUSSO' };
const ADMIN = { pseudo: process.env.DEMO_ADMIN ?? 'admin.demo', mdp: process.env.DEMO_MDP_ADMIN, variable: 'DEMO_MDP_ADMIN' };
const RECHERCHE = process.env.DEMO_RECHERCHE ?? 'sans accents';
const ENVIRONNEMENT = process.env.DEMO_ENVIRONNEMENT ?? 'Java 21, Windows 11';
const SANS_ECRITURE = process.env.DEMO_ECRITURE !== '1';
const manquants = [FLEURY, BOUSSO, ADMIN].filter((c) => !c.mdp).map((c) => `${c.variable} (compte ${c.pseudo})`);
if (manquants.length) {
  console.error('Mots de passe manquants : ' + manquants.join(', ') + '. Voir README.md.');
  process.exit(1);
}

const LARGEUR = 1280;
const HAUTEUR = 720;
const dossierTmp = join(ici, 'video-tmp');
const sortie = join(ici, 'devbrik-demo.webm');

const pause = (ms) => new Promise((r) => setTimeout(r, ms));

// ---------------------------------------------------------------------------
// Habillage injecté dans chaque page : curseur visible, bandeau de légende.
// ---------------------------------------------------------------------------
const HABILLAGE = (origineSite) => {
  const installer = () => {
    if (document.getElementById('curseur-demo')) return;
    // Rideau blanc sur les pages du site : il cache le chargement (page vide, pied de page seul,
    // squelettes) et se lève quand les données sont affichées (voir leverRideau).
    if (location.origin === origineSite && window.top === window) {
      const r = document.createElement('div');
      r.id = 'rideau-demo';
      Object.assign(r.style, {
        position: 'fixed', inset: '0', background: '#fff', zIndex: '2147483645',
        pointerEvents: 'none', transition: 'opacity .35s', opacity: '1',
      });
      document.body.appendChild(r);
    }
    const c = document.createElement('div');
    c.id = 'curseur-demo';
    Object.assign(c.style, {
      position: 'fixed', left: '0', top: '0', width: '22px', height: '22px', marginLeft: '-11px', marginTop: '-11px',
      borderRadius: '50%', background: 'rgba(234, 88, 12, 0.35)', border: '2px solid #EA580C',
      zIndex: '2147483647', pointerEvents: 'none', transition: 'transform .12s',
      transform: `translate(${window.__cx ?? -50}px, ${window.__cy ?? -50}px)`,
    });
    document.body.appendChild(c);
    document.addEventListener('mousemove', (e) => {
      window.__cx = e.clientX; window.__cy = e.clientY;
      c.style.transform = `translate(${e.clientX}px, ${e.clientY}px)`;
    }, true);
    document.addEventListener('mousedown', () => { c.style.background = 'rgba(234, 88, 12, 0.8)'; }, true);
    document.addEventListener('mouseup', () => { c.style.background = 'rgba(234, 88, 12, 0.35)'; }, true);

    const l = document.createElement('div');
    l.id = 'legende-demo';
    Object.assign(l.style, {
      position: 'fixed', left: '0', right: '0', bottom: '0', zIndex: '2147483646', pointerEvents: 'none',
      background: 'linear-gradient(to top, rgba(15,23,42,.92), rgba(15,23,42,.80))', color: '#fff',
      font: '600 19px/1.35 system-ui, sans-serif', padding: '12px 24px', textAlign: 'center',
      transition: 'opacity .25s', opacity: '0',
    });
    document.body.appendChild(l);
    // Laisser de la place au bandeau : le contenu de bas de page reste visible en défilant.
    document.documentElement.style.scrollPaddingBottom = '70px';
  };
  if (document.body) installer();
  else document.addEventListener('DOMContentLoaded', installer);
};

async function legende(page, texte) {
  await page.evaluate((t) => {
    const l = document.getElementById('legende-demo');
    if (!l) return;
    l.textContent = t;
    l.style.opacity = t ? '1' : '0';
  }, texte).catch(() => {});
}

/** Attend que la page ait fini de charger ses données (application rendue, plus de squelette ni de « Chargement »). */
async function attendreContenu(page, selecteurAttendu) {
  await pause(300);
  await page.waitForLoadState('networkidle', { timeout: 15000 }).catch(() => {});
  await page.waitForFunction(() => {
    const main = document.querySelector('main');
    if (!main || main.innerText.trim().length < 20) return false;
    if (document.querySelector('db-squelette-cartes, [aria-busy="true"], .squelette, .squelette-tuiles')) return false;
    return !/Chargement/.test(main.innerText);
  }, null, { timeout: 20000, polling: 250 }).catch(() => {});
  if (selecteurAttendu) await page.waitForSelector(selecteurAttendu, { timeout: 15000 }).catch(() => {});
  await pause(400);
  await leverRideau(page);
}

async function leverRideau(page) {
  await page.evaluate(() => {
    const r = document.getElementById('rideau-demo');
    if (r) { r.style.opacity = '0'; setTimeout(() => r.remove(), 400); }
  }).catch(() => {});
  await pause(350);
}

/** Voile blanc bref (fondu) pendant un changement de page. */
async function baisserRideau(page) {
  await page.evaluate(() => {
    let r = document.getElementById('rideau-demo');
    if (!r) {
      r = document.createElement('div');
      r.id = 'rideau-demo';
      Object.assign(r.style, { position: 'fixed', inset: '0', background: '#fff', zIndex: '2147483645',
        pointerEvents: 'none', transition: 'opacity .25s', opacity: '0' });
      document.body.appendChild(r);
      r.getBoundingClientRect();
    }
    r.style.opacity = '1';
  }).catch(() => {});
  await pause(260);
}

/**
 * Change de page SANS recharger le site (navigation interne d'Angular) : pas d'écran blanc,
 * pas de nouveau téléchargement de l'application.
 */
async function aller(page, chemin, selecteurAttendu) {
  if (!page.url().startsWith(URL_SITE)) {
    await page.goto(`${URL_SITE}${chemin}`);
  } else {
    await baisserRideau(page);
    await page.evaluate((c) => {
      history.pushState({}, '', c);
      window.dispatchEvent(new PopStateEvent('popstate', { state: {} }));
    }, chemin);
  }
  await attendreContenu(page, selecteurAttendu);
}

/** Déplace le curseur visible jusqu'à l'élément, puis clique. */
async function cliquer(page, locator) {
  await locator.scrollIntoViewIfNeeded();
  const b = await locator.boundingBox();
  if (b) {
    await page.mouse.move(b.x + b.width / 2, b.y + b.height / 2, { steps: 25 });
    await pause(250);
  }
  await locator.click();
}

async function taper(page, locator, texte, delai = 70) {
  await cliquer(page, locator);
  await locator.pressSequentially(texte, { delay: delai });
}

async function defiler(page, distance, pas = 40) {
  const sens = Math.sign(distance);
  for (let y = 0; y < Math.abs(distance); y += pas) {
    await page.mouse.wheel(0, pas * sens);
    await pause(35);
  }
  await pause(300);
}

/** Fait défiler jusqu'à un élément, en le plaçant vers le haut de l'écran. */
async function montrer(page, locator, decalage = 120) {
  const b = await locator.boundingBox();
  if (b) await defiler(page, b.y - decalage);
}

// Écrans d'ouverture et de fin : fond blanc couvert de logos DevBrik qui glissent lentement.
const image = (nom) => 'data:image/png;base64,' + readFileSync(join(ici, 'assets', nom + '.png')).toString('base64');
const LOGO = image('devbrik-logo');
const ICONES = [image('devbrik-plein'), image('devbrik-contour')];

async function ecranLogos(page, contenu, duree) {
  const motif = Array.from({ length: 16 * 11 }, (_, i) =>
    `<img src="${ICONES[(Math.floor(i / 16) + i) % 2]}">`).join('');
  const css = `
    .marque-demo{position:fixed;inset:0;z-index:2147483646;overflow:hidden;background:#fff;font-family:Inter,'Segoe UI',system-ui,sans-serif;
      transition:opacity .5s;opacity:0}
    .marque-demo .motif{position:absolute;inset:-160px;display:grid;grid-template-columns:repeat(16,110px);grid-auto-rows:110px;
      transform:rotate(-14deg);animation:glisse-demo 18s linear infinite}
    .marque-demo .motif img{width:58px;height:58px;margin:26px;opacity:.10}
    .marque-demo .motif img:nth-child(3n){opacity:.16} .marque-demo .motif img:nth-child(7n){opacity:.06}
    @keyframes glisse-demo{from{transform:rotate(-14deg) translateX(0)}to{transform:rotate(-14deg) translateX(110px)}}
    .marque-demo .voile{position:absolute;inset:0;background:radial-gradient(ellipse 46% 52% at 50% 50%,#fff 55%,rgba(255,255,255,.85) 72%,rgba(255,255,255,0) 100%)}
    .marque-demo .centre{position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center}
    .marque-demo .logo{width:560px} .marque-demo .accroche{margin-top:30px;font-size:27px;font-weight:600;color:#0F172A}
    .marque-demo .accroche b{color:#1E3A8A}
    .marque-demo .ligne{width:72px;height:5px;border-radius:3px;background:#EA580C;margin:26px auto 0}
    .marque-demo .pied{margin-top:22px;font-size:17px;color:#475569;font-weight:500}
    .marque-demo .url{margin-top:26px;font-size:30px;font-weight:700;color:#1E3A8A}
    .marque-demo .pastilles{margin-top:22px;display:flex;gap:10px;justify-content:center}
    .marque-demo .pastilles span{padding:7px 16px;border-radius:999px;background:#EEF2FF;color:#1E3A8A;font-weight:600;font-size:16px}
    .marque-demo .cote{display:flex;align-items:center;justify-content:center;gap:64px;text-align:left}
    .marque-demo .cote h2{margin:0;font-size:40px;color:#0F172A;line-height:1.15}
    .marque-demo .cote p{max-width:380px;font-size:20px;color:#475569;line-height:1.45;margin:14px 0 0}
    .marque-demo .telephone{width:288px;height:640px;border-radius:34px;border:9px solid #0B1220;overflow:hidden;background:#fff;
      box-shadow:0 22px 50px rgba(15,23,42,.35)}
    .marque-demo .telephone iframe{width:360px;height:800px;border:0;transform:scale(.8);transform-origin:0 0;opacity:0;transition:opacity .4s}`;
  await page.evaluate(({ css, html }) => {
    const ancien = document.getElementById('marque-demo');
    if (ancien) ancien.id = 'marque-demo-ancien';
    const st = document.createElement('style'); st.id = 'marque-style'; st.textContent = css;
    document.head.appendChild(st);
    const d = document.createElement('div'); d.id = 'marque-demo'; d.className = 'marque-demo'; d.innerHTML = html;
    document.body.appendChild(d);
    d.getBoundingClientRect();
    d.style.opacity = '1';
    if (ancien) setTimeout(() => ancien.remove(), 550); // fondu enchaîné d'un écran de marque à l'autre
  }, { css, html: `<div class="motif">${motif}</div><div class="voile"></div><div class="centre">${contenu}</div>` });
  await pause(duree);
}

async function masquerMarque(page) {
  await page.evaluate(() => {
    const d = document.getElementById('marque-demo');
    if (d) { d.style.opacity = '0'; setTimeout(() => d.remove(), 600); }
  }).catch(() => {});
  await pause(600);
}

const ecranOuverture = (page) => ecranLogos(page,
  `<img class="logo" src="${LOGO}">
   <div class="accroche">Des briques de code <b>réutilisables</b>, <b>testées</b> et <b>notées</b> par la communauté</div>
   <div class="ligne"></div><div class="pied">Les Lions et les Lionnes du code · CADEV 2026</div>`, 5000);

const ecranFin = (page) => ecranLogos(page,
  `<img class="logo" style="width:440px" src="${LOGO}">
   <div class="url">${new URL(URL_SITE).host}</div>
   <div class="pastilles"><span>Angular 21</span><span>Spring Boot 4</span><span>PostgreSQL 18</span><span>Datacloud</span></div>
   <div class="ligne"></div>
   <div class="pied">Fleury Ba · Papa Birane Diao · Bousso Diémé · Ndiémé Ndiaye · Magatte Tall</div>`, 6000);

async function etape(nom, fn) {
  try {
    await fn();
    console.log(`  ✓ ${nom}`);
  } catch (e) {
    console.warn(`  ⚠ ${nom} : ${e.message.split('\n')[0]}`);
  }
}

async function deconnecter(page) {
  try {
    await cliquer(page, page.locator('.pastille-compte').first());
    await pause(600);
    await cliquer(page, page.getByRole('menuitem', { name: /Se déconnecter/ }));
    await attendreContenu(page);
    await pause(800);
  } catch {
    await page.evaluate(() => { try { localStorage.clear(); sessionStorage.clear(); } catch {} }).catch(() => {});
    await page.goto(`${URL_SITE}/briques`);
    await attendreContenu(page);
  }
}

async function connecter(page, pseudo, mdp, retour, selecteurApres) {
  await aller(page, `/compte/connexion${retour ? `?retour=${encodeURIComponent(retour)}` : ''}`, '#connexion-pseudo');
  await taper(page, page.locator('#connexion-pseudo'), pseudo);
  await cliquer(page, page.locator('#connexion-mdp'));
  await page.locator('#connexion-mdp').fill(mdp);
  await pause(300);
  await cliquer(page, page.locator('.formulaire-auth form button[type="submit"]'));
  await page.waitForURL((u) => !u.pathname.startsWith('/compte/connexion'), { timeout: 15000 });
  await attendreContenu(page, selecteurApres);
}

async function main() {
  rmSync(dossierTmp, { recursive: true, force: true });
  mkdirSync(dossierTmp, { recursive: true });

  console.log(`Enregistrement de la démo sur ${URL_SITE} (${SANS_ECRITURE ? 'sans' : 'avec'} enregistrement du test et de l\'avis) …`);
  const navigateur = await chromium.launch({ headless: true });

  // Préchauffage hors caméra : le premier chargement de « ng serve » peut être lent.
  const prechauffe = await navigateur.newContext();
  const pp = await prechauffe.newPage();
  await pp.goto(`${URL_SITE}/briques`, { timeout: 120000 }).catch(() => {});
  await pp.waitForLoadState('networkidle').catch(() => {});
  await prechauffe.close();

  const contexte = await navigateur.newContext({
    viewport: { width: LARGEUR, height: HAUTEUR },
    recordVideo: { dir: dossierTmp, size: { width: LARGEUR, height: HAUTEUR } },
    locale: 'fr-FR',
    permissions: ['clipboard-read', 'clipboard-write'],
  });
  await contexte.addInitScript(HABILLAGE, new URL(URL_SITE).origin);
  // Autorise l'aperçu mobile en cadre (le site interdit normalement l'affichage dans un iframe).
  await contexte.route(`${URL_SITE}/**`, async (route) => {
    try {
      const r = await route.fetch();
      const h = { ...r.headers() };
      delete h['x-frame-options'];
      if (h['content-security-policy']) h['content-security-policy'] = h['content-security-policy'].replace(/frame-ancestors[^;]*;?/i, '');
      await route.fulfill({ response: r, headers: h });
    } catch {
      await route.continue().catch(() => {});
    }
  });

  const page = await contexte.newPage();
  page.setDefaultTimeout(15000);
  page.setDefaultNavigationTimeout(90000);

  // Ouverture : le site se charge derrière l'écran de marque (aucun écran blanc).
  await page.goto(`${URL_SITE}/briques`);
  await page.waitForSelector('body');
  await ecranOuverture(page);
  await attendreContenu(page, 'db-brique-carte');
  await masquerMarque(page);

  // 1. Accueil et recherche ---------------------------------------------------
  await etape('Accueil', async () => {
    await page.mouse.move(640, 300);
    await legende(page, 'DevBrik : une bibliothèque de petites briques de code prêtes à réutiliser');
    await pause(3500);
    await defiler(page, 420);
    await legende(page, 'Chaque brique affiche son langage, sa catégorie et le signal « testé » de la communauté');
    await pause(4000);
    await defiler(page, -420);
  });

  let titreBrique = null;
  await etape('Recherche', async () => {
    await legende(page, `On cherche une brique : « ${RECHERCHE} »`);
    await taper(page, page.locator('form[role="search"] input[type="search"]').first(), RECHERCHE, 110);
    await attendreContenu(page, 'db-brique-carte');
    await pause(1500);
    await legende(page, 'Résultats instantanés ; on peut aussi filtrer par catégorie et par langage');
    await defiler(page, 300);
    await pause(3000);
  });

  // 2. Fiche d'une brique -------------------------------------------------------
  await etape('Fiche brique', async () => {
    const cartes = page.locator('db-brique-carte');
    // Fleury teste et note la brique : elle ne doit pas être la sienne (boutons masqués pour l'auteur).
    let carte = cartes.filter({ hasNotText: FLEURY.nom }).first();
    if (!(await carte.count())) carte = cartes.first();
    const lien = carte.getByRole('link', { name: /^Voir/ }).first();
    titreBrique = ((await lien.getAttribute('aria-label')) ?? '').replace(/^Voir la brique\s*/, '') || null;
    await cliquer(page, lien);
    await page.waitForURL(/\/briques\/\d+/);
    await attendreContenu(page, 'h1');
    await legende(page, 'La fiche : description, auteur, licence, version et dernière vérification');
    await pause(4000);
    await defiler(page, 330);
    await legende(page, 'Évaluation (note moyenne) et tests sur la version actuelle du code');
    await pause(4000);
  });

  await etape('Code et copie', async () => {
    const copier = page.getByRole('button', { name: /Copier le code/ }).first();
    await montrer(page, page.locator('text=Code source').first(), 90);
    await legende(page, "Le code est affiché en texte brut : DevBrik ne l'exécute jamais");
    await pause(3500);
    await legende(page, 'Un clic pour le copier');
    await cliquer(page, copier);
    await pause(2500);
    await defiler(page, 380);
    await legende(page, "Mode d'emploi pas à pas et prérequis");
    await pause(4000);
  });

  // 3. Connexion puis test et avis ---------------------------------------------
  const urlFiche = new URL(page.url()).pathname;
  await etape('Connexion', async () => {
    await legende(page, 'Pour tester ou noter une brique, on se connecte : ici avec le compte de Fleury Ba');
    await pause(1500);
    await connecter(page, FLEURY.pseudo, FLEURY.mdp, urlFiche, 'h1');
    await legende(page, 'Retour automatique sur la fiche après la connexion');
    await pause(2500);
  });

  await etape('Tester la brique', async () => {
    const bouton = page.getByRole('button', { name: 'Tester cette brique' });
    await montrer(page, bouton, 260);
    await legende(page, "« Tester cette brique » : chacun indique si elle fonctionne, et dans quel environnement");
    await cliquer(page, bouton);
    const dialogue = page.locator('mat-dialog-container');
    await dialogue.waitFor();
    await pause(1200);
    await cliquer(page, dialogue.locator('label.option').filter({ hasText: 'Testé OK' }).first());
    const env = dialogue.locator('#test-environnement');
    await env.fill('');
    await taper(page, env, ENVIRONNEMENT, 60);
    await pause(1200);
    if (SANS_ECRITURE) {
      await cliquer(page, dialogue.getByRole('button', { name: 'Annuler' }));
    } else {
      await cliquer(page, dialogue.locator('button[type="submit"]'));
    }
    await dialogue.waitFor({ state: 'detached' }).catch(() => {});
    await attendreContenu(page);
    await pause(2500);
  });

  await etape('Donner son avis', async () => {
    const bouton = page.getByRole('button', { name: 'Donner mon avis' });
    await montrer(page, bouton, 260);
    await legende(page, '« Donner mon avis » : une note de 1 à 5 et un commentaire');
    await cliquer(page, bouton);
    const dialogue = page.locator('mat-dialog-container');
    await dialogue.waitFor();
    await pause(1000);
    await cliquer(page, dialogue.getByRole('radio', { name: '5 sur 5' }));
    const com = dialogue.locator('#avis-commentaire');
    await com.fill('');
    await taper(page, com, 'Fonctionne du premier coup, mode d\'emploi très clair.', 35);
    await pause(1200);
    if (SANS_ECRITURE) {
      await cliquer(page, dialogue.getByRole('button', { name: 'Annuler' }));
    } else {
      await cliquer(page, dialogue.locator('button[type="submit"]'));
    }
    await dialogue.waitFor({ state: 'detached' }).catch(() => {});
    await attendreContenu(page);
    await page.evaluate(() => window.scrollTo({ top: 0, behavior: 'smooth' }));
    await pause(900);
    const tuiles = page.locator('h2, h3, p, span').filter({ hasText: /^\s*(Évaluation|ÉVALUATION)\s*$/ }).first();
    if (await tuiles.count()) await montrer(page, tuiles, 160);
    else await defiler(page, 330);
    await legende(page, SANS_ECRITURE
      ? 'Les notes et les tests de chacun alimentent les tuiles Évaluation et Tests de la fiche'
      : 'La note et le compteur de tests de la fiche sont mis à jour immédiatement');
    await pause(4500);
  });

  // 4. Bousso : ses briques et la publication ------------------------------------
  await etape('Mes briques (Bousso)', async () => {
    await deconnecter(page);
    await legende(page, '');
    await connecter(page, BOUSSO.pseudo, BOUSSO.mdp, '/briques/mes-briques', 'db-brique-carte');
    await legende(page, 'Bousso Diémé retrouve ses briques publiées dans « Mes briques »');
    await pause(4500);
  });

  await etape('Formulaire de publication', async () => {
    await aller(page, '/briques/nouvelle', 'form');
    await legende(page, 'Publier une brique : titre, catégorie, langage, code, mode d\'emploi');
    await pause(4000);
    await defiler(page, 1100);
    await legende(page, "Licence permissive obligatoire, et l'auteur confirme détenir les droits sur le code");
    await pause(4500);
  });

  // 5. Demandes -----------------------------------------------------------------
  await etape('Demandes', async () => {
    await aller(page, '/demandes', 'main');
    await legende(page, "Demandes : on décrit la brique qu'on cherche, la communauté propose des solutions");
    await pause(4000);
    // Une demande résolue montre la réponse acceptée ; sans aucune demande, on passe à la suite.
    const resolues = page.getByText('Demandes résolues', { exact: true }).first();
    if (await page.getByRole('link', { name: /^Voir la demande/ }).count() && await resolues.count()) {
      await cliquer(page, resolues);
      await attendreContenu(page);
      if (!(await page.getByRole('link', { name: /^Voir la demande/ }).count())) {
        await cliquer(page, page.getByText('Demandes ouvertes', { exact: true }).first());
        await attendreContenu(page);
      }
    }
    const voir = page.getByRole('link', { name: /^Voir la demande/ }).first();
    if (await voir.count()) {
      await cliquer(page, voir);
      await page.waitForURL(/\/demandes\/\d+/);
      await attendreContenu(page, 'h1');
      await legende(page, "Une réponse peut lier une brique existante ; l'auteur de la demande accepte la meilleure");
      await pause(3500);
      await defiler(page, 450);
      await pause(3500);
    }
  });

  // 6. Classement ---------------------------------------------------------------
  await etape('Classement', async () => {
    await aller(page, '/developpeurs/classement', 'table, ol, .classement');
    await legende(page, 'Classement : la réputation récompense les briques publiées, testées et les bonnes réponses');
    await pause(5000);
  });

  // 7. Administration -----------------------------------------------------------
  await etape('Administration', async () => {
    await deconnecter(page);
    await legende(page, '');
    await connecter(page, ADMIN.pseudo, ADMIN.mdp, '/admin/statistiques');
    await attendreContenu(page);
    await legende(page, "Administration : tableau de bord de l'activité de la plateforme");
    await pause(4500);
    await aller(page, '/admin/signalements', 'main');
    await legende(page, 'Modération : chaque signalement est examiné ; une brique peut être masquée puis republiée');
    await pause(3000);
    const examiner = page.getByRole('button', { name: /Examiner/ }).first();
    if (await examiner.count()) {
      await cliquer(page, examiner);
      await attendreContenu(page);
      await pause(1000);
      await defiler(page, 400);
      await pause(3500);
    }
  });

  // 8. Aperçu mobile ------------------------------------------------------------
  await etape('Vue mobile', async () => {
    await deconnecter(page); // l'aperçu montre le site tel qu'un visiteur le voit
    // Téléphone Android standard : 360 × 800 px (affiché à 80 % pour tenir dans la vidéo).
    await ecranLogos(page, `<div class="cote"><div><h2>Aussi sur mobile</h2>
      <p>Interface pensée pour un écran Android standard (360 × 800), avec une barre de navigation en bas et un bouton « + » contextuel.</p></div>
      <div class="telephone"><iframe id="tel" src="${URL_SITE}/briques"></iframe></div></div>`, 1500);
    // Le téléphone reste vide tant que la page n'est pas chargée, puis l'écran apparaît en fondu.
    const cadre = page.frameLocator('#tel');
    await cadre.locator('db-brique-carte').first().waitFor({ timeout: 30000 }).catch(() => {});
    await pause(800);
    await page.evaluate(() => { const f = document.getElementById('tel'); if (f) f.style.opacity = '1'; });
    await pause(3500);
    const bt = await page.locator('#tel').boundingBox();
    if (bt) await page.mouse.move(bt.x + bt.width / 2, bt.y + bt.height / 2, { steps: 20 });
    for (let i = 0; i < 14; i++) { await page.mouse.wheel(0, 70); await pause(110); }
    await pause(3500);
  });

  await ecranFin(page);

  const video = page.video();
  await contexte.close();
  await navigateur.close();

  const chemin = video ? await video.path() : join(dossierTmp, readdirSync(dossierTmp)[0]);
  rmSync(sortie, { force: true });
  renameSync(chemin, sortie);
  rmSync(dossierTmp, { recursive: true, force: true });
  if (titreBrique) console.log(`\nBrique montrée : ${titreBrique}`);
  console.log(`Vidéo enregistrée : ${sortie}`);
  console.log('Pour la convertir en MP4 (facultatif) : ffmpeg -i devbrik-demo.webm -c:v libx264 -crf 23 -preset veryfast devbrik-demo.mp4');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});

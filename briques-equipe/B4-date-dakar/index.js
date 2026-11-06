'use strict';

/**
 * B4 — Date et heure au format Dakar (Africa/Dakar, UTC+0 toute l'année).
 *
 * Pas de décalage été/hiver : Africa/Dakar reste à UTC+0 en permanence.
 */

const FUSEAU = 'Africa/Dakar';
const LOCALE  = 'fr-SN';

const FORMATEUR_DATE = new Intl.DateTimeFormat(LOCALE, {
  timeZone: FUSEAU,
  weekday: 'long',
  year:    'numeric',
  month:   'long',
  day:     'numeric',
});

const FORMATEUR_HEURE = new Intl.DateTimeFormat(LOCALE, {
  timeZone: FUSEAU,
  hour:     '2-digit',
  minute:   '2-digit',
  second:   '2-digit',
  hour12:   false,
});

/**
 * Normalise une entrée en objet Date valide.
 * @param {Date|string|number} entree
 * @returns {Date}
 * @throws {TypeError}
 */
function normaliser(entree) {
  if (entree == null) {
    throw new TypeError('La date ne peut pas être null ou undefined');
  }
  const d = entree instanceof Date ? entree : new Date(entree);
  if (isNaN(d.getTime())) {
    throw new TypeError(`Date invalide : ${entree}`);
  }
  return d;
}

/**
 * Retourne la date formatée en français pour le fuseau Africa/Dakar.
 * @param {Date|string|number} date
 * @returns {string}  ex. "mardi 1 janvier 2025"
 */
function formaterDateDakar(date) {
  return FORMATEUR_DATE.format(normaliser(date));
}

/**
 * Retourne l'heure formatée (HH:mm:ss) pour le fuseau Africa/Dakar.
 * @param {Date|string|number} date
 * @returns {string}  ex. "14:05:03"
 */
function formaterHeureDakar(date) {
  return FORMATEUR_HEURE.format(normaliser(date));
}

/**
 * Retourne date et heure combinées.
 * @param {Date|string|number} date
 * @returns {string}  ex. "mardi 1 janvier 2025 à 14:05:03"
 */
function formaterDateHeureDakar(date) {
  const d = normaliser(date);
  return `${formaterDateDakar(d)} à ${formaterHeureDakar(d)}`;
}

/**
 * Retourne les composants individuels de la date dans le fuseau Dakar.
 * @param {Date|string|number} date
 * @returns {{ annee: number, mois: number, jour: number, heure: number, minute: number, seconde: number }}
 */
function composantsDakar(date) {
  const d = normaliser(date);
  const parties = new Intl.DateTimeFormat('en-US', {
    timeZone: FUSEAU,
    year:     'numeric',
    month:    'numeric',
    day:      'numeric',
    hour:     'numeric',
    minute:   'numeric',
    second:   'numeric',
    hour12:   false,
  }).formatToParts(d);

  const valeur = (type) => {
    const p = parties.find(x => x.type === type);
    // Intl renvoie "24" pour minuit en hour12:false — corriger en 0
    const v = p ? parseInt(p.value, 10) : 0;
    return type === 'hour' && v === 24 ? 0 : v;
  };

  return {
    annee:   valeur('year'),
    mois:    valeur('month'),
    jour:    valeur('day'),
    heure:   valeur('hour'),
    minute:  valeur('minute'),
    seconde: valeur('second'),
  };
}

module.exports = {
  formaterDateDakar,
  formaterHeureDakar,
  formaterDateHeureDakar,
  composantsDakar,
};

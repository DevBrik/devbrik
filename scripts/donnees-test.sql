-- =====================================================================================
-- DevBrik : données de TEST locales (comptes, briques, avis, tests, demandes, réponses,
-- signalements). À exécuter sur la base « devbrik » APRÈS un premier démarrage du backend
-- (Flyway doit avoir créé les tables et les référentiels V1, V2, V4).
--
--   psql -U devbrik -d devbrik -f scripts/donnees-test.sql
--
-- Comptes : bousso, fleury, ndieme, magou (développeurs) et test.admin (administrateur)
-- Mot de passe de TOUS les comptes créés : Test2026!   (haché en BCrypt par pgcrypto)
-- Le script est rejouable : rien n'est créé deux fois.
-- NE PAS exécuter en production.
-- =====================================================================================

BEGIN;

-- pgcrypto fournit crypt() et gen_salt('bf') : hachage BCrypt ($2a$), lu par Spring Security.
-- Extension « de confiance » depuis PostgreSQL 13 : le propriétaire de la base peut la créer.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- -------------------------------------------------------------------------------------
-- 0. Base déjà remplie par l'ancienne version du script (pseudos test.*) :
--    on renomme les comptes au lieu d'en créer de nouveaux. Sans effet sinon.
-- -------------------------------------------------------------------------------------
UPDATE utilisateur u
SET pseudo = v.nouveau, nom_affiche = NULL
FROM (VALUES
    ('test.awa',      'bousso'),
    ('test.moussa',   'fleury'),
    ('test.fatou',    'ndieme'),
    ('test.ibrahima', 'magou')
) AS v(ancien, nouveau)
WHERE lower(u.pseudo) = lower(v.ancien)
  AND NOT EXISTS (
      SELECT 1 FROM utilisateur x
      WHERE lower(x.pseudo) = lower(v.nouveau) AND x.id_utilisateur <> u.id_utilisateur
  );

-- -------------------------------------------------------------------------------------
-- 1. Comptes (RG1 : pseudo valide ; RG2 : mot de passe haché)
--    bousso, fleury, ndieme, magou : développeurs ; test.admin : administrateur
-- -------------------------------------------------------------------------------------
INSERT INTO utilisateur (pseudo, nom_affiche, mot_de_passe, role, pays, bio, lien_profil, date_creation)
SELECT v.pseudo, v.nom, crypt('Test2026!', gen_salt('bf', 10)), v.role, v.pays, v.bio, v.lien, now() - v.age
FROM (VALUES
    ('bousso',    NULL,             'DEVELOPPEUR', 'Sénégal',       'Développeuse backend à Dakar, passionnée par les paiements mobiles.', 'https://exemple.sn', interval '20 days'),
    ('fleury',      NULL,            'DEVELOPPEUR', 'Sénégal',       'Étudiant en informatique de gestion, je partage mes utilitaires.',     NULL,                 interval '18 days'),
    ('ndieme',      NULL,            'DEVELOPPEUR', 'Côte d''Ivoire', 'Développeuse mobile, Kotlin et Flutter.',                            NULL,                 interval '15 days'),
    ('magou',       NULL,            'DEVELOPPEUR', 'Mali',          NULL,                                                                   NULL,                 interval '10 days'),
    ('test.admin',  'Admin de test',  'ADMIN',       'Sénégal',       NULL,                                                                   NULL,                 interval '25 days')
) AS v(pseudo, nom, role, pays, bio, lien, age)
WHERE NOT EXISTS (SELECT 1 FROM utilisateur u WHERE lower(u.pseudo) = lower(v.pseudo));

-- -------------------------------------------------------------------------------------
-- 2. Briques (RG3 longueurs, RG4 licence, RG5 titre unique par auteur)
-- -------------------------------------------------------------------------------------
INSERT INTO brique (titre, resume, code, mode_emploi, prerequis, licence, statut, version_code,
                    date_creation, date_modification, id_categorie, id_langage, id_auteur)
SELECT v.titre, v.resume, v.code, v.mode, v.prereq, v.licence, v.statut, v.version,
       now() - v.age, now() - v.age + interval '1 day',
       (SELECT id_categorie FROM categorie WHERE slug = v.cat),
       (SELECT id_langage   FROM langage   WHERE slug = v.lang),
       (SELECT id_utilisateur FROM utilisateur WHERE pseudo = v.auteur)
FROM (VALUES
  ('Validation des numéros sénégalais',
   'Valide et normalise un numéro de téléphone mobile sénégalais au format E.164.',
   E'public final class Telephone {\n    public static boolean estValide(String n) {\n        return n != null && n.replaceAll("\\\\s", "").matches("^(\\\\+221|00221)?7[05678]\\\\d{7}$");\n    }\n}',
   E'Copier la classe Telephone dans votre projet.\nAppeler Telephone.estValide("77 123 45 67").\nLimites : numéros mobiles uniquement.',
   E'Java 17 ou plus\nAucune dépendance',
   'MIT', 'PUBLIEE', 1, 'validation-donnees', 'java', 'bousso', interval '14 days'),

  ('Formatage d''un montant en FCFA',
   'Formate un montant en francs CFA avec séparateur de milliers : 1 500 000 FCFA.',
   E'export function formaterFcfa(montant: number): string {\n  return Math.round(montant).toString().replace(/\\B(?=(\\d{3})+(?!\\d))/g, \' \') + \' FCFA\';\n}',
   E'Copier la fonction dans votre projet.\nAppeler formaterFcfa(1500000).\nLe montant est arrondi à l''unité.',
   'TypeScript 5',
   'MIT', 'PUBLIEE', 2, 'formats-devises', 'typescript', 'fleury', interval '12 days'),

  ('Date et heure en Africa/Dakar',
   'Affiche une date en français dans le fuseau Africa/Dakar, quel que soit le fuseau du serveur.',
   E'const f = new Intl.DateTimeFormat(\'fr-SN\', { timeZone: \'Africa/Dakar\', dateStyle: \'full\', timeStyle: \'short\' });\nexport const dateDakar = (d) => f.format(new Date(d));',
   E'Importer dateDakar.\nAppeler dateDakar(Date.now()).\nDakar reste à UTC+0 toute l''année.',
   'Node.js 18 ou un navigateur récent',
   'MIT', 'PUBLIEE', 1, 'formats-devises', 'javascript', 'bousso', interval '9 days'),

  ('Signature HMAC-SHA256 de webhook',
   'Vérifie en temps constant la signature HMAC-SHA256 d''un rappel HTTP reçu.',
   E'import hmac, hashlib\n\ndef verifier(secret: bytes, corps: bytes, signature: str) -> bool:\n    attendue = hmac.new(secret, corps, hashlib.sha256).hexdigest()\n    return hmac.compare_digest(attendue, signature)',
   E'Lire le corps brut de la requête.\nAppeler verifier(secret, corps, entete).\nRefuser la requête si le résultat est faux.',
   'Python 3.8 ou plus',
   'APACHE_2_0', 'PUBLIEE', 1, 'securite-authentification', 'python', 'ndieme', interval '7 days'),

  ('Réessai avec attente exponentielle',
   'Relance une requête réseau qui échoue avec des attentes croissantes et un plafond.',
   E'export async function reessayer<T>(f: () => Promise<T>, n = 5, d = 500): Promise<T> {\n  try { return await f(); }\n  catch (e) { if (n <= 1) throw e; await new Promise(r => setTimeout(r, d)); return reessayer(f, n - 1, d * 2); }\n}',
   E'Envelopper l''appel réseau : reessayer(() => fetch(url)).\nAjuster le nombre d''essais et le délai initial.',
   NULL,
   'MIT', 'PUBLIEE', 1, 'reseau-instable', 'typescript', 'fleury', interval '5 days'),

  ('Distance entre deux points GPS',
   'Calcule la distance en kilomètres entre deux coordonnées GPS avec la formule de haversine.',
   E'CREATE FUNCTION distance_km(a1 float8, o1 float8, a2 float8, o2 float8) RETURNS float8 LANGUAGE sql AS $$\n  SELECT 2 * 6371 * asin(sqrt(power(sin(radians(a2 - a1) / 2), 2) + cos(radians(a1)) * cos(radians(a2)) * power(sin(radians(o2 - o1) / 2), 2)));\n$$;',
   E'Exécuter le script dans votre base.\nSELECT distance_km(14.69, -17.44, 16.02, -16.49);',
   'PostgreSQL 12 ou plus',
   'MIT', 'PUBLIEE', 1, 'donnees-geographiques', 'sql', 'ndieme', interval '3 days'),

  ('Masquage de numéro dans les journaux',
   'Masque les numéros de téléphone avant de les écrire dans les journaux applicatifs.',
   E'fun masquer(n: String): String {\n    val c = n.filter { it.isDigit() }\n    return if (c.length < 6) "*".repeat(c.length) else c.take(2) + "*".repeat(c.length - 4) + c.takeLast(2)\n}',
   E'Copier la fonction masquer.\nAppeler masquer("771234567") avant de journaliser.',
   'Kotlin 1.9 ou plus',
   'MIT', 'PUBLIEE', 1, 'securite-authentification', 'kotlin', 'magou', interval '2 days'),

  ('Script de test masqué <script>alert(1)</script>',
   'Brique masquée pour tester la modération et l''affichage en texte brut (RG7, RG18).',
   E'<script>alert(''xss'')</script>\necho "ce code ne doit jamais être exécuté"',
   E'Cette brique sert uniquement aux tests.\nElle est masquée : seuls son auteur et les administrateurs la voient.',
   NULL,
   'CC0_1_0', 'MASQUEE', 1, 'outils', 'shell', 'magou', interval '1 day')
) AS v(titre, resume, code, mode, prereq, licence, statut, version, cat, lang, auteur, age)
WHERE NOT EXISTS (
    SELECT 1 FROM brique b
    WHERE b.id_auteur = (SELECT id_utilisateur FROM utilisateur WHERE pseudo = v.auteur)
      AND lower(b.titre) = lower(v.titre));

-- -------------------------------------------------------------------------------------
-- 3. Avis (RG8 : un par utilisateur et par brique, jamais sur sa propre brique)
-- -------------------------------------------------------------------------------------
INSERT INTO avis (note, commentaire, date_creation, date_modification, id_brique, id_auteur)
SELECT v.note, v.commentaire, now() - v.age, now() - v.age,
       (SELECT id_brique FROM brique WHERE titre = v.titre),
       (SELECT id_utilisateur FROM utilisateur WHERE pseudo = v.auteur)
FROM (VALUES
    ('Validation des numéros sénégalais', 'fleury',   5, 'Fonction simple et facile à intégrer dans mon projet.', interval '10 days'),
    ('Validation des numéros sénégalais', 'ndieme',    4, 'Bonne base, la documentation pourrait être plus détaillée.', interval '8 days'),
    ('Validation des numéros sénégalais', 'magou', 5, NULL, interval '6 days'),
    ('Formatage d''un montant en FCFA',   'bousso',      4, 'Pratique pour nos factures.', interval '9 days'),
    ('Signature HMAC-SHA256 de webhook',  'bousso',      5, 'Exactement ce qu''il me fallait.', interval '4 days'),
    ('Réessai avec attente exponentielle','ndieme',    3, 'Il manque une option pour ne pas relancer les erreurs 400.', interval '2 days')
) AS v(titre, auteur, note, commentaire, age)
ON CONFLICT (id_brique, id_auteur) DO NOTHING;

-- -------------------------------------------------------------------------------------
-- 4. Tests (RG9 : version du code de la brique ; pas sur sa propre brique)
--    Pour « Formatage FCFA » (version 2), un test porte sur la version 1 : il ne doit PAS compter.
-- -------------------------------------------------------------------------------------
INSERT INTO test_brique (resultat, environnement, version_code, date_test, id_brique, id_auteur)
SELECT v.resultat, v.env, COALESCE(v.version, b.version_code), now() - v.age, b.id_brique,
       (SELECT id_utilisateur FROM utilisateur WHERE pseudo = v.auteur)
FROM (VALUES
    ('Validation des numéros sénégalais', 'fleury',   'OK', 'Java 21, Windows 11',        NULL::int, interval '10 days'),
    ('Validation des numéros sénégalais', 'ndieme',    'OK', 'Java 17, Ubuntu 24.04',      NULL,      interval '7 days'),
    ('Validation des numéros sénégalais', 'magou', 'KO', 'Java 11',                    NULL,      interval '6 days'),
    ('Formatage d''un montant en FCFA',   'bousso',      'OK', 'Node.js 22 (version 1)',     1,         interval '11 days'),
    ('Formatage d''un montant en FCFA',   'ndieme',    'OK', 'Node.js 22, TypeScript 5.6', NULL,      interval '1 day'),
    ('Date et heure en Africa/Dakar',     'fleury',   'OK', 'Chrome 130',                 NULL,      interval '5 days'),
    ('Signature HMAC-SHA256 de webhook',  'bousso',      'OK', 'Python 3.12',                NULL,      interval '4 days'),
    ('Réessai avec attente exponentielle','bousso',      'KO', 'Node.js 18',                 NULL,      interval '2 days'),
    ('Réessai avec attente exponentielle','magou', 'KO', 'Deno 2',                     NULL,      interval '1 day')
) AS v(titre, auteur, resultat, env, version, age)
JOIN brique b ON b.titre = v.titre
ON CONFLICT (id_brique, id_auteur) DO NOTHING;

-- -------------------------------------------------------------------------------------
-- 5. Demandes (RG12) : deux ouvertes, une résolue
-- -------------------------------------------------------------------------------------
INSERT INTO demande (titre, description, statut, date_creation, id_categorie, id_langage, id_auteur)
SELECT v.titre, v.description, v.statut, now() - v.age,
       (SELECT id_categorie FROM categorie WHERE slug = v.cat),
       (SELECT id_langage FROM langage WHERE slug = v.lang),
       (SELECT id_utilisateur FROM utilisateur WHERE pseudo = v.auteur)
FROM (VALUES
    ('Générer un montant en lettres',
     E'Bonjour,\n\nJe recherche une fonction Java qui convertit un montant en toutes lettres pour mes factures en FCFA.\nExemple : 15000 doit donner « quinze mille francs CFA ».',
     'RESOLUE', 'formats-devises', 'java', 'fleury', interval '8 days'),
    ('Envoyer des SMS sans fournisseur payant',
     'Existe-t-il une brique pour mettre en file des notifications et les envoyer quand le réseau revient ?',
     'OUVERTE', 'sms-notifications', 'javascript', 'ndieme', interval '4 days'),
    ('Valider un NINEA sénégalais',
     'Je cherche une validation du format du NINEA (identifiant des entreprises) pour un formulaire d''inscription.',
     'OUVERTE', 'validation-donnees', NULL, 'magou', interval '1 day')
) AS v(titre, description, statut, cat, lang, auteur, age)
WHERE NOT EXISTS (SELECT 1 FROM demande d WHERE d.titre = v.titre);

-- -------------------------------------------------------------------------------------
-- 6. Réponses (RG12 : une seule acceptée, jamais la sienne ; RG13 : lien vers une brique publiée)
-- -------------------------------------------------------------------------------------
INSERT INTO reponse (contenu, acceptee, date_creation, id_demande, id_brique, id_auteur)
SELECT v.contenu, v.acceptee, now() - v.age,
       (SELECT id_demande FROM demande WHERE titre = v.demande),
       (SELECT id_brique FROM brique WHERE titre = v.brique),
       (SELECT id_utilisateur FROM utilisateur WHERE pseudo = v.auteur)
FROM (VALUES
    ('Générer un montant en lettres', 'J''ai publié une brique de formatage FCFA qui peut servir de base.', TRUE,  'Formatage d''un montant en FCFA', 'bousso',   interval '7 days'),
    ('Générer un montant en lettres', 'Je propose une approche avec une table de correspondance.',        FALSE, NULL,                              'ndieme', interval '6 days'),
    ('Envoyer des SMS sans fournisseur payant', 'Regarde la brique de réessai, elle se combine bien avec une file.', FALSE, 'Réessai avec attente exponentielle', 'bousso', interval '3 days')
) AS v(demande, contenu, acceptee, brique, auteur, age)
WHERE NOT EXISTS (
    SELECT 1 FROM reponse r
    WHERE r.id_demande = (SELECT id_demande FROM demande WHERE titre = v.demande) AND r.contenu = v.contenu);

-- -------------------------------------------------------------------------------------
-- 7. Signalements (RG11) : deux ouverts pour l'écran d'administration
-- -------------------------------------------------------------------------------------
INSERT INTO signalement_brique (motif, details, statut, date_creation, id_brique, id_auteur)
SELECT v.motif, v.details, 'OUVERT', now() - v.age,
       (SELECT id_brique FROM brique WHERE titre = v.titre),
       (SELECT id_utilisateur FROM utilisateur WHERE pseudo = v.auteur)
FROM (VALUES
    ('Masquage de numéro dans les journaux', 'fleury', 'INEXACT',        'Le masquage ne garde pas l''indicatif +221.', interval '1 day'),
    ('Réessai avec attente exponentielle',   'bousso',    'CODE_DANGEREUX', NULL,                                          interval '12 hours')
) AS v(titre, auteur, motif, details, age)
ON CONFLICT (id_brique, id_auteur) DO NOTHING;

COMMIT;

-- Contrôle rapide
SELECT (SELECT count(*) FROM utilisateur WHERE pseudo IN ('bousso', 'fleury', 'ndieme', 'magou', 'test.admin')) AS comptes,
       (SELECT count(*) FROM brique)             AS briques,
       (SELECT count(*) FROM avis)               AS avis,
       (SELECT count(*) FROM test_brique)        AS tests,
       (SELECT count(*) FROM demande)            AS demandes,
       (SELECT count(*) FROM reponse)            AS reponses,
       (SELECT count(*) FROM signalement_brique) AS signalements;

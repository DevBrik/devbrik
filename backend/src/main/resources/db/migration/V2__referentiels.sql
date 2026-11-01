INSERT INTO categorie (slug, nom, description) VALUES
 ('paiement-mobile', 'Paiement mobile', 'Utilitaires et intégrations liés aux paiements mobiles'),
 ('sms-notifications', 'SMS et notifications', 'Envoi de messages et notifications'),
 ('validation-donnees', 'Validation de données', 'Numéros, identifiants et formats locaux'),
 ('formats-devises', 'Formats, dates et devises', 'Montants en FCFA, dates et fuseaux horaires'),
 ('donnees-geographiques', 'Données géographiques', 'Régions, départements et communes'),
 ('securite-authentification', 'Sécurité et authentification', 'Signatures, jetons et bonnes pratiques'),
 ('reseau-instable', 'Réseaux instables et hors ligne', 'Réessais, reprises et compression'),
 ('outils', 'Outils divers', 'Autres utilitaires')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO langage (slug, nom) VALUES
 ('java', 'Java'), ('typescript', 'TypeScript'), ('javascript', 'JavaScript'),
 ('python', 'Python'), ('php', 'PHP'), ('sql', 'SQL'), ('csharp', 'C#'),
 ('kotlin', 'Kotlin'), ('dart', 'Dart'), ('go', 'Go'), ('shell', 'Shell')
ON CONFLICT (slug) DO NOTHING;
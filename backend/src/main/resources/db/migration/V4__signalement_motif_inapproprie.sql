ALTER TABLE signalement_brique DROP CONSTRAINT ck_signalement_motif;
ALTER TABLE signalement_brique ADD CONSTRAINT ck_signalement_motif
    CHECK (motif IN ('CODE_DANGEREUX', 'DROITS_AUTEUR', 'SPAM', 'INEXACT', 'INAPPROPRIE', 'AUTRE'));
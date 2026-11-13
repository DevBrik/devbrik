-- B7 · Validation de coordonnées GPS et distance (PostgreSQL)
-- Remplace « Régions et départements du Sénégal » tant que la source officielle et la licence
-- des données ne sont pas vérifiées (dossier de référence, sections 16 et 19, V9).
-- Licence : MIT.

-- Vrai si (latitude, longitude) est une coordonnée WGS 84 valide (degrés décimaux).
CREATE OR REPLACE FUNCTION coordonnee_valide(latitude double precision, longitude double precision)
RETURNS boolean
LANGUAGE sql IMMUTABLE PARALLEL SAFE AS $$
    SELECT latitude IS NOT NULL AND longitude IS NOT NULL
       AND latitude  BETWEEN -90  AND 90
       AND longitude BETWEEN -180 AND 180
       AND latitude  <> 'NaN'::double precision
       AND longitude <> 'NaN'::double precision;
$$;

-- Distance du grand cercle en kilomètres (formule de haversine, sphère de rayon 6 371 km).
-- Renvoie NULL si l'une des coordonnées est invalide.
CREATE OR REPLACE FUNCTION distance_km(lat1 double precision, lon1 double precision,
                                       lat2 double precision, lon2 double precision)
RETURNS double precision
LANGUAGE sql IMMUTABLE PARALLEL SAFE AS $$
    SELECT CASE
        WHEN NOT (coordonnee_valide(lat1, lon1) AND coordonnee_valide(lat2, lon2)) THEN NULL
        ELSE 2 * 6371.0 * asin(sqrt(
                 power(sin(radians(lat2 - lat1) / 2), 2)
               + cos(radians(lat1)) * cos(radians(lat2)) * power(sin(radians(lon2 - lon1) / 2), 2)))
    END;
$$;

-- Exemple de contrainte : une table qui n'accepte que des coordonnées valides.
-- CREATE TABLE point_livraison (
--     id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
--     latitude  double precision NOT NULL,
--     longitude double precision NOT NULL,
--     CONSTRAINT ck_point_coordonnees CHECK (coordonnee_valide(latitude, longitude))
-- );

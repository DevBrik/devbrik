-- Tests de B7 : psql -v ON_ERROR_STOP=1 -f coordonnees.sql -f tests.sql
-- Les valeurs attendues découlent de la géométrie de la sphère (rayon R = 6 371 km) :
--   1 degré de méridien = π R / 180 ≈ 111,195 km ; un quart d'équateur = π R / 2 ≈ 10 007,543 km.
DO $$
BEGIN
    ASSERT coordonnee_valide(14.6928, -17.4467), 'Dakar doit être valide';
    ASSERT coordonnee_valide(-90, 180) AND coordonnee_valide(90, -180), 'les bornes sont incluses';
    ASSERT NOT coordonnee_valide(91, 0), 'latitude > 90 refusée';
    ASSERT NOT coordonnee_valide(0, -180.0001), 'longitude < -180 refusée';
    ASSERT NOT coordonnee_valide(NULL, 0), 'NULL refusé';
    ASSERT NOT coordonnee_valide('NaN', 0), 'NaN refusé';

    ASSERT distance_km(14.6928, -17.4467, 14.6928, -17.4467) = 0, 'distance nulle';
    ASSERT abs(distance_km(0, 0, 1, 0) - 111.195) < 0.001, '1 degré de méridien';
    ASSERT abs(distance_km(0, 0, 0, 90) - 10007.543) < 0.001, 'quart d''équateur';
    ASSERT abs(distance_km(10, 20, 30, 40) - distance_km(30, 40, 10, 20)) < 1e-9, 'symétrie';
    ASSERT distance_km(100, 0, 0, 0) IS NULL, 'coordonnée invalide : NULL';
    RAISE NOTICE 'B7 : tous les tests sont passés';
END $$;

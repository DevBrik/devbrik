package sn.devbrik.backend.avis.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.devbrik.backend.avis.dto.StatsBrique;
import sn.devbrik.backend.avis.repository.AgregatAvis;
import sn.devbrik.backend.avis.repository.AgregatTests;
import sn.devbrik.backend.avis.repository.AvisRepository;
import sn.devbrik.backend.avis.repository.TestBriqueRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * Service public du module avis (13.4), utilisé par les membres 2 et 4. Lecture
 * seule.
 */
@Service
public class StatsBriqueService {

    private final AvisRepository avisRepository;
    private final TestBriqueRepository testBriqueRepository;

    public StatsBriqueService(AvisRepository avisRepository, TestBriqueRepository testBriqueRepository) {
        this.avisRepository = avisRepository;
        this.testBriqueRepository = testBriqueRepository;
    }

    /**
     * Statistiques de chaque brique demandée. Chaque id reçu a une entrée dans le
     * résultat,
     * avec des valeurs neutres s'il n'a ni avis ni test de la version actuelle.
     */
    @Transactional(readOnly = true)
    public Map<Long, StatsBrique> statistiques(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        Set<Long> distincts = new LinkedHashSet<>(ids);
        distincts.remove(null);
        if (distincts.isEmpty()) {
            return Map.of();
        }

        Map<Long, AgregatAvis> avis = avisRepository.agreger(distincts).stream()
                .collect(Collectors.toMap(AgregatAvis::getIdBrique, Function.identity()));
        Map<Long, AgregatTests> tests = testBriqueRepository.agreger(distincts).stream()
                .collect(Collectors.toMap(AgregatTests::getIdBrique, Function.identity()));

        Map<Long, StatsBrique> resultat = new LinkedHashMap<>();
        for (Long id : distincts) {
            AgregatAvis a = avis.get(id);
            AgregatTests t = tests.get(id);
            long nombreAvis = a == null ? 0L : valeur(a.getNombre());
            Double moyenne = a == null ? null : moyenne(valeur(a.getSomme()), nombreAvis);
            long ok = t == null ? 0L : valeur(t.getOk());
            long ko = t == null ? 0L : valeur(t.getKo());
            Instant derniere = t == null ? null : versInstant(t.getDerniere());
            resultat.put(id, new StatsBrique(moyenne, nombreAvis, ok, ko, derniere));
        }
        return Collections.unmodifiableMap(resultat);
    }

    /** Tests OK de la version actuelle reçus sur les briques de l'auteur (RG14). */
    @Transactional(readOnly = true)
    public long compterTestsOkRecus(Long idAuteur) {
        if (idAuteur == null) {
            return 0L;
        }
        return testBriqueRepository.compterOkRecusVersionActuelle(idAuteur);
    }

    /**
     * RG8 : moyenne arrondie à une décimale (calcul exact, demi vers le haut), null
     * sans avis.
     */
    static Double moyenne(long somme, long nombre) {
        if (nombre <= 0) {
            return null;
        }
        return BigDecimal.valueOf(somme)
                .divide(BigDecimal.valueOf(nombre), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static long valeur(Long v) {
        return v == null ? 0L : v;
    }

    private static Instant versInstant(Object valeur) {
        if (valeur == null) {
            return null;
        }
        if (valeur instanceof Instant i) {
            return i;
        }
        if (valeur instanceof OffsetDateTime o) {
            return o.toInstant();
        }
        if (valeur instanceof ZonedDateTime z) {
            return z.toInstant();
        }
        if (valeur instanceof java.util.Date d) { // couvre java.sql.Timestamp
            return d.toInstant();
        }
        throw new IllegalStateException("Type de date inattendu : " + valeur.getClass().getName());
    }
}
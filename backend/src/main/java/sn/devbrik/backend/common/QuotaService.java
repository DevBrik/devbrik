package sn.devbrik.backend.common;

import org.springframework.stereotype.Service;

@Service
public class QuotaService {

    /** actionsDansLes24h = nombre d'actions déjà faites par l'utilisateur sur 24 h glissantes (avant celle-ci). */
    public void verifier(TypeQuota type, long actionsDansLes24h) {
        if (actionsDansLes24h >= type.limite()) {
            throw new ApiException(CodeErreur.LIMITE_QUOTIDIENNE,
                    "Limite quotidienne atteinte : " + type.limite() + " " + type.libelle()
                            + " par 24 heures. Réessayez plus tard.");
        }
    }
}
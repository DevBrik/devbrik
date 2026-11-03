package sn.devbrik.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

public record ReponseErreur(
        int statut,
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<Detail> details,
        String horodatage) {

    public record Detail(String champ, String message) {}

    public static ReponseErreur de(CodeErreur code, String message, List<Detail> details) {
        return new ReponseErreur(code.statut().value(), code.name(), message, details, Instant.now().toString());
    }
}
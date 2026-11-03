package sn.devbrik.backend.common;

/** Seule exception métier : un code du catalogue 11.7 et un message en français. */
public class ApiException extends RuntimeException {

    private final CodeErreur code;

    public ApiException(CodeErreur code, String message) {
        super(message);
        this.code = code;
    }

    public CodeErreur getCode() {
        return code;
    }
}
package sn.devbrik.backend.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.springframework.stereotype.Service;
import sn.devbrik.backend.common.IdentiteUtilisateur;

@Service
public class JwtService {

    private final byte[] cle;
    private final Duration duree;

    public JwtService(JwtConfig config) {
        if (config.getSecret() == null || config.getSecret().length() < 32) {
            throw new IllegalStateException("app.jwt.secret (JWT_SECRET) doit contenir au moins 32 caractères.");
        }
        this.cle = config.getSecret().getBytes(StandardCharsets.UTF_8);
        this.duree = Duration.ofHours(config.getDureeHeures());
    }

    /** Utilisé par le membre 5 à l'inscription et à la connexion. */
    public String generer(Long idUtilisateur, String role) {
        Instant maintenant = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(idUtilisateur))
                .claim("role", role)
                .issueTime(Date.from(maintenant))
                .expirationTime(Date.from(maintenant.plus(duree)))
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(cle));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Impossible de signer le jeton", e);
        }
    }

    /** Renvoie l'identité si le jeton est valide (signature, algorithme, expiration), sinon vide. */
    public Optional<IdentiteUtilisateur> lire(String jeton) {
        try {
            SignedJWT jwt = SignedJWT.parse(jeton);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(cle))) {
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            String role = claims.getStringClaim("role");
            if (expiration == null || expiration.before(new Date()) || role == null) {
                return Optional.empty();
            }
            return Optional.of(new IdentiteUtilisateur(Long.valueOf(claims.getSubject()), role));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
package sn.devbrik.backend.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import sn.devbrik.backend.common.IdentiteUtilisateur;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Jeton (RG2, 11.1) : signature, algorithme, expiration (12 heures). */
class JwtServiceTest {

    private static final String SECRET = "cle-de-test-uniquement-au-moins-32-caracteres-0123456789";
    private static final String AUTRE_SECRET = "une-autre-cle-de-test-au-moins-32-caracteres-9876543210";

    private static JwtService service(String secret, long dureeHeures) {
        JwtConfig config = new JwtConfig();
        config.setSecret(secret);
        config.setDureeHeures(dureeHeures);
        return new JwtService(config);
    }

    @Test
    void un_jeton_genere_est_relu_avec_l_identifiant_et_le_role() {
        JwtService jwt = service(SECRET, 12);

        Optional<IdentiteUtilisateur> identite = jwt.lire(jwt.generer(42L, "DEVELOPPEUR"));

        assertThat(identite).contains(new IdentiteUtilisateur(42L, "DEVELOPPEUR"));
    }

    @Test
    void le_role_administrateur_est_conserve() {
        JwtService jwt = service(SECRET, 12);

        IdentiteUtilisateur identite = jwt.lire(jwt.generer(1L, "ADMIN")).orElseThrow();

        assertThat(identite.estAdmin()).isTrue();
    }

    @Test
    void un_jeton_est_valable_12_heures() throws Exception {
        String jeton = service(SECRET, 12).generer(7L, "DEVELOPPEUR");

        JWTClaimsSet claims = SignedJWT.parse(jeton).getJWTClaimsSet();

        long ecart = claims.getExpirationTime().getTime() - claims.getIssueTime().getTime();
        assertThat(ecart).isEqualTo(Duration.ofHours(12).toMillis());
        assertThat(claims.getSubject()).isEqualTo("7");
    }

    @Test
    void un_jeton_expire_est_refuse() {
        JwtService expire = service(SECRET, -1);

        assertThat(expire.lire(expire.generer(7L, "DEVELOPPEUR"))).isEmpty();
    }

    @Test
    void un_jeton_signe_avec_une_autre_cle_est_refuse() {
        String jeton = service(AUTRE_SECRET, 12).generer(7L, "DEVELOPPEUR");

        assertThat(service(SECRET, 12).lire(jeton)).isEmpty();
    }

    @Test
    void un_jeton_dont_la_signature_est_alteree_est_refuse() {
        JwtService jwt = service(SECRET, 12);
        String jeton = jwt.generer(7L, "DEVELOPPEUR");
        int debutSignature = jeton.lastIndexOf('.') + 1;
        char premier = jeton.charAt(debutSignature);
        String falsifie = jeton.substring(0, debutSignature) + (premier == 'A' ? 'B' : 'A')
                + jeton.substring(debutSignature + 1);

        assertThat(jwt.lire(falsifie)).isEmpty();
    }

    @Test
    void un_jeton_dont_le_contenu_est_modifie_est_refuse() {
        JwtService jwt = service(SECRET, 12);
        String[] morceaux = jwt.generer(7L, "DEVELOPPEUR").split("\\.");
        String autreCharge = jwt.generer(8L, "ADMIN").split("\\.")[1];

        assertThat(jwt.lire(morceaux[0] + "." + autreCharge + "." + morceaux[2])).isEmpty();
    }

    @Test
    void un_algorithme_autre_que_HS256_est_refuse() throws Exception {
        String secretLong = SECRET + SECRET; // 64 octets au moins, requis par HS512
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("7").claim("role", "ADMIN")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build();
        SignedJWT jwt512 = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
        jwt512.sign(new MACSigner(secretLong.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        assertThat(service(secretLong, 12).lire(jwt512.serialize())).isEmpty();
    }

    @Test
    void un_texte_quelconque_est_refuse() {
        JwtService jwt = service(SECRET, 12);

        assertThat(jwt.lire("abc.def.ghi")).isEmpty();
        assertThat(jwt.lire("")).isEmpty();
        assertThat(jwt.lire(null)).isEmpty();
    }

    @Test
    void un_secret_de_moins_de_32_caracteres_empeche_le_demarrage() {
        assertThatThrownBy(() -> service("trop-court", 12))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service(null, 12))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service("", 12))
                .isInstanceOf(IllegalStateException.class);
    }
}

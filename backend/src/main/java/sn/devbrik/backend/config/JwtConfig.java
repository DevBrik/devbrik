package sn.devbrik.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    private String secret;
    private long dureeHeures = 12;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getDureeHeures() { return dureeHeures; }
    public void setDureeHeures(long dureeHeures) { this.dureeHeures = dureeHeures; }
}
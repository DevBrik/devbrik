package sn.devbrik.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sn.devbrik.backend.auth.service.UtilisateurService;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // RG2, utilisé par le module auth
    }

    @Bean
    SecurityFilterChain chaine(HttpSecurity http, JwtService jwt, ReponseErreurSecurite erreurs,
                               UtilisateurService utilisateurs) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // API sans cookie : le jeton est dans l'en-tête
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(erreurs).accessDeniedHandler(erreurs))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.POST, "/api/auth/inscription", "/api/auth/connexion").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/moi/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/langages",
                        "/api/briques/**", "/api/demandes/**", "/api/developpeurs/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtFiltre(jwt, erreurs, utilisateurs::existe), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
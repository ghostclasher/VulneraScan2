package com.example.SpringREST.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private RSAKey rsaKey;

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder encoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwks) {
        return new NimbusJwtEncoder(jwks);
    }

    // ===================== BULLETPROOF CORS =====================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // ✅ Works for ALL Vercel deployments + local dev
        cfg.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://*.vercel.app"
        ));

        // Allow everything modern apps need
        cfg.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow all headers typically sent by browsers + JWT
        cfg.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // Required when sending JWT cookies or Authorization headers
        cfg.setAllowCredentials(true);

        // Cache preflight for 1 hour (reduces browser checks)
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src =
                new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);

        return src;
    }

    // ===================== PUBLIC ENDPOINTS =====================
    @Bean
    @Order(1)
    public SecurityFilterChain publicEndpoints(HttpSecurity http)
            throws Exception {

        http
            // Apply to auth + allow all OPTIONS globally
            .securityMatcher(
                "/api/v1/auth/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/db-console/**",
                "/**"
            )

            // 🔥 CRITICAL: enables CORS for preflight
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless API = no CSRF tokens needed
            .csrf(csrf -> csrf.disable())

            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth ->
                auth.anyRequest().permitAll()
            );

        return http.build();
    }

    // ===================== SECURED ENDPOINTS =====================
    @Bean
    @Order(2)
    public SecurityFilterChain securedEndpoints(HttpSecurity http)
            throws Exception {

        http
            .securityMatcher("/api/**")

            // 🔥 Keep CORS here too
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            .headers(headers -> headers.frameOptions().disable())

            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/users")
                    .hasAuthority("SCOPE_ADMIN")

                .requestMatchers(
                    "/api/v1/auth/users/{user_id}/update-authorities/")
                    .hasAuthority("SCOPE_ADMIN")

                .requestMatchers("/api/v1/auth/profile/**")
                    .authenticated()

                .requestMatchers("/api/v1/album/{album_id}/upload-photos")
                    .authenticated()

                .requestMatchers("/api/gitleaks/**")
                    .authenticated()

                .anyRequest().authenticated()
            )

            // Keep your JWT resource server
            .oauth2ResourceServer(oauth -> oauth.jwt());

        return http.build();
    }
}

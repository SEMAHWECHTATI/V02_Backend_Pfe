package com.sagem.g2ii.config;

import com.sagem.g2ii.securiter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
            throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // 💡 AJOUT ICI : Autoriser la route d'erreur par défaut de Spring Boot
                        .requestMatchers("/error").permitAll()

                        // SWAGGER PUBLIC
                        .requestMatchers("/api/inventory/articles/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ROUTES PUBLIQUES
                        .requestMatchers("/authentification/**").permitAll()
                        .requestMatchers("/api/localisations/**").permitAll()
                        .requestMatchers("/api/fournisseurs/**").permitAll()
                        .requestMatchers("/api/consommations-pieces/**").permitAll()
                        .requestMatchers("/api/demandes-materiel/**").permitAll()
                        .requestMatchers("/api/inventory/**").permitAll()
                        .requestMatchers("/api/inventory/stocks/**").permitAll()
                        .requestMatchers("/api/equipements/**").permitAll()
                        .requestMatchers("/api/enumerations/**").permitAll()
                        .requestMatchers("/api/groupes/**").permitAll()
                        .requestMatchers("/api/demandes/**").permitAll()
                        .requestMatchers("/api/tickets/**").permitAll()
                        .requestMatchers("/api/stocks/dashboard-finance/**").permitAll()
                        .requestMatchers("/api/dashboard/**").permitAll()
                        .requestMatchers("/api/slas/**").permitAll()

                        // ADMIN / ROLES SPECIFIQUES
                        .requestMatchers("/api/categories/**")
                        .hasAnyRole("Administrateur", "Technicien", "Demandeur", "Gestionnaire_Stock")

                        .requestMatchers("/api/users/**")
                        .hasAnyRole("Administrateur", "Technicien", "Demandeur", "Gestionnaire_Stock")

                        // TOUT LE RESTE SECURISE
                        .anyRequest().authenticated()
                )

                // JWT FILTER
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
                List.of("*")
        );

        configuration.setAllowedMethods(
                Arrays.asList(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS",
                        "PATCH"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
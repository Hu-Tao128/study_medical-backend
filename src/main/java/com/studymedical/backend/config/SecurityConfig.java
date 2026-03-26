package com.studymedical.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final boolean devMode;

    public SecurityConfig(@Value("${app.dev-mode:false}") boolean devMode) {
        this.devMode = devMode;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        if (devMode) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/health", "/api/v1/auth/**", "/api/v1/profile/**", "/actuator/**").permitAll()
                    .anyRequest().authenticated()
            );
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/health", "/actuator/**").permitAll()
                    .anyRequest().authenticated()
            );
        }

        return http.build();
    }
}

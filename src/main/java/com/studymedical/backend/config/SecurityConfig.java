package com.studymedical.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
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
        BearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {
                            String path = request.getServletPath();
                            if ("/api/v1/auth/sync-session".equals(path)
                                    || path.startsWith("/api/v1/profile/")
                                    || path.startsWith("/api/v1/study-sessions")
                                    || path.startsWith("/api/v1/progress")) {
                                return null;
                            }
                            return defaultResolver.resolve(request);
                        })
                        .jwt(Customizer.withDefaults()));

        if (devMode) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/favicon.ico", "/api/v1/health", "/api/v1/auth/**", "/api/v1/profile/**", "/actuator/**").permitAll()
                    .anyRequest().authenticated()
            );
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/",
                            "/favicon.ico",
                            "/api/v1/health",
                            "/api/v1/auth/sync-session",
                            "/api/v1/profile/**",
                            "/api/v1/study-sessions/**",
                            "/api/v1/progress/**",
                            "/actuator/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            );
        }

        return http.build();
    }
}

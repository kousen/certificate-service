package com.kousen.cert.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Protects the analytics dashboard and stored-certificate listings with HTTP
 * basic auth when an admin password is configured (ADMIN_PASSWORD). With no
 * password set — e.g. local development and tests — all endpoints stay open.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PROTECTED_PATHS = {
            "/admin/**",
            "/api/analytics/**",
            "/api/certificates/stored",
            "/api/certificates/stored/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            @Value("${admin.password:}") String adminPassword) throws Exception {
        // The service exposes a stateless JSON/PDF API; CSRF tokens don't apply
        http.csrf(AbstractHttpConfigurer::disable);
        if (adminPassword.isBlank()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers(PROTECTED_PATHS).authenticated()
                            .anyRequest().permitAll())
                    .httpBasic(Customizer.withDefaults());
        }
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(@Value("${admin.username:admin}") String adminUsername,
                                          @Value("${admin.password:}") String adminPassword) {
        if (adminPassword.isBlank()) {
            return new InMemoryUserDetailsManager();
        }
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password("{noop}" + adminPassword)
                        .roles("ADMIN")
                        .build());
    }
}

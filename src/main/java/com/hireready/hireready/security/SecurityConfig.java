package com.hireready.hireready.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // Defines the security rules for every HTTP request.
    // This is the main config — it sets up which endpoints are public,
    // which require auth, disables sessions, and plugs JwtFilter into the chain.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs.
            // CSRF protects session-based apps where the browser auto-sends cookies.
            // We don't use cookies or sessions so this is irrelevant.
            .csrf(csrf -> csrf.disable())

            // Define which endpoints are public and which require a valid JWT.
            .authorizeHttpRequests(auth -> auth
                // Anyone can hit login and register — no token needed yet.
                .requestMatchers("/api/auth/**", "/error").permitAll()
                // Every other endpoint requires a valid JWT token.
                .anyRequest().authenticated()
            )

            // Tell Spring Security to never create or use HTTP sessions.
            // Every request must authenticate itself via JWT — no session memory.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Plug JwtFilter into the chain just before Spring Security's own
            // username/password filter. This ensures the JWT is validated and the
            // user is set in SecurityContextHolder before any access decisions are made.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt is the industry standard for hashing passwords.
    // AuthService will use this to hash passwords on register and verify them on login.
    // Declared here as a Bean so Spring can inject it wherever it's needed.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager is what Spring Security uses internally to process login attempts.
    // AuthService needs this to trigger the authentication flow when a user logs in.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

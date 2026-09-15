package com.art.erfassung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Erfassung application.
 * <p>
 * This class configures Spring Security to provide form login and authorization
 * for the application. Users are loaded from the database by
 * {@link com.art.erfassung.service.BenutzerService}.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain with authentication and authorization rules.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/", "/login", "/error", "/css/**", "/images/**", "/js/**").permitAll()
                // User management: admins only
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Attendance and statistics: teachers and admins
                .requestMatchers("/gruppen/**", "/anwesenheit/**", "/liste/**", "/studenten/**").hasAnyRole("TEACHER", "ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/gruppen", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // CSRF protection is enabled (Spring Security default), so logout must be a POST request
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }

    /**
     * Configures the password encoder for secure password storage.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

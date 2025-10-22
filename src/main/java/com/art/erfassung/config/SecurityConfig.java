package com.art.erfassung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Erfassung application.
 * <p>
 * This class configures Spring Security to provide basic authentication
 * and authorization for the application. It defines user roles and
 * access control for different endpoints.
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
                .requestMatchers("/", "/login", "/css/**", "/images/**", "/js/**").permitAll()
                // Admin endpoints
                .requestMatchers("/gruppen/**", "/studenten/**", "/statistik/**").hasRole("ADMIN")
                // Teacher endpoints
                .requestMatchers("/anwesenheit/**", "/liste/**").hasAnyRole("TEACHER", "ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/gruppen", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for development - enable in production

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

    /**
     * Configures in-memory user details service with default users.
     * In production, this should be replaced with a database-backed user service.
     *
     * @param passwordEncoder the password encoder to use
     * @return UserDetailsService with configured users
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin123"))
            .roles("ADMIN")
            .build();

        UserDetails teacher = User.builder()
            .username("teacher")
            .password(passwordEncoder.encode("teacher123"))
            .roles("TEACHER")
            .build();

        return new InMemoryUserDetailsManager(admin, teacher);
    }
}

package com.rahul.inventorybilling.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * All the security rules in one place.
 *
 * The lambdas below are the only ones left in this project. Spring Security 6
 * removed the older chained style, so this is how the configuration has to be
 * written now - each lambda just customises one part of the setup.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protects browser form posts using cookies. This API has
                // no cookies and no sessions, so it does not apply.
                .csrf(csrf -> csrf.disable())

                // Login, register and the static frontend are open. Everything
                // else needs a valid token.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/", "/index.html", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated())

                // STATELESS means no session is created or looked up. Every
                // request must carry its own token. That is what "stateless
                // authentication" means, and it is why the server can be
                // restarted without logging anyone out.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())

                // Our filter runs before the username/password one, so a
                // request with a token never reaches the login machinery.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Checks a username and password against the database at login time. */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt hashes passwords one way - a hash cannot be turned back into the
     * password. It also salts each hash, so two people with the same password
     * get different hashes, and it is deliberately slow to make brute forcing
     * expensive.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

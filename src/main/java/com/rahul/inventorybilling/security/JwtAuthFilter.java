package com.rahul.inventorybilling.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs before every request reaches a controller.
 *
 * Its job: if the request carries a valid token, tell Spring Security who the
 * caller is. If it does not, do nothing and let the request continue - the
 * security rules decide whether an anonymous request is allowed through.
 *
 * OncePerRequestFilter guarantees it runs exactly once per request, even when
 * the request is forwarded internally.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER_NAME);

        // No token, or not in the expected format. Hand the request on
        // untouched - it stays anonymous.
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Cut off "Bearer " and keep the token itself.
        String jwt = authHeader.substring(TOKEN_PREFIX.length());
        String username = jwtService.extractUsername(jwt);

        // The second check stops us redoing work if something earlier in the
        // chain already authenticated this request.
        boolean notYetAuthenticated = SecurityContextHolder.getContext().getAuthentication() == null;

        if (username != null && notYetAuthenticated) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // The null in the middle is the password. We do not have it
                // and do not need it - the signature already proved identity.
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // This line is the whole point of the filter: from here on,
                // the request is authenticated.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Always continue the chain, authenticated or not.
        filterChain.doFilter(request, response);
    }
}

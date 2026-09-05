package com.rahul.inventorybilling.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Makes and reads JSON Web Tokens.
 *
 * A JWT is three base64 chunks joined by dots: a header, the claims (the
 * data), and a signature. The signature is what matters - it is made with a
 * secret key only this server knows, so if anyone edits the claims the
 * signature stops matching and the token is rejected.
 *
 * The token is not encrypted. Anyone can read the claims. So never put a
 * password or anything private in there.
 */
@Service
public class JwtService {

    // For a real deployment this belongs in application.properties, read with
    // @Value, and out of version control.
    private static final String SECRET_KEY =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private static final long TWENTY_FOUR_HOURS_IN_MILLIS = 1000L * 60 * 60 * 24;

    /** The username is stored in the token's "subject" claim. */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> noExtraClaims = new HashMap<>();
        return generateToken(noExtraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + TWENTY_FOUR_HOURS_IN_MILLIS))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** A token is valid if it names this user and has not expired. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String usernameInToken = extractUsername(token);

        boolean sameUser = usernameInToken.equals(userDetails.getUsername());
        boolean expired = isTokenExpired(token);

        return sameUser && !expired;
    }

    private boolean isTokenExpired(String token) {
        Date expiry = extractExpiration(token);
        return expiry.before(new Date());
    }

    private Date extractExpiration(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration();
    }

    /**
     * Opens the token and checks the signature at the same time. If the token
     * was tampered with, this throws rather than returning bad data.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

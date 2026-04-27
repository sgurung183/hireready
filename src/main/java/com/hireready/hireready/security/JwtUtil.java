package com.hireready.hireready.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

// This class is the JWT toolbox. It has exactly 3 jobs:
// 1. Generate a token when a user logs in
// 2. Extract the email from a token when a request comes in
// 3. Validate that a token is legitimate and not expired
// Nothing else in the app touches JWT logic — it all goes through here.
@Component
public class JwtUtil {

    // The secret signing key read from the JWT_SECRET environment variable.
    // This is what makes our tokens trustworthy — only our server knows this value.
    // If someone doesn't have this key, they cannot create or fake a valid token.
    @Value("${jwt.secret}")
    private String secret;

    // How long the token lives in milliseconds, read from JWT_EXPIRATION env variable.
    // Defaults to 86400000ms (24 hours) if the env variable is not set.
    @Value("${jwt.expiration}")
    private long expiration;

    // PURPOSE: Create a new token for a user who just logged in or registered.
    // INPUT: the user's email address (this is what we embed inside the token)
    // OUTPUT: a token string that looks like "xxxxx.yyyyy.zzzzz" — three base64 sections separated by dots
    //         section 1 (header): algorithm info
    //         section 2 (payload): the email, issue time, expiry time
    //         section 3 (signature): cryptographic proof the token came from us
    // The frontend stores this string and sends it in the Authorization header on every future request.
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)                                                 // embeds the email into the token payload so we can retrieve it later
                .setIssuedAt(new Date())                                           // stamps the token with the current time (e.g. "created at 3:00pm")
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // stamps the token with the death time (e.g. "expires at 3:00pm tomorrow")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)               // uses our secret key to sign the token — this is what prevents tampering
                .compact();                                                        // assembles all of the above into the final xxxxx.yyyyy.zzzzz string
    }

    // PURPOSE: Read the email out of a token that was sent by the frontend.
    // INPUT: the raw token string from the Authorization header
    // OUTPUT: the email address that was embedded when the token was generated
    // Example: token was generated for "john@gmail.com" — this method returns "john@gmail.com"
    // JwtFilter calls this to figure out which user is making the current request.
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject(); // getSubject() returns whatever we passed to setSubject() during generation
    }

    // PURPOSE: Check whether a token is safe to trust before we let the request through.
    // INPUT: the raw token string from the Authorization header
    // OUTPUT: true if the token is valid, false if anything is wrong
    // A token is invalid if:
    //   - it has expired (past the expiration time we set)
    //   - it was tampered with (someone changed the payload, breaking the signature)
    //   - it is malformed (not a real JWT format at all)
    // We catch the exception and return false instead of letting the app crash.
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // if this runs without throwing, the token is valid
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // token is expired, tampered with, or not a valid JWT
        }
    }

    // PURPOSE: Do the actual heavy lifting of decoding and verifying the token.
    // INPUT: the raw token string
    // OUTPUT: a Claims object containing everything stored in the token (email, issued at, expiration)
    // This method is private — only generateToken, extractEmail, and isTokenValid use it internally.
    // The moment this method runs it verifies the signature — if anyone changed even one character
    // of the token after it was issued, this will throw an exception.
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // loads our secret key so the parser can verify the signature
                .build()
                .parseClaimsJws(token)          // decodes the token AND verifies the signature in one step
                .getBody();                     // returns just the payload section (email, dates) as a Claims object
    }

    // PURPOSE: Convert our raw base64 secret string into a Key object that the JJWT library can use.
    // The secret in application.properties is stored as a base64 string because env variables are text.
    // JJWT needs actual bytes to do cryptographic signing — this method does that conversion.
    // HS256 requires at least 32 bytes — our secret is long enough to satisfy this.
    // Private because no other class should ever touch the raw signing key directly.
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

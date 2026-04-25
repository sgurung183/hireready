package com.hireready.hireready.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This filter runs on every single incoming HTTP request before it reaches any controller.
// Its only job is to check if the request carries a valid JWT token.
// If the token is valid, it tells Spring Security who the user is, so the rest of the app
// can trust the request and know which user is making it.
//
// OncePerRequestFilter guarantees this filter runs exactly once per request — not twice,
// not zero times — even if the request passes through multiple parts of the app.
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // JwtUtil handles all token logic — this filter just uses it to validate and read tokens.
    private final JwtUtil jwtUtil;

    // CustomUserDetailsService loads the full user from the DB once we know their email.
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // STEP 1: Read the Authorization header from the incoming HTTP request.
        // Every protected request from the frontend must include this header.
        // It looks like this: Authorization: Bearer xxxxx.yyyyy.zzzzz
        final String authHeader = request.getHeader("Authorization");

        // STEP 2: If there is no Authorization header or it does not start with "Bearer ",
        // this is either a public endpoint (login/register) or a bad request.
        // We skip JWT processing entirely and pass it to the next filter in the chain.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // STEP 3: Strip the "Bearer " prefix (7 characters) to get just the raw token.
        // Before: "Bearer xxxxx.yyyyy.zzzzz"
        // After:        "xxxxx.yyyyy.zzzzz"
        final String token = authHeader.substring(7);

        // STEP 4: Validate the token first — reject expired or tampered tokens immediately
        // before doing any database work or email extraction.
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // STEP 5: Token is confirmed valid — now extract the email embedded inside it.
        final String email = jwtUtil.extractEmail(token);

        // STEP 6: Only continue if we got an email back AND the user is not already
        // authenticated for this request. getAuthentication() returns null if no one
        // has been authenticated yet — we do not want to authenticate the same request twice.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // STEP 7: Load the full User object from the database using the email.
            // This confirms the user actually exists in our system — not just that
            // someone crafted a token with a random email inside it.
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // STEP 8: Create a Spring Security authentication object.
            // This is the object that represents "a confirmed, authenticated user."
            // - First argument: the User object (who they are)
            // - Second argument: credentials — null because JWT already proved identity
            // - Third argument: the user's roles/permissions (e.g. ROLE_USER, ROLE_ADMIN)
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            // STEP 9: Attach additional request details like IP address to the auth token.
            // Useful for audit logging and Spring Security internals.
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // STEP 10: Store the authentication object in the SecurityContext.
            // This is the most important step — it is what tells Spring Security
            // "this request is authenticated and belongs to this user."
            // After this line, any controller can use @AuthenticationPrincipal
            // to get the current User object directly from the security context.
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // STEP 11: Pass the request along to the next filter or the controller.
        // If we authenticated the user above, the controller will see them as logged in.
        // If we didn't (bad token, missing header), Spring Security will block them
        // before they ever reach the controller — handled by SecurityConfig.
        filterChain.doFilter(request, response);
    }
}

package com.hireready.hireready.security;

import com.hireready.hireready.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// This class has one job: load a user from the database by their email.
// Spring Security calls this whenever it needs to verify who a user is.
// JwtFilter specifically calls this after extracting the email from the token —
// it needs to confirm that the email in the token actually belongs to a real user in our DB.
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security calls this method with the user's identifier — in our case the email.
    // It is called loadUserByUsername because that is the interface contract,
    // but we treat the "username" as email since that is how users log in to HireReady.
    // If no user is found with that email, we throw UsernameNotFoundException
    // which Spring Security catches and treats as an authentication failure.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
    }
}

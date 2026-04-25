package com.hireready.hireready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// implements UserDetails tells Spring Security that this class represents an authenticated user.
// UserDetails is a Spring Security interface — by implementing it, Spring Security can work
// directly with our User entity without needing a separate wrapper class.
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisaStatus visaStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Resume> resumeList = new ArrayList<>();

    //on each creation of a user this function is evoked because of the @prepersist annotaion
    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
    }

    // Returns the user's role as a list of permissions that Spring Security understands.
    // We prefix with "ROLE_" because Spring Security expects that format (e.g. "ROLE_USER", "ROLE_ADMIN").
    // This is how Spring Security knows what the user is allowed to do.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Spring Security calls getUsername() to identify the user — we use email as our unique identifier.
    // Even though the method is called getUsername(), we return email because that is how users log in.
    @Override
    public String getUsername() {
        return email;
    }

    // The four methods below are about the state of the account.
    // All return true for now — we do not have account locking or expiry features yet.
    // In a more advanced app you could add an "isBanned" field and return !isBanned here.
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
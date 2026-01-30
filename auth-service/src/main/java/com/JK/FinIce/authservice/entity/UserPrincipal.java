package com.JK.FinIce.authservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Principal - Spring Security UserDetails implementation
 * Wraps Users entity for authentication
 *
 * @author LastCoderBoy
 * @since 2026-01-26
 */
@Getter
@Builder
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;

    @JsonIgnore
    private final Collection<? extends GrantedAuthority> authorities;  // Authorization

    /**
     * Factory method to create UserPrincipal from JWT claims
     */
    public static UserPrincipal fromJwtClaims(Long userId, String username, String email,
                                              List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return UserPrincipal.builder()
                .id(userId)
                .username(username)
                .email(email)
                .authorities(authorities)
                .build();
    }

    /**
     * Factory method to create UserPrincipal from User entity
     * FOR LOGIN ONLY (when we have the User object already)
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String roleName = role.getName().name();
                    return new SimpleGrantedAuthority(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
                })
                .collect(Collectors.toList());

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // not stored in JWT
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Checked at login, JWT expiration handles this
    }

    @Override
    public boolean isAccountNonLocked() {
       return true; // Checked at login, refresh if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // JWT expiration handles
    }

    @Override
    public boolean isEnabled() {
        return true;  // Checked at login
    }

    // ==================== Helper Methods ====================
    public boolean hasRole(String roleName) {
        String roleToCheck = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleToCheck));
    }

    public boolean hasAnyRole(String... roleNames) {
        return Arrays.stream(roleNames)
                .anyMatch(this::hasRole);
    }

    public Set<String> getRoleNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}

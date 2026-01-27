package com.JK.FinIce.authservice.entity;

import com.JK.FinIce.authservice.enums.AccountStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    private final String password;  // Only for authentication
    private final String firstName;
    private final String lastName;
    private final AccountStatus accountStatus;
    private final Boolean accountLocked;
    private final LocalDateTime accountLockedUntil;
    private final Boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Factory method to create UserPrincipal from User entity
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
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .accountStatus(user.getAccountStatus())
                .accountLocked(user.getAccountLocked())
                .accountLockedUntil(user.getAccountLockedUntil())
                .emailVerified(user.getEmailVerified())
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        if (!accountLocked) {
            return true;
        }

        // Check if lock has expired
        if (accountLockedUntil != null && LocalDateTime.now().isAfter(accountLockedUntil)) {
            return true;  // Lock expired
        }

        return false;  // Still locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE;
    }

    public boolean hasRole(String roleName) {
        String roleToCheck = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleToCheck));
    }

    public boolean hasAnyRole(String... roleNames) {
        return Arrays.stream(roleNames)
                .anyMatch(this::hasRole);
    }
}

package com.jk.finice.authservice.entity;

import com.jk.finice.authservice.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_phone_number", columnList = "phone_number"),
                @Index(name = "idx_account_status", columnList = "account_status")
        },
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_phone_number", columnNames = "phone_number")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password; // Bcrypt hashed

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "phone_number", unique = true, length = 100)
    private String phoneNumber; // E.164 format: +1234567890

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false; // the field only checks whether the User is able to login or not

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil; // used when many password attempts are made

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ============= RELATIONSHIPS =============
    // Many-to-Many relationship with Role
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // One-to-Many relationship with RefreshToken
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RefreshToken> refreshTokens = new HashSet<>();



    // ============= HELPER METHODS =============

    public void addRole(Role role) {
        if (role == null) {
            return;
        }
        this.roles.add(role);

        // Safe bidirectional sync
        if (role.getUsers() != null) {
            role.getUsers().add(this);
        }
    }

    public void removeRole(Role role) {
        if (role == null) {
            return;
        }
        this.roles.remove(role);

        if (role.getUsers() != null) {
            role.getUsers().remove(this);
        }
    }

    public boolean isAccountNonLocked(){
        if (!accountLocked) {
            return true;
        }

        // Check if lock has expired
        if (accountLockedUntil != null && LocalDateTime.now().isAfter(accountLockedUntil)) {
            return true;
        }

        return false;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.accountLockedUntil = null;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;

        // Lock account after 5 failed attempts
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30); // Lock for 30 minutes
        }
    }


    public String getFullName(){
        if(firstName != null && lastName != null){
            return firstName + " " + lastName;
        }
        return firstName;
    }

    // The MFA Logic might be considered later
}

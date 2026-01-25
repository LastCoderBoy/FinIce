package com.JK.FinIce.authservice.entity;

import com.JK.FinIce.authservice.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_username", columnList = "username"),
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_phone_numer", columnList = "phoneNumber"),
                @Index(name = "idx_account_status", columnList = "account_status")
        },
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_phone_number", columnNames = "phoneNumber")
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

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hashed

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(unique = true, length = 100)
    private String phoneNumber; // E.164 format: +1234567890

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(nullable = false)
    private Boolean phoneVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    @Column(nullable = false)
    private Boolean accountLocked = false;

    @Column
    private LocalDateTime accountLockedUntil;

    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column
    private LocalDateTime lastLoginAt;

    @Column(length = 45)
    private String lastLoginIp;

    @Column
    private LocalDateTime passwordChangedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ============= RELATIONSHIPS =============
    // Many-to-Many relationship with Role
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // One-to-Many relationship with RefreshToken
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RefreshToken> refreshTokens = new HashSet<>();



    // ============= HELPER METHODS =============

    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    public boolean isAccountNonLocked(){
        if (!accountLocked) {
            return true;
        }

        // Check if lock has expired
        if (accountLockedUntil != null && LocalDateTime.now().isAfter(accountLockedUntil)) {
            resetFailedLoginAttempts();
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
        if(firstName != null & lastName != null){
            return firstName + " " + lastName;
        }
        return username;
    }

    // The MFA Logic might be considered later
}

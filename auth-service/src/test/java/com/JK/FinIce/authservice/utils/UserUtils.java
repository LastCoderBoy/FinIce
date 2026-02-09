package com.JK.FinIce.authservice.utils;

import com.JK.FinIce.authservice.entity.Role;
import com.JK.FinIce.authservice.entity.User;
import com.JK.FinIce.authservice.enums.AccountStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

public class UserUtils {

    public static User freshUser(){
        Role role = RoleUtil.roleUserOnly();
        User user = User.builder()
                .id(1L)
                .username("MTven")
                .email("Tven@gmail.com")
                .password(new BCryptPasswordEncoder().encode("password123"))
                .firstName("Thomas")
                .lastName("Kowalski")
                .phoneNumber("+48600000000")
                .emailVerified(true)
                .phoneVerified(false)
                .accountStatus(AccountStatus.ACTIVE)
                .accountLocked(false)
                .accountLockedUntil(null)
                .lastLoginAt(LocalDateTime.of(2026, 1, 20, 15, 35))
                .lastLoginIp("127.0.0.1")
                .passwordChangedAt(null)
                .failedLoginAttempts(0)
                .build();
        user.addRole(role);
        return user;
    }

}

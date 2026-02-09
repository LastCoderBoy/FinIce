package com.JK.FinIce.authservice.utils;

import com.JK.FinIce.authservice.entity.Role;
import com.JK.FinIce.authservice.enums.RoleName;

import java.time.LocalDateTime;
import java.util.HashSet;

public class RoleUtil {

    public static Role roleUserOnly(){
        return Role.builder()
                .name(RoleName.ROLE_USER)
                .description(RoleName.ROLE_USER.getDescription())
                .createdAt(LocalDateTime.of(2021, 1, 1, 0, 0))
                .users(new HashSet<>())
                .build();
    }
}

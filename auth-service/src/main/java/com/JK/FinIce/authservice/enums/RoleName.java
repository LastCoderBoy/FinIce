package com.JK.FinIce.authservice.enums;

import lombok.Getter;

@Getter
public enum RoleName {
    ROLE_USER("Standard user with basic features"),
    ROLE_ADMIN("Full system access, including user and configuration management"),
    ROLE_MANAGER("Managerial access"),
    ROLE_AUDITOR("Read-only access for auditing logs, reports, and transaction histories");

    private final String description;

    RoleName(String description) {
        this.description = description;
    }
}


package com.JK.FinIce.authservice.enums;

public enum AccountStatus {
    ACTIVE ("Normal active account"),
    INACTIVE ("Temporarily inactive"),
    SUSPENDED ("Suspended due to suspicious activity"),
    CLOSED("Permanently closed"),
    PENDING_VERIFICATION ("Awaiting email/phone verification");

    private final String description;

    AccountStatus(String description){
        this.description = description;
    }
}

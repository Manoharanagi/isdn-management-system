package com.isdn.model;

public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SUCCESS("Success"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    CHARGEDBACK("Charged Back");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

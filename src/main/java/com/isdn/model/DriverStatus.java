package com.isdn.model;

public enum DriverStatus {
    AVAILABLE("Available"),
    ON_DELIVERY("On Delivery"),
    OFF_DUTY("Off Duty"),
    ON_BREAK("On Break");

    private final String displayName;

    DriverStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

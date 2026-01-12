package com.isdn.model;

public enum Region {
    NORTH("North"),
    SOUTH("South"),
    EAST("East"),
    WEST("West"),
    CENTRAL("Central");

    private final String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
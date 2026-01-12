package com.isdn.model;

public enum Category {
    PACKAGED_FOODS("Packaged Foods"),
    BEVERAGES("Beverages"),
    HOME_CLEANING("Home Cleaning"),
    PERSONAL_CARE("Personal Care"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
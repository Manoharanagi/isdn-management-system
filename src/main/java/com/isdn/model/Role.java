package com.isdn.model;

public enum Role {
    CUSTOMER("Customer"),
    RDC_STAFF("RDC Staff"),
    LOGISTICS_OFFICER("Logistics Officer"),
    DRIVER("Driver"),
    HO_MANAGER("Head Office Manager"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
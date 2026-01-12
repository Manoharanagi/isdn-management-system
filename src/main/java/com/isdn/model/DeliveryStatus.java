package com.isdn.model;

public enum DeliveryStatus {
    PENDING_ASSIGNMENT("Pending Assignment"),
    ASSIGNED("Assigned to Driver"),
    PICKED_UP("Picked Up from RDC"),
    IN_TRANSIT("In Transit"),
    ARRIVED("Arrived at Destination"),
    DELIVERED("Delivered"),
    FAILED("Failed Delivery"),
    RETURNED("Returned to RDC");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

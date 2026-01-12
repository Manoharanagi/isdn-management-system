package com.isdn.model;

public enum MovementType {
    RECEIVED("Stock Received"),
    SOLD("Sold to Customer"),
    DAMAGED("Damaged/Expired"),
    RETURNED("Customer Return"),
    TRANSFERRED_OUT("Transferred to Another RDC"),
    TRANSFERRED_IN("Received from Another RDC"),
    ADJUSTMENT("Stock Adjustment");

    private final String displayName;

    MovementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

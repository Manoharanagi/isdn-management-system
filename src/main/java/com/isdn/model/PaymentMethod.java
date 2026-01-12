package com.isdn.model;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Cash on Delivery"),
    ONLINE_PAYMENT("Online Payment"),
    BANK_TRANSFER("Bank Transfer"),
    CHEQUE("Cheque");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
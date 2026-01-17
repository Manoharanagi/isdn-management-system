package com.isdn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayHereNotifyRequest {

    // Merchant identification
    private String merchant_id;

    // Order identification
    private String order_id;

    // PayHere payment ID
    private String payment_id;

    // Payment amount
    private String payhere_amount;

    // Payment currency
    private String payhere_currency;

    // Payment status code (2 = success, 0 = pending, -1 = cancelled, -2 = failed, -3 = chargedback)
    private Integer status_code;

    // MD5 signature for verification
    private String md5sig;

    // Payment method (VISA, MASTER, AMEX, etc.)
    private String method;

    // Status message
    private String status_message;

    // Card holder name (masked)
    private String card_holder_name;

    // Card number (masked)
    private String card_no;

    // Card expiry (MM/YY)
    private String card_expiry;

    // Customer saved card token (if enabled)
    private String customer_token;

    // Recurring payment token (if enabled)
    private String recurring_token;

    // Custom field 1 (payment reference)
    private String custom_1;

    // Custom field 2
    private String custom_2;
}

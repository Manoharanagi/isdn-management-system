package com.isdn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private String returnUrl;

    private String cancelUrl;
}

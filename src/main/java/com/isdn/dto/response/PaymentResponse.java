package com.isdn.dto.response;

import com.isdn.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private String paymentReference;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String statusDisplayName;
    private String payherePaymentId;
    private String method;
    private String cardNo;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

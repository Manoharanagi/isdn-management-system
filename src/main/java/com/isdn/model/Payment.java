package com.isdn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @NotBlank(message = "Payment reference is required")
    @Column(name = "payment_reference", unique = true, nullable = false, length = 100)
    private String paymentReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Amount is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    @Column(nullable = false, length = 10)
    private String currency;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // PayHere specific fields
    @Column(name = "payhere_order_id", length = 100)
    private String payhereOrderId;

    @Column(name = "payhere_payment_id", length = 100)
    private String payherePaymentId;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "status_message", length = 255)
    private String statusMessage;

    @Column(name = "md5sig", length = 64)
    private String md5sig;

    @Column(name = "method", length = 50)
    private String method;

    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;

    @Column(name = "card_no", length = 20)
    private String cardNo;

    @Column(name = "card_expiry", length = 10)
    private String cardExpiry;

    // Customer info from PayHere callback
    @Column(name = "customer_token", length = 255)
    private String customerToken;

    @Column(name = "recurring_token", length = 255)
    private String recurringToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Generate unique payment reference
     */
    public static String generatePaymentReference() {
        return "PAY-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
    }
}

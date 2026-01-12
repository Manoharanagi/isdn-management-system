package com.isdn.dto.response;

import com.isdn.model.OrderStatus;
import com.isdn.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String contactNumber;
    private PaymentMethod paymentMethod;
    private LocalDateTime orderDate;
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String notes;
    private List<OrderItemResponse> items;
    private Integer totalItems;
    private String rdcName;
}
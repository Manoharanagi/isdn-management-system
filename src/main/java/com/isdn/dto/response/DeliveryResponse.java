package com.isdn.dto.response;

import com.isdn.model.DeliveryStatus;
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
public class DeliveryResponse {
    private Long deliveryId;
    private Long orderId;
    private String orderNumber;
    private Long driverId;
    private String driverName;
    private String vehicleNumber;
    private DeliveryStatus status;
    private String deliveryAddress;
    private String contactNumber;
    private LocalDateTime assignedDate;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private BigDecimal destinationLatitude;
    private BigDecimal destinationLongitude;
    private BigDecimal estimatedDistanceKm;
    private String notes;
}

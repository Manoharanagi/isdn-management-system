package com.isdn.dto.response;

import com.isdn.model.DriverStatus;
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
public class DriverResponse {
    private Long driverId;
    private String name;
    private String username;
    private String phoneNumber;
    private String licenseNumber;
    private String vehicleNumber;
    private String vehicleType;
    private DriverStatus status;
    private String rdcName;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private LocalDateTime lastLocationUpdate;
    private Integer activeDeliveries;
}

package com.isdn.dto.request;

import com.isdn.model.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverRequest {

    private String phoneNumber;

    private String licenseNumber;

    private String vehicleNumber;

    private String vehicleType;

    private DriverStatus status;

    private Long rdcId;

    private Boolean active;
}

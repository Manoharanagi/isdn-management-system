package com.isdn.dto.request;

import com.isdn.model.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequest {

    @NotNull(message = "Status is required")
    private DeliveryStatus status;

    private String notes;
}

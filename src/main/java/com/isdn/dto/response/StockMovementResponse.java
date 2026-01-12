package com.isdn.dto.response;

import com.isdn.model.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long movementId;
    private Long inventoryId;
    private String productName;
    private String rdcName;
    private MovementType movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private String performedBy;
    private LocalDateTime timestamp;
}

package com.isdn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long inventoryId;
    private Long productId;
    private String productName;
    private String productSku;
    private Long rdcId;
    private String rdcName;
    private Integer quantityOnHand;
    private Integer reorderLevel;
    private String status; // "OK", "LOW_STOCK", "OUT_OF_STOCK"
    private LocalDateTime lastUpdated;
}

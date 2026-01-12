package com.isdn.dto.response;

import com.isdn.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long productId;
    private String sku;
    private String name;
    private String description;
    private Category category;
    private BigDecimal unitPrice;
    private String imageUrl;
    private Boolean available;
    private Integer totalStock;
    private PromotionInfo promotion;
}


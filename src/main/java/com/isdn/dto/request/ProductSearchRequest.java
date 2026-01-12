package com.isdn.dto.request;

import com.isdn.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    private String keyword;
    private Category category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
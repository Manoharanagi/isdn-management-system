package com.isdn.service;

import com.isdn.dto.request.ProductSearchRequest;
import com.isdn.dto.response.ProductResponse;
import com.isdn.dto.response.PromotionInfo;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.Category;
import com.isdn.model.Inventory;
import com.isdn.model.Product;
import com.isdn.model.Promotion;
import com.isdn.repository.InventoryRepository;
import com.isdn.repository.ProductRepository;
import com.isdn.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PromotionRepository promotionRepository;

    /**
     * Get all active products
     */
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all active products");
        List<Product> products = productRepository.findByActiveTrueOrderByNameAsc();
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    public ProductResponse getProductById(Long productId) {
        log.info("Fetching product with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.getActive()) {
            throw new ResourceNotFoundException("Product is not available");
        }

        return mapToResponse(product);
    }

    /**
     * Search products with filters
     */
    public List<ProductResponse> searchProducts(ProductSearchRequest request) {
        log.info("Searching products with filters: {}", request);
        List<Product> products = productRepository.searchProducts(
                request.getKeyword(),
                request.getCategory(),
                request.getMinPrice(),
                request.getMaxPrice()
        );

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category
     */
    public List<ProductResponse> getProductsByCategory(Category category) {
        log.info("Fetching products for category: {}", category);
        List<Product> products = productRepository.findByCategoryAndActiveTrue(category);
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get promotional products
     */
    public List<ProductResponse> getPromotionalProducts() {
        log.info("Fetching promotional products");
        List<Product> products = productRepository.findProductsOnPromotion();
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToResponse(Product product) {
        // Get total stock across all RDCs
        List<Inventory> inventories = inventoryRepository.findByProduct(product);
        int totalStock = inventories.stream()
                .mapToInt(Inventory::getQuantityOnHand)
                .sum();

        boolean available = totalStock > 0;

        // Check for active promotions
        PromotionInfo promotionInfo = getActivePromotion(product);

        return ProductResponse.builder()
                .productId(product.getProductId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .unitPrice(product.getUnitPrice())
                .imageUrl(product.getImageUrl())
                .available(available)
                .totalStock(totalStock)
                .promotion(promotionInfo)
                .build();
    }

    /**
     * Get active promotion for a product
     */
    private PromotionInfo getActivePromotion(Product product) {
        List<Promotion> activePromotions = promotionRepository.findActivePromotions();

        for (Promotion promotion : activePromotions) {
            if (promotion.getProducts().contains(product)) {
                return PromotionInfo.builder()
                        .promotionId(promotion.getPromotionId())
                        .title(promotion.getTitle())
                        .discountPercentage(promotion.getDiscountPercentage())
                        .build();
            }
        }

        return null;
    }
}
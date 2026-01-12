package com.isdn.controller;

import com.isdn.dto.request.ProductSearchRequest;
import com.isdn.dto.response.ProductResponse;
import com.isdn.model.Category;
import com.isdn.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products - Get all products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("GET /api/products - Fetch all products");
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id} - Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - Fetch product by ID", id);
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * POST /api/products/search - Search products with filters
     */
    @PostMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @Valid @RequestBody ProductSearchRequest request) {
        log.info("POST /api/products/search - Search with filters: {}", request);
        List<ProductResponse> products = productService.searchProducts(request);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/category/{category} - Get products by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable Category category) {
        log.info("GET /api/products/category/{} - Fetch products by category", category);
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/promotions - Get promotional products
     */
    @GetMapping("/promotions")
    public ResponseEntity<List<ProductResponse>> getPromotionalProducts() {
        log.info("GET /api/products/promotions - Fetch promotional products");
        List<ProductResponse> products = productService.getPromotionalProducts();
        return ResponseEntity.ok(products);
    }
}
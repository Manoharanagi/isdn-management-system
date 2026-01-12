package com.isdn.controller;

import com.isdn.dto.request.AddToCartRequest;
import com.isdn.dto.request.UpdateCartItemRequest;
import com.isdn.dto.response.ApiResponse;
import com.isdn.dto.response.CartResponse;
import com.isdn.model.User;
import com.isdn.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final com.isdn.repository.UserRepository userRepository;

    /**
     * GET /api/cart - Get user's cart
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/cart - Fetch cart for user: {}", userDetails.getUsername());
        Long userId = getUserId(userDetails);
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/cart/items - Add item to cart
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/cart/items - Add item to cart for user: {}", userDetails.getUsername());
        Long userId = getUserId(userDetails);
        CartResponse cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * PUT /api/cart/items/{cartItemId} - Update cart item quantity
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/cart/items/{} - Update cart item", cartItemId);
        Long userId = getUserId(userDetails);
        CartResponse cart = cartService.updateCartItem(userId, cartItemId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/items/{cartItemId} - Remove item from cart
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/cart/items/{} - Remove item from cart", cartItemId);
        Long userId = getUserId(userDetails);
        CartResponse cart = cartService.removeCartItem(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart - Clear cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/cart - Clear cart for user: {}", userDetails.getUsername());
        Long userId = getUserId(userDetails);
        cartService.clearCart(userId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Cart cleared successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to get user ID from UserDetails
     */
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }
}

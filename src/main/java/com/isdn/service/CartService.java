package com.isdn.service;

import com.isdn.dto.request.AddToCartRequest;
import com.isdn.dto.request.UpdateCartItemRequest;
import com.isdn.dto.response.CartItemResponse;
import com.isdn.dto.response.CartResponse;
import com.isdn.exception.BadRequestException;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.*;
import com.isdn.repository.CartItemRepository;
import com.isdn.repository.CartRepository;
import com.isdn.repository.InventoryRepository;
import com.isdn.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserService userService;

    /**
     * Get user's cart
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        log.info("Fetching cart for user: {}", userId);
        Cart cart = getOrCreateCart(userId);
        return mapToResponse(cart);
    }

    /**
     * Add item to cart
     */
    @Transactional
    public CartResponse addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, product: {}, quantity: {}",
                userId, request.getProductId(), request.getQuantity());

        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Get product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getActive()) {
            throw new BadRequestException("Product is not available");
        }

        // Check stock availability
        Integer totalStock = inventoryRepository.getTotalStockForProduct(product.getProductId());
        if (totalStock == null || totalStock < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + (totalStock != null ? totalStock : 0));
        }

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // Check if new quantity exceeds stock
            if (newQuantity > totalStock) {
                throw new BadRequestException("Cannot add more items. Available stock: " + totalStock);
            }

            existingItem.setQuantity(newQuantity);
            existingItem.calculateSubtotal();
            cartItemRepository.save(existingItem);
            log.info("Updated existing cart item quantity to: {}", newQuantity);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .build();

            cartItem.calculateSubtotal();
            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
            log.info("Added new item to cart");
        }

        // Recalculate cart total
        cart.calculateTotal();
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item: {} for user: {}", cartItemId, userId);

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new BadRequestException("Cart item does not belong to this user");
        }

        // Check stock availability
        Integer totalStock = inventoryRepository.getTotalStockForProduct(cartItem.getProduct().getProductId());
        if (totalStock == null || totalStock < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + (totalStock != null ? totalStock : 0));
        }

        cartItem.setQuantity(request.getQuantity());
        cartItem.calculateSubtotal();
        cartItemRepository.save(cartItem);

        cart.calculateTotal();
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        log.info("Removing cart item: {} for user: {}", cartItemId, userId);

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new BadRequestException("Cart item does not belong to this user");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.calculateTotal();
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    /**
     * Clear cart
     */
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCart(cart);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    /**
     * Get or create cart for user
     */
    private Cart getOrCreateCart(Long userId) {
        User user = userService.getUserById(userId);

        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .user(user)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
            return cartRepository.save(newCart);
        });
    }

    /**
     * Map Cart to CartResponse
     */
    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(items)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .build();
    }

    /**
     * Map CartItem to CartItemResponse
     */
    private CartItemResponse mapItemToResponse(CartItem item) {
        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .productImage(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
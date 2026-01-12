package com.isdn.controller;

import com.isdn.dto.request.OrderRequest;
import com.isdn.dto.request.UpdateOrderStatusRequest;
import com.isdn.dto.response.ApiResponse;
import com.isdn.dto.response.OrderResponse;
import com.isdn.model.OrderStatus;
import com.isdn.model.User;
import com.isdn.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final com.isdn.repository.UserRepository userRepository;

    /**
     * POST /api/orders - Place order from cart
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/orders - Place order for user: {}", userDetails.getUsername());
        Long userId = getUserId(userDetails);
        OrderResponse order = orderService.placeOrder(userId, request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    /**
     * GET /api/orders - Get user's orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/orders - Fetch orders for user: {}", userDetails.getUsername());
        Long userId = getUserId(userDetails);
        List<OrderResponse> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/{orderId} - Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/orders/{} - Fetch order details", orderId);
        Long userId = getUserId(userDetails);
        OrderResponse order = orderService.getOrderById(userId, orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * GET /api/orders/number/{orderNumber} - Get order by order number
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        log.info("GET /api/orders/number/{} - Fetch order by number", orderNumber);
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * PUT /api/orders/{orderId}/cancel - Cancel order
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/orders/{}/cancel - Cancel order", orderId);
        Long userId = getUserId(userDetails);
        OrderResponse order = orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * GET /api/orders/all - Get all orders (staff only) - Feature 4
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("GET /api/orders/all - Fetch all orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/status/{status} - Get orders by status - Feature 4
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("GET /api/orders/status/{} - Fetch orders by status", status);
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * PUT /api/orders/{orderId}/confirm - Confirm order - Feature 4
     */
    @PutMapping("/{orderId}/confirm")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long orderId) {
        log.info("PUT /api/orders/{}/confirm - Confirm order", orderId);
        OrderResponse order = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * PUT /api/orders/{orderId}/status - Update order status - Feature 4
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("PUT /api/orders/{}/status - Update order status", orderId);
        OrderResponse order = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(order);
    }

    /**
     * GET /api/orders/rdc/{rdcId} - Get orders by RDC - Feature 4
     */
    @GetMapping("/rdc/{rdcId}")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByRdc(@PathVariable Long rdcId) {
        log.info("GET /api/orders/rdc/{} - Fetch orders for RDC", rdcId);
        List<OrderResponse> orders = orderService.getOrdersByRdc(rdcId);
        return ResponseEntity.ok(orders);
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
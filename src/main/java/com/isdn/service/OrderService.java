package com.isdn.service;

import com.isdn.dto.request.OrderRequest;
import com.isdn.dto.request.UpdateOrderStatusRequest;
import com.isdn.dto.response.OrderItemResponse;
import com.isdn.dto.response.OrderResponse;
import com.isdn.exception.BadRequestException;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.*;
import com.isdn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final InventoryRepository inventoryRepository;
    private final RDCRepository rdcRepository;
    private final UserService userService;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    /**
     * Place order from cart
     */
    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        log.info("Placing order for user: {}", userId);

        User user = userService.getUserById(userId);

        // Get user's cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place order with empty cart");
        }

        // Check inventory for all items
        for (CartItem cartItem : cart.getItems()) {
            Integer totalStock = inventoryRepository.getTotalStockForProduct(
                    cartItem.getProduct().getProductId());

            if (totalStock == null || totalStock < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + cartItem.getProduct().getName() +
                                ". Available: " + (totalStock != null ? totalStock : 0));
            }
        }

        // Assign nearest RDC (simplified - just pick first active one)
        RDC rdc = rdcRepository.findByActiveTrue().stream()
                .findFirst()
                .orElse(null);

        // Create order
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .user(user)
                .rdc(rdc)
                .status(OrderStatus.PENDING)
                .totalAmount(cart.getTotalAmount())
                .deliveryAddress(request.getDeliveryAddress())
                .contactNumber(request.getContactNumber())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .estimatedDeliveryDate(LocalDate.now().plusDays(2)) // 2 days from now
                .build();

        // Copy cart items to order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .build();

            orderItem.calculateSubtotal();
            order.addItem(orderItem);
        }

        // Save order
        order = orderRepository.save(order);
        log.info("Order created: {}", order.getOrderNumber());

        // Reserve inventory (decrease stock)
        reserveInventory(order);

        // Clear cart
        cartRepository.delete(cart);
        log.info("Cart cleared for user: {}", userId);

        return mapToResponse(order);
    }

    /**
     * Get user's orders
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        log.info("Fetching orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUser_UserIdOrderByOrderDateDesc(userId);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        log.info("Fetching order: {} for user: {}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify order belongs to user
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }

        return mapToResponse(order);
    }

    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        log.info("Fetching order by number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToResponse(order);
    }

    /**
     * Cancel order
     */
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        log.info("Cancelling order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify order belongs to user
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }

        // Can only cancel if order is PENDING or CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order in " + order.getStatus() + " status");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Restore inventory
        restoreInventory(order);

        log.info("Order cancelled: {}", order.getOrderNumber());
        return mapToResponse(order);
    }

    /**
     * Get all orders (for staff/admin) - Feature 4
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders for staff");
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by status - Feature 4
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        List<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Confirm order (PENDING → CONFIRMED) - Feature 4
     */
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        log.info("Confirming order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be confirmed");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // Generate and send invoice email
        sendInvoiceEmail(order);

        log.info("Order {} confirmed successfully", orderId);
        return mapToResponse(order);
    }

    /**
     * Generate and send invoice email to customer
     */
    public void sendInvoiceEmail(Order order) {
        try {
            log.info("Generating invoice for order: {}", order.getOrderNumber());
            byte[] invoicePdf = invoiceService.generateInvoice(order);

            String customerName = order.getUser().getBusinessName() != null ?
                    order.getUser().getBusinessName() : order.getUser().getUsername();

            emailService.sendInvoiceEmail(
                    order.getUser().getEmail(),
                    customerName,
                    order.getOrderNumber(),
                    invoicePdf
            );
            log.info("Invoice email sent for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send invoice email for order: {}", order.getOrderNumber(), e);
        }
    }

    /**
     * Update order status - Feature 4
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        log.info("Updating order {} status to {}", orderId, request.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        // Update timestamps based on status
        switch (request.getStatus()) {
            case PROCESSING -> {
                // Order is being processed
            }
            case READY_FOR_DELIVERY -> {
                // Order is ready for pickup
            }
            case OUT_FOR_DELIVERY -> {
                // Order is out for delivery (handled by delivery service)
            }
            case DELIVERED -> {
                order.setActualDeliveryDate(LocalDate.now());
            }
            case CANCELLED -> {
                // Order cancelled
            }
            case FAILED_DELIVERY -> {
                // Delivery failed
            }
        }

        orderRepository.save(order);

        log.info("Order status updated from {} to {}", oldStatus, request.getStatus());
        return mapToResponse(order);
    }

    /**
     * Get orders by RDC - Feature 4
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByRdc(Long rdcId) {
        log.info("Fetching orders for RDC: {}", rdcId);

        RDC rdc = rdcRepository.findById(rdcId)
                .orElseThrow(() -> new ResourceNotFoundException("RDC not found"));

        List<Order> orders = orderRepository.findByRdcOrderByOrderDateDesc(rdc);

        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Reserve inventory (decrease stock)
     */
    private void reserveInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            List<Inventory> inventories = inventoryRepository.findByProduct(item.getProduct());

            int remainingQuantity = item.getQuantity();

            for (Inventory inventory : inventories) {
                if (remainingQuantity <= 0) break;

                int available = inventory.getQuantityOnHand();
                int toReserve = Math.min(available, remainingQuantity);

                inventory.setQuantityOnHand(available - toReserve);
                inventoryRepository.save(inventory);

                remainingQuantity -= toReserve;

                log.info("Reserved {} units of {} from RDC {}",
                        toReserve, item.getProduct().getName(), inventory.getRdc().getName());
            }
        }
    }

    /**
     * Restore inventory (increase stock) when order is cancelled
     */
    private void restoreInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            List<Inventory> inventories = inventoryRepository.findByProduct(item.getProduct());

            if (!inventories.isEmpty()) {
                // Add back to first available inventory
                Inventory inventory = inventories.get(0);
                inventory.setQuantityOnHand(inventory.getQuantityOnHand() + item.getQuantity());
                inventoryRepository.save(inventory);

                log.info("Restored {} units of {} to RDC {}",
                        item.getQuantity(), item.getProduct().getName(), inventory.getRdc().getName());
            }
        }
    }

    /**
     * Map Order to OrderResponse
     */
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .contactNumber(order.getContactNumber())
                .paymentMethod(order.getPaymentMethod())
                .orderDate(order.getOrderDate())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .notes(order.getNotes())
                .items(items)
                .totalItems(items.size())
                .rdcName(order.getRdc() != null ? order.getRdc().getName() : null)
                .build();
    }

    /**
     * Map OrderItem to OrderItemResponse
     */
    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
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
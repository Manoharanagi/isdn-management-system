package com.isdn.controller;

import com.isdn.dto.request.InitiatePaymentRequest;
import com.isdn.dto.request.PayHereNotifyRequest;
import com.isdn.dto.response.PaymentInitiationResponse;
import com.isdn.dto.response.PaymentResponse;
import com.isdn.model.User;
import com.isdn.repository.UserRepository;
import com.isdn.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    /**
     * POST /api/payments/initiate - Initiate payment for an order
     * Requires CUSTOMER role
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/payments/initiate - Initiating payment for order: {}", request.getOrderId());
        Long userId = getUserId(userDetails);
        PaymentInitiationResponse response = paymentService.initiatePayment(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * POST /api/payments/notify - PayHere webhook callback
     * This endpoint must be public (no authentication) for PayHere to call
     */
    @PostMapping("/notify")
    public ResponseEntity<String> handlePayHereNotify(PayHereNotifyRequest request) {
        log.info("POST /api/payments/notify - Received PayHere notification for order: {}", request.getOrder_id());
        try {
            paymentService.handlePayHereNotify(request);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing PayHere notification: {}", e.getMessage(), e);
            // Still return OK to prevent PayHere from retrying with the same invalid data
            return ResponseEntity.ok("RECEIVED");
        }
    }

    /**
     * GET /api/payments/status/{paymentReference} - Check payment status
     * Requires authentication
     */
    @GetMapping("/status/{paymentReference}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @PathVariable String paymentReference,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/payments/status/{} - Checking payment status", paymentReference);
        PaymentResponse response = paymentService.getPaymentByReference(paymentReference);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/payments/order/{orderId} - Get all payments for an order
     * Requires authentication
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/payments/order/{} - Fetching payments for order", orderId);
        List<PaymentResponse> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/payments - Get user's payment history
     * Requires CUSTOMER role
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getUserPayments(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/payments - Fetching payment history for user: {}", userDetails.getUsername());
        Long userId = getUserId(userDetails);
        List<PaymentResponse> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
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

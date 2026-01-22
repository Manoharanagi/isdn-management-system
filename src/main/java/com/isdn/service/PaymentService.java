package com.isdn.service;

import com.isdn.config.PayHereConfig;
import com.isdn.dto.request.InitiatePaymentRequest;
import com.isdn.dto.request.PayHereNotifyRequest;
import com.isdn.dto.response.PaymentInitiationResponse;
import com.isdn.dto.response.PaymentResponse;
import com.isdn.exception.BadRequestException;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.*;
import com.isdn.repository.OrderRepository;
import com.isdn.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final PayHereHashService payHereHashService;
    private final PayHereConfig payHereConfig;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    /**
     * Initiate payment for an order
     */
    @Transactional
    public PaymentInitiationResponse initiatePayment(Long userId, InitiatePaymentRequest request) {
        log.info("Initiating payment for order: {} by user: {}", request.getOrderId(), userId);

        User user = userService.getUserById(userId);

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify order belongs to user
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }

        // Verify order is in PENDING status
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING status. Current status: " + order.getStatus());
        }

        // Verify payment method is ONLINE_PAYMENT
        if (order.getPaymentMethod() != PaymentMethod.ONLINE_PAYMENT) {
            throw new BadRequestException("Order payment method is not ONLINE_PAYMENT");
        }

        // Check if successful payment already exists
        if (paymentRepository.existsSuccessfulPaymentForOrder(order.getOrderId())) {
            throw new BadRequestException("A successful payment already exists for this order");
        }

        // Generate payment reference
        String paymentReference = Payment.generatePaymentReference();
        String payhereOrderId = order.getOrderNumber() + "-" + System.currentTimeMillis();

        // Create payment record
        Payment payment = Payment.builder()
                .paymentReference(paymentReference)
                .order(order)
                .user(user)
                .amount(order.getTotalAmount())
                .currency(payHereConfig.getCurrency())
                .status(PaymentStatus.PENDING)
                .payhereOrderId(payhereOrderId)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment record created: {}", paymentReference);

        // Generate hash for PayHere
        String hash = payHereHashService.generatePaymentHash(
                payhereOrderId,
                order.getTotalAmount(),
                payHereConfig.getCurrency()
        );

        // Build form data for PayHere
        Map<String, String> formData = buildPayHereFormData(
                payment,
                order,
                user,
                hash,
                request.getReturnUrl(),
                request.getCancelUrl()
        );

        return PaymentInitiationResponse.builder()
                .paymentReference(paymentReference)
                .paymentUrl(payHereConfig.getCheckoutUrl())
                .payhereFormData(formData)
                .message("Payment initiated successfully")
                .build();
    }

    /**
     * Handle PayHere notification callback
     */
    @Transactional
    public void handlePayHereNotify(PayHereNotifyRequest request) {
        log.info("Received PayHere notification for order: {}, status: {}",
                request.getOrder_id(), request.getStatus_code());

        // Verify hash
        boolean isValidHash = payHereHashService.verifyNotificationHash(
                request.getMerchant_id(),
                request.getOrder_id(),
                request.getPayhere_amount(),
                request.getPayhere_currency(),
                request.getStatus_code(),
                request.getMd5sig()
        );

        if (!isValidHash) {
            log.error("Invalid hash received for order: {}", request.getOrder_id());
            throw new BadRequestException("Invalid payment notification hash");
        }

        // Find payment by payhere order ID
        Payment payment = paymentRepository.findByPayhereOrderId(request.getOrder_id())
                .orElseThrow(() -> {
                    log.error("Payment not found for PayHere order ID: {}", request.getOrder_id());
                    return new ResourceNotFoundException("Payment not found for order: " + request.getOrder_id());
                });

        // Check for duplicate notification (idempotency)
        if (payment.getStatus() == PaymentStatus.SUCCESS && request.getStatus_code() == 2) {
            log.info("Duplicate success notification received for payment: {}", payment.getPaymentReference());
            return;
        }

        // Update payment details from PayHere
        payment.setPayherePaymentId(request.getPayment_id());
        payment.setStatusCode(request.getStatus_code());
        payment.setStatusMessage(request.getStatus_message());
        payment.setMd5sig(request.getMd5sig());
        payment.setMethod(request.getMethod());
        payment.setCardHolderName(request.getCard_holder_name());
        payment.setCardNo(request.getCard_no());
        payment.setCardExpiry(request.getCard_expiry());
        payment.setCustomerToken(request.getCustomer_token());
        payment.setRecurringToken(request.getRecurring_token());

        // Update payment status based on status code
        PaymentStatus newStatus = mapPayHereStatusCode(request.getStatus_code());
        payment.setStatus(newStatus);

        if (newStatus == PaymentStatus.SUCCESS) {
            payment.setCompletedAt(LocalDateTime.now());

            // Update order status to CONFIRMED
            Order order = payment.getOrder();
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                log.info("Order {} status updated to CONFIRMED after successful payment", order.getOrderNumber());

                // Generate and send invoice email
                sendInvoiceEmail(order);
            }
        }

        paymentRepository.save(payment);
        log.info("Payment {} updated with status: {}", payment.getPaymentReference(), newStatus);
    }

    /**
     * Get payment by reference
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String paymentReference) {
        log.info("Fetching payment by reference: {}", paymentReference);

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return mapToResponse(payment);
    }

    /**
     * Get payments for an order
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        log.info("Fetching payments for order: {}", orderId);

        List<Payment> payments = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user's payment history
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPayments(Long userId) {
        log.info("Fetching payments for user: {}", userId);

        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build PayHere form data
     */
    private Map<String, String> buildPayHereFormData(Payment payment, Order order, User user,
                                                      String hash, String customReturnUrl, String customCancelUrl) {
        Map<String, String> formData = new LinkedHashMap<>();

        formData.put("merchant_id", payHereConfig.getMerchantId());
        formData.put("return_url", customReturnUrl != null ? customReturnUrl : payHereConfig.getReturnUrl());
        formData.put("cancel_url", customCancelUrl != null ? customCancelUrl : payHereConfig.getCancelUrl());
        formData.put("notify_url", payHereConfig.getNotifyUrl());

        formData.put("order_id", payment.getPayhereOrderId());
        formData.put("items", "Order " + order.getOrderNumber());
        formData.put("currency", payHereConfig.getCurrency());
        formData.put("amount", payHereHashService.formatAmount(order.getTotalAmount()));

        // Use contactPerson or businessName as name (split for first/last name)
        String fullName = user.getContactPerson() != null ? user.getContactPerson() :
                (user.getBusinessName() != null ? user.getBusinessName() : user.getUsername());
        String[] nameParts = fullName.split(" ", 2);
        formData.put("first_name", nameParts[0]);
        formData.put("last_name", nameParts.length > 1 ? nameParts[1] : "");
        formData.put("email", user.getEmail());
        formData.put("phone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        formData.put("address", order.getDeliveryAddress());
        formData.put("city", "");
        formData.put("country", "Sri Lanka");

        formData.put("hash", hash);

        // Custom fields
        formData.put("custom_1", payment.getPaymentReference());
        formData.put("custom_2", order.getOrderNumber());

        return formData;
    }

    /**
     * Map PayHere status code to PaymentStatus
     * 2 = success, 0 = pending, -1 = cancelled, -2 = failed, -3 = chargedback
     */
    private PaymentStatus mapPayHereStatusCode(Integer statusCode) {
        if (statusCode == null) {
            return PaymentStatus.PENDING;
        }

        return switch (statusCode) {
            case 2 -> PaymentStatus.SUCCESS;
            case 0 -> PaymentStatus.PROCESSING;
            case -1 -> PaymentStatus.CANCELLED;
            case -2 -> PaymentStatus.FAILED;
            case -3 -> PaymentStatus.CHARGEDBACK;
            default -> PaymentStatus.PENDING;
        };
    }

    /**
     * Map Payment to PaymentResponse
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .paymentReference(payment.getPaymentReference())
                .orderId(payment.getOrder().getOrderId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .statusDisplayName(payment.getStatus().getDisplayName())
                .payherePaymentId(payment.getPayherePaymentId())
                .method(payment.getMethod())
                .cardNo(payment.getCardNo())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }

    /**
     * Generate and send invoice email to customer after successful payment
     */
    private void sendInvoiceEmail(Order order) {
        try {
            log.info("Generating invoice for order: {} after successful payment", order.getOrderNumber());
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
}

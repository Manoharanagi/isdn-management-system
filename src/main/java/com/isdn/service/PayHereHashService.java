package com.isdn.service;

import com.isdn.config.PayHereConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayHereHashService {

    private final PayHereConfig payHereConfig;

    /**
     * Generate MD5 hash for payment initiation
     * Format: merchant_id + order_id + amount + currency + MD5(merchant_secret)
     */
    public String generatePaymentHash(String orderId, BigDecimal amount, String currency) {
        String merchantSecret = payHereConfig.getMerchantSecret();
        String merchantId = payHereConfig.getMerchantId();

        // Format amount to 2 decimal places without grouping
        String formattedAmount = formatAmount(amount);

        // MD5(merchant_secret) - convert to uppercase
        String hashedSecret = md5(merchantSecret).toUpperCase();

        // Concatenate: merchant_id + order_id + amount + currency + hashedSecret
        String toHash = merchantId + orderId + formattedAmount + currency + hashedSecret;

        log.debug("Generating payment hash for order: {}, amount: {}, currency: {}", orderId, formattedAmount, currency);

        // Final MD5 hash - convert to uppercase
        return md5(toHash).toUpperCase();
    }

    /**
     * Verify MD5 hash from PayHere notification
     * Format: merchant_id + order_id + payhere_amount + payhere_currency + status_code + MD5(merchant_secret)
     */
    public boolean verifyNotificationHash(String merchantId, String orderId, String amount,
                                          String currency, Integer statusCode, String receivedHash) {
        String merchantSecret = payHereConfig.getMerchantSecret();

        // MD5(merchant_secret) - convert to uppercase
        String hashedSecret = md5(merchantSecret).toUpperCase();

        // Concatenate: merchant_id + order_id + amount + currency + status_code + hashedSecret
        String toHash = merchantId + orderId + amount + currency + statusCode + hashedSecret;

        // Generate expected hash - convert to uppercase
        String expectedHash = md5(toHash).toUpperCase();

        boolean isValid = expectedHash.equalsIgnoreCase(receivedHash);

        if (!isValid) {
            log.warn("PayHere hash verification failed for order: {}. Expected: {}, Received: {}",
                    orderId, expectedHash, receivedHash);
        } else {
            log.info("PayHere hash verification successful for order: {}", orderId);
        }

        return isValid;
    }

    /**
     * Format amount to PayHere required format (2 decimal places, no grouping)
     */
    public String formatAmount(BigDecimal amount) {
        return String.format("%.2f", amount);
    }

    /**
     * Generate MD5 hash
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not found", e);
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}

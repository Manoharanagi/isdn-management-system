package com.isdn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payhere")
@Data
public class PayHereConfig {

    private boolean sandbox = true;
    private String sandboxUrl = "https://sandbox.payhere.lk/pay/checkout";
    private String productionUrl = "https://www.payhere.lk/pay/checkout";
    private String merchantId;
    private String merchantSecret;
    private String notifyUrl;
    private String returnUrl;
    private String cancelUrl;
    private String currency = "LKR";

    /**
     * Get the appropriate PayHere checkout URL based on sandbox mode
     */
    public String getCheckoutUrl() {
        return sandbox ? sandboxUrl : productionUrl;
    }
}

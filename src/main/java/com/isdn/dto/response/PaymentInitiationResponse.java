package com.isdn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationResponse {

    private String paymentReference;
    private String paymentUrl;
    private Map<String, String> payhereFormData;
    private String message;
}

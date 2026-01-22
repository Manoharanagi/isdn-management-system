package com.isdn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "invoice")
@Data
public class InvoiceConfig {

    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String logoPath;
}

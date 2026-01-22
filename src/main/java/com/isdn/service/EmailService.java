package com.isdn.service;

import com.isdn.config.InvoiceConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final InvoiceConfig invoiceConfig;

    /**
     * Send plain text email
     */
    @Async
    public void sendPlainTextEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(invoiceConfig.getCompanyEmail());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Plain text email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send plain text email to: {}", to, e);
        }
    }

    /**
     * Send HTML email using Thymeleaf template
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(invoiceConfig.getCompanyEmail());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
        }
    }

    /**
     * Send email with PDF attachment
     */
    @Async
    public void sendEmailWithAttachment(String to, String subject, String templateName,
                                        Map<String, Object> variables, byte[] attachment, String attachmentName) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(invoiceConfig.getCompanyEmail());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.addAttachment(attachmentName, new ByteArrayResource(attachment));

            mailSender.send(message);
            log.info("Email with attachment sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email with attachment to: {}", to, e);
        }
    }

    /**
     * Send invoice email with PDF attachment
     */
    @Async
    public void sendInvoiceEmail(String to, String customerName, String orderNumber,
                                  byte[] invoicePdf) {
        try {
            Map<String, Object> variables = Map.of(
                    "customerName", customerName,
                    "orderNumber", orderNumber
            );

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/invoice-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(invoiceConfig.getCompanyEmail());
            helper.setTo(to);
            helper.setSubject("Your Invoice for Order #" + orderNumber);
            helper.setText(htmlContent, true);
            helper.addAttachment("Invoice-" + orderNumber + ".pdf", new ByteArrayResource(invoicePdf));

            mailSender.send(message);
            log.info("Invoice email sent to: {} for order: {}", to, orderNumber);
        } catch (MessagingException e) {
            log.error("Failed to send invoice email to: {} for order: {}", to, orderNumber, e);
        }
    }
}

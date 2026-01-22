package com.isdn.service;

import com.isdn.config.InvoiceConfig;
import com.isdn.model.Order;
import com.isdn.model.OrderItem;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceConfig invoiceConfig;

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb HEADER_BG_COLOR = new DeviceRgb(52, 73, 94);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);

    /**
     * Generate PDF invoice from Order
     */
    public byte[] generateInvoice(Order order) {
        log.info("Generating invoice for order: {}", order.getOrderNumber());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Header Section
            addHeader(document, boldFont, regularFont);

            // Invoice Info Section
            addInvoiceInfo(document, order, boldFont, regularFont);

            // Customer Info Section
            addCustomerInfo(document, order, boldFont, regularFont);

            // Order Items Table
            addItemsTable(document, order, boldFont, regularFont);

            // Totals Section
            addTotals(document, order, boldFont, regularFont);

            // Footer Section
            addFooter(document, regularFont);

            document.close();
            log.info("Invoice generated successfully for order: {}", order.getOrderNumber());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate invoice for order: {}", order.getOrderNumber(), e);
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

    private void addHeader(Document document, PdfFont boldFont, PdfFont regularFont) {
        // Company Name
        Paragraph companyName = new Paragraph(invoiceConfig.getCompanyName())
                .setFont(boldFont)
                .setFontSize(24)
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(5);
        document.add(companyName);

        // Company Details
        Paragraph companyDetails = new Paragraph()
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .add(invoiceConfig.getCompanyAddress() + "\n")
                .add("Phone: " + invoiceConfig.getCompanyPhone() + "\n")
                .add("Email: " + invoiceConfig.getCompanyEmail())
                .setMarginBottom(20);
        document.add(companyDetails);

        // Invoice Title
        Paragraph invoiceTitle = new Paragraph("INVOICE")
                .setFont(boldFont)
                .setFontSize(28)
                .setFontColor(HEADER_BG_COLOR)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20);
        document.add(invoiceTitle);
    }

    private void addInvoiceInfo(Document document, Order order, PdfFont boldFont, PdfFont regularFont) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Left column - Invoice details
        Cell leftCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph("Invoice Number:").setFont(boldFont).setFontSize(10))
                .add(new Paragraph("INV-" + order.getOrderNumber()).setFont(regularFont).setFontSize(10))
                .add(new Paragraph("Order Number:").setFont(boldFont).setFontSize(10).setMarginTop(5))
                .add(new Paragraph(order.getOrderNumber()).setFont(regularFont).setFontSize(10));

        // Right column - Date details
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

        Cell rightCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph("Invoice Date:").setFont(boldFont).setFontSize(10))
                .add(new Paragraph(order.getOrderDate().format(dateTimeFormatter)).setFont(regularFont).setFontSize(10))
                .add(new Paragraph("Payment Method:").setFont(boldFont).setFontSize(10).setMarginTop(5))
                .add(new Paragraph(order.getPaymentMethod().name().replace("_", " ")).setFont(regularFont).setFontSize(10));

        infoTable.addCell(leftCell);
        infoTable.addCell(rightCell);
        document.add(infoTable);
    }

    private void addCustomerInfo(Document document, Order order, PdfFont boldFont, PdfFont regularFont) {
        // Bill To Section
        Table customerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        Cell billToCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(10)
                .add(new Paragraph("BILL TO:").setFont(boldFont).setFontSize(10).setFontColor(PRIMARY_COLOR))
                .add(new Paragraph(order.getUser().getBusinessName() != null ?
                        order.getUser().getBusinessName() : order.getUser().getUsername())
                        .setFont(boldFont).setFontSize(11))
                .add(new Paragraph(order.getUser().getEmail()).setFont(regularFont).setFontSize(10))
                .add(new Paragraph(order.getUser().getPhoneNumber() != null ?
                        order.getUser().getPhoneNumber() : order.getContactNumber())
                        .setFont(regularFont).setFontSize(10));

        Cell shipToCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(10)
                .add(new Paragraph("SHIP TO:").setFont(boldFont).setFontSize(10).setFontColor(PRIMARY_COLOR))
                .add(new Paragraph(order.getDeliveryAddress()).setFont(regularFont).setFontSize(10))
                .add(new Paragraph("Contact: " + order.getContactNumber()).setFont(regularFont).setFontSize(10));

        customerTable.addCell(billToCell);
        customerTable.addCell(shipToCell);
        document.add(customerTable);
    }

    private void addItemsTable(Document document, Order order, PdfFont boldFont, PdfFont regularFont) {
        // Items Table Header
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{0.5f, 3, 1, 1, 1.5f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Header Row
        String[] headers = {"#", "Item Description", "Qty", "Unit Price", "Amount"};
        for (String header : headers) {
            Cell headerCell = new Cell()
                    .setBackgroundColor(HEADER_BG_COLOR)
                    .setPadding(8)
                    .add(new Paragraph(header).setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE));
            if (header.equals("Qty") || header.equals("Unit Price") || header.equals("Amount")) {
                headerCell.setTextAlignment(TextAlignment.RIGHT);
            }
            itemsTable.addHeaderCell(headerCell);
        }

        // Item Rows
        int itemNumber = 1;
        for (OrderItem item : order.getItems()) {
            boolean isEvenRow = itemNumber % 2 == 0;

            Cell numCell = createItemCell(String.valueOf(itemNumber), regularFont, isEvenRow, TextAlignment.LEFT);
            Cell descCell = createItemCell(item.getProduct().getName() + "\nSKU: " + item.getProduct().getSku(),
                    regularFont, isEvenRow, TextAlignment.LEFT);
            Cell qtyCell = createItemCell(String.valueOf(item.getQuantity()), regularFont, isEvenRow, TextAlignment.RIGHT);
            Cell priceCell = createItemCell(formatCurrency(item.getUnitPrice()), regularFont, isEvenRow, TextAlignment.RIGHT);
            Cell amountCell = createItemCell(formatCurrency(item.getSubtotal()), regularFont, isEvenRow, TextAlignment.RIGHT);

            itemsTable.addCell(numCell);
            itemsTable.addCell(descCell);
            itemsTable.addCell(qtyCell);
            itemsTable.addCell(priceCell);
            itemsTable.addCell(amountCell);

            itemNumber++;
        }

        document.add(itemsTable);
    }

    private Cell createItemCell(String text, PdfFont font, boolean isEvenRow, TextAlignment alignment) {
        Cell cell = new Cell()
                .setPadding(8)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setTextAlignment(alignment);
        if (isEvenRow) {
            cell.setBackgroundColor(LIGHT_GRAY);
        }
        return cell;
    }

    private void addTotals(Document document, Order order, PdfFont boldFont, PdfFont regularFont) {
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f}))
                .setWidth(UnitValue.createPercentValue(40))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginBottom(30);

        // Subtotal
        totalsTable.addCell(createTotalLabelCell("Subtotal:", regularFont));
        totalsTable.addCell(createTotalValueCell(formatCurrency(order.getTotalAmount()), regularFont));

        // Tax (if applicable - showing 0% for now)
        totalsTable.addCell(createTotalLabelCell("Tax (0%):", regularFont));
        totalsTable.addCell(createTotalValueCell(formatCurrency(BigDecimal.ZERO), regularFont));

        // Total
        Cell totalLabelCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(8)
                .add(new Paragraph("TOTAL:").setFont(boldFont).setFontSize(12).setFontColor(ColorConstants.WHITE));
        totalsTable.addCell(totalLabelCell);

        Cell totalValueCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(formatCurrency(order.getTotalAmount()))
                        .setFont(boldFont).setFontSize(12).setFontColor(ColorConstants.WHITE));
        totalsTable.addCell(totalValueCell);

        document.add(totalsTable);
    }

    private Cell createTotalLabelCell(String text, PdfFont font) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(5)
                .add(new Paragraph(text).setFont(font).setFontSize(10));
    }

    private Cell createTotalValueCell(String text, PdfFont font) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(5)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(text).setFont(font).setFontSize(10));
    }

    private void addFooter(Document document, PdfFont regularFont) {
        // Notes section if present
        Paragraph thankYou = new Paragraph("Thank you for your business!")
                .setFont(regularFont)
                .setFontSize(12)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(thankYou);

        // Footer
        Paragraph footer = new Paragraph()
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .add("This is a computer-generated invoice. No signature required.\n")
                .add("For any queries, please contact us at " + invoiceConfig.getCompanyEmail());
        document.add(footer);
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("LKR %.2f", amount);
    }
}

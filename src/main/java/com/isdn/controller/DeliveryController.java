package com.isdn.controller;

import com.isdn.dto.request.AssignDeliveryRequest;
import com.isdn.dto.request.UpdateDeliveryStatusRequest;
import com.isdn.dto.response.DeliveryResponse;
import com.isdn.model.Delivery;
import com.isdn.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * POST /api/deliveries/assign - Assign delivery to driver
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> assignDelivery(
            @Valid @RequestBody AssignDeliveryRequest request) {
        log.info("POST /api/deliveries/assign - Assign delivery");
        DeliveryResponse delivery = deliveryService.assignDelivery(request);
        return new ResponseEntity<>(delivery, HttpStatus.CREATED);
    }

    /**
     * PUT /api/deliveries/{deliveryId}/status - Update delivery status
     */
    @PutMapping("/{deliveryId}/status")
    @PreAuthorize("hasAnyRole('DRIVER', 'LOGISTICS_OFFICER', 'RDC_STAFF', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        log.info("PUT /api/deliveries/{}/status - Update status", deliveryId);
        DeliveryResponse delivery = deliveryService.updateDeliveryStatus(deliveryId, request);
        return ResponseEntity.ok(delivery);
    }

    /**
     * GET /api/deliveries/rdc/{rdcId} - Get deliveries by RDC
     */
    @GetMapping("/rdc/{rdcId}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByRdc(@PathVariable Long rdcId) {
        log.info("GET /api/deliveries/rdc/{} - Fetch deliveries", rdcId);
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByRdc(rdcId);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * GET /api/deliveries/driver/{driverId} - Get deliveries by driver
     */
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'LOGISTICS_OFFICER', 'ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByDriver(@PathVariable Long driverId) {
        log.info("GET /api/deliveries/driver/{} - Fetch deliveries", driverId);
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByDriver(driverId);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * GET /api/deliveries/active - Get active deliveries
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getActiveDeliveries() {
        log.info("GET /api/deliveries/active - Fetch active deliveries");
        List<DeliveryResponse> deliveries = deliveryService.getActiveDeliveries();
        return ResponseEntity.ok(deliveries);
    }

    /**
     * GET /api/deliveries/{deliveryId} - Get delivery by ID
     */
    @GetMapping("/{deliveryId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'LOGISTICS_OFFICER', 'RDC_STAFF', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long deliveryId) {
        log.info("GET /api/deliveries/{} - Fetch delivery", deliveryId);
        DeliveryResponse delivery = deliveryService.getDeliveryById(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    /**
     * GET /api/deliveries/order/{orderId} - Get delivery by order ID
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DRIVER', 'LOGISTICS_OFFICER', 'RDC_STAFF', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(@PathVariable Long orderId) {
        log.info("GET /api/deliveries/order/{} - Fetch delivery for order", orderId);
        DeliveryResponse delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(delivery);
    }

    /**
     * PUT /api/deliveries/{deliveryId}/pickup - Mark delivery as picked up
     */
    @PutMapping("/{deliveryId}/pickup")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> pickupDelivery(@PathVariable Long deliveryId) {
        log.info("PUT /api/deliveries/{}/pickup - Mark as picked up", deliveryId);
        DeliveryResponse delivery = deliveryService.pickupDelivery(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    /**
     * PUT /api/deliveries/{deliveryId}/start - Start delivery (IN_TRANSIT)
     */
    @PutMapping("/{deliveryId}/start")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> startDelivery(@PathVariable Long deliveryId) {
        log.info("PUT /api/deliveries/{}/start - Start delivery", deliveryId);
        DeliveryResponse delivery = deliveryService.startDelivery(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    /**
     * PUT /api/deliveries/{deliveryId}/arrive - Mark as arrived at destination
     */
    @PutMapping("/{deliveryId}/arrive")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> arriveAtDestination(@PathVariable Long deliveryId) {
        log.info("PUT /api/deliveries/{}/arrive - Mark as arrived", deliveryId);
        DeliveryResponse delivery = deliveryService.arriveAtDestination(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    /**
     * PUT /api/deliveries/{deliveryId}/complete - Complete delivery
     */
    @PutMapping("/{deliveryId}/complete")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> completeDelivery(@PathVariable Long deliveryId) {
        log.info("PUT /api/deliveries/{}/complete - Complete delivery", deliveryId);
        DeliveryResponse delivery = deliveryService.completeDelivery(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    /**
     *
     * POST  /api/deliveries/1/proof -Complete delivery with proof
     */
    @PostMapping("/{deliveryId}/proof")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> completeDeliveryProof(@PathVariable long deliveryId,@RequestParam("file") MultipartFile file){
      log.info("POST  /api/deliveries/{}/proof - Complete delivery with proof",deliveryId);
        DeliveryResponse delivery =deliveryService.completeDeliveryProof(deliveryId,file);
        return ResponseEntity.ok(delivery);
    }

    /**
     * PUT /api/deliveries/{deliveryId}/fail - Mark delivery as failed
     */
    @PutMapping("/{deliveryId}/fail")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> failDelivery(
            @PathVariable Long deliveryId,
            @RequestParam(required = false, defaultValue = "Delivery failed") String reason) {
        log.info("PUT /api/deliveries/{}/fail - Mark as failed", deliveryId);
        DeliveryResponse delivery = deliveryService.failDelivery(deliveryId, reason);
        return ResponseEntity.ok(delivery);
    }
}

package com.isdn.service;

import com.isdn.dto.request.AssignDeliveryRequest;
import com.isdn.dto.request.UpdateDeliveryStatusRequest;
import com.isdn.dto.response.DeliveryResponse;
import com.isdn.exception.BadRequestException;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.*;
import com.isdn.repository.DeliveryRepository;
import com.isdn.repository.DriverRepository;
import com.isdn.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;

    /**
     * Assign delivery to driver
     */
    @Transactional
    public DeliveryResponse assignDelivery(AssignDeliveryRequest request) {
        log.info("Assigning delivery for order: {} to driver: {}",
                 request.getOrderId(), request.getDriverId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order must be CONFIRMED before assignment");
        }

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new BadRequestException("Driver is not available");
        }

        // Check if delivery already exists
        Delivery delivery = deliveryRepository.findByOrder(order)
                .orElseGet(() -> Delivery.builder()
                        .order(order)
                        .status(DeliveryStatus.PENDING_ASSIGNMENT)
                        .build());

        // Set destination coordinates (simplified - in real app, use geocoding)
        delivery.setDestinationLatitude(new BigDecimal("6.9271")); // Colombo
        delivery.setDestinationLongitude(new BigDecimal("79.8612"));
        delivery.setEstimatedDistanceKm(new BigDecimal("15.5"));

        // Assign driver
        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedDate(LocalDateTime.now());
        delivery.setNotes(request.getNotes());

        deliveryRepository.save(delivery);

        // Update driver status
        driver.setStatus(DriverStatus.ON_DELIVERY);
        driverRepository.save(driver);

        // Update order status
        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        orderRepository.save(order);

        log.info("Delivery assigned successfully");
        return mapToResponse(delivery);
    }

    /**
     * Update delivery status
     */
    @Transactional
    public DeliveryResponse updateDeliveryStatus(Long deliveryId, UpdateDeliveryStatusRequest request) {
        log.info("Updating delivery status: {} to {}", deliveryId, request.getStatus());

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        DeliveryStatus oldStatus = delivery.getStatus();
        delivery.setStatus(request.getStatus());

        // Update timestamps based on status
        switch (request.getStatus()) {
            case PICKED_UP -> {
                delivery.setPickupTime(LocalDateTime.now());
                delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);
            }
            case IN_TRANSIT -> {
                delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);
            }
            case DELIVERED -> {
                delivery.setDeliveryTime(LocalDateTime.now());
                delivery.getOrder().setStatus(OrderStatus.DELIVERED);
                delivery.getOrder().setActualDeliveryDate(LocalDateTime.now().toLocalDate());

                // Free up driver
                if (delivery.getDriver() != null) {
                    delivery.getDriver().setStatus(DriverStatus.AVAILABLE);
                    driverRepository.save(delivery.getDriver());
                }
            }
            case FAILED -> {
                delivery.getOrder().setStatus(OrderStatus.FAILED_DELIVERY);

                // Free up driver
                if (delivery.getDriver() != null) {
                    delivery.getDriver().setStatus(DriverStatus.AVAILABLE);
                    driverRepository.save(delivery.getDriver());
                }
            }
        }

        if (request.getNotes() != null) {
            delivery.setNotes(request.getNotes());
        }

        deliveryRepository.save(delivery);
        orderRepository.save(delivery.getOrder());

        log.info("Delivery status updated from {} to {}", oldStatus, request.getStatus());
        return mapToResponse(delivery);
    }

    /**
     * Get all deliveries for an RDC
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByRdc(Long rdcId) {
        log.info("Fetching deliveries for RDC: {}", rdcId);
        List<Delivery> deliveries = deliveryRepository.findByRdcIdOrderByCreatedAtDesc(rdcId);
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get deliveries by driver
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByDriver(Long driverId) {
        log.info("Fetching deliveries for driver: {}", driverId);
        List<Delivery> deliveries = deliveryRepository.findByDriver_DriverIdOrderByCreatedAtDesc(driverId);
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active deliveries (in transit)
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getActiveDeliveries() {
        log.info("Fetching active deliveries");
        List<DeliveryStatus> activeStatuses = List.of(
                DeliveryStatus.ASSIGNED,
                DeliveryStatus.PICKED_UP,
                DeliveryStatus.IN_TRANSIT,
                DeliveryStatus.ARRIVED
        );
        List<Delivery> deliveries = deliveryRepository.findByStatusIn(activeStatuses);
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get delivery by ID
     */
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryById(Long deliveryId) {
        log.info("Fetching delivery: {}", deliveryId);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        return mapToResponse(delivery);
    }

    /**
     * Get delivery by order ID
     */
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        log.info("Fetching delivery for order: {}", orderId);
        Delivery delivery = deliveryRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for this order"));
        return mapToResponse(delivery);
    }

    /**
     * Mark delivery as picked up (convenience endpoint for drivers)
     */
    @Transactional
    public DeliveryResponse pickupDelivery(Long deliveryId) {
        log.info("Marking delivery {} as picked up", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new BadRequestException("Only ASSIGNED deliveries can be picked up");
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickupTime(LocalDateTime.now());
        delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);

        deliveryRepository.save(delivery);
        orderRepository.save(delivery.getOrder());

        log.info("Delivery {} marked as picked up", deliveryId);
        return mapToResponse(delivery);
    }

    /**
     * Start delivery (convenience endpoint - marks as IN_TRANSIT)
     */
    @Transactional
    public DeliveryResponse startDelivery(Long deliveryId) {
        log.info("Starting delivery {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new BadRequestException("Delivery must be PICKED_UP before starting transit");
        }

        delivery.setStatus(DeliveryStatus.IN_TRANSIT);
        delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);

        deliveryRepository.save(delivery);
        orderRepository.save(delivery.getOrder());

        log.info("Delivery {} started (IN_TRANSIT)", deliveryId);
        return mapToResponse(delivery);
    }

    /**
     * Mark delivery as arrived at destination
     */
    @Transactional
    public DeliveryResponse arriveAtDestination(Long deliveryId) {
        log.info("Marking delivery {} as arrived at destination", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
            throw new BadRequestException("Only IN_TRANSIT deliveries can be marked as arrived");
        }

        delivery.setStatus(DeliveryStatus.ARRIVED);
        deliveryRepository.save(delivery);

        log.info("Delivery {} marked as arrived", deliveryId);
        return mapToResponse(delivery);
    }

    /**
     * Complete delivery (convenience endpoint - marks as DELIVERED)
     */
    @Transactional
    public DeliveryResponse completeDelivery(Long deliveryId) {
        log.info("Completing delivery {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.IN_TRANSIT &&
            delivery.getStatus() != DeliveryStatus.ARRIVED) {
            throw new BadRequestException("Only IN_TRANSIT or ARRIVED deliveries can be completed");
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveryTime(LocalDateTime.now());
        delivery.getOrder().setStatus(OrderStatus.DELIVERED);
        delivery.getOrder().setActualDeliveryDate(LocalDateTime.now().toLocalDate());

        // Free up driver
        if (delivery.getDriver() != null) {
            delivery.getDriver().setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(delivery.getDriver());
        }

        deliveryRepository.save(delivery);
        orderRepository.save(delivery.getOrder());

        log.info("Delivery {} completed", deliveryId);
        return mapToResponse(delivery);
    }

    /**
     * Fail delivery (convenience endpoint - marks as FAILED)
     */
    @Transactional
    public DeliveryResponse failDelivery(Long deliveryId, String reason) {
        log.info("Marking delivery {} as failed. Reason: {}", deliveryId, reason);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setNotes(reason);
        delivery.getOrder().setStatus(OrderStatus.FAILED_DELIVERY);

        // Free up driver
        if (delivery.getDriver() != null) {
            delivery.getDriver().setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(delivery.getDriver());
        }

        deliveryRepository.save(delivery);
        orderRepository.save(delivery.getOrder());

        log.info("Delivery {} marked as failed", deliveryId);
        return mapToResponse(delivery);
    }

    /**
     * Map Delivery to DeliveryResponse
     */
    private DeliveryResponse mapToResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrder().getOrderId())
                .orderNumber(delivery.getOrder().getOrderNumber())
                .driverId(delivery.getDriver() != null ? delivery.getDriver().getDriverId() : null)
                .driverName(delivery.getDriver() != null ? delivery.getDriver().getUser().getUsername() : null)
                .vehicleNumber(delivery.getDriver() != null ? delivery.getDriver().getVehicleNumber() : null)
                .status(delivery.getStatus())
                .deliveryAddress(delivery.getOrder().getDeliveryAddress())
                .contactNumber(delivery.getOrder().getContactNumber())
                .assignedDate(delivery.getAssignedDate())
                .pickupTime(delivery.getPickupTime())
                .deliveryTime(delivery.getDeliveryTime())
                .currentLatitude(delivery.getCurrentLatitude())
                .currentLongitude(delivery.getCurrentLongitude())
                .destinationLatitude(delivery.getDestinationLatitude())
                .destinationLongitude(delivery.getDestinationLongitude())
                .estimatedDistanceKm(delivery.getEstimatedDistanceKm())
                .notes(delivery.getNotes())
                .build();
    }
}

# FEATURE 4: ORDER MANAGEMENT FOR STAFF

---

## FEATURE OVERVIEW

### What We'll Build:
1. ✅ Order Management Dashboard for staff
2. ✅ View all orders (not just customer's own orders)
3. ✅ Confirm pending orders (PENDING → CONFIRMED)
4. ✅ Update order status at various stages
5. ✅ Filter orders by status
6. ✅ Bulk order operations
7. ✅ Order details with action buttons
8. ✅ Role-based permissions

---

## BACKEND IMPLEMENTATION

### 1. Additional DTOs

**File: `src/main/java/com/isdn/dto/request/UpdateOrderStatusRequest.java`**
```java
package com.isdn.dto.request;

import com.isdn.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String notes;
}
```

---

### 2. Updated OrderService Methods

**File: `src/main/java/com/isdn/service/OrderService.java`**

Add these methods to the existing OrderService:

```java
/**
 * Get all orders (for staff/admin)
 */
@Transactional(readOnly = true)
public List<OrderResponse> getAllOrders() {
    log.info("Fetching all orders for staff");

    List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();

    return orders.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}

/**
 * Get orders by status
 */
@Transactional(readOnly = true)
public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
    log.info("Fetching orders with status: {}", status);

    List<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status);

    return orders.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}

/**
 * Confirm order (PENDING → CONFIRMED)
 */
@Transactional
public OrderResponse confirmOrder(Long orderId) {
    log.info("Confirming order: {}", orderId);

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (order.getStatus() != OrderStatus.PENDING) {
        throw new BadRequestException("Only PENDING orders can be confirmed");
    }

    order.setStatus(OrderStatus.CONFIRMED);
    orderRepository.save(order);

    log.info("Order {} confirmed successfully", orderId);
    return mapToResponse(order);
}

/**
 * Update order status
 */
@Transactional
public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
    log.info("Updating order {} status to {}", orderId, request.getStatus());

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    OrderStatus oldStatus = order.getStatus();
    order.setStatus(request.getStatus());

    // Update timestamps based on status
    switch (request.getStatus()) {
        case PROCESSING -> {
            // Order is being processed
        }
        case READY_FOR_DELIVERY -> {
            // Order is ready for pickup
        }
        case OUT_FOR_DELIVERY -> {
            // Order is out for delivery (handled by delivery service)
        }
        case DELIVERED -> {
            order.setActualDeliveryDate(LocalDate.now());
        }
        case CANCELLED -> {
            // Order cancelled
        }
        case FAILED_DELIVERY -> {
            // Delivery failed
        }
    }

    orderRepository.save(order);

    log.info("Order status updated from {} to {}", oldStatus, request.getStatus());
    return mapToResponse(order);
}

/**
 * Get orders by RDC
 */
@Transactional(readOnly = true)
public List<OrderResponse> getOrdersByRdc(Long rdcId) {
    log.info("Fetching orders for RDC: {}", rdcId);

    RDC rdc = rdcRepository.findById(rdcId)
            .orElseThrow(() -> new ResourceNotFoundException("RDC not found"));

    List<Order> orders = orderRepository.findByRdcOrderByOrderDateDesc(rdc);

    return orders.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}
```

---

### 3. Updated OrderRepository

**File: `src/main/java/com/isdn/repository/OrderRepository.java`**

Add these methods:

```java
List<Order> findAllByOrderByOrderDateDesc();

List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

List<Order> findByRdcOrderByOrderDateDesc(RDC rdc);

@Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.orderDate DESC")
List<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses);
```

---

### 4. Updated OrderController

**File: `src/main/java/com/isdn/controller/OrderController.java`**

Add these endpoints:

```java
/**
 * GET /api/orders/all - Get all orders (staff only)
 */
@GetMapping("/all")
@PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
public ResponseEntity<List<OrderResponse>> getAllOrders() {
    log.info("GET /api/orders/all - Fetch all orders");
    List<OrderResponse> orders = orderService.getAllOrders();
    return ResponseEntity.ok(orders);
}

/**
 * GET /api/orders/status/{status} - Get orders by status
 */
@GetMapping("/status/{status}")
@PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
    log.info("GET /api/orders/status/{} - Fetch orders by status", status);
    List<OrderResponse> orders = orderService.getOrdersByStatus(status);
    return ResponseEntity.ok(orders);
}

/**
 * PUT /api/orders/{orderId}/confirm - Confirm order
 */
@PutMapping("/{orderId}/confirm")
@PreAuthorize("hasAnyRole('RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long orderId) {
    log.info("PUT /api/orders/{}/confirm - Confirm order", orderId);
    OrderResponse order = orderService.confirmOrder(orderId);
    return ResponseEntity.ok(order);
}

/**
 * PUT /api/orders/{orderId}/status - Update order status
 */
@PutMapping("/{orderId}/status")
@PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
public ResponseEntity<OrderResponse> updateOrderStatus(
        @PathVariable Long orderId,
        @Valid @RequestBody UpdateOrderStatusRequest request) {
    log.info("PUT /api/orders/{}/status - Update order status", orderId);
    OrderResponse order = orderService.updateOrderStatus(orderId, request);
    return ResponseEntity.ok(order);
}

/**
 * GET /api/orders/rdc/{rdcId} - Get orders by RDC
 */
@GetMapping("/rdc/{rdcId}")
@PreAuthorize("hasAnyRole('RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
public ResponseEntity<List<OrderResponse>> getOrdersByRdc(@PathVariable Long rdcId) {
    log.info("GET /api/orders/rdc/{} - Fetch orders for RDC", rdcId);
    List<OrderResponse> orders = orderService.getOrdersByRdc(rdcId);
    return ResponseEntity.ok(orders);
}
```

---

## FRONTEND IMPLEMENTATION

### 1. Updated Order Service

**File: `src/services/orderService.js`**

Add these new API calls:

```javascript
/**
 * Get all orders (staff only)
 */
export const getAllOrders = async () => {
    const response = await api.get('/orders/all');
    return response.data;
};

/**
 * Get orders by status
 */
export const getOrdersByStatus = async (status) => {
    const response = await api.get(`/orders/status/${status}`);
    return response.data;
};

/**
 * Confirm order (PENDING → CONFIRMED)
 */
export const confirmOrder = async (orderId) => {
    const response = await api.put(`/orders/${orderId}/confirm`);
    return response.data;
};

/**
 * Update order status
 */
export const updateOrderStatus = async (orderId, statusData) => {
    const response = await api.put(`/orders/${orderId}/status`, statusData);
    return response.data;
};

/**
 * Get orders by RDC
 */
export const getOrdersByRdc = async (rdcId) => {
    const response = await api.get(`/orders/rdc/${rdcId}`);
    return response.data;
};
```

---

### 2. Order Management Component

**File: `src/components/orders/OrderManagement.jsx`**

```javascript
import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import * as orderService from '../../services/orderService';
import OrderConfirmModal from './OrderConfirmModal';
import Loader from '../common/Loader';

export default function OrderManagement() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [filterStatus, setFilterStatus] = useState('ALL');

    useEffect(() => {
        loadOrders();
    }, [filterStatus]);

    const loadOrders = async () => {
        try {
            setLoading(true);
            let data;

            if (filterStatus === 'ALL') {
                data = await orderService.getAllOrders();
            } else {
                data = await orderService.getOrdersByStatus(filterStatus);
            }

            setOrders(data);
            setError(null);
        } catch (err) {
            setError('Failed to load orders');
            toast.error('Failed to load orders');
            console.error('Error loading orders:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleConfirmOrder = async (orderId) => {
        if (!window.confirm('Are you sure you want to confirm this order?')) {
            return;
        }

        try {
            await orderService.confirmOrder(orderId);
            toast.success('Order confirmed successfully!');
            loadOrders();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to confirm order');
        }
    };

    const getStatusColor = (status) => {
        const colors = {
            PENDING: 'bg-yellow-100 text-yellow-800',
            CONFIRMED: 'bg-blue-100 text-blue-800',
            PROCESSING: 'bg-purple-100 text-purple-800',
            READY_FOR_DELIVERY: 'bg-indigo-100 text-indigo-800',
            OUT_FOR_DELIVERY: 'bg-orange-100 text-orange-800',
            DELIVERED: 'bg-green-100 text-green-800',
            CANCELLED: 'bg-red-100 text-red-800',
            FAILED_DELIVERY: 'bg-red-100 text-red-800'
        };
        return colors[status] || 'bg-gray-100 text-gray-800';
    };

    const filteredOrders = orders;

    if (loading) return <Loader />;

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Order Management</h1>
                    <p className="text-gray-600">Manage and confirm customer orders</p>
                </div>
                <button
                    onClick={loadOrders}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                    Refresh
                </button>
            </div>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
                    {error}
                </div>
            )}

            {/* Status Filter */}
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <h3 className="font-semibold mb-3">Filter by Status</h3>
                <div className="flex flex-wrap gap-2">
                    <button
                        onClick={() => setFilterStatus('ALL')}
                        className={`px-4 py-2 rounded-lg transition ${
                            filterStatus === 'ALL'
                                ? 'bg-blue-600 text-white'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                        All Orders
                    </button>
                    <button
                        onClick={() => setFilterStatus('PENDING')}
                        className={`px-4 py-2 rounded-lg transition ${
                            filterStatus === 'PENDING'
                                ? 'bg-yellow-600 text-white'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                        Pending
                    </button>
                    <button
                        onClick={() => setFilterStatus('CONFIRMED')}
                        className={`px-4 py-2 rounded-lg transition ${
                            filterStatus === 'CONFIRMED'
                                ? 'bg-blue-600 text-white'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                        Confirmed
                    </button>
                    <button
                        onClick={() => setFilterStatus('OUT_FOR_DELIVERY')}
                        className={`px-4 py-2 rounded-lg transition ${
                            filterStatus === 'OUT_FOR_DELIVERY'
                                ? 'bg-orange-600 text-white'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                        Out for Delivery
                    </button>
                    <button
                        onClick={() => setFilterStatus('DELIVERED')}
                        className={`px-4 py-2 rounded-lg transition ${
                            filterStatus === 'DELIVERED'
                                ? 'bg-green-600 text-white'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                        Delivered
                    </button>
                </div>
            </div>

            {/* Orders Table */}
            <div className="bg-white rounded-lg shadow-md overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-xl font-bold">
                        Orders ({filteredOrders.length})
                    </h2>
                </div>

                {filteredOrders.length === 0 ? (
                    <div className="text-center py-12 text-gray-500">
                        No orders found
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Order Number
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Customer
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Date
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Total Amount
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Status
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Actions
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {filteredOrders.map(order => (
                                    <tr key={order.orderId} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">
                                                {order.orderNumber}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">
                                                {order.customerName || 'N/A'}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                {order.contactNumber}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {new Date(order.orderDate).toLocaleDateString()}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-semibold text-gray-900">
                                                Rs. {order.totalAmount.toFixed(2)}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(order.status)}`}>
                                                {order.status.replace('_', ' ')}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                                            {order.status === 'PENDING' && (
                                                <button
                                                    onClick={() => handleConfirmOrder(order.orderId)}
                                                    className="text-green-600 hover:text-green-900"
                                                >
                                                    Confirm
                                                </button>
                                            )}
                                            <button
                                                onClick={() => setSelectedOrder(order)}
                                                className="text-blue-600 hover:text-blue-900"
                                            >
                                                View Details
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {showConfirmModal && selectedOrder && (
                <OrderConfirmModal
                    order={selectedOrder}
                    onClose={() => {
                        setShowConfirmModal(false);
                        setSelectedOrder(null);
                    }}
                    onSuccess={loadOrders}
                />
            )}
        </div>
    );
}
```

---

### 3. Order Confirm Modal

**File: `src/components/orders/OrderConfirmModal.jsx`**

```javascript
import React, { useState } from 'react';
import { toast } from 'react-toastify';
import * as orderService from '../../services/orderService';

export default function OrderConfirmModal({ order, onClose, onSuccess }) {
    const [loading, setLoading] = useState(false);
    const [notes, setNotes] = useState('');

    const handleConfirm = async () => {
        try {
            setLoading(true);
            await orderService.confirmOrder(order.orderId);
            toast.success('Order confirmed successfully!');
            onSuccess();
            onClose();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to confirm order');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-bold text-gray-900">Confirm Order</h2>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <div className="mb-4 p-4 bg-gray-50 rounded">
                    <p className="text-sm text-gray-600">Order Number</p>
                    <p className="font-semibold text-gray-900">{order.orderNumber}</p>

                    <p className="text-sm text-gray-600 mt-2">Total Amount</p>
                    <p className="font-semibold text-gray-900">Rs. {order.totalAmount.toFixed(2)}</p>

                    <p className="text-sm text-gray-600 mt-2">Customer</p>
                    <p className="font-semibold text-gray-900">{order.customerName}</p>
                </div>

                <p className="text-gray-700 mb-4">
                    Are you sure you want to confirm this order? This will change the status from PENDING to CONFIRMED.
                </p>

                <div className="flex gap-3 pt-4">
                    <button
                        type="button"
                        onClick={onClose}
                        className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleConfirm}
                        disabled={loading}
                        className="flex-1 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
                    >
                        {loading ? 'Confirming...' : 'Confirm Order'}
                    </button>
                </div>
            </div>
        </div>
    );
}
```

---

### 4. Add Routes to App.jsx

**Update: `src/App.jsx`**

Add import:
```javascript
import OrderManagement from './components/orders/OrderManagement';
```

Add route:
```javascript
<Route path="/order-management" element={
    <ProtectedRoute>
        <OrderManagement />
    </ProtectedRoute>
} />
```

---

### 5. Update Navbar

**Update: `src/components/common/Navbar.jsx`**

Add order management link:
```javascript
{(user?.role === 'RDC_STAFF' || user?.role === 'HO_MANAGER' || user?.role === 'ADMIN') && (
    <Link to="/order-management" className="hover:text-blue-200 transition">
        Order Management
    </Link>
)}
```

---

## TESTING FEATURE 4

### Test Users
```
Username: rdc_staff1
Password: password123
Role: RDC_STAFF

Username: admin
Password: password123
Role: ADMIN
```

### Test Steps
1. Login as RDC staff or admin
2. Navigate to "Order Management"
3. View all orders in the system
4. Filter by status (Pending, Confirmed, etc.)
5. Click "Confirm" on a PENDING order
6. Verify order status changes to CONFIRMED
7. View order details
8. Test with different filters

---

## API ENDPOINTS SUMMARY

| Method | Endpoint | Role Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/orders/all` | RDC_STAFF, HO_MANAGER, ADMIN | Get all orders |
| GET | `/api/orders/status/{status}` | RDC_STAFF, HO_MANAGER, ADMIN | Get orders by status |
| GET | `/api/orders/rdc/{rdcId}` | RDC_STAFF, HO_MANAGER, ADMIN | Get orders by RDC |
| PUT | `/api/orders/{orderId}/confirm` | RDC_STAFF, HO_MANAGER, ADMIN | Confirm pending order |
| PUT | `/api/orders/{orderId}/status` | RDC_STAFF, LOGISTICS_OFFICER, HO_MANAGER, ADMIN | Update order status |

---

## PERMISSIONS MATRIX

| Role | View All Orders | Confirm Order | Update Status | Cancel Order |
|------|----------------|---------------|---------------|--------------|
| CUSTOMER | ❌ | ❌ | ❌ | ✅ (Own only) |
| RDC_STAFF | ✅ | ✅ | ✅ | ❌ |
| LOGISTICS_OFFICER | ✅ | ❌ | ✅ | ❌ |
| HO_MANAGER | ✅ | ✅ | ✅ | ❌ |
| ADMIN | ✅ | ✅ | ✅ | ✅ |

---

## WORKFLOW

```
Customer Places Order (PENDING)
    ↓
[RDC_STAFF] Reviews order in Order Management
    ↓
[RDC_STAFF] Clicks "Confirm" button
    ↓
Order status: PENDING → CONFIRMED
    ↓
[LOGISTICS_OFFICER] Assigns to driver in Delivery Dashboard
    ↓
Order status: CONFIRMED → READY_FOR_DELIVERY
    ↓
[DRIVER] Picks up from RDC
    ↓
Order status: READY_FOR_DELIVERY → OUT_FOR_DELIVERY
    ↓
[DRIVER] Delivers to customer
    ↓
Order status: OUT_FOR_DELIVERY → DELIVERED
```

---

END OF FEATURE 4 IMPLEMENTATION

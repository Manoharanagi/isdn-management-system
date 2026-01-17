# PayHere Payment Gateway Integration

## Overview

This document describes the PayHere sandbox payment gateway integration for the ISDN Management System.

---

## Backend Implementation (Completed)

### New Files Created

| File | Description |
|------|-------------|
| `src/main/java/com/isdn/model/PaymentStatus.java` | Payment status enum |
| `src/main/java/com/isdn/model/Payment.java` | Payment entity |
| `src/main/java/com/isdn/repository/PaymentRepository.java` | Payment repository |
| `src/main/java/com/isdn/dto/request/InitiatePaymentRequest.java` | Payment initiation request DTO |
| `src/main/java/com/isdn/dto/request/PayHereNotifyRequest.java` | PayHere webhook DTO |
| `src/main/java/com/isdn/dto/response/PaymentInitiationResponse.java` | Payment initiation response DTO |
| `src/main/java/com/isdn/dto/response/PaymentResponse.java` | Payment details response DTO |
| `src/main/java/com/isdn/config/PayHereConfig.java` | PayHere configuration |
| `src/main/java/com/isdn/service/PayHereHashService.java` | Hash generation/verification |
| `src/main/java/com/isdn/service/PaymentService.java` | Payment business logic |
| `src/main/java/com/isdn/controller/PaymentController.java` | Payment REST endpoints |

### Modified Files

| File | Changes |
|------|---------|
| `src/main/resources/application.properties` | Added payhere.* configuration |
| `src/main/java/com/isdn/config/SecurityConfig.java` | Added `/api/payments/notify` as public endpoint |

### API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/payments/initiate` | Required | Initiate payment for an order |
| POST | `/api/payments/notify` | None | PayHere webhook callback |
| GET | `/api/payments/status/{ref}` | Required | Check payment status |
| GET | `/api/payments/order/{orderId}` | Required | Get payments for order |
| GET | `/api/payments` | Required | User's payment history |

---

## Frontend Implementation (React.js)

### Step 1: Create Payment Service

Create file: `src/services/paymentService.js`

```javascript
import api from './api'; // Your axios instance

const paymentService = {
  // Initiate payment for an order
  initiatePayment: async (orderId) => {
    const response = await api.post('/payments/initiate', { orderId });
    return response.data;
  },

  // Get payment status by reference
  getPaymentStatus: async (paymentReference) => {
    const response = await api.get(`/payments/status/${paymentReference}`);
    return response.data;
  },

  // Get all payments for an order
  getPaymentsByOrder: async (orderId) => {
    const response = await api.get(`/payments/order/${orderId}`);
    return response.data;
  },

  // Get user's payment history
  getPaymentHistory: async () => {
    const response = await api.get('/payments');
    return response.data;
  },
};

export default paymentService;
```

### Step 2: Create PayHere Payment Component

Create file: `src/components/PayHerePayment.jsx`

```jsx
import React, { useState } from 'react';
import paymentService from '../services/paymentService';

const PayHerePayment = ({ orderId, onSuccess, onError }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handlePayment = async () => {
    setLoading(true);
    setError(null);

    try {
      // Step 1: Initiate payment from backend
      const response = await paymentService.initiatePayment(orderId);

      const { paymentUrl, payhereFormData, paymentReference } = response;

      // Step 2: Store payment reference for later verification
      localStorage.setItem('pendingPaymentRef', paymentReference);

      // Step 3: Create and submit form to PayHere
      submitToPayHere(paymentUrl, payhereFormData);

    } catch (err) {
      setError(err.response?.data?.message || 'Failed to initiate payment');
      setLoading(false);
      if (onError) onError(err);
    }
  };

  const submitToPayHere = (paymentUrl, formData) => {
    // Create a hidden form
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = paymentUrl;

    // Add all form fields
    Object.entries(formData).forEach(([key, value]) => {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = key;
      input.value = value;
      form.appendChild(input);
    });

    // Append to body and submit
    document.body.appendChild(form);
    form.submit();
  };

  return (
    <div className="payhere-payment">
      {error && (
        <div className="alert alert-danger mb-3">
          {error}
        </div>
      )}

      <button
        onClick={handlePayment}
        disabled={loading}
        className="btn btn-primary btn-lg w-100"
      >
        {loading ? (
          <>
            <span className="spinner-border spinner-border-sm me-2" />
            Processing...
          </>
        ) : (
          <>
            <i className="bi bi-credit-card me-2"></i>
            Pay with PayHere
          </>
        )}
      </button>

      <p className="text-muted small mt-2 text-center">
        You will be redirected to PayHere secure payment page
      </p>
    </div>
  );
};

export default PayHerePayment;
```

### Step 3: Create Payment Success Page

Create file: `src/pages/PaymentSuccess.jsx`

```jsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import paymentService from '../services/paymentService';

const PaymentSuccess = () => {
  const navigate = useNavigate();
  const [status, setStatus] = useState('verifying');
  const [payment, setPayment] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    verifyPayment();
  }, []);

  const verifyPayment = async () => {
    const paymentRef = localStorage.getItem('pendingPaymentRef');

    if (!paymentRef) {
      setStatus('error');
      setError('No pending payment found');
      return;
    }

    try {
      // Poll for payment status (PayHere webhook may take a moment)
      let attempts = 0;
      const maxAttempts = 10;

      const checkStatus = async () => {
        const response = await paymentService.getPaymentStatus(paymentRef);
        setPayment(response);

        if (response.status === 'SUCCESS') {
          setStatus('success');
          localStorage.removeItem('pendingPaymentRef');
          return true;
        } else if (response.status === 'FAILED' || response.status === 'CANCELLED') {
          setStatus('failed');
          localStorage.removeItem('pendingPaymentRef');
          return true;
        }
        return false;
      };

      // Initial check
      if (await checkStatus()) return;

      // Poll every 2 seconds
      const interval = setInterval(async () => {
        attempts++;
        if (await checkStatus() || attempts >= maxAttempts) {
          clearInterval(interval);
          if (attempts >= maxAttempts && status === 'verifying') {
            setStatus('pending');
          }
        }
      }, 2000);

      return () => clearInterval(interval);

    } catch (err) {
      setStatus('error');
      setError(err.response?.data?.message || 'Failed to verify payment');
    }
  };

  const renderContent = () => {
    switch (status) {
      case 'verifying':
        return (
          <div className="text-center">
            <div className="spinner-border text-primary mb-3" style={{ width: '3rem', height: '3rem' }} />
            <h4>Verifying Payment...</h4>
            <p className="text-muted">Please wait while we confirm your payment</p>
          </div>
        );

      case 'success':
        return (
          <div className="text-center">
            <div className="text-success mb-3">
              <i className="bi bi-check-circle-fill" style={{ fontSize: '4rem' }}></i>
            </div>
            <h4 className="text-success">Payment Successful!</h4>
            <p className="text-muted">Your order has been confirmed</p>
            {payment && (
              <div className="card mt-4">
                <div className="card-body">
                  <p><strong>Payment Reference:</strong> {payment.paymentReference}</p>
                  <p><strong>Amount:</strong> {payment.currency} {payment.amount}</p>
                  <p><strong>Order:</strong> {payment.orderNumber}</p>
                </div>
              </div>
            )}
            <button
              className="btn btn-primary mt-4"
              onClick={() => navigate('/orders')}
            >
              View My Orders
            </button>
          </div>
        );

      case 'failed':
        return (
          <div className="text-center">
            <div className="text-danger mb-3">
              <i className="bi bi-x-circle-fill" style={{ fontSize: '4rem' }}></i>
            </div>
            <h4 className="text-danger">Payment Failed</h4>
            <p className="text-muted">Your payment could not be processed</p>
            <button
              className="btn btn-primary mt-4"
              onClick={() => navigate('/orders')}
            >
              Try Again
            </button>
          </div>
        );

      case 'pending':
        return (
          <div className="text-center">
            <div className="text-warning mb-3">
              <i className="bi bi-clock-fill" style={{ fontSize: '4rem' }}></i>
            </div>
            <h4 className="text-warning">Payment Processing</h4>
            <p className="text-muted">Your payment is being processed. You will receive confirmation shortly.</p>
            <button
              className="btn btn-primary mt-4"
              onClick={() => navigate('/orders')}
            >
              View My Orders
            </button>
          </div>
        );

      case 'error':
        return (
          <div className="text-center">
            <div className="text-danger mb-3">
              <i className="bi bi-exclamation-triangle-fill" style={{ fontSize: '4rem' }}></i>
            </div>
            <h4 className="text-danger">Error</h4>
            <p className="text-muted">{error}</p>
            <button
              className="btn btn-primary mt-4"
              onClick={() => navigate('/')}
            >
              Go Home
            </button>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card shadow">
            <div className="card-body p-5">
              {renderContent()}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;
```

### Step 4: Create Payment Cancel Page

Create file: `src/pages/PaymentCancel.jsx`

```jsx
import React from 'react';
import { useNavigate } from 'react-router-dom';

const PaymentCancel = () => {
  const navigate = useNavigate();

  // Clear pending payment reference
  React.useEffect(() => {
    localStorage.removeItem('pendingPaymentRef');
  }, []);

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card shadow">
            <div className="card-body p-5 text-center">
              <div className="text-warning mb-3">
                <i className="bi bi-x-circle-fill" style={{ fontSize: '4rem' }}></i>
              </div>
              <h4>Payment Cancelled</h4>
              <p className="text-muted">You have cancelled the payment process</p>
              <div className="mt-4">
                <button
                  className="btn btn-primary me-2"
                  onClick={() => navigate('/orders')}
                >
                  View Orders
                </button>
                <button
                  className="btn btn-outline-secondary"
                  onClick={() => navigate('/cart')}
                >
                  Back to Cart
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentCancel;
```

### Step 5: Add Routes

Update your `App.jsx` or router file:

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import PaymentSuccess from './pages/PaymentSuccess';
import PaymentCancel from './pages/PaymentCancel';

// Add these routes
<Routes>
  {/* ... other routes ... */}
  <Route path="/payment/success" element={<PaymentSuccess />} />
  <Route path="/payment/cancel" element={<PaymentCancel />} />
</Routes>
```

### Step 6: Use in Order/Checkout Page

Example usage in your checkout or order details page:

```jsx
import React from 'react';
import PayHerePayment from '../components/PayHerePayment';

const OrderDetails = ({ order }) => {
  // Only show payment button for ONLINE_PAYMENT orders that are PENDING
  const showPaymentButton =
    order.paymentMethod === 'ONLINE_PAYMENT' &&
    order.status === 'PENDING';

  return (
    <div className="card">
      <div className="card-body">
        <h5>Order #{order.orderNumber}</h5>
        <p>Total: LKR {order.totalAmount}</p>
        <p>Status: {order.status}</p>

        {showPaymentButton && (
          <div className="mt-4">
            <h6>Complete Payment</h6>
            <PayHerePayment
              orderId={order.orderId}
              onError={(err) => console.error('Payment error:', err)}
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default OrderDetails;
```

---

## Payment Flow Diagram

```
+------------------+     +------------------+     +------------------+
|     Customer     |     |     Backend      |     |     PayHere      |
+------------------+     +------------------+     +------------------+
        |                        |                        |
        | 1. Click "Pay Now"     |                        |
        |----------------------->|                        |
        |                        |                        |
        | 2. POST /payments/initiate                      |
        |----------------------->|                        |
        |                        |                        |
        |    3. Create Payment   |                        |
        |    Generate Hash       |                        |
        |                        |                        |
        | 4. Return form data    |                        |
        |<-----------------------|                        |
        |                        |                        |
        | 5. Submit form to PayHere                       |
        |------------------------------------------------>|
        |                        |                        |
        |                        |    6. Customer pays    |
        |                        |                        |
        |                        | 7. POST /payments/notify
        |                        |<-----------------------|
        |                        |                        |
        |                        |    8. Verify hash      |
        |                        |    Update payment      |
        |                        |    Update order        |
        |                        |                        |
        | 9. Redirect to success |                        |
        |<------------------------------------------------|
        |                        |                        |
        | 10. GET /payments/status/{ref}                  |
        |----------------------->|                        |
        |                        |                        |
        | 11. Payment confirmed  |                        |
        |<-----------------------|                        |
        |                        |                        |
```

---

## Configuration

### Backend (application.properties)

```properties
# PayHere Payment Gateway Configuration
payhere.sandbox=true
payhere.sandbox-url=https://sandbox.payhere.lk/pay/checkout
payhere.production-url=https://www.payhere.lk/pay/checkout
payhere.merchant-id=${PAYHERE_MERCHANT_ID:1223118}
payhere.merchant-secret=${PAYHERE_MERCHANT_SECRET:MjI3NjU4MDY3NDI2MjQyODI3NjMyOTg2Njc0NDc5MTk2ODE2NTg4Mw==}
payhere.notify-url=${PAYHERE_NOTIFY_URL:http://localhost:8080/api/payments/notify}
payhere.return-url=${PAYHERE_RETURN_URL:http://localhost:3000/payment/success}
payhere.cancel-url=${PAYHERE_CANCEL_URL:http://localhost:3000/payment/cancel}
payhere.currency=LKR
```

### Environment Variables (Production)

Set these environment variables for production:

```bash
PAYHERE_MERCHANT_ID=your_merchant_id
PAYHERE_MERCHANT_SECRET=your_merchant_secret
PAYHERE_NOTIFY_URL=https://yourdomain.com/api/payments/notify
PAYHERE_RETURN_URL=https://yourdomain.com/payment/success
PAYHERE_CANCEL_URL=https://yourdomain.com/payment/cancel
```

---

## Testing with PayHere Sandbox

### Test Cards

| Card Type | Card Number | Expiry | CVV |
|-----------|-------------|--------|-----|
| Success | 4916217501611292 | Any future date | Any 3 digits |
| Failure | Use invalid numbers | - | - |

### Testing Steps

1. Create an order with `paymentMethod: "ONLINE_PAYMENT"`
2. Click "Pay with PayHere" button
3. You will be redirected to PayHere sandbox
4. Enter test card details
5. Complete payment
6. You will be redirected back to `/payment/success`
7. Payment status will be verified

### Webhook Testing (Local Development)

For local testing, PayHere cannot reach `localhost`. Options:

1. **Use ngrok** to expose your local server:
   ```bash
   ngrok http 8080
   ```
   Then update `payhere.notify-url` to the ngrok URL.

2. **Manual testing**: After payment, manually call the notify endpoint to simulate webhook.

---

## API Response Examples

### POST /api/payments/initiate

**Request:**
```json
{
  "orderId": 123
}
```

**Response:**
```json
{
  "paymentReference": "PAY-1705567890123-4567",
  "paymentUrl": "https://sandbox.payhere.lk/pay/checkout",
  "payhereFormData": {
    "merchant_id": "1223118",
    "return_url": "http://localhost:3000/payment/success",
    "cancel_url": "http://localhost:3000/payment/cancel",
    "notify_url": "http://localhost:8080/api/payments/notify",
    "order_id": "ORD-1705567890123-1705567890456",
    "items": "Order ORD-1705567890123",
    "currency": "LKR",
    "amount": "5000.00",
    "first_name": "John",
    "last_name": "Doe",
    "email": "john@example.com",
    "phone": "0771234567",
    "address": "123 Main Street, Colombo",
    "city": "",
    "country": "Sri Lanka",
    "hash": "ABCD1234EFGH5678..."
  },
  "message": "Payment initiated successfully"
}
```

### GET /api/payments/status/{ref}

**Response:**
```json
{
  "paymentId": 1,
  "paymentReference": "PAY-1705567890123-4567",
  "orderId": 123,
  "orderNumber": "ORD-1705567890123",
  "amount": 5000.00,
  "currency": "LKR",
  "status": "SUCCESS",
  "statusDisplayName": "Success",
  "payherePaymentId": "320027F2D2-2408221545-654987",
  "method": "VISA",
  "cardNo": "************1292",
  "createdAt": "2024-01-18T10:30:00",
  "completedAt": "2024-01-18T10:32:00"
}
```

---

## Troubleshooting

### Payment not updating after PayHere redirect

- Check if the notify URL is accessible from the internet
- Check backend logs for webhook errors
- Verify the MD5 hash is correct

### Hash verification failed

- Ensure merchant_secret matches exactly
- Check amount formatting (2 decimal places)
- Verify currency code is correct

### Order status not changing to CONFIRMED

- Check if payment status is SUCCESS
- Verify order was in PENDING status
- Check backend logs for errors

---

## Security Notes

1. **Never expose merchant_secret** in frontend code
2. **Always verify MD5 hash** in webhook handler
3. **Use HTTPS** in production for all URLs
4. **Validate order ownership** before initiating payment
5. **Handle duplicate webhooks** gracefully (idempotency)

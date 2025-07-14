# API Management Documentation

## 🏦 Payment Money Flow Management APIs

### Base URL: `/api/management/payments/`

#### 1. Money Flow Overview
```http
GET /api/management/payments/money-flow
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalIncome": "15000.00",
  "pendingAmount": "500.00", 
  "failedAmount": "200.00",
  "incomeByPaymentMethod": {
    "CREDIT_CARD": "8000.00",
    "PAYPAL": "5000.00",
    "BANK_TRANSFER": "2000.00"
  },
  "dailyIncome": {
    "2025-07-14": "1500.00",
    "2025-07-13": "2300.00"
  },
  "totalTransactions": 150
}
```

#### 2. All Transactions
```http
GET /api/management/payments/transactions?page=0&size=20&status=COMPLETED
Authorization: Bearer {token}
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `status`: Filter by status (PENDING, COMPLETED, FAILED)

**Response:**
```json
{
  "transactions": [
    {
      "id": 1,
      "orderId": 123,
      "userId": 456,
      "amount": "99.99",
      "method": "CREDIT_CARD",
      "status": "COMPLETED",
      "paymentReference": "PAY-ABC123",
      "transactionId": "TXN-XYZ789",
      "createdAt": "2025-07-14T10:00:00",
      "processedAt": "2025-07-14T10:01:00"
    }
  ],
  "totalCount": 150,
  "currentPage": 0,
  "pageSize": 20
}
```

#### 3. Transaction Detail
```http
GET /api/management/payments/transaction/{id}
Authorization: Bearer {token}
```

#### 4. Payment Statistics
```http
GET /api/management/payments/statistics
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalTransactions": 150,
  "completedCount": 135,
  "failedCount": 10,
  "pendingCount": 5,
  "successRate": 90.0
}
```

---

## 📦 Order Status Management APIs

### Base URL: `/api/management/orders/`

#### 1. Order Status Overview
```http
GET /api/management/orders/status-overview
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalOrders": 200,
  "statusCount": {
    "PENDING": 15,
    "CONFIRMED": 50,
    "PROCESSING": 30,
    "SHIPPED": 25,
    "DELIVERED": 70,
    "CANCELLED": 10
  },
  "statusValue": {
    "DELIVERED": "25000.00",
    "CANCELLED": "1500.00"
  },
  "deliveredOrders": 70,
  "deliveredValue": "25000.00",
  "cancelledOrders": 10,
  "cancelledValue": "1500.00"
}
```

#### 2. All Orders
```http
GET /api/management/orders/orders?page=0&size=20&status=DELIVERED
Authorization: Bearer {token}
```

**Query Parameters:**
- `page`: Page number
- `size`: Page size
- `status`: Filter by status

#### 3. Order Detail
```http
GET /api/management/orders/order/{id}
Authorization: Bearer {token}
```

#### 4. Update Order Status
```http
PUT /api/management/orders/order/{id}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "DELIVERED"
}
```

**Available Statuses:**
- `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

#### 5. Delivered Orders
```http
GET /api/management/orders/delivered?page=0&size=20
Authorization: Bearer {token}
```

**Response:**
```json
{
  "deliveredOrders": [...],
  "totalCount": 70,
  "totalValue": "25000.00",
  "currentPage": 0,
  "pageSize": 20
}
```

#### 6. Cancelled Orders
```http
GET /api/management/orders/cancelled?page=0&size=20
Authorization: Bearer {token}
```

#### 7. Revenue Report
```http
GET /api/management/orders/revenue-report
Authorization: Bearer {token}
```

**Response:**
```json
{
  "confirmedRevenue": "23500.00",
  "lostRevenue": "1500.00",
  "dailyRevenue": {
    "2025-07-14": "3500.00",
    "2025-07-13": "2800.00"
  },
  "netRevenue": "22000.00"
}
```

---

## 💰 Financial Reporting APIs

### Base URL: `/api/management/financial/`

#### 1. Financial Dashboard
```http
GET /api/management/financial/dashboard
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalOrderValue": "30000.00",
  "deliveredRevenue": "25000.00",
  "cancelledLoss": "1500.00",
  "netRevenue": "23500.00",
  "totalOrders": 200,
  "deliveredOrders": 70,
  "deliverySuccessRate": 35.0,
  "paymentIncome": "24800.00",
  "pendingPayments": "500.00",
  "paymentSuccessRate": 90.0
}
```

#### 2. Money Flow Report
```http
GET /api/management/financial/money-flow-report?period=weekly
Authorization: Bearer {token}
```

**Query Parameters:**
- `period`: `weekly` or `monthly`

**Response:**
```json
{
  "period": "weekly",
  "dailyIncome": {
    "2025-07-14": "3500.00",
    "2025-07-13": "2800.00",
    "2025-07-12": "4200.00"
  },
  "dailyLoss": {
    "2025-07-14": "200.00",
    "2025-07-13": "150.00"
  },
  "incomeByPaymentMethod": {
    "CREDIT_CARD": "15000.00",
    "PAYPAL": "8000.00",
    "BANK_TRANSFER": "2000.00"
  },
  "totalIncome": "25000.00",
  "totalLoss": "350.00",
  "netFlow": "24650.00"
}
```

#### 3. Order-Payment Reconciliation
```http
GET /api/management/financial/order-payment-reconciliation
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalOrders": 200,
  "ordersWithSuccessfulPayment": 180,
  "cancelledOrders": 10,
  "pendingOrders": 10,
  "totalPaymentTransactions": 150,
  "completedPayments": 135,
  "failedPayments": 10
}
```

---

## 🔐 Authentication

### Required Headers
```http
Authorization: Bearer {JWT_TOKEN}
```

### Token Validation
- All management endpoints require valid JWT token
- Token must be obtained from authentication service
- Invalid/expired tokens return 401 Unauthorized

### Example Usage
```javascript
// Get auth token first
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'admin', password: 'admin123' })
});
const { token } = await loginResponse.json();

// Use token for management APIs
const dashboardResponse = await fetch('/api/management/financial/dashboard', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

---

## 📊 Key Features

### 1. Payment Transaction Tracking
- Real-time payment status monitoring
- Transaction success/failure rates
- Payment method breakdown
- Daily/weekly income tracking

### 2. Order Status Management
- Complete order lifecycle tracking
- Status update capabilities
- Delivery success monitoring
- Cancellation loss tracking

### 3. Financial Reporting
- Comprehensive revenue analysis
- Money flow visualization
- Order-payment reconciliation
- Loss/profit calculations

### 4. Data Security
- JWT-based authentication
- Secure API endpoints
- Token validation on all requests
- Admin-level access control

---

## 🚀 Implementation Notes

### Services Architecture
- **Payment Service**: Port 8085 - Payment transaction management
- **Order Service**: Port 8083 - Order status and financial reporting
- **Auth Service**: Port 8084 - Token authentication

### Database Integration
- Real-time data from `payment_db` and `order_db`
- Automatic status synchronization via webhooks
- Consistent data across microservices

### Performance Considerations
- Pagination support for large datasets
- Efficient database queries
- Caching for frequently accessed data
- Service-to-service communication optimization
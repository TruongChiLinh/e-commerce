# Microservices API Guide

## Tổng quan hệ thống

Hệ thống microservices gồm 5 services chính:
- **Auth Service** (Port 8084): Xử lý authentication
- **User Service** (Port 8081): Quản lý user profiles
- **Product Service** (Port 8082): Quản lý sản phẩm
- **Order Service** (Port 8083): Quản lý đơn hàng
- **Payment Service** (Port 8085): Xử lý thanh toán

## Cách chạy hệ thống

### 1. Prerequisites
```bash
# Cài đặt Docker và Docker Compose
docker --version
docker-compose --version
```

### 2. Khởi động hệ thống
```bash
# Clone repository và cd vào thư mục
cd /path/to/be

# Build và khởi động tất cả services
docker-compose up -d

# Xem trạng thái containers
docker ps

# Xem logs nếu cần
docker logs <container_name>
```

### 3. Kiểm tra health
```bash
# Kiểm tra health của các services
curl http://localhost:8084/actuator/health  # auth-service
curl http://localhost:8081/actuator/health  # user-service
curl http://localhost:8082/actuator/health  # product-service
curl http://localhost:8083/actuator/health  # order-service
curl http://localhost:8085/actuator/health  # payment-service
```

## Authentication

### 1. Register User
```bash
curl -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser", 
    "password": "password123"
  }'
```

Response sẽ chứa JWT token:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "testuser"
  }
}
```

### 3. Sử dụng Token
Thêm header `Authorization: Bearer <token>` cho tất cả API calls:
```bash
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## API Endpoints

### Auth Service (Port 8084)

#### Public Endpoints (không cần token)
```bash
# Register
POST /api/auth/register
{
  "username": "string",
  "email": "string", 
  "password": "string"
}

# Login  
POST /api/auth/login
{
  "username": "string",
  "password": "string"
}
```

#### Protected Endpoints (cần token)
```bash
# Logout
POST /api/auth/logout
Header: Authorization: Bearer <token>

# Validate token
GET /api/auth/validate  
Header: Authorization: Bearer <token>
```

### Product Service (Port 8082)

#### Public Endpoints (GET - không cần token)
```bash
# Lấy tất cả sản phẩm
GET /api/products

# Lấy sản phẩm theo ID
GET /api/products/{id}

# Lấy sản phẩm có sẵn
GET /api/products/available

# Lấy sản phẩm theo category
GET /api/products/category/{category}

# Tìm kiếm sản phẩm
GET /api/products/search?name={name}
```

#### Protected Endpoints (cần token)
```bash
# Tạo sản phẩm mới
POST /api/products
Header: Authorization: Bearer <token>
{
  "name": "Product Name",
  "description": "Product Description",
  "price": 100.0,
  "category": "Electronics",
  "stockQuantity": 50
}

# Cập nhật sản phẩm
PUT /api/products/{id}
Header: Authorization: Bearer <token>
{
  "name": "Updated Name",
  "description": "Updated Description", 
  "price": 120.0,
  "category": "Electronics",
  "stockQuantity": 30
}

# Xóa sản phẩm
DELETE /api/products/{id}
Header: Authorization: Bearer <token>
```

### User Service (Port 8081)

#### Tất cả endpoints cần token
```bash
# Lấy tất cả users
GET /api/users
Header: Authorization: Bearer <token>

# Lấy user theo ID
GET /api/users/{id}
Header: Authorization: Bearer <token>

# Lấy user theo userId
GET /api/users/user/{userId}
Header: Authorization: Bearer <token>

# Tạo user mới
POST /api/users
Header: Authorization: Bearer <token>
{
  "userId": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "123456789"
}

# Cập nhật user
PUT /api/users/{id}
Header: Authorization: Bearer <token>
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "phoneNumber": "987654321"
}

# Xóa user
DELETE /api/users/{id}
Header: Authorization: Bearer <token>
```

### Order Service (Port 8083)

#### Tất cả endpoints cần token
```bash
# Lấy tất cả orders
GET /api/orders
Header: Authorization: Bearer <token>

# Lấy order theo ID
GET /api/orders/{id}
Header: Authorization: Bearer <token>

# Lấy orders theo userId
GET /api/orders/user/{userId}
Header: Authorization: Bearer <token>

# Lấy orders theo status
GET /api/orders/status/{status}
Header: Authorization: Bearer <token>

# Đặt hàng
POST /api/orders/place
Header: Authorization: Bearer <token>
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 100.0
    }
  ]
}

# Cập nhật trạng thái order
PUT /api/orders/{id}/status?status=SHIPPED
Header: Authorization: Bearer <token>

# Xóa order
DELETE /api/orders/{id}
Header: Authorization: Bearer <token>
```

### Payment Service (Port 8085)

#### Tất cả endpoints cần token
```bash
# Lấy tất cả payments
GET /api/payments
Header: Authorization: Bearer <token>

# Lấy payment theo ID
GET /api/payments/{id}
Header: Authorization: Bearer <token>

# Lấy payments theo userId
GET /api/payments/user/{userId}
Header: Authorization: Bearer <token>

# Lấy payments theo orderId
GET /api/payments/order/{orderId}
Header: Authorization: Bearer <token>

# Lấy payments theo status
GET /api/payments/status/{status}
Header: Authorization: Bearer <token>

# Tạo payment
POST /api/payments
Header: Authorization: Bearer <token>
{
  "orderId": 1,
  "userId": 1,
  "amount": 200.0,
  "method": "CREDIT_CARD"
}

# Tạo payment cho order
POST /api/payments/create-for-order
Header: Authorization: Bearer <token>
{
  "orderId": 1,
  "userId": 1,
  "amount": 200.0,
  "method": "CREDIT_CARD"
}

# Xử lý payment
POST /api/payments/{id}/process
Header: Authorization: Bearer <token>

# Cập nhật trạng thái payment
PUT /api/payments/{id}/status?status=COMPLETED
Header: Authorization: Bearer <token>

# Xóa payment
DELETE /api/payments/{id}
Header: Authorization: Bearer <token>
```

## Workflow ví dụ

### 1. Đăng ký và đăng nhập
```bash
# 1. Register
curl -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com", 
    "password": "secure123"
  }'

# 2. Login để lấy token
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "secure123"
  }'

# Lưu token từ response để sử dụng
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

### 2. Quản lý sản phẩm
```bash
# Xem sản phẩm (public)
curl http://localhost:8082/api/products

# Tạo sản phẩm mới (cần token)
curl -X POST http://localhost:8082/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15",
    "description": "Latest iPhone model",
    "price": 999.0,
    "category": "Electronics", 
    "stockQuantity": 100
  }'
```

### 3. Đặt hàng và thanh toán
```bash
# Tạo user profile
curl -X POST http://localhost:8081/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "123456789"
  }'

# Đặt hàng
curl -X POST http://localhost:8083/api/orders/place \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 1,
        "price": 999.0
      }
    ]
  }'

# Tạo payment cho order
curl -X POST http://localhost:8085/api/payments/create-for-order \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 1,
    "amount": 999.0,
    "method": "CREDIT_CARD"
  }'

# Xử lý payment
curl -X POST http://localhost:8085/api/payments/1/process \
  -H "Authorization: Bearer $TOKEN"
```

## Database

Hệ thống sử dụng MySQL với các database:
- `auth_db`: Users và authentication
- `user_db`: User profiles  
- `product_db`: Sản phẩm
- `order_db`: Đơn hàng
- `payment_db`: Thanh toán

### Database Connection
```
Host: localhost:3306
Username: root
Password: password
```

## Monitoring

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## Troubleshooting

### 1. Services không start
```bash
# Xem logs
docker logs <service_name>

# Restart service
docker-compose restart <service_name>

# Rebuild nếu có thay đổi code
docker-compose up -d --build
```

### 2. Authentication issues
```bash
# Kiểm tra token còn valid không
curl -X GET http://localhost:8084/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"

# Login lại để lấy token mới
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "your_username", "password": "your_password"}'
```

### 3. Database connection issues
```bash
# Kiểm tra MySQL container
docker logs mysql

# Connect vào MySQL
docker exec -it mysql mysql -u root -p
```

## Security Notes

- Tất cả API calls (trừ auth public endpoints và product GET) cần JWT token
- Token có thời hạn 24 giờ
- Sử dụng HTTPS trong production
- Không commit secrets vào git
- Đổi default passwords trong production

## Development

### Thêm features mới
1. Tạo endpoint trong controller
2. Thêm business logic trong service
3. Update database entities nếu cần
4. Thêm authentication annotations nếu cần bảo mật
5. Test với Postman hoặc curl
6. Update API documentation

### Build custom image
```bash
# Build specific service
docker-compose build <service_name>

# Build tất cả
docker-compose build
```
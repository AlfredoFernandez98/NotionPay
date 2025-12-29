# NotionPay Backend

## Overview
Java 21 backend application using Javalin, Hibernate, and Stripe API for managing subscription payments and SMS credits.

## Technology Stack
- **Framework**: Javalin 6.x
- **ORM**: Hibernate 6.6.3
- **Database**: PostgreSQL
- **Payment**: Stripe API
- **Auth**: JWT (jjwt 0.12.6)
- **Build**: Maven
- **Java Version**: 21

## Architecture

### Layers
```
Main.java
├── Routes (API endpoints)
├── Controllers (request handling)
├── Services (business logic)
├── DAOs (data access)
└── Entities (database models)
```

### Key Components

**Security**
- JWT authentication with role-based access (USER, ADMIN)
- BCrypt password hashing
- Token validation on protected endpoints

**Entities**
- User, Customer, Subscription, Plan
- Payment, PaymentMethod, Receipt
- SmsBalance, Product, ActivityLog, Session

**Controllers**
- SecurityController: auth (login, register, logout)
- CustomerController: customer profile management
- PaymentController: payment processing, payment methods
- SubscriptionController: subscription management
- ProductController: SMS products
- ReceiptController: invoice/receipt retrieval

**Services**
- StripePaymentService: Stripe API integration
- SubscriptionService: billing date calculations

## Configuration

### Database (`config.properties`)
```properties
DB_CONNECTION_STRING=jdbc:postgresql://localhost:5432/notionpay
DB_USERNAME=postgres
DB_PASSWORD=postgres
SECRET_KEY=your-secret-jwt-key
STRIPE_SECRET_KEY=sk_test_...
```

### Running
```bash
cd backend
mvn clean install
mvn exec:java -Dexec.mainClass="dat.Main"
```

Server starts on: `http://localhost:7070/api`

## API Endpoints

### Authentication
- POST `/api/auth/register` - Register new user
- POST `/api/auth/login` - Login
- POST `/api/auth/logout` - Logout
- POST `/api/auth/validate` - Validate JWT token

### Customer
- GET `/api/customers/{id}` - Get customer profile
- GET `/api/customers/{customerId}/subscription` - Get active subscription
- GET `/api/customers/{customerId}/sms-balance` - Get SMS balance
- GET `/api/customers/{customerId}/receipts` - Get all receipts

### Payments
- POST `/api/payments` - Process payment
- GET `/api/payments/{id}` - Get payment details
- POST `/api/payment-methods` - Add payment method
- GET `/api/customers/{customerId}/payment-methods` - List payment methods

### Products
- GET `/api/products` - List all SMS products

### Plans
- GET `/api/plans` - List all subscription plans

### Receipts
- GET `/api/receipts/{id}` - Get receipt by ID

## Testing
Use HTTP test files in `src/main/resources/http/`:
1. `1-auth-test.http` - Authentication
2. `2-subscription-products-test.http` - Plans and products
3. `3-customer-profile-test.http` - Customer info
4. `4-payment-test.http` - Payments
5. `5-subscription-billing-test.http` - Subscription payments
6. `6-receipts-test.http` - Receipt retrieval

## Database Schema
- Hibernate auto-creates tables on startup
- Mode: `hbm2ddl.auto=create` (recreates on restart)
- For production, change to `validate` or `update`

## Security Notes
- JWT tokens expire after configured time
- Passwords hashed with BCrypt (cost factor 12)
- Stripe test mode for development
- CORS configured in ApplicationConfig.java
- Role-based endpoint protection

## Development
- Logs: `logs/javalin-app.log`, `logs/debug.log`
- Test cards: 4242 4242 4242 4242 (Visa test)
- Mock data: SerialLinkMigration.java seeds initial data


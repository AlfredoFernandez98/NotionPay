# NotionPay

Subscription billing and SMS credit platform with Stripe payment processing.

## Overview

NotionPay is a full-stack web application for managing subscriptions and selling SMS credits. Users register with a serial number, get assigned a subscription plan, and can purchase SMS packages using Stripe payments.

## Architecture

**Backend**: Java 21 + Javalin + Hibernate + PostgreSQL + Stripe API  
**Frontend**: React 18 + Vite + styled-components + Stripe Elements

```
Frontend (React) ←→ REST API (Javalin) ←→ Database (PostgreSQL)
                         ↓
                   Stripe API
```

## Features

- User authentication (JWT)
- Subscription management
- SMS credit purchases
- Payment processing with Stripe
- Payment method management
- Receipt generation
- Activity logging
- Dashboard with analytics

## Quick Start

### Prerequisites
- Java 21
- Node.js 18+
- PostgreSQL
- Maven
- Stripe account (test mode)

### Backend Setup
```bash
cd backend

# Configure database and Stripe in config.properties
# DB_CONNECTION_STRING, STRIPE_SECRET_KEY, etc.

mvn clean install
mvn exec:java -Dexec.mainClass="dat.Main"
```
Backend runs on: `http://localhost:7070/api`

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on: `http://localhost:3001`

## Project Structure

```
NotionPay/
├── backend/           # Java backend
│   ├── src/main/java/dat/
│   │   ├── controllers/
│   │   ├── entities/
│   │   ├── daos/
│   │   ├── services/
│   │   └── security/
│   └── pom.xml
├── frontend/          # React frontend
│   ├── src/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── util/
│   │   └── store/
│   └── package.json
├── BACKEND.md         # Backend documentation
├── FRONTEND.md        # Frontend documentation
├── TESTING_GUIDE.md   # Testing instructions
└── README.md          # This file
```

## Documentation

- **[BACKEND.md](./BACKEND.md)** - Backend API, architecture, and endpoints
- **[FRONTEND.md](./FRONTEND.md)** - Frontend structure, components, and state
- **[TESTING_GUIDE.md](./TESTING_GUIDE.md)** - How to test the application
- **[PAYMENT_QUICK_START.md](./PAYMENT_QUICK_START.md)** - Payment integration guide
- **[LOGIN_TEST_GUIDE.md](./LOGIN_TEST_GUIDE.md)** - Authentication testing

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout

### Customer
- `GET /api/customers/{id}` - Get customer profile
- `GET /api/customers/{customerId}/subscription` - Get subscription
- `GET /api/customers/{customerId}/sms-balance` - Get SMS balance

### Payments
- `POST /api/payments` - Process payment
- `POST /api/payment-methods` - Add payment method
- `GET /api/customers/{customerId}/payment-methods` - List cards

### Products
- `GET /api/products` - List SMS products
- `GET /api/plans` - List subscription plans

### Receipts
- `GET /api/receipts/{id}` - Get receipt
- `GET /api/customers/{customerId}/receipts` - List receipts

## Database

PostgreSQL database with entities:
- User, Customer, Subscription, Plan
- Payment, PaymentMethod, Receipt
- Product, SmsBalance
- Session, ActivityLog

Schema auto-created by Hibernate on startup.

## Testing

**Backend**: HTTP test files in `backend/src/main/resources/http/`  
**Frontend**: Manual testing via browser  
**Stripe**: Use test card `4242 4242 4242 4242`

See [TESTING_GUIDE.md](./TESTING_GUIDE.md) for details.

## Security

- Passwords hashed with BCrypt
- JWT token authentication
- Role-based access control
- Stripe test mode for development
- PCI-compliant card handling (via Stripe Elements)

## Configuration

### Backend (`backend/src/main/resources/config.properties`)
```properties
DB_CONNECTION_STRING=jdbc:postgresql://localhost:5432/notionpay
DB_USERNAME=postgres
DB_PASSWORD=postgres
SECRET_KEY=your-jwt-secret
STRIPE_SECRET_KEY=sk_test_...
```

### Frontend (`.env`)
```
VITE_API_URL=http://localhost:7070/api
```

## Tech Stack Details

### Backend
- Java 21
- Javalin 6.x (web framework)
- Hibernate 6.6.3 (ORM)
- PostgreSQL (database)
- Stripe Java SDK
- JWT (jjwt 0.12.6)
- BCrypt
- Lombok

### Frontend
- React 18.3.1
- Vite 6.0.1
- React Router DOM 7.1.1
- styled-components 6.1.14
- Stripe React
- Zustand (state)
- Radix UI components

## Development

### Backend
```bash
cd backend
mvn clean install   # Build
mvn exec:java       # Run
```

### Frontend
```bash
cd frontend
npm run dev         # Development server
npm run build       # Production build
npm run preview     # Preview production build
```

## License

Private project - All rights reserved

## Contact

For questions or issues, see `frontend/src/pages/Support.jsx` for contact information.

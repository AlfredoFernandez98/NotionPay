# NotionPay

A subscription billing and SMS credit management platform. Customers register with serial numbers, manage subscriptions, and purchase SMS credits via Stripe.

## Status

**In development** - Educational project (thesis/portfolio)

Not production-hardened. No SLOs, limited monitoring, no load testing. Suitable for learning and demonstration purposes.

## Quickstart

### Requirements

- Java 21
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+
- Stripe account (test mode)

### Setup

All commands assume you start from the repository root.

1. **Clone and configure database**
```bash
# Create database
createdb notionpay

# Configure backend (from repo root)
cd backend
cp src/main/resources/config.properties.example src/main/resources/config.properties
# Edit config.properties with your database credentials
cd ..
```

2. **Configure frontend**
```bash
# From repo root
cd frontend
cp .env.example .env
# Edit .env with your Stripe publishable key
cd ..
```

### Run

**Backend**
```bash
# From repo root
cd backend
mvn clean install
mvn exec:java -Dexec.mainClass="dat.Main"
```
API available at: `http://localhost:7070/api`

**Frontend**
```bash
# From repo root (in a separate terminal)
cd frontend
npm install
npm run dev
```
App available at: `http://localhost:3001`

### Test

Use Stripe test card: `4242 4242 4242 4242` (any future expiry, any CVC)

HTTP test files available in `backend/src/main/resources/http/`

### Common Issues

**Database connection fails**: Check PostgreSQL is running and credentials in `config.properties` are correct.

**Stripe payments fail**: Verify `STRIPE_SECRET_KEY` in backend and `VITE_STRIPE_PUBLISHABLE_KEY` in frontend are set correctly.

**Port already in use**: Backend uses 7070, frontend uses 3001. Change in `Main.java` or `vite.config.js`.

## Configuration

### Backend Environment

Location: `backend/src/main/resources/config.properties`

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_NAME` | PostgreSQL database name | `notionpay` |
| `SECRET_KEY` | JWT signing key | (generate random 64-char hex) |
| `ISSUER` | JWT token issuer | `YourCompanyName` |
| `TOKEN_EXPIRE_TIME` | JWT expiry in milliseconds | `1800000` (30 min) |
| `STRIPE_SECRET_KEY` | Stripe secret key | `sk_test_...` |
| `STRIPE_PUBLISHABLE_KEY` | Stripe publishable key | `pk_test_...` |

### Frontend Environment

Location: `frontend/.env`

| Variable | Description | Example |
|----------|-------------|---------|
| `VITE_API_URL` | Backend API URL | `http://localhost:7070/api` |
| `VITE_STRIPE_PUBLISHABLE_KEY` | Stripe publishable key | `pk_test_...` |

### Secrets Handling

**NEVER commit these files:**
- `frontend/.env` (use `.env.example` as template)
- Any file containing Stripe live keys (`sk_live_*`, `pk_live_*`)

**For production deployment:**
- Use environment variables instead of config files
- Backend automatically reads from `System.getenv()` when `DEPLOYED` env var is set
- Rotate keys immediately if accidentally committed

Example production configuration:
```bash
export DEPLOYED=true
export DB_NAME=notionpay
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_password
export SECRET_KEY=your_64_char_jwt_secret
export STRIPE_SECRET_KEY=sk_live_your_production_key
export ISSUER=NotionPay
export TOKEN_EXPIRE_TIME=1800000
```

## Usage / API

### Authentication

All protected endpoints require JWT token in `Authorization` header:
```
Authorization: Bearer <token>
```

### Example Requests

**Register user**
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123",
  "serialNumber": "SN-12345678"
}
```

**Process payment**
```bash
POST /api/payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerId": 1,
  "amount": 2500,
  "currency": "DKK",
  "productId": 2,
  "paymentMethodId": "pm_card_visa"
}
```

### Complete API Documentation

See [BACKEND.md](./BACKEND.md) for full endpoint list and specifications.

### Stripe Webhooks

**Status**: Not implemented (MVP scope)

Current payment flow relies on client-side confirmation. For production reliability, implement webhook handlers for:
- `payment_intent.succeeded`
- `payment_intent.failed`
- `customer.subscription.updated`
- `customer.subscription.deleted`

Endpoint would be: `/api/webhooks/stripe` (future implementation)

## Architecture Overview

### Components

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│   Frontend  │ REST    │   Backend    │  JDBC   │  PostgreSQL  │
│   (React)   │────────▶│  (Javalin)   │────────▶│   Database   │
└─────────────┘         └──────────────┘         └──────────────┘
                              │
                              │ HTTPS
                              ▼
                        ┌──────────────┐
                        │  Stripe API  │
                        └──────────────┘
```

### Main Modules

**Backend** (`backend/src/main/java/dat/`)
- `controllers/` - REST endpoint handlers
- `services/` - Business logic (StripePaymentService, SubscriptionService)
- `daos/` - Database access layer
- `entities/` - JPA entities (Customer, Payment, Subscription, etc.)
- `security/` - Authentication, JWT handling
- `dtos/` - Data transfer objects

**Frontend** (`frontend/src/`)
- `pages/` - Main application views
- `components/` - Reusable UI components
- `store/` - Zustand state management
- `util/` - API facade, validation helpers

### Database Schema

**Development**: Hibernate auto-generates schema on startup (set `hibernate.hbm2ddl.auto=update` in config).

**Production**: Schema auto-generation should be disabled. Use manual migrations (Flyway/Liquibase) or apply schema via SQL scripts. Current setup is dev-only.

Key tables:
- `users`, `customers` - Authentication and profiles
- `subscriptions`, `plans` - Subscription management
- `payments`, `payment_methods`, `receipts` - Payment processing
- `products`, `sms_balance` - SMS credits
- `sessions`, `activity_log` - Security and audit

### External Dependencies

- **Stripe API** - Payment processing, card tokenization
- **PostgreSQL** - Primary data store
- **Hibernate ORM** - Object-relational mapping (JPA implementation)

## Security

### Authentication
- JWT tokens with HS256 signing
- Tokens stored in browser localStorage (consider httpOnly cookies for production)
- 30-minute token expiration (configurable)

### Password Security
- BCrypt hashing with salt (handled by security library)
- Passwords never logged or stored in plain text

### Payment Security
- PCI-compliant via Stripe Elements (card data never touches server)
- Stripe test keys for development
- Payment intents for secure payment flow

### Data Handling
- PII stored: email, username, payment history
- Activity log tracks auth events (IP not logged currently)
- Database credentials in config file (dev only - use secrets manager in production)

## Development Workflow

### Branching Model

- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - New features
- `bugfix/*` - Bug fixes

Pull requests required before merging to `main` or `develop`.

### Local Development

**Run with live reload**
```bash
# Backend (auto-recompile on change) - from repo root
cd backend
mvn compile exec:java

# Frontend (Vite HMR) - from repo root
cd frontend
npm run dev
```

**Linting and Formatting**
```bash
# Frontend - from repo root
cd frontend
npm run lint        # Check for issues
npm run lint:fix    # Auto-fix issues
```

### Docker (Optional)

```bash
# Run PostgreSQL in Docker
docker run -d \
  --name notionpay-db \
  -e POSTGRES_DB=notionpay \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:14
```

### Commit Convention

Use conventional commits format:

```
feat: add SMS balance refund endpoint
fix: correct subscription renewal date calculation
docs: update API documentation
test: add payment service unit tests
refactor: simplify customer DAO queries
```

## Additional Documentation

- [BACKEND.md](./BACKEND.md) - Complete backend API reference
- [FRONTEND.md](./FRONTEND.md) - Frontend architecture and components
- [PAYMENT_QUICK_START.md](./PAYMENT_QUICK_START.md) - Stripe integration guide
- [ERROR_HANDLING.md](./ERROR_HANDLING.md) - Error handling patterns

## License

Private project - All rights reserved

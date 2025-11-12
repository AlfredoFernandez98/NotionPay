# 💳 NotionPay

A subscription billing and payment platform for SaaS products built with Java, Javalin, and JPA/Hibernate.

##  Overview

NotionPay is a comprehensive billing solution that handles user registration, subscription management, payment processing, and SMS product purchases. It includes pre-registration verification through serial numbers, JWT-based authentication, and a complete entity-relationship model for managing customers, plans, and transactions.

##  Architecture

The project follows a clean, layered architecture:

```
┌─────────────────────────────────────────┐
│  CONTROLLER (HTTP Handlers)            │  ← REST API endpoints
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  SERVICE (Business Logic)               │  ← Orchestration & validation
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  DAO (Data Access)                      │  ← Database operations
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  ENTITY (Domain Models)                 │  ← JPA entities
└─────────────────────────────────────────┘
```

##  Features

### Implemented
- **User Authentication & Authorization**
  - Email-based registration and login
  - JWT token generation and verification
  - Role-based access control
  - BCrypt password hashing

- **Pre-Registration Verification**
  - Serial number validation against mock external database
  - Automatic plan assignment based on serial number
  - One-time use serial links

- **Customer Management**
  - User-Customer separation (security vs business data)
  - Company profile management
  - Serial number linking

- **Plan & Subscription Structure**
  - Multiple plan tiers (Basic, Pro, Enterprise)
  - Flexible billing periods (Monthly, Quarterly, Yearly)
  - Plan features and limits



##  Tech Stack

**Backend:**
- Java 17
- Javalin (Web framework)
- Hibernate/JPA (ORM)
- PostgreSQL (Database)
- JWT (Authentication)
- BCrypt (Password hashing)
- Lombok (Boilerplate reduction)
- Jackson (JSON processing)

**Tools:**
- Maven (Build tool)
- Logback (Logging)

## 📁 Project Structure

```
NotionPay/
├── backend/
│   ├── src/main/java/dat/
│   │   ├── config/          # Hibernate & app configuration
│   │   ├── controllers/     # REST API controllers
│   │   ├── daos/            # Data Access Objects
│   │   ├── dtos/            # Data Transfer Objects
│   │   ├── entities/        # JPA Entities
│   │   ├── enums/           # Enumerations
│   │   ├── exceptions/      # Custom exceptions
│   │   ├── routes/          # API routes
│   │   ├── security/        # Authentication & authorization
│   │   ├── services/        # Business logic
│   │   ├── mockdatabase/    # Test data population
│   │   └── Main.java        # Application entry point
│   ├── src/main/resources/
│   │   ├── config.properties
│   │   └── logback.xml
│   ├── pom.xml
│   ├── ARCHITECTURE_GUIDE.md
│   └── README.md
└── frontend/                # (Future frontend application)
```

## 🗄️ Database Schema

### Core Entities

**Security Layer:**
- `User` - Authentication (email, password, roles)
- `Role` - User roles and permissions

**Business Layer:**
- `Customer` - Business profile (company, serial number)
- `Plan` - Subscription plans (Basic, Pro, Enterprise)
- `SerialLink` - Pre-registration verification
- `Subscription` - Customer subscriptions
- `Product` - Purchasable products
- `SmsProduct` - SMS packages
- `SmsBalance` - SMS usage tracking
- `PaymentMethod` - Stored payment methods
- `Payment` - Transaction records
- `Receipt` - Payment receipts
- `Session` - User sessions
- `ActivityLog` - Audit trail

## Setup & Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+




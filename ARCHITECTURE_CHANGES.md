# Architecture Changes - Subscription & SMS Balance Refactoring

## Overview
Refactored the subscription and SMS balance management to properly model external system integrations and improve separation of concerns.

## Key Changes

### 1. SerialLink Entity - Simplified to Lookup Table
**Before:** Complex entity with relationships to Customer and Plan  
**After:** Pure lookup table with simple data fields

**Changes:**
- Removed: `customer` FK, `plan` entity FK, `status`, timestamps, `externalProof`
- Added: `planName` (String), `initialSmsBalance` (Integer)
- Purpose: Maps serial numbers to external customer IDs, expected emails, and plan names

### 2. Subscription Entity - New Entity Created
**Purpose:** Manages customer subscription lifecycle

**Structure:**
- `customer_id` (FK to Customer)
- `plan_id` (FK to Plan)
- `status` (ENUM: trialing, active, past_due, canceled, expired)
- `start_date`, `end_date`, `next_billing_date`
- `anchor_policy` (ENUM: calendar, anniversary)

**Why:** Separates billing/subscription logic from customer and plan entities

### 3. Customer Entity - Plan Relationship Removed
**Before:** Direct `@ManyToOne` relationship with Plan  
**After:** Plan relationship managed through Subscription entity

**Retained:**
- `external_customer_id` - Links to external systems (e.g., SMS provider)
- `user_id`, `company_name`, `serial_number`, `created_at`

### 4. SmsBalance Entity - External System Integration
**Before:** FK relationship to Customer via `customer_id`  
**After:** Linked via `external_customer_id` (String field, not FK)

**Why:** SmsBalance represents data from an external SMS provider system that uses its own customer identifiers. The link is via `external_customer_id`, not a database foreign key.

**Structure:**
- `id` (PK)
- `external_customer_id` (String, unique) - Links to Customer.external_customer_id
- `remaining_sms` (Integer)

## Registration Flow
When a customer registers:
1. Verify serial number and email match (from SerialLink)
2. Create User (authentication)
3. Create Customer (with `external_customer_id` from SerialLink)
4. Create Subscription (links Customer to Plan with billing details)
5. Create SmsBalance (linked via `external_customer_id`, initialized with credits from SerialLink)
6. Generate JWT token
7. Return success response

## Benefits
- **Clear separation:** Subscription logic isolated from Customer/Plan
- **Proper modeling:** SmsBalance correctly represents external system data
- **Flexible:** Easy to add multiple subscriptions per customer in the future
- **Maintainable:** Each entity has a single, clear responsibility

## Files Modified
- `Customer.java` - Removed Plan relationship
- `SerialLink.java` - Simplified to lookup table
- `SmsBalance.java` - Changed to use external_customer_id (String)
- `Subscription.java` - New entity created
- `SecurityController.java` - Updated registration flow
- `CustomerController.java` - Updated customer creation flow
- `SmsBalanceDAO.java` - Updated queries to use external_customer_id
- `SerialLinkMigration.java` - Updated migration for new structure
- `HibernateConfig.java` - Added Subscription to annotated classes
- `pom.xml` - Fixed Maven compiler plugin version for Java 23 compatibility


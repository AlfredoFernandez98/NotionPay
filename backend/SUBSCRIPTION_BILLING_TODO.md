# ⚠️ CRITICAL: Subscription Billing System - Missing Implementation

## Overview

**Status:** 🔴 **INCOMPLETE** - The subscription system lacks recurring billing functionality

The current implementation can **create** subscriptions and process **manual** payments, but it **CANNOT** automatically handle recurring billing cycles. This is a critical gap that must be addressed before the system can function as a true subscription billing platform.

---

## 🚨 Current Limitations

### What Works ✅
- Creating subscriptions during customer onboarding
- Setting initial `nextBillingDate` from SerialLink data
- Processing manual one-time payments via Stripe
- Displaying subscription information to customers
- Cancelling subscriptions manually

### What's Missing ❌
- **No automatic recurring payments** - System never charges customers automatically
- **No `nextBillingDate` calculation** - After payment, the date is never updated
- **No subscription renewal logic** - No automated billing cycle management
- **No scheduled jobs** - No background tasks to process due payments
- **No Stripe webhook integration** - No handling of Stripe payment events
- **No retry logic** - Failed payments are not retried
- **No grace period handling** - Expired subscriptions are not managed
- **No dunning management** - No follow-up on failed payments

---

## 📋 TODO List - Phase 1: Core Billing Logic

### 1. Create SubscriptionService
**Priority:** 🔴 CRITICAL  
**File:** `backend/src/main/java/dat/services/SubscriptionService.java`

- [ ] Create `SubscriptionService` class
- [ ] Implement `calculateNextBillingDate(Subscription subscription)` method
  - [ ] Handle `Period.MONTHLY` - add 1 month
  - [ ] Handle `Period.YEARLY` - add 1 year
  - [ ] Respect `AnchorPolicy.ANNIVERSARY` - maintain original day of month
  - [ ] Handle edge cases (e.g., Jan 31 → Feb 28/29)
- [ ] Implement `updateSubscriptionAfterPayment(Subscription subscription, Payment payment)` method
  - [ ] Calculate new `nextBillingDate`
  - [ ] Update subscription in database
  - [ ] Log the renewal
- [ ] Implement `getSubscriptionsDueForBilling()` method
  - [ ] Query subscriptions where `nextBillingDate <= now()`
  - [ ] Filter by `status = ACTIVE`
  - [ ] Return list of subscriptions to charge
- [ ] Add error handling and logging

**Acceptance Criteria:**
- Service can calculate next billing date based on Plan period
- Service can update subscription after successful payment
- Service can identify subscriptions that need billing

---

### 2. Update PaymentController
**Priority:** 🔴 CRITICAL  
**File:** `backend/src/main/java/dat/controllers/impl/PaymentController.java`

- [ ] Inject `SubscriptionService` into constructor
- [ ] In `create()` method, after successful payment:
  - [ ] Call `subscriptionService.updateSubscriptionAfterPayment(subscription, payment)`
  - [ ] Verify `nextBillingDate` was updated
  - [ ] Include new `nextBillingDate` in response
- [ ] Add logging for subscription updates
- [ ] Update integration tests to verify subscription renewal

**Current Code Location:** Line 183-193 (after payment is created)

**Acceptance Criteria:**
- After successful subscription payment, `nextBillingDate` is automatically updated
- New billing date is calculated based on Plan period
- Changes are persisted to database

---

### 3. Add Utility Method to Period Enum
**Priority:** 🟡 HIGH  
**File:** `backend/src/main/java/dat/enums/Period.java`

- [ ] Add method `OffsetDateTime addPeriod(OffsetDateTime date)`
- [ ] Implement for `MONTHLY` - use `plusMonths(1)`
- [ ] Implement for `YEARLY` - use `plusYears(1)`
- [ ] Add unit tests for date calculations
- [ ] Test edge cases (leap years, month-end dates)

**Acceptance Criteria:**
- Period enum can calculate next billing date
- All edge cases are handled correctly
- Unit tests pass

---

## 📋 TODO List - Phase 2: Automated Billing

### 4. Create Billing Scheduler
**Priority:** 🟡 HIGH  
**File:** `backend/src/main/java/dat/jobs/SubscriptionBillingJob.java`

**Options:**
1. **Quartz Scheduler** (recommended for production)
2. **Java ScheduledExecutorService** (simple, built-in)
3. **Stripe Billing** (outsource to Stripe)

- [ ] Choose scheduling approach
- [ ] Add required dependencies to `pom.xml`
- [ ] Create job that runs daily (or hourly)
- [ ] Job should:
  - [ ] Call `subscriptionService.getSubscriptionsDueForBilling()`
  - [ ] For each subscription, attempt to charge customer
  - [ ] Handle successful and failed payments
  - [ ] Log all billing attempts
- [ ] Add configuration for job timing
- [ ] Add job to application startup

**Acceptance Criteria:**
- Job runs automatically on schedule
- Due subscriptions are identified and charged
- Failed payments are logged
- System can handle multiple subscriptions

---

### 5. Implement Automatic Payment Processing
**Priority:** 🟡 HIGH  
**File:** `backend/src/main/java/dat/services/SubscriptionService.java`

- [ ] Add method `processSubscriptionBilling(Subscription subscription)`
  - [ ] Get customer's default payment method
  - [ ] Create payment via `PaymentController` or `StripePaymentService`
  - [ ] If successful: update `nextBillingDate`
  - [ ] If failed: handle retry logic
  - [ ] Create receipt if successful
  - [ ] Send notification email (optional)
- [ ] Handle missing payment method
- [ ] Handle expired payment method
- [ ] Add comprehensive error handling

**Acceptance Criteria:**
- Subscriptions can be charged automatically
- Payment success/failure is handled correctly
- Customers are notified (optional)
- Activity is logged

---

## 📋 TODO List - Phase 3: Failure Handling

### 6. Implement Failed Payment Logic
**Priority:** 🟠 MEDIUM  
**File:** `backend/src/main/java/dat/services/SubscriptionService.java`

- [ ] Add field `retryCount` to Subscription entity
- [ ] Add field `lastBillingAttempt` to Subscription entity
- [ ] Create retry strategy:
  - [ ] Retry after 3 days
  - [ ] Retry after 7 days
  - [ ] Retry after 14 days
  - [ ] After 3 failed attempts, mark as `PAST_DUE`
- [ ] Implement grace period (e.g., 30 days)
- [ ] After grace period, set status to `CANCELED`
- [ ] Log all retry attempts

**Acceptance Criteria:**
- Failed payments are retried automatically
- Subscription status reflects payment state
- Customers have grace period before cancellation

---

### 7. Add Subscription Status Management
**Priority:** 🟠 MEDIUM  
**File:** Multiple files

**Update SubscriptionStatus enum:**
- [ ] Add `PAST_DUE` status
- [ ] Add `UNPAID` status
- [ ] Update controllers to handle new statuses

**Update SubscriptionService:**
- [ ] Add `suspendSubscription(Long subscriptionId)` method
- [ ] Add `reactivateSubscription(Long subscriptionId)` method
- [ ] Add logic to prevent access for PAST_DUE subscriptions

**Acceptance Criteria:**
- Subscription statuses accurately reflect billing state
- Failed payments trigger status changes
- Customers can reactivate after failed payment

---

## 📋 TODO List - Phase 4: Webhook Integration

### 8. Create Stripe Webhook Handler
**Priority:** 🟠 MEDIUM  
**File:** `backend/src/main/java/dat/controllers/impl/StripeWebhookController.java`

- [ ] Create webhook endpoint `/api/webhooks/stripe`
- [ ] Implement signature verification
- [ ] Handle key events:
  - [ ] `payment_intent.succeeded`
  - [ ] `payment_intent.payment_failed`
  - [ ] `customer.subscription.updated`
  - [ ] `charge.refunded`
- [ ] Update local database based on Stripe events
- [ ] Ensure idempotency (don't process same event twice)
- [ ] Add comprehensive logging

**Acceptance Criteria:**
- Webhook receives and verifies Stripe events
- Database stays in sync with Stripe
- Events are processed only once

---

### 9. Migrate to Stripe Billing (Optional)
**Priority:** 🔵 FUTURE  
**Decision Required:** Build custom billing vs use Stripe Billing

**If using Stripe Billing:**
- [ ] Create subscriptions in Stripe (not just locally)
- [ ] Use Stripe's automatic billing
- [ ] Sync subscription data from Stripe webhooks
- [ ] Remove custom billing scheduler
- [ ] Simplify codebase

**If building custom:**
- [ ] Complete Phases 1-3
- [ ] Maintain dual sync (local DB + Stripe)
- [ ] More control, more complexity

---

## 🗂️ Current Architecture

### Entities Involved
```
Plan
├── period: MONTHLY | YEARLY
├── priceCents: int
└── currency: Currency

Subscription
├── customer: Customer
├── plan: Plan
├── status: ACTIVE | CANCELED | (PAST_DUE, UNPAID - to add)
├── startDate: OffsetDateTime
├── nextBillingDate: OffsetDateTime ⚠️ (never updated after creation)
├── endDate: OffsetDateTime
└── anchorPolicy: ANNIVERSARY

Payment
├── customer: Customer
├── subscription: Subscription
├── paymentMethod: PaymentMethod
├── status: COMPLETED | PENDING | FAILED
├── priceCents: int
└── processorIntentId: String
```

### Current Flow

**1. Onboarding (SecurityController.register()):**
```
SerialLink (mock data)
  ↓
nextBillingDate from serialLink.getNextPaymentDate()
  ↓
Create Subscription with nextBillingDate
  ↓
Save to database
  ↓
❌ nextBillingDate is NEVER updated again
```

**2. Manual Payment (PaymentController.create()):**
```
Customer initiates payment
  ↓
Charge via Stripe
  ↓
Save Payment entity
  ↓
Generate Receipt
  ↓
❌ Subscription.nextBillingDate is NOT updated
❌ No automatic renewal
```

### Required Flow (After Implementation)

**Automatic Recurring Billing:**
```
Billing Job runs daily
  ↓
Query subscriptions where nextBillingDate <= NOW
  ↓
For each subscription:
  ├── Get customer's payment method
  ├── Create PaymentIntent in Stripe
  ├── If successful:
  │     ├── Save Payment entity
  │     ├── Calculate new nextBillingDate (Plan.period)
  │     ├── Update Subscription
  │     └── Generate Receipt
  └── If failed:
        ├── Increment retryCount
        ├── Schedule retry
        └── Update subscription status (if needed)
```

---

## 🔧 Technical Decisions Required

### 1. Scheduling Approach
**Options:**
- **Quartz Scheduler:** Robust, configurable, production-ready
- **ScheduledExecutorService:** Simple, built-in, good for MVP
- **Spring @Scheduled:** If migrating to Spring Boot
- **External Cron:** Unix cron + API endpoint

**Recommendation:** Quartz Scheduler for production flexibility

---

### 2. Billing Logic Location
**Options:**
- **Custom SubscriptionService:** Full control, more code
- **Stripe Billing:** Less code, vendor lock-in, easier

**Recommendation:** Start with custom service, migrate to Stripe later if needed

---

### 3. Failed Payment Strategy
**Options:**
- **Immediate cancellation:** Strict, simple
- **Grace period with retries:** Customer-friendly, complex
- **Email dunning only:** Manual follow-up

**Recommendation:** Grace period with 3 automatic retries

---

## 📚 Related Documentation

- **Stripe Billing Guide:** https://stripe.com/docs/billing
- **Quartz Scheduler:** https://www.quartz-scheduler.org/
- **Stripe Webhooks:** https://stripe.com/docs/webhooks

---

## 🧪 Testing Requirements

### Unit Tests Needed
- [ ] `SubscriptionService.calculateNextBillingDate()` with various dates
- [ ] `SubscriptionService.updateSubscriptionAfterPayment()`
- [ ] `Period.addPeriod()` for edge cases (Feb 29, month-end)
- [ ] Failed payment retry logic

### Integration Tests Needed
- [ ] End-to-end billing cycle (create → charge → renew)
- [ ] Failed payment handling and retry
- [ ] Subscription cancellation after grace period
- [ ] Webhook processing

---

## 📅 Implementation Timeline (Estimated)

### Sprint 3 (Week 1-2)
- ✅ Phase 1: Core Billing Logic (SubscriptionService)
- Priority: Complete items 1-3

### Sprint 4 (Week 3-4)
- ⏳ Phase 2: Automated Billing (Scheduler)
- Priority: Complete items 4-5

### Sprint 5 (Week 5-6)
- ⏳ Phase 3: Failure Handling
- Priority: Complete items 6-7

### Sprint 6+ (Future)
- ⏳ Phase 4: Webhook Integration
- Priority: Complete items 8-9

---

## 📞 Questions to Resolve

1. **Business Requirements:**
   - How long should the grace period be after a failed payment?
   - How many retry attempts before cancellation?
   - Should customers receive email notifications?

2. **Technical Architecture:**
   - Should we build custom billing or use Stripe Billing?
   - What timezone should be used for billing calculations?
   - How should we handle refunds and proration?

3. **Compliance:**
   - Are there PCI-DSS requirements for automated billing?
   - Do we need to store payment failure reasons?
   - What audit logging is required?

---

## 🚀 Getting Started

**To begin implementation, start with Phase 1, Item 1:**
1. Create `SubscriptionService.java`
2. Implement `calculateNextBillingDate()` method
3. Add unit tests
4. Update `PaymentController` to use the service

**This document should be updated as implementation progresses.**

---

**Last Updated:** December 13, 2025  
**Status:** 🔴 Critical Gap Identified - Implementation Required  
**Owner:** Development Team

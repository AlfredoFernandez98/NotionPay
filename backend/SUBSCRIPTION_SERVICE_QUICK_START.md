# 🚀 SubscriptionService - Quick Start Guide

## What Problem Does It Solve?

**Before SubscriptionService:**
```
❌ Customer pays on Jan 15
❌ nextBillingDate stays "Jan 15" forever
❌ System never knows when to charge again
❌ Manual tracking required
```

**After SubscriptionService:**
```
✅ Customer pays on Jan 15
✅ nextBillingDate automatically updates to "Feb 15"
✅ System knows exactly when to charge next
✅ Fully automated billing cycle
```

---

## 🎯 Simple Example

### Scenario: Coffee Subscription Service

**Customer:** John's Cafe  
**Plan:** Premium Coffee (Monthly, 299 DKK)  
**Start Date:** January 15, 2025

### Month 1: January 15, 2025
```java
// John signs up
Subscription sub = new Subscription(...);
sub.setNextBillingDate("2025-01-15");
sub.setStatus(ACTIVE);

// John pays 299 DKK
Payment payment = processPayment(john, 299);

// ✨ Magic happens here:
subscriptionService.updateSubscriptionAfterPayment(sub, payment);

// Result:
sub.getNextBillingDate(); // "2025-02-15" ← Automatically updated!
```

### Month 2: February 15, 2025
```java
// System checks: "Is it Feb 15 yet?"
List<Subscription> due = subscriptionService.getSubscriptionsDueForBilling();
// Returns: [John's subscription]

// Charge John again
Payment payment = processPayment(john, 299);

// Update next billing date
subscriptionService.updateSubscriptionAfterPayment(sub, payment);

// Result:
sub.getNextBillingDate(); // "2025-03-15" ← Updated again!
```

### This repeats every month automatically! 🔄

---

## 📋 Three Main Methods

### 1️⃣ Calculate Next Billing Date
**What it does:** Figures out when to charge next  
**Example:**
```java
OffsetDateTime nextDate = subscriptionService.calculateNextBillingDate(subscription);
// Jan 15 → Feb 15 (monthly)
// Jan 15 → Jan 15, 2026 (yearly)
```

### 2️⃣ Update After Payment
**What it does:** Updates subscription when customer pays  
**Example:**
```java
subscriptionService.updateSubscriptionAfterPayment(subscription, payment);
// Before: nextBillingDate = "2025-01-15"
// After:  nextBillingDate = "2025-02-15"
```

### 3️⃣ Get Subscriptions Due
**What it does:** Finds who needs to be charged today  
**Example:**
```java
List<Subscription> due = subscriptionService.getSubscriptionsDueForBilling();
// Returns all subscriptions where nextBillingDate <= today
```

---

## 🔄 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│  STEP 1: Customer Makes Payment                             │
├─────────────────────────────────────────────────────────────┤
│  POST /api/payments                                          │
│  {                                                           │
│    "customerId": 1,                                          │
│    "subscriptionId": 10,                                     │
│    "amount": 9900                                            │
│  }                                                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 2: PaymentController Processes Payment                │
├─────────────────────────────────────────────────────────────┤
│  ✓ Validate customer & payment method                       │
│  ✓ Charge via Stripe                                        │
│  ✓ Save Payment to database                                 │
│  ✓ Generate Receipt                                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 3: ✨ NEW - Update Subscription                       │
├─────────────────────────────────────────────────────────────┤
│  subscriptionService.updateSubscriptionAfterPayment(...)    │
│                                                              │
│  What happens:                                               │
│  1. Get current billing date: "2025-01-15"                  │
│  2. Calculate next date: "2025-02-15"                       │
│  3. Update subscription in database                         │
│  4. Log the change                                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 4: Return Response                                     │
├─────────────────────────────────────────────────────────────┤
│  {                                                           │
│    "msg": "Payment processed successfully",                 │
│    "paymentId": 42,                                          │
│    "status": "COMPLETED",                                    │
│    "nextBillingDate": "2025-02-15"  ← Customer knows when!  │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🧪 Test It Yourself

### Step 1: Create a Monthly Subscription
```java
Plan monthlyPlan = new Plan("Basic", Period.MONTHLY, 9900, Currency.DKK, "Basic", true);
Customer customer = customerDAO.getById(1L).get();

Subscription sub = new Subscription(
    customer,
    monthlyPlan,
    SubscriptionStatus.ACTIVE,
    OffsetDateTime.now(),
    OffsetDateTime.parse("2025-01-15T10:00:00Z"), // Next billing: Jan 15
    AnchorPolicy.ANNIVERSARY
);
subscriptionDAO.create(sub);
```

### Step 2: Process a Payment
```java
Payment payment = new Payment(customer, paymentMethod, sub, ...);
paymentDAO.create(payment);
```

### Step 3: Update Subscription
```java
SubscriptionService service = SubscriptionService.getInstance(emf);
service.updateSubscriptionAfterPayment(sub, payment);
```

### Step 4: Check the Result
```java
Subscription updated = subscriptionDAO.getById(sub.getId()).get();
System.out.println("Old date: 2025-01-15");
System.out.println("New date: " + updated.getNextBillingDate()); // 2025-02-15
```

---

## 🎓 Edge Cases Handled

### 1. Month-End Dates
```
Jan 31 → Feb 28 (Feb has only 28 days)
May 31 → Jun 30 (Jun has only 30 days)
```

### 2. Leap Years
```
Feb 29, 2024 → Feb 28, 2025 (2025 is not a leap year)
```

### 3. Yearly Subscriptions
```
Mar 10, 2025 → Mar 10, 2026 (exactly one year)
```

---

## ✅ What's Working Now

1. ✅ **Service Created** - `SubscriptionService.java` in `/services` folder
2. ✅ **Billing Date Calculation** - Handles monthly/yearly with edge cases
3. ✅ **Subscription Updates** - Automatically updates after payment
4. ✅ **Due Subscription Detection** - Finds subscriptions that need billing

---

## ⏭️ Next Step: Integrate with PaymentController

The next task is to add this line to `PaymentController.create()`:

```java
// After successful payment (around line 193)
if (subscription != null && status == PaymentStatus.COMPLETED) {
    subscriptionService.updateSubscriptionAfterPayment(subscription, payment);
}
```

This will make the subscription system fully functional! 🎉

---

## 💡 Key Takeaway

**Before:** Subscriptions were "set and forget" - billing dates never updated  
**After:** Subscriptions are "living entities" - billing dates update automatically after each payment

This is the foundation for automated recurring billing in Phase 2!

---

**Created:** December 18, 2025  
**Author:** NotionPay Development Team

# SubscriptionService - Usage Examples

## Overview
The `SubscriptionService` handles subscription billing logic, including calculating next billing dates and updating subscriptions after payments.

---

## 📚 Example 1: Calculate Next Billing Date

### Scenario: Monthly Subscription
A customer has a **monthly subscription** that renews on the 15th of each month.

```java
// Current billing date: January 15, 2025
Subscription subscription = subscriptionDAO.getById(1L).get();
subscription.getNextBillingDate(); // 2025-01-15

// Calculate next billing date
SubscriptionService service = SubscriptionService.getInstance(emf);
OffsetDateTime nextDate = service.calculateNextBillingDate(subscription);

// Result: February 15, 2025
System.out.println(nextDate); // 2025-02-15
```

### Scenario: Monthly Subscription (Edge Case - Month End)
Customer subscribed on **January 31st** (month with 31 days).

```java
// Current billing date: January 31, 2025
subscription.getNextBillingDate(); // 2025-01-31

// Calculate next billing date
OffsetDateTime nextDate = service.calculateNextBillingDate(subscription);

// Result: February 28, 2025 (February only has 28 days)
System.out.println(nextDate); // 2025-02-28

// Next calculation from Feb 28:
subscription.setNextBillingDate(nextDate);
OffsetDateTime marchDate = service.calculateNextBillingDate(subscription);

// Result: March 28, 2025 (maintains the 28th, not 31st)
System.out.println(marchDate); // 2025-03-28
```

---

## 📚 Example 2: Yearly Subscription

### Scenario: Annual Subscription
Customer has a **yearly subscription** that renews on March 10th.

```java
// Current billing date: March 10, 2025
subscription.getNextBillingDate(); // 2025-03-10

// Calculate next billing date
OffsetDateTime nextDate = service.calculateNextBillingDate(subscription);

// Result: March 10, 2026 (one year later)
System.out.println(nextDate); // 2026-03-10
```

### Scenario: Leap Year Edge Case
Customer subscribed on **February 29, 2024** (leap year).

```java
// Current billing date: February 29, 2024
subscription.getNextBillingDate(); // 2024-02-29

// Calculate next billing date
OffsetDateTime nextDate = service.calculateNextBillingDate(subscription);

// Result: February 28, 2025 (2025 is not a leap year)
System.out.println(nextDate); // 2025-02-28
```

---

## 📚 Example 3: Update Subscription After Payment

### Scenario: Customer Makes a Payment
When a customer successfully pays for their subscription, the system automatically calculates and updates the next billing date.

```java
// Step 1: Customer makes a payment
Payment payment = new Payment(customer, paymentMethod, subscription, ...);
paymentDAO.create(payment);

// Step 2: Update subscription with new billing date
SubscriptionService service = SubscriptionService.getInstance(emf);
service.updateSubscriptionAfterPayment(subscription, payment);

// What happens internally:
// 1. Calculates next billing date (e.g., Jan 15 → Feb 15)
// 2. Updates subscription.nextBillingDate in database
// 3. Ensures subscription.status = ACTIVE
// 4. Logs the update
```

### Full Example:
```java
// Before payment
System.out.println(subscription.getNextBillingDate()); // 2025-01-15
System.out.println(subscription.getStatus());          // ACTIVE

// Process payment
Payment payment = paymentController.processPayment(...);

// Update subscription
SubscriptionService service = SubscriptionService.getInstance(emf);
service.updateSubscriptionAfterPayment(subscription, payment);

// After payment
System.out.println(subscription.getNextBillingDate()); // 2025-02-15 (updated!)
System.out.println(subscription.getStatus());          // ACTIVE
```

---

## 📚 Example 4: Find Subscriptions Due for Billing

### Scenario: Daily Billing Job
A scheduled job runs every day to find subscriptions that need to be charged.

```java
SubscriptionService service = SubscriptionService.getInstance(emf);

// Get all subscriptions due today or earlier
List<Subscription> dueSubscriptions = service.getSubscriptionsDueForBilling();

// Example output:
// Found 3 subscriptions due for billing:
// - Subscription #1: Customer "Acme Corp", next billing: 2025-01-14 (overdue by 1 day)
// - Subscription #2: Customer "TechStart", next billing: 2025-01-15 (due today)
// - Subscription #3: Customer "DataFlow", next billing: 2025-01-15 (due today)

for (Subscription sub : dueSubscriptions) {
    System.out.println("Processing subscription: " + sub.getId());
    System.out.println("Customer: " + sub.getCustomer().getCompanyName());
    System.out.println("Due date: " + sub.getNextBillingDate());
    
    // TODO: Process payment for this subscription
    // (This will be implemented in Phase 2)
}
```

---

## 🔄 Complete Payment Flow (How It All Works Together)

### Current Flow (Manual Payment):
```
1. Customer initiates payment via API
   POST /api/payments
   {
     "customerId": 1,
     "paymentMethodId": 5,
     "subscriptionId": 10,
     "amount": 9900,
     "currency": "dkk"
   }

2. PaymentController.create() processes payment:
   ├── Validates customer, payment method, subscription
   ├── Creates PaymentIntent in Stripe
   ├── Saves Payment entity to database
   └── Generates Receipt

3. ✅ NEW: After successful payment, update subscription:
   ├── Call subscriptionService.updateSubscriptionAfterPayment(subscription, payment)
   ├── Calculate new nextBillingDate (e.g., Feb 15 → Mar 15)
   ├── Update subscription in database
   └── Log the renewal

4. Response to customer:
   {
     "msg": "Payment processed successfully",
     "paymentId": 42,
     "status": "COMPLETED",
     "nextBillingDate": "2025-03-15T10:00:00Z"  ← NEW!
   }
```

---

## 🧪 Testing the Service

### Test Case 1: Monthly Billing
```java
@Test
public void testMonthlyBillingDateCalculation() {
    // Setup
    Plan monthlyPlan = new Plan("Basic", Period.MONTHLY, 9900, Currency.DKK, "Basic plan", true);
    Subscription sub = new Subscription(customer, monthlyPlan, SubscriptionStatus.ACTIVE,
        OffsetDateTime.parse("2025-01-15T10:00:00Z"),
        OffsetDateTime.parse("2025-01-15T10:00:00Z"),
        AnchorPolicy.ANNIVERSARY);
    
    // Execute
    OffsetDateTime nextDate = subscriptionService.calculateNextBillingDate(sub);
    
    // Verify
    assertEquals("2025-02-15", nextDate.toLocalDate().toString());
}
```

### Test Case 2: Edge Case - January 31st
```java
@Test
public void testMonthEndEdgeCase() {
    // Setup: Subscription on Jan 31
    subscription.setNextBillingDate(OffsetDateTime.parse("2025-01-31T10:00:00Z"));
    
    // Execute: Calculate next billing date
    OffsetDateTime nextDate = subscriptionService.calculateNextBillingDate(subscription);
    
    // Verify: Should be Feb 28 (last day of February)
    assertEquals("2025-02-28", nextDate.toLocalDate().toString());
}
```

---

## 📊 Real-World Example: Acme Corp

### Customer Profile:
- **Company:** Acme Corp
- **Plan:** Pro Plan (Monthly, 19,900 DKK)
- **Start Date:** January 15, 2025
- **Next Billing Date:** January 15, 2025

### Timeline:

**January 15, 2025 - Initial Subscription**
```
nextBillingDate: 2025-01-15
status: ACTIVE
```

**January 15, 2025 - First Payment**
```java
// Customer is charged 19,900 DKK
Payment payment = processPayment(customer, 19900, "dkk");

// Subscription is updated
subscriptionService.updateSubscriptionAfterPayment(subscription, payment);

// New state:
nextBillingDate: 2025-02-15  ← Updated!
status: ACTIVE
```

**February 15, 2025 - Second Payment**
```java
// Customer is charged again
Payment payment = processPayment(customer, 19900, "dkk");

// Subscription is updated again
subscriptionService.updateSubscriptionAfterPayment(subscription, payment);

// New state:
nextBillingDate: 2025-03-15  ← Updated again!
status: ACTIVE
```

**And so on...** every month the cycle repeats.

---

## 🚀 Next Steps (Phase 2 - Automated Billing)

Currently, payments are **manual** (customer initiates via API).

In Phase 2, we'll add **automatic billing**:

```java
// Scheduled job runs daily
public void dailyBillingJob() {
    // 1. Find subscriptions due for billing
    List<Subscription> dueSubscriptions = subscriptionService.getSubscriptionsDueForBilling();
    
    // 2. For each subscription, charge the customer automatically
    for (Subscription sub : dueSubscriptions) {
        try {
            // Get customer's default payment method
            PaymentMethod paymentMethod = getDefaultPaymentMethod(sub.getCustomer());
            
            // Create payment automatically
            Payment payment = createPayment(sub.getCustomer(), paymentMethod, sub);
            
            // Update subscription
            subscriptionService.updateSubscriptionAfterPayment(sub, payment);
            
            logger.info("Successfully billed subscription {}", sub.getId());
            
        } catch (Exception e) {
            logger.error("Failed to bill subscription {}", sub.getId(), e);
            // TODO: Handle failed payment (retry logic)
        }
    }
}
```

---

## 📝 Summary

### What SubscriptionService Does:
1. ✅ **Calculates next billing dates** - Handles monthly/yearly periods and edge cases
2. ✅ **Updates subscriptions after payment** - Automatically sets new billing date
3. ✅ **Finds subscriptions due for billing** - Identifies which customers need to be charged

### What It Doesn't Do Yet (Phase 2):
- ❌ Automatically charge customers (requires scheduled job)
- ❌ Handle failed payments (requires retry logic)
- ❌ Send email notifications

### Integration Point:
The `PaymentController` should call `subscriptionService.updateSubscriptionAfterPayment()` after every successful subscription payment.

---

**Created:** December 18, 2025  
**Status:** ✅ Phase 1 Complete - Core Billing Logic Implemented

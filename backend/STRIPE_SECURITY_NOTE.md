# ⚠️ Stripe Security - IMPORTANT

## Current Implementation (Testing/Development)

The current `PaymentController.addPaymentMethod()` endpoint accepts **raw card numbers** for testing purposes.

```java
POST /api/payment-methods
{
  "customerId": 1,
  "cardNumber": "4242424242424242",  // ❌ NEVER do this in production!
  "expMonth": 12,
  "expYear": 2025,
  "cvc": "123"
}
```

**This is ONLY acceptable for:**
- ✅ Testing in development
- ✅ Integration tests
- ✅ Internal testing environments

## ⚠️ BEFORE PRODUCTION - MUST CHANGE!

### Current Flow (INSECURE):
```
Frontend → Sends raw card → Backend → Stripe API
         ❌ Card exposed to your server
```

### Production Flow (SECURE):
```
Frontend → Stripe.js tokenizes → Backend receives token → Stripe API
         ✅ Card never touches your server
```

## Production Implementation Steps:

### 1. Frontend (React/Vue/etc):
```javascript
// Load Stripe.js
import { loadStripe } from '@stripe/stripe-js';
const stripe = await loadStripe('pk_live_YOUR_KEY');

// Create card element
const cardElement = elements.create('card');
cardElement.mount('#card-element');

// Create payment method (card stays in browser)
const {paymentMethod, error} = await stripe.createPaymentMethod({
  type: 'card',
  card: cardElement,
});

// Send ONLY the token to your backend
await fetch('/api/payment-methods', {
  method: 'POST',
  body: JSON.stringify({
    customerId: 1,
    paymentMethodId: paymentMethod.id  // ✅ Token, not card number!
  })
});
```

### 2. Backend - NEW Endpoint (Create this):
```java
/**
 * Add payment method using Stripe token (PRODUCTION SAFE)
 * POST /api/payment-methods/token
 * Body: { customerId, paymentMethodId }
 */
public void addPaymentMethodWithToken(Context ctx) {
    try {
        ObjectNode request = ctx.bodyAsClass(ObjectNode.class);
        Long customerId = request.get("customerId").asLong();
        String stripePaymentMethodId = request.get("paymentMethodId").asText(); // Token from frontend
        
        Customer customer = customerDAO.getById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Get payment method from Stripe (already tokenized by frontend)
        PaymentMethod stripePaymentMethod = stripeService.retrievePaymentMethod(stripePaymentMethodId);

        // Save to database
        dat.entities.PaymentMethod paymentMethod = new dat.entities.PaymentMethod(
                customer,
                stripePaymentMethod.getType(),
                stripePaymentMethod.getCard().getBrand(),
                stripePaymentMethod.getCard().getLast4(),
                stripePaymentMethod.getCard().getExpMonth().intValue(),
                stripePaymentMethod.getCard().getExpYear().intValue(),
                stripePaymentMethod.getId(),
                true,
                dat.enums.PaymentMethodStatus.ACTIVE,
                stripePaymentMethod.getCard().getFingerprint()
        );

        paymentMethodDAO.create(paymentMethod);
        
        ctx.status(201).json(Map.of("msg", "Payment method added"));
    } catch (Exception e) {
        ctx.status(400).json(Map.of("msg", "Failed: " + e.getMessage()));
    }
}
```

### 3. Remove or Restrict Current Endpoint:
```java
// Option A: Delete the addPaymentMethod() endpoint entirely
// Option B: Add environment check:
public void addPaymentMethod(Context ctx) {
    // Only allow in development
    if (System.getenv("DEPLOYED") != null) {
        ctx.status(403).json(Map.of("msg", "This endpoint is disabled in production"));
        return;
    }
    // ... rest of code
}
```

## Why This Matters:

1. **PCI Compliance**: Handling raw card numbers requires extensive compliance
2. **Security**: Raw cards can be intercepted in your backend logs/database
3. **Liability**: You're responsible if cards are stolen from your server
4. **Stripe Radar**: Works better when using Stripe.js (fraud detection)

## Current Warning from Stripe:

You may receive warnings like:
> "We noticed that you passed a customer's full credit card number to Stripe's API in test mode."

**In test mode**: This warning is informational - tests will still work.
**In production mode**: Stripe will reject requests with raw card numbers.

## Testing Strategy:

- ✅ **Development**: Use current implementation with test cards
- ✅ **Integration Tests**: Current approach is fine
- ✅ **Production**: MUST switch to token-based approach

## References:

- [Stripe.js Integration](https://stripe.com/docs/js)
- [Accept a Payment](https://stripe.com/docs/payments/accept-a-payment)
- [PCI Compliance](https://stripe.com/docs/security/guide)
- [Test Cards](https://stripe.com/docs/testing)

---

## Summary:

✅ **For Now**: Keep current implementation for testing
⚠️ **Before Production**: Implement Stripe.js on frontend
❌ **Never**: Send raw card numbers to backend in production


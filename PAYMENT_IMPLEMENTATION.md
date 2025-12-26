# Payment Implementation Guide

## Overview
This document describes the complete payment implementation for NotionPay, connecting the frontend React application to the backend payment API powered by Stripe.

## What Was Fixed

### 1. Login Issue - Customer ID Missing ✅
**Problem**: After login, the dashboard couldn't fetch customer data because the `customerId` wasn't being returned or stored.

**Solution**:
- Updated `SecurityController.java` login endpoint to include `customerId` in response
- Updated `apiFacade.js` to store the `customerId` in localStorage
- Updated `Login.jsx` to include `customerId` in the auth state

**Files Modified**:
- `backend/src/main/java/dat/security/controllers/SecurityController.java`
- `frontend/src/util/apiFacade.js`
- `frontend/src/pages/Login.jsx`

### 2. Payment System Implementation ✅
**Added**: Complete payment functionality including payment methods management and SMS purchase flow.

## New Frontend Components

### 1. PaymentMethods Page (`/payment-methods`)
**Purpose**: Manage saved payment methods (credit/debit cards)

**Features**:
- View all saved payment methods
- Add new payment method with card details
- Display default payment method
- Show card status (ACTIVE/INACTIVE)
- Beautiful card display with brand icons
- Test mode warning with Stripe test card number

**Files Created**:
- `frontend/src/pages/PaymentMethods.jsx`
- `frontend/src/pages/PaymentMethods.styles.js`

### 2. BuySMS Page (`/buy-sms`)
**Purpose**: Purchase SMS credits using saved payment methods

**Features**:
- Display available SMS packages (products)
- Select SMS package with pricing details
- Choose payment method
- View purchase summary
- Complete payment transaction
- Redirect to payment methods if no cards saved

**Files Created**:
- `frontend/src/pages/BuySMS.jsx`
- `frontend/src/pages/BuySMS.styles.js`

### 3. Updated Dashboard
**Purpose**: Quick access to payment features

**Changes**:
- Added "Buy More SMS" button in SMS Balance card
- Added "Manage Payment Methods" button in Payment Methods card
- Added "Add Payment Method" link when no cards saved

## Backend API Endpoints Used

### Payment Methods
```
POST /api/payment-methods
Body: {
  customerId: number,
  cardNumber: string,
  expMonth: number,
  expYear: number,
  cvc: string,
  isDefault: boolean
}
Response: { msg, paymentMethodId, brand, last4 }
```

```
GET /api/customers/{customerId}/payment-methods
Response: Array of PaymentMethodDTO
```

### Payments
```
POST /api/payments
Body: {
  customerId: number,
  paymentMethodId: number,
  amount: number (in cents),
  currency: string,
  description: string,
  productId?: number,
  subscriptionId?: number
}
Response: { msg, paymentId, status, amount, currency, receiptId, receiptNumber }
```

### Products
```
GET /api/products
Response: Array of Product (SMS packages)
```

## Updated Routes

### New Routes Added:
```javascript
{
  paymentMethods: "/payment-methods",
  buySMS: "/buy-sms",
}
```

### Navigation Updated:
- **Navbar**: Added "Buy SMS" and "Payment Methods" links for authenticated users
- **Dashboard**: Added direct links to payment features from relevant cards
- **App.jsx**: Added routes for new payment pages

## Testing Guide

### Prerequisites
1. Backend server running on `http://localhost:7070`
2. Frontend running on development server
3. Stripe API configured with test keys
4. Test user account created and logged in

### Test Scenarios

#### Scenario 1: View Payment Methods
1. Login to the application
2. Navigate to "Payment Methods" from navbar or dashboard
3. Should see either:
   - List of saved payment methods
   - Empty state with "Add New Card" button

**Expected Result**: Page loads without errors, displays payment methods or empty state

#### Scenario 2: Add Payment Method
1. Go to Payment Methods page
2. Click "+ Add New Card" button
3. Fill in the form with Stripe test card:
   - Card Number: `4242 4242 4242 4242`
   - Expiry Month: `12`
   - Expiry Year: `2025`
   - CVC: `123`
   - Check "Set as default" if desired
4. Click "Add Card"

**Expected Result**: 
- Success message appears
- New card appears in the list
- Modal closes automatically after 2 seconds

#### Scenario 3: Buy SMS Credits
1. Navigate to "Buy SMS" from navbar
2. View available SMS packages
3. Click on a package to select it
4. Select a payment method
5. Review purchase summary
6. Click "Complete Purchase"

**Expected Result**:
- Payment processes successfully
- Success message appears
- Redirects to dashboard after 3 seconds
- SMS balance updated on dashboard

#### Scenario 4: Buy SMS Without Payment Method
1. Navigate to "Buy SMS" page
2. If no payment methods exist, should see:
   - Empty state for payment methods
   - Link to "Add a payment method"
3. Click the link
4. Should navigate to Payment Methods page

**Expected Result**: User is guided to add a payment method before purchasing

### Test Cards (Stripe Test Mode)

#### Successful Payments
- **Visa**: `4242 4242 4242 4242`
- **Mastercard**: `5555 5555 5555 4444`
- **American Express**: `3782 822463 10005`

#### Failed Payments (for testing error handling)
- **Generic decline**: `4000 0000 0000 0002`
- **Insufficient funds**: `4000 0000 0000 9995`
- **Lost card**: `4000 0000 0000 9987`

**Note**: Use any future expiry date and any 3-digit CVC for test cards.

## API Integration Flow

### Add Payment Method Flow
```
1. User fills payment method form
   ↓
2. Frontend calls apiFacade.addPaymentMethod()
   ↓
3. Backend creates payment method in Stripe
   ↓
4. Backend saves payment method to database
   ↓
5. Backend returns payment method details
   ↓
6. Frontend refreshes payment methods list
```

### Process Payment Flow
```
1. User selects product and payment method
   ↓
2. Frontend calls apiFacade.processPayment()
   ↓
3. Backend retrieves customer and payment method
   ↓
4. Backend creates PaymentIntent in Stripe
   ↓
5. Backend saves payment to database
   ↓
6. Backend generates receipt
   ↓
7. Backend updates SMS balance (if SMS product)
   ↓
8. Backend logs activity
   ↓
9. Frontend shows success and redirects
```

## Security Considerations

### ⚠️ IMPORTANT - Production Security
The current implementation accepts raw card numbers for **TESTING PURPOSES ONLY**.

**For production, you MUST**:
1. Use Stripe.js to tokenize cards on the frontend
2. Never send raw card numbers to your backend
3. Send only Stripe tokens from frontend to backend
4. Remove or restrict the current `addPaymentMethod` endpoint

**Recommended Production Flow**:
```javascript
// Frontend (using Stripe.js)
const {token, error} = await stripe.createToken(cardElement);
if (error) {
  // Handle error
} else {
  // Send token.id to backend
  await apiFacade.addPaymentMethod({ stripeToken: token.id });
}
```

## Troubleshooting

### Issue: "Customer ID not found"
**Solution**: Ensure user is logged in and `customerId` is stored in localStorage
```javascript
const customerId = apiFacade.getCustomerId();
console.log('Customer ID:', customerId);
```

### Issue: Payment methods not loading
**Solution**: Check backend is running and authentication token is valid
```javascript
// Check in browser console
console.log('Token:', apiFacade.getToken());
console.log('Logged in:', apiFacade.loggedIn());
```

### Issue: Stripe error "Invalid card number"
**Solution**: Use correct test card format without spaces
- ✅ Correct: `4242424242424242`
- ❌ Wrong: `4242 4242 4242 4242` (spaces removed automatically in UI)

### Issue: "No payment methods available" on Buy SMS page
**Solution**: Add a payment method first via the Payment Methods page or the provided link

## Features Summary

### ✅ Implemented
- View payment methods
- Add payment method (with Stripe integration)
- Display card brands and expiry dates
- Set default payment method
- View SMS packages/products
- Select SMS package
- Choose payment method for purchase
- Process payment with Stripe
- Generate receipt
- Update SMS balance after purchase
- Activity logging
- Navigation integration
- Responsive design
- Loading states
- Error handling
- Success messages

### 🚀 Future Enhancements
- Remove/delete payment methods
- Edit payment method (update default status)
- Payment history page
- Download receipts as PDF
- Multiple currency support
- Subscription renewal payments via UI
- Payment method verification (3D Secure)
- Saved payment method nicknames
- Auto-recharge when SMS balance is low

## File Structure

```
frontend/src/
├── pages/
│   ├── PaymentMethods.jsx          # Payment methods management
│   ├── PaymentMethods.styles.js    # Styled components
│   ├── BuySMS.jsx                  # SMS purchase page
│   ├── BuySMS.styles.js            # Styled components
│   └── Dashboard.jsx               # Updated with payment links
├── components/
│   └── layout/
│       └── Navbar.jsx              # Updated with payment nav links
├── util/
│   └── apiFacade.js                # Updated with payment APIs
└── utils/
    └── routes.js                   # Updated with payment routes

backend/src/main/java/dat/
├── controllers/impl/
│   └── PaymentController.java     # Payment endpoints
└── security/controllers/
    └── SecurityController.java    # Updated login response
```

## Summary

The payment system is now fully integrated into the frontend with:
- ✅ Beautiful, responsive UI
- ✅ Complete payment method management
- ✅ SMS purchase flow
- ✅ Stripe integration
- ✅ Error handling and validation
- ✅ User-friendly navigation
- ✅ Dashboard integration
- ✅ Security warnings for production

Users can now:
1. Add and manage payment methods
2. Purchase SMS credits
3. View their payment methods on the dashboard
4. Complete end-to-end payment transactions

All functionality is tested and working with Stripe test mode.

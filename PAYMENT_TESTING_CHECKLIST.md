# Payment System Testing Checklist

## Pre-Testing Setup

### Backend Setup
- [ ] Backend server is running on `http://localhost:7070`
- [ ] Database is connected and migrations are complete
- [ ] Stripe API keys are configured in `config.properties`
- [ ] Test data (plans, products, serial links) is seeded

### Frontend Setup
- [ ] Frontend dev server is running
- [ ] `.env` file has correct `VITE_API_URL`
- [ ] No console errors on page load
- [ ] Styled-components is installed

### User Account
- [ ] Test user is created (via registration or seed data)
- [ ] Test user can successfully login
- [ ] Customer profile exists for test user

---

## Testing Checklist

### 1. Login & Customer Data ✅
**Test**: Login should fetch and display customer data

- [ ] Navigate to `/login`
- [ ] Enter credentials and login
- [ ] Redirected to `/dashboard`
- [ ] Dashboard loads without errors
- [ ] Customer profile displays:
  - [ ] Company name
  - [ ] Email
  - [ ] Serial number
  - [ ] Customer ID
- [ ] SMS balance displays (even if 0)
- [ ] Subscription information displays

**Console Check**:
```javascript
// Should see in console:
✅ Login successful: user@email.com
✅ Customer ID stored: [number]
```

---

### 2. Payment Methods Page 🆕
**Test**: View and manage payment methods

#### View Payment Methods
- [ ] Navigate to `/payment-methods` (from navbar or dashboard)
- [ ] Page loads without errors
- [ ] Title shows "Payment Methods"
- [ ] Subtitle shows "Manage your saved payment methods"

#### Empty State (if no cards)
- [ ] Empty state displays card icon 💳
- [ ] Message: "No payment methods saved yet"
- [ ] Helpful text suggests adding a card

#### Add New Payment Method
- [ ] Click "+ Add New Card" button
- [ ] Modal opens with form
- [ ] Warning about test cards is visible
- [ ] Form fields present:
  - [ ] Card Number (with formatting)
  - [ ] Month
  - [ ] Year
  - [ ] CVC
  - [ ] "Set as default" checkbox

#### Submit Test Card
- [ ] Enter: `4242 4242 4242 4242`
- [ ] Enter expiry: `12` / `2025`
- [ ] Enter CVC: `123`
- [ ] Check "Set as default"
- [ ] Click "Add Card"
- [ ] Button shows "Adding..."
- [ ] Success message appears
- [ ] New card appears in list
- [ ] Modal closes after 2 seconds

#### Card Display
- [ ] Card shows brand icon 💳
- [ ] Card shows "Visa" label
- [ ] Card shows masked number: `•••• •••• •••• 4242`
- [ ] Card shows expiry date: `Expires 12/2025`
- [ ] "Default" badge shows if default
- [ ] Status badge shows "ACTIVE" in green

**Console Check**:
```javascript
💳 Adding payment method: {...}
✅ Payment method added successfully
```

---

### 3. Buy SMS Page 🆕
**Test**: Purchase SMS credits

#### View Products
- [ ] Navigate to `/buy-sms` (from navbar or dashboard)
- [ ] Page loads without errors
- [ ] Title shows "Buy SMS Credits"
- [ ] SMS packages display with:
  - [ ] Package name
  - [ ] Price in DKK
  - [ ] Description
  - [ ] SMS count
  - [ ] Price per SMS

#### Select Package
- [ ] Click on a package card
- [ ] Card border changes to blue
- [ ] "✓ Selected" badge appears
- [ ] "Select Package" button changes to "Selected"

#### Payment Method Selection
- [ ] Payment methods section shows
- [ ] Saved cards display with:
  - [ ] Radio button
  - [ ] Card brand
  - [ ] Masked number
  - [ ] Expiry date
  - [ ] "Default" badge if applicable
- [ ] Click on a payment method
- [ ] Radio button becomes selected
- [ ] Card background changes color

#### No Payment Methods Flow
- [ ] If no cards saved, empty state shows
- [ ] "Add a payment method" link displays
- [ ] Clicking link navigates to `/payment-methods`

#### Purchase Summary
- [ ] Summary section appears when package selected
- [ ] Shows:
  - [ ] Package name
  - [ ] SMS credits count
  - [ ] Subtotal
  - [ ] Total (in large blue text)
- [ ] "Complete Purchase" button enabled when both selected

#### Complete Purchase
- [ ] Click "Complete Purchase"
- [ ] Button shows "Processing..."
- [ ] Success message appears
- [ ] Message shows package name
- [ ] Automatic redirect to dashboard after 3 seconds

**Console Check**:
```javascript
💰 Processing payment: {...}
✅ Payment successful
```

---

### 4. Dashboard Integration ✅
**Test**: Payment features accessible from dashboard

#### SMS Balance Card
- [ ] SMS balance displays current credits
- [ ] "Buy More SMS" button present
- [ ] Clicking button navigates to `/buy-sms`

#### Payment Methods Card
- [ ] If cards exist:
  - [ ] Shows list of payment methods
  - [ ] "Manage Payment Methods" button present
  - [ ] Clicking navigates to `/payment-methods`
- [ ] If no cards:
  - [ ] Empty state displays
  - [ ] "Add Payment Method" link present
  - [ ] Clicking navigates to `/payment-methods`

---

### 5. Navigation ✅
**Test**: All payment pages accessible from navbar

- [ ] "Buy SMS" link visible when authenticated
- [ ] "Payment Methods" link visible when authenticated
- [ ] Links hidden when not authenticated
- [ ] Clicking links navigates correctly
- [ ] Active link styling works

---

## Error Scenarios

### Test Failed Card
**Purpose**: Ensure error handling works

- [ ] Go to Payment Methods
- [ ] Try adding card: `4000 0000 0000 0002` (test decline)
- [ ] Should show error message
- [ ] Form should stay open
- [ ] Can try again with valid card

### Test Payment Without Selection
**Purpose**: Validate form requirements

- [ ] Go to Buy SMS
- [ ] Click "Complete Purchase" without selecting package
- [ ] Should show error: "Please select a product and payment method"

### Test Unauthorized Access
**Purpose**: Check authentication protection

- [ ] Logout
- [ ] Try to visit `/payment-methods` directly
- [ ] Should redirect to `/login`
- [ ] Same for `/buy-sms`

---

## Browser Console Checks

### On Login
```javascript
=== LOGIN ATTEMPT ===
Email: test@example.com
Backend URL: http://localhost:7070/api
🔐 LOGIN REQUEST:
  URL: http://localhost:7070/api/auth/login
✅ Login response data: {token, email, sessionID, customerId}
✅ Login successful: test@example.com
✅ Customer ID stored: 123
```

### On Add Payment Method
```javascript
💳 Adding payment method: {
  customerId: 123,
  cardNumber: "4242424242424242",
  expMonth: 12,
  expYear: 2025,
  cvc: "123",
  isDefault: true
}
```

### On Process Payment
```javascript
💰 Processing payment: {
  customerId: 123,
  paymentMethodId: 456,
  amount: 10000,
  currency: "DKK",
  description: "Purchase: 100 SMS Credits",
  productId: 789
}
```

---

## Network Tab Checks

### Login Request
```
POST http://localhost:7070/api/auth/login
Status: 200 OK
Response: {
  "token": "eyJ...",
  "email": "test@example.com",
  "sessionID": 123,
  "customerId": 456  ← NEW!
}
```

### Get Payment Methods
```
GET http://localhost:7070/api/customers/456/payment-methods
Status: 200 OK
Headers: Authorization: Bearer eyJ...
Response: [
  {
    "id": 1,
    "customerId": 456,
    "type": "card",
    "brand": "visa",
    "last4": "4242",
    "expMonth": 12,
    "expYear": 2025,
    "isDefault": true,
    "status": "ACTIVE"
  }
]
```

### Add Payment Method
```
POST http://localhost:7070/api/payment-methods
Status: 201 Created
Body: {
  "customerId": 456,
  "cardNumber": "4242424242424242",
  "expMonth": 12,
  "expYear": 2025,
  "cvc": "123",
  "isDefault": true
}
Response: {
  "msg": "Payment method added successfully",
  "paymentMethodId": 1,
  "brand": "visa",
  "last4": "4242"
}
```

### Process Payment
```
POST http://localhost:7070/api/payments
Status: 201 Created
Body: {
  "customerId": 456,
  "paymentMethodId": 1,
  "amount": 10000,
  "currency": "DKK",
  "description": "Purchase: 100 SMS Credits",
  "productId": 1
}
Response: {
  "msg": "Payment processed successfully",
  "paymentId": 789,
  "status": "COMPLETED",
  "amount": 10000,
  "currency": "DKK",
  "receiptId": 101,
  "receiptNumber": "RCP-1234567890"
}
```

---

## Success Criteria

✅ All checklist items pass
✅ No console errors
✅ No network errors (except intentional test failures)
✅ All navigation works smoothly
✅ Data persists after page refresh
✅ Responsive design works on mobile view
✅ Loading states display correctly
✅ Error messages are user-friendly
✅ Success messages appear and disappear appropriately

---

## Common Issues & Solutions

### Issue: "Customer ID not found"
**Check**: 
```javascript
localStorage.getItem('customerId')
```
**Solution**: Login again or check backend response includes customerId

### Issue: Payment methods not loading
**Check**: Network tab for 401 or 403 errors
**Solution**: Check token is valid, login again if needed

### Issue: Stripe error messages
**Common Errors**:
- Invalid card number → Use correct test card
- Card declined → Intentional for test card `4000000000000002`
- Expired card → Use future year

### Issue: Styles not loading
**Check**: Styled-components is installed
```bash
npm install styled-components
```

### Issue: Module not found
**Check**: All new files are in correct directories
**Solution**: Restart dev server: `npm run dev`

---

## Performance Checks

- [ ] Pages load within 2 seconds
- [ ] Transitions are smooth
- [ ] No layout shift during loading
- [ ] Images and icons load properly
- [ ] Forms are responsive and fast

---

## Accessibility Checks

- [ ] All buttons have hover states
- [ ] Form labels are present
- [ ] Error messages are clear
- [ ] Success messages are visible
- [ ] Navigation is keyboard-accessible
- [ ] Color contrast is sufficient

---

## Mobile Responsiveness

- [ ] Pages work on mobile viewport (375px)
- [ ] Payment method cards stack vertically
- [ ] Modals are scrollable on small screens
- [ ] Buttons are easily tappable
- [ ] Text is readable without zooming

---

## Final Verification

Run through complete user journey:

1. [ ] Register new account or login
2. [ ] View dashboard (all data loads)
3. [ ] Navigate to Payment Methods
4. [ ] Add a test card
5. [ ] Navigate to Buy SMS
6. [ ] Select a package
7. [ ] Select the payment method
8. [ ] Complete purchase
9. [ ] Return to dashboard
10. [ ] Verify SMS balance increased
11. [ ] Check payment method still shows on dashboard

**Time to complete**: ~5 minutes
**Expected result**: All steps complete without errors

---

## Test Report Template

Date: ___________
Tester: ___________

| Test Category | Status | Notes |
|--------------|--------|-------|
| Login & Customer Data | ☐ Pass ☐ Fail | |
| Payment Methods - View | ☐ Pass ☐ Fail | |
| Payment Methods - Add | ☐ Pass ☐ Fail | |
| Buy SMS - View Products | ☐ Pass ☐ Fail | |
| Buy SMS - Complete Purchase | ☐ Pass ☐ Fail | |
| Dashboard Integration | ☐ Pass ☐ Fail | |
| Navigation | ☐ Pass ☐ Fail | |
| Error Handling | ☐ Pass ☐ Fail | |
| Mobile Responsive | ☐ Pass ☐ Fail | |

**Overall Result**: ☐ Pass ☐ Fail

**Issues Found**: _____________________

**Additional Notes**: _____________________

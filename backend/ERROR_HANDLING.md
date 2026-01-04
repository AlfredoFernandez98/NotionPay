# Error Handling in NotionPay

## Overview

Error handling in NotionPay is implemented systematically across both backend and frontend to ensure system stability, data integrity, and clear user feedback. As a payment processing system, robust error handling is critical to prevent inconsistent data states and provide a reliable user experience.

---

## Backend Error Handling

### 1. Input Validation

Input validation occurs at the earliest stage in the controller layer to reject invalid or missing data before processing.

#### ValidationUtil.java

The `ValidationUtil` class provides comprehensive input validation:

```java
// Email validation
public static boolean isValidEmail(String email)

// Password validation (min 8 chars, 1 digit required)
public static boolean isStrongPassword(String password)

// Company name validation (max 100 chars, non-empty)
public static boolean isValidCompanyName(String companyName)
```

**Validation Rules:**
- Email: Valid format matching regex pattern
- Password: Minimum 8 characters, at least one digit
- Company Name: 1-100 characters, not empty after trimming
- Serial Number: Verified against external system database

### 2. Centralized Error Responses

The `ErrorResponse` utility class provides consistent HTTP status codes and error messages across all controllers.

#### ErrorResponse.java

```java
// Standard error responses
ErrorResponse.badRequest(ctx, "Invalid input format");           // 400
ErrorResponse.unauthorized(ctx, "Authentication required");      // 401
ErrorResponse.forbidden(ctx, "Access denied");                   // 403
ErrorResponse.notFound(ctx, "Resource not found");               // 404
ErrorResponse.conflict(ctx, "Resource already exists");          // 409
ErrorResponse.unprocessableEntity(ctx, "Duplicate entry");       // 422
ErrorResponse.internalError(ctx, "Server error", logger, e);     // 500
```

**Benefits:**
- Consistent error format across all endpoints
- Automatic logging for internal errors
- Clean, maintainable controller code
- Prevents exposure of internal system details

### 3. Database Transaction Management

All database operations are wrapped in explicit transactions using Hibernate/JPA.

#### Transaction Pattern (DAO Layer)

```java
public Payment create(Payment payment) {
    try (EntityManager em = emf.createEntityManager()) {
        em.getTransaction().begin();
        em.persist(payment);
        em.getTransaction().commit();
        return payment;
    }
    // Automatic rollback on exception
}
```

**Key Features:**
- Automatic transaction rollback on failure
- Try-with-resources ensures EntityManager cleanup
- ACID properties maintained
- No partial data commits

**Example Scenarios:**
- Payment creation fails → No receipt generated
- Subscription update fails → Original state preserved
- SMS balance update fails → No deduction recorded

### 4. External Integration Error Handling

Integration with external systems (Stripe) includes comprehensive error handling for network failures and API errors.

#### Stripe Payment Processing

```java
try {
    // Create Stripe PaymentIntent
    PaymentIntent paymentIntent = stripeService.createPaymentIntent(
        amountCents, 
        currency, 
        paymentMethodId
    );
    
    // Process payment
    Payment payment = createPaymentRecord(paymentIntent);
    Receipt receipt = generateReceipt(payment);
    
} catch (StripeException e) {
    logger.error("Stripe payment error: {}", e.getMessage());
    ErrorResponse.badRequest(ctx, stripeService.getErrorMessage(e));
    // No payment or receipt created
    
} catch (Exception e) {
    logger.error("Payment processing failed", e);
    ErrorResponse.internalError(ctx, "Payment failed", logger, e);
}
```

**Stripe Error Handling:**
- Card declined → User-friendly message returned
- Network timeout → Transaction not recorded
- Invalid API key → Logged, generic error to user
- Payment requires action → 3D Secure flow initiated

### 5. Logging Infrastructure

All errors and important events are logged using SLF4J with Logback.

#### Log Levels

- **INFO**: Successful operations (payment completed, user registered)
- **WARN**: Recoverable errors (invalid input, authentication failed)
- **ERROR**: System errors (database failure, Stripe error)

#### Configuration (logback.xml)

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/javalin-app.log</file>
    <!-- Rolling policy and formatting -->
</appender>
```

**Log Files:**
- `logs/javalin-app.log` - Application logs
- `logs/debug.log` - Debug level logging

### 6. Activity Logging (Audit Trail)

The `ActivityLog` entity records business-relevant events for audit and compliance.

#### ActivityLog Events

```java
public enum ActivityLogType {
    LOGIN,
    LOGOUT,
    PAYMENT,
    SUBSCRIPTION_CREATED,
    SUBSCRIPTION_RENEWED,
    SUBSCRIPTION_CANCELLED,
    ADD_CARD,
    REMOVE_CARD,
    SMS_PURCHASE
}
```

**Logged Information:**
- Customer ID
- Activity type
- Timestamp
- Status (SUCCESS/FAILURE)
- Metadata (IP address, amount, etc.)

**Example Usage:**

```java
ActivityLog log = new ActivityLog();
log.setCustomer(customer);
log.setType(ActivityLogType.PAYMENT);
log.setStatus(ActivityLogStatus.SUCCESS);
log.setMetadata("Amount: " + amount + ", Currency: " + currency);
log.setTimestamp(OffsetDateTime.now());
activityLogDAO.create(log);
```

---

## Frontend Error Handling

### 1. Form Validation

Client-side validation provides immediate feedback before API calls.

#### validation.js Utilities

```javascript
// Email validation
export const validateEmail = (email) => {
  if (!email) return 'Email is required';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    return 'Please enter a valid email address';
  }
  return '';
};

// Password validation
export const validatePassword = (password) => {
  if (!password) return 'Password is required';
  if (password.length < 8) return 'Password must be at least 8 characters';
  if (!/\d/.test(password)) return 'Password must contain at least one number';
  return '';
};

// Password confirmation
export const validateConfirmPassword = (password, confirmPassword) => {
  if (!confirmPassword) return 'Please confirm your password';
  if (password !== confirmPassword) return 'Passwords do not match';
  return '';
};
```

**Additional Validators:**
- `validateCompanyName()` - Length and emptiness checks
- `validateSerialNumber()` - Digits only validation
- `validateCardNumber()` - Card format validation
- `validateCVC()` - 3-4 digit validation
- `validateExpiryMonth()` and `validateExpiryYear()` - Card expiry validation

### 2. API Error Handling

All API calls are wrapped in try-catch blocks with comprehensive error handling.

#### SignUp.jsx Example

```javascript
try {
  const response = await apiFacade.register(email, password, companyName, serialNumber);
  
  // Store auth data
  setAuth(user, response.token);
  
  // Navigate to dashboard
  navigate('/dashboard');
  
} catch (err) {
  console.error('Registration error:', err);
  
  // Handle specific error codes
  if (err.status === 400) {
    setError('Invalid input. Please check all fields and try again.');
  } else if (err.status === 403) {
    setError('Invalid serial number or email. Please verify your information.');
  } else if (err.status === 422) {
    setError('An account with this email already exists. Please login instead.');
  } else if (err.message) {
    setError(err.message);
  } else {
    setError('Registration failed. Please try again.');
  }
}
```

**Error Response Handling:**
- 400 Bad Request → "Invalid input" message
- 401 Unauthorized → Redirect to login
- 403 Forbidden → "Access denied" message
- 404 Not Found → "Resource not found" message
- 422 Unprocessable → "Duplicate entry" message
- 500 Server Error → "System error, please try again"

### 3. API Facade Error Processing

The `apiFacade.js` centralizes all API communication and error handling.

#### handleHttpErrors Function

```javascript
async function handleHttpErrors(res) {
  if (!res.ok) {
    try {
      const errorData = await res.json();
      return Promise.reject({ 
        status: res.status, 
        fullError: errorData,
        message: errorData.msg || errorData.message || 'An error occurred'
      });
    } catch (e) {
      return Promise.reject({
        status: res.status,
        message: res.statusText || 'An error occurred'
      });
    }
  }
  return res.json();
}
```

**Features:**
- Parses backend error messages
- Provides fallback for non-JSON responses
- Includes HTTP status code
- Returns structured error object

### 4. User-Friendly Error Messages

Error messages are displayed prominently with clear, actionable guidance.

#### Error Display Component

```jsx
{error && (
  <div style={{ 
    padding: '12px', 
    marginBottom: '16px', 
    backgroundColor: '#fee', 
    color: '#c33',
    borderRadius: '8px',
    fontSize: '14px'
  }}>
    {error}
  </div>
)}
```

**Error Message Guidelines:**
- Clear and concise
- Actionable (tell user what to do)
- Non-technical language
- Specific to the problem

**Examples:**
- ✅ "Passwords do not match. Please try again."
- ✅ "Invalid serial number. Please verify your information."
- ❌ "NullPointerException in SecurityController"
- ❌ "Database constraint violation"

### 5. Loading and Error States

All async operations show loading states and handle errors gracefully.

#### State Management Pattern

```javascript
const [loading, setLoading] = useState(false);
const [error, setError] = useState('');

const handleSubmit = async (e) => {
  e.preventDefault();
  setError('');
  setLoading(true);
  
  try {
    await apiFacade.someOperation();
  } catch (err) {
    setError(err.message);
  } finally {
    setLoading(false);
  }
};
```

**UI Feedback:**
- Loading spinner during API calls
- Disabled buttons to prevent double-submission
- Error message display
- Success confirmation

### 6. Graceful Degradation

If optional data fails to load, the application continues functioning.

#### Dashboard.jsx Example

```javascript
try {
  const activities = await apiFacade.getCustomerActivities(customerId);
  setActivities(activities || []);
} catch (err) {
  console.error('Error fetching activities:', err);
  // Show empty state instead of blocking entire dashboard
  setActivities([]);
}
```

**Fallback Strategies:**
- Empty arrays for lists
- Default values for missing data
- Skeleton screens during loading
- "No data available" messages

---

## Error Handling Best Practices

### Backend

1. **Validate Early** - Check input at controller layer before processing
2. **Use Transactions** - Wrap database operations in transactions
3. **Log Appropriately** - ERROR for system issues, WARN for business logic
4. **Return Meaningful Codes** - Use correct HTTP status codes
5. **Hide Internal Details** - Never expose stack traces or DB errors to client
6. **Handle External Failures** - Stripe, network, etc. must not crash system

### Frontend

1. **Validate Client-Side** - Catch errors before API calls
2. **Handle All Scenarios** - Success, loading, error, empty states
3. **Show User-Friendly Messages** - Clear, actionable, non-technical
4. **Graceful Degradation** - App continues if optional features fail
5. **Prevent Double-Submission** - Disable buttons during loading
6. **Log Errors** - Console.error for debugging (remove in production)

---

## Testing Error Scenarios

### Backend Testing

**Manual Testing with HTTP files:**
- `backend/src/main/resources/http/*.http`
- Test invalid inputs, missing fields, wrong IDs

**Unit Testing:**
- Test ValidationUtil methods
- Test DAO transaction rollback
- Mock Stripe failures

### Frontend Testing

**Manual Testing:**
- Try invalid email formats
- Try short passwords
- Try non-matching passwords
- Try invalid serial numbers
- Disconnect network and test

**User Testing:**
- Observe actual user behavior
- Collect error reports
- Improve messages based on feedback

---

## Conclusion

Error handling in NotionPay is designed to:

✅ **Protect Data Integrity** - Transactions ensure consistency  
✅ **Provide Stability** - Errors don't crash the system  
✅ **Give Clear Feedback** - Users know what went wrong and what to do  
✅ **Enable Debugging** - Logs and audit trail aid troubleshooting  
✅ **Maintain Security** - Internal details never exposed  
✅ **Ensure Reliability** - Critical for payment processing systems  

This comprehensive approach to error handling ensures NotionPay remains stable, secure, and user-friendly even when errors occur.



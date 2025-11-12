# Registration Verification with Serial Numbers

## 🎯 Overview

Users can now only register if they have a valid **email + serialNumber** combination that exists in the PreRegistrationData table (simulating an external database).

---

## 📋 What Was Implemented

### 1. **PreRegistrationData Entity**
**File:** `src/main/java/dat/entities/PreRegistrationData.java`

Represents pre-approved combinations of email + serialNumber:
- `email` - Pre-approved email address
- `serialNumber` - Pre-approved serial number
- `companyName` - Optional company information
- `used` - Boolean flag (false = available, true = already registered)

### 2. **VerificationService**
**File:** `src/main/java/dat/services/VerificationService.java`

Handles verification logic:

**Methods:**
- `verifyPreRegistration(String email, Integer serialNumber)` 
  - Checks if combination exists
  - Checks if not already used
  - Returns true/false

- `markAsUsed(String email, Integer serialNumber)`
  - Marks a record as used after successful registration
  - Prevents reuse of same serial number

### 3. **Modified SecurityController**
**File:** `src/main/java/dat/security/controllers/SecurityController.java`

**Changes:**
- Added `VerificationService` instance
- Changed `register()` to accept `RegisterRequest` (includes serialNumber)
- Added verification step BEFORE user creation
- Returns 403 Forbidden if verification fails

**New Registration Flow:**
```java
1. Receive RegisterRequest (email, password, serialNumber)
2. Verify email + serialNumber combination → verificationService.verifyPreRegistration()
3. If invalid/used → Return 403 error
4. If valid → Create User → securityDAO.createUser()
5. Mark as used → verificationService.markAsUsed()
6. Generate JWT token
7. Return 201 Created with token
```

### 4. **RegisterRequest DTO**
**File:** `src/main/java/dat/dtos/RegisterRequest.java`

```java
public class RegisterRequest {
    public String email;
    public String password;
    public String companyName;    // Optional
    public Integer serialNumber;  // Required for verification
}
```

### 5. **Test Data in Main.java**
**File:** `src/main/java/dat/Main.java`

Creates 4 test records:
1. `user@example.com` + `12345` [AVAILABLE]
2. `john.doe@company.com` + `67890` [AVAILABLE]
3. `jane.smith@startup.io` + `11111` [AVAILABLE]
4. `used@example.com` + `99999` [ALREADY USED]

### 6. **Updated HTTP Tests**
**File:** `src/main/java/dat/security/http/demoSecurity.http`

Registration now includes serialNumber:
```http
POST {{url}}/auth/register/

{
    "email": "user@example.com",
    "password": "test123",
    "serialNumber": 12345
}
```

---

## 🧪 Testing Scenarios

### ✅ Scenario 1: Valid Registration (Should Succeed)
```http
POST http://localhost:7070/auth/register/

{
    "email": "user@example.com",
    "password": "test123",
    "serialNumber": 12345
}
```

**Expected Result:**
- Status: `201 Created`
- Response:
```json
{
    "token": "eyJhbGc...",
    "email": "user@example.com",
    "msg": "Registration successful"
}
```

---

### ❌ Scenario 2: Invalid Serial Number (Should Fail)
```http
POST http://localhost:7070/auth/register/

{
    "email": "user@example.com",
    "password": "test123",
    "serialNumber": 99999999
}
```

**Expected Result:**
- Status: `403 Forbidden`
- Response:
```json
{
    "msg": "Invalid email or serial number combination, or already registered"
}
```

---

### ❌ Scenario 3: Wrong Email + Serial Combination (Should Fail)
```http
POST http://localhost:7070/auth/register/

{
    "email": "wrong@example.com",
    "password": "test123",
    "serialNumber": 12345
}
```

**Expected Result:**
- Status: `403 Forbidden`
- Response:
```json
{
    "msg": "Invalid email or serial number combination, or already registered"
}
```

---

### ❌ Scenario 4: Already Used Serial Number (Should Fail)
```http
POST http://localhost:7070/auth/register/

{
    "email": "used@example.com",
    "password": "test123",
    "serialNumber": 99999
}
```

**Expected Result:**
- Status: `403 Forbidden`
- Response:
```json
{
    "msg": "Invalid email or serial number combination, or already registered"
}
```

---

### ❌ Scenario 5: Try to Register Same User Twice (Should Fail on 2nd attempt)

**First Attempt:**
```http
POST http://localhost:7070/auth/register/

{
    "email": "john.doe@company.com",
    "password": "test123",
    "serialNumber": 67890
}
```
- Status: `201 Created` ✅

**Second Attempt (same email + serial):**
```http
POST http://localhost:7070/auth/register/

{
    "email": "john.doe@company.com",
    "password": "test123",
    "serialNumber": 67890
}
```
- Status: `403 Forbidden` ❌ (serial number already marked as used)

---

## 📊 Database Tables

### PreRegistrationData Table Structure
```sql
CREATE TABLE pre_registration_data (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255),
    serial_number INTEGER,
    company_name VARCHAR(255),
    used BOOLEAN
);
```

### Sample Data
| id | email | serial_number | company_name | used |
|----|-------|---------------|--------------|------|
| 1 | user@example.com | 12345 | Example Company A | false |
| 2 | john.doe@company.com | 67890 | Company B | false |
| 3 | jane.smith@startup.io | 11111 | Startup Inc | false |
| 4 | used@example.com | 99999 | Already Registered Co | true |

---

## 🔧 How to Run

1. **Start the application:**
```bash
mvn clean compile exec:java
```

2. **Check console output:**
You should see:
```
🔑 Test 2.5: PreRegistration Data
--------------------------------------------------
✅ Created PreRegistrationData records:
   1. user@example.com + 12345 [AVAILABLE]
   2. john.doe@company.com + 67890 [AVAILABLE]
   3. jane.smith@startup.io + 11111 [AVAILABLE]
   4. used@example.com + 99999 [ALREADY USED]
✅ Use these combinations to test registration!
```

3. **Test with HTTP client:**
Open `src/main/java/dat/security/http/demoSecurity.http` and run the requests

---

## 🔄 Registration Flow Diagram

```
┌─────────────────────────────────────┐
│  Client sends RegisterRequest       │
│  {email, password, serialNumber}    │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│  SecurityController.register()      │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│  VerificationService                │
│  verifyPreRegistration()            │
└──────────────┬──────────────────────┘
               │
      ┌────────┴────────┐
      │                 │
   VALID            INVALID
      │                 │
      ↓                 ↓
┌──────────┐    ┌──────────────┐
│ Continue │    │ Return 403   │
└────┬─────┘    │ Forbidden    │
     │          └──────────────┘
     ↓
┌─────────────────────────────────────┐
│  SecurityDAO.createUser()           │
│  (Create User in database)          │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│  VerificationService.markAsUsed()   │
│  (Set used = true)                  │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│  Generate JWT Token                 │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│  Return 201 Created                 │
│  {token, email, msg}                │
└─────────────────────────────────────┘
```

---

## 📝 Status Codes

| Code | Meaning | When |
|------|---------|------|
| 201 | Created | Successful registration |
| 403 | Forbidden | Invalid/used serial number |
| 422 | Unprocessable Entity | User already exists |
| 500 | Internal Server Error | Server error during registration |

---

## 🚀 Future Enhancements

Consider adding:

1. **Expiration dates** for PreRegistrationData
2. **IP address logging** for registration attempts
3. **Rate limiting** for failed attempts
4. **Email verification** after registration
5. **Async notification** to external system when serial is used
6. **Bulk import** of PreRegistrationData from CSV/external API
7. **Admin dashboard** to manage PreRegistrationData records

---

## 🔒 Security Notes

- Serial numbers cannot be reused (marked as `used = true`)
- Invalid combinations return same error as used ones (prevent enumeration)
- Passwords are hashed with BCrypt before storage
- JWT tokens are used for authentication after registration

---

**Last Updated:** November 2025


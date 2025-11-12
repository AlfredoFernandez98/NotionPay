
# SerialLink Integration Guide

## 🎯 Architecture Overview

Your system now uses **SerialLink** instead of PreRegistrationData. This is more sophisticated because:

1. ✅ **Plan Eligibility**: Each serial number is linked to a specific Plan
2. ✅ **Status Tracking**: PENDING → VERIFIED → linked to Customer
3. ✅ **Customer Linking**: SerialLink connects to Customer after registration
4. ✅ **External Proof**: Tracks verification source

---

## 📊 Data Flow

```
┌─────────────────────────────────────────────┐
│  1. SerialLink exists (Pre-registration)    │
│     - serialNumber: 101010101               │
│     - plan: Basic Monthly                   │
│     - status: PENDING                       │
│     - customer: NULL                        │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│  2. User Registers                          │
│     POST /auth/register/                    │
│     { serialNumber: 101010101 }             │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│  3. Verify SerialNumber                     │
│     SerialLinkVerificationService           │
│     - Check status = PENDING                │
│     - Check customer = NULL                 │
└─────────────────┬───────────────────────────┘
                  │
          ┌───────┴────────┐
          │                │
       VALID           INVALID
          │                │
          ↓                ↓
┌──────────────┐   ┌──────────────┐
│ Continue     │   │ Return 403   │
└──────┬───────┘   └──────────────┘
       │
       ↓
┌─────────────────────────────────────────────┐
│  4. Create User                             │
│     SecurityDAO.createUser()                │
│     email + password → User entity          │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│  5. Create Customer                         │
│     CustomerDAO.createCustomer()            │
│     Link to User                            │
│     Store serialNumber                      │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│  6. Link Customer to SerialLink             │
│     SerialLinkService.linkCustomer()        │
│     - Set customer_id                       │
│     - Set status = VERIFIED                 │
│     - Set verified_at = NOW()               │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│  7. Return Success                          │
│     201 Created + JWT Token                 │
└─────────────────────────────────────────────┘
```

---

## 🛠️ What's Been Done

### ✅ Step 1: SerialLink Entity Updated
- Made `customer` optional (nullable)
- Added unique constraint on `serialNumber`
- Ready for pre-registration state

### ✅ Step 2: SerialLinkVerificationService Created
- `verifySerialNumber()` - Check if serial is valid and pending
- `linkCustomerToSerialLink()` - Link customer after creation
- `getPlanForSerialNumber()` - Get eligible plan
- `getSerialLink()` - Get full SerialLink entity

### ✅ Step 3: SerialLinkMigration Created
- Creates 3 Plans (Basic, Professional, Enterprise)
- Creates 5 SerialLink records
- 3 PENDING (available)
- 1 VERIFIED (already used)
- 1 REJECTED (invalid)

### ✅ Step 4: HibernateConfig Updated
- Added Plan, SerialLink, Customer entities

### ✅ Step 5: Main.java Updated
- Uses SerialLinkMigration instead of PreRegistrationData

---

## 🚧 What You Need to Implement

### **NEXT STEP 1: Create CustomerDAO**

**File:** `src/main/java/dat/daos/impl/CustomerDAO.java`

```java
package dat.daos.impl;

import dat.entities.Customer;
import dat.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.OffsetDateTime;

public class CustomerDAO {
    private EntityManagerFactory emf;

    public CustomerDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Create a Customer linked to a User
     */
    public Customer createCustomer(User user, String companyName, Integer serialNumber) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setCompanyName(companyName);
            customer.setSerialNumber(serialNumber);
            customer.setCreatedAt(OffsetDateTime.now());
            // external_customer_id will be set when Stripe integration is added
            
            em.persist(customer);
            em.getTransaction().commit();
            
            return customer;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
```

---

### **NEXT STEP 2: Update SecurityController.register()**

You need to modify the registration flow to:
1. Verify serial number
2. Create User
3. Create Customer
4. Link to SerialLink

**File:** `src/main/java/dat/security/controllers/SecurityController.java`

**Add at top of class:**
```java
private SerialLinkVerificationService serialLinkService;
private CustomerDAO customerDAO;
```

**Update getInstance():**
```java
public static SecurityController getInstance() {
    if (instance == null) {
        instance = new SecurityController();
    }
    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    securityDAO = new SecurityDAO(emf);
    instance.serialLinkService = new SerialLinkVerificationService(emf);
    instance.customerDAO = new CustomerDAO(emf);
    return instance;
}
```

**Update register() method:**
```java
@Override
public Handler register() {
    return (ctx) -> {
        ObjectNode returnObject = objectMapper.createObjectNode();
        try {
            RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);
            
            // STEP 1: Verify serial number
            boolean isValid = serialLinkService.verifySerialNumber(registerRequest.serialNumber);
            
            if (!isValid) {
                ctx.status(HttpStatus.FORBIDDEN);
                ctx.json(returnObject.put("msg", "Invalid serial number or already used"));
                return;
            }
            
            // STEP 2: Get the eligible Plan
            Plan eligiblePlan = serialLinkService.getPlanForSerialNumber(registerRequest.serialNumber);
            
            // STEP 3: Create User
            User created = securityDAO.createUser(registerRequest.email, registerRequest.password);
            
            // STEP 4: Create Customer (linked to User)
            Customer customer = customerDAO.createCustomer(
                created, 
                registerRequest.companyName, 
                registerRequest.serialNumber
            );
            
            // STEP 5: Link Customer to SerialLink
            serialLinkService.linkCustomerToSerialLink(registerRequest.serialNumber, customer);
            
            // STEP 6: Create JWT token
            String token = createToken(new UserDTO(created.getEmail(), Set.of("USER")));
            
            ctx.status(HttpStatus.CREATED).json(returnObject
                    .put("token", token)
                    .put("email", created.getEmail())
                    .put("customerId", customer.getId())
                    .put("plan", eligiblePlan.getName())
                    .put("msg", "Registration successful"));
                    
        } catch (EntityExistsException e) {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
            ctx.json(returnObject.put("msg", "User already exists"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(returnObject.put("msg", "Registration failed: " + e.getMessage()));
            logger.error("Registration error: ", e);
        }
    };
}
```

---

### **NEXT STEP 3: Update HTTP Test File**

**File:** `src/main/java/dat/security/http/demoSecurity.http`

```http
### TEST 1: Register with Basic Plan serial ✅
POST {{url}}/auth/register/

{
    "email": "ellab@company.dk",
    "password": "test123",
    "serialNumber": 101010101,
    "companyName": "Ellab A/S"
}

### TEST 2: Register with Professional Plan serial ✅
POST {{url}}/auth/register/

{
    "email": "notion@tech.io",
    "password": "securePass",
    "serialNumber": 404040404,
    "companyName": "Notion Technologies"
}

### TEST 3: Try already verified serial (should fail) ❌
POST {{url}}/auth/register/

{
    "email": "test@test.com",
    "password": "test123",
    "serialNumber": 202020202,
    "companyName": "Test Company"
}

### TEST 4: Try rejected serial (should fail) ❌
POST {{url}}/auth/register/

{
    "email": "invalid@test.com",
    "password": "test123",
    "serialNumber": 999999999,
    "companyName": "Invalid Company"
}
```

---

## 📋 SerialLink Mock Data

After running `Main.java`, you'll have:

| Serial Number | Plan | Status | Available? |
|--------------|------|--------|-----------|
| 101010101 | Basic Monthly (499 DKK) | PENDING | ✅ Yes |
| 404040404 | Professional Monthly (999 DKK) | PENDING | ✅ Yes |
| 505050505 | Enterprise Yearly (9999 DKK) | PENDING | ✅ Yes |
| 202020202 | Basic Monthly | VERIFIED | ❌ Already used |
| 999999999 | Basic Monthly | REJECTED | ❌ Invalid |

---

## 🔍 Verification Flow Details

### **When User Registers:**

1. **Receive Request:**
```json
{
    "email": "user@example.com",
    "password": "securePass",
    "serialNumber": 101010101,
    "companyName": "My Company"
}
```

2. **Verify SerialLink:**
```sql
SELECT * FROM serial_link 
WHERE serial_number = 101010101 
AND status = 'PENDING' 
AND customer_id IS NULL
```

3. **If Valid → Create User:**
```sql
INSERT INTO users (email, password) 
VALUES ('user@example.com', 'hashed_password')
```

4. **Create Customer:**
```sql
INSERT INTO customer (user_email, company_name, serial_number, created_at)
VALUES ('user@example.com', 'My Company', 101010101, NOW())
```

5. **Update SerialLink:**
```sql
UPDATE serial_link 
SET customer_id = 123,
    status = 'VERIFIED',
    verified_at = NOW(),
    updated_at = NOW()
WHERE serial_number = 101010101
```

6. **Return Success:**
```json
{
    "token": "eyJhbGc...",
    "email": "user@example.com",
    "customerId": 123,
    "plan": "Basic Monthly",
    "msg": "Registration successful"
}
```

---

## 🎨 Database Schema

```
┌──────────────┐
│   User       │
│──────────────│
│ email (PK)   │
│ password     │
└──────┬───────┘
       │ 1:1
       ↓
┌──────────────┐      ┌──────────────┐
│  Customer    │ 1:M  │ SerialLink   │
│──────────────│←─────│──────────────│
│ id (PK)      │      │ id (PK)      │
│ user_email   │      │ customer_id  │
│ company_name │      │ serialNumber │
│ serial_number│      │ plan_id (FK) │
│ created_at   │      │ status       │
└──────────────┘      │ verified_at  │
                      └──────┬───────┘
                             │ M:1
                             ↓
                      ┌──────────────┐
                      │    Plan      │
                      │──────────────│
                      │ id (PK)      │
                      │ name         │
                      │ period       │
                      │ price_cents  │
                      └──────────────┘
```

---

## 🚀 How to Run

```bash
# 1. Run Main.java to populate database
mvn exec:java -Dexec.mainClass="dat.Main"

# You'll see:
# ✅ Created 3 Plans
# ✅ Created 5 SerialLink records

# 2. Start your API server
mvn exec:java -Dexec.mainClass="dat.config.ApplicationConfig"

# 3. Test registration with HTTP client
# Use demoSecurity.http
```

---

## ✅ Benefits of SerialLink vs PreRegistrationData

| Feature | PreRegistrationData | SerialLink |
|---------|-------------------|------------|
| Plan Eligibility | ❌ No | ✅ Yes |
| Status Tracking | ❌ Boolean only | ✅ PENDING/VERIFIED/REJECTED |
| Customer Linking | ❌ No | ✅ Yes |
| External Proof | ❌ No | ✅ Yes |
| Timestamps | ❌ No | ✅ created_at, updated_at, verified_at |
| Future Subscriptions | ❌ No | ✅ Ready for Subscription creation |

---

## 🔐 Security Notes

- Serial numbers are unique
- Cannot reuse VERIFIED serials
- REJECTED serials are blocked
- Customer is linked only after successful User creation
- Transaction rollback if any step fails

---

## 📚 Next Steps After Integration

Once SerialLink is working, you can:

1. **Create Subscription** when Customer is created
2. **Link to Stripe** for external_customer_id
3. **Send welcome email** with Plan details
4. **Activate Plan features** based on SerialLink.plan
5. **Track Activity** in ActivityLog

---

**Your system is now ready for sophisticated registration with Plan eligibility!** 🎉


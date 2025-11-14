# 🔗 External Customer ID Integration

## 📋 Overview

The `external_customer_id` is now properly integrated from the **SerialLink** entity (external database) to the **Customer** entity. This field represents the customer ID from an external payment system (e.g., Stripe, PayPal) and is used to link to **SMS Balance** from another database.

---

## 🎯 Why This Change?

### **Problem:**
Previously, `external_customer_id` could be set from user input, which doesn't make sense because:
- This ID comes from an external payment system
- Users shouldn't be able to choose their own external customer ID
- It must match the ID in the SMS Balance system

### **Solution:**
`external_customer_id` is now:
1. Stored in `SerialLink` (simulating external database)
2. Automatically populated when creating a Customer
3. Used to link to SMS Balance tracking

---

## 🏗️ Architecture Flow

```
External System (Mock)              NotionPay System
┌─────────────────────┐            ┌──────────────────┐
│  SERIALLINK         │            │   CUSTOMER       │
│  ┌───────────────┐  │            │  ┌────────────┐  │
│  │ serial_number │  │────────────┼─▶│ serial_no  │  │
│  │ external_id   │  │────────────┼─▶│ external_id│  │
│  │ plan_id       │  │            │  │ company    │  │
│  └───────────────┘  │            │  └────────────┘  │
└─────────────────────┘            └──────────────────┘
         │                                   │
         │                                   │
         └───────────────┬───────────────────┘
                         │
                         ▼
                ┌─────────────────┐
                │   SMS_BALANCE   │
                │  ┌────────────┐ │
                │  │ external_id│ │ ← Links here!
                │  │ sms_credits│ │
                │  │ used_sms   │ │
                │  └────────────┘ │
                └─────────────────┘
```

---

## 📝 Changes Made

### **1. Updated SerialLink Entity**

**File:** `dat/entities/SerialLink.java`

**Added:**
```java
@Column(name = "external_customer_id", unique = true)
private String externalCustomerId;  // From external system (e.g., Stripe customer ID)
```

**Why:**
- Simulates data from external payment/billing system
- Each SerialLink comes with a pre-existing external customer ID
- This ID is used across multiple external systems (payments, SMS, etc.)

---

### **2. Updated SerialLink Migration**

**File:** `dat/mockdatabase/SerialLinkMigration.java`

**Added external IDs for each serial:**
```java
serial1.setExternalCustomerId("cus_external_ellab_001");     // Serial: 101010101
serial2.setExternalCustomerId("cus_external_notion_002");    // Serial: 404040404
serial3.setExternalCustomerId("cus_external_startup_003");   // Serial: 505050505
serial4.setExternalCustomerId("cus_external_bbb_004");       // Serial: 202020202
serial5.setExternalCustomerId("cus_external_rejected_005");  // Serial: 999999999
```

**Why:**
- Simulates that external IDs already exist in external system
- Format mimics Stripe customer IDs (`cus_*`)
- Each serial has a unique external ID

---

### **3. Updated CustomerController**

**File:** `dat/controllers/impl/CustomerController.java`

**Key Changes:**

**Before:**
```java
Customer customer = new Customer(user, dto.companyName, dto.serialNumber);
Customer savedCustomer = customerDAO.create(customer);
```

**After:**
```java
// Get SerialLink (contains external_customer_id)
SerialLink serialLink = serialLinkService.getSerialLink(dto.serialNumber);

// Create Customer and populate external_customer_id FROM SerialLink
Customer customer = new Customer(user, dto.companyName, dto.serialNumber);
customer.setExternalCustomerId(serialLink.getExternalCustomerId()); // ← FROM EXTERNAL DB!
Customer savedCustomer = customerDAO.create(customer);
```

**Why:**
- Ensures `external_customer_id` comes from trusted source (SerialLink)
- User cannot fake or choose their own external ID
- Maintains data integrity with external systems

---

## 🔄 Complete Customer Creation Flow

```
1. User submits: { email, companyName, serialNumber }
   ↓
2. Verify serialNumber is valid (PENDING status)
   ↓
3. Get Plan for serialNumber (Basic/Pro/Enterprise)
   ↓
4. Get SerialLink entity (contains external_customer_id)
   ↓
5. Verify User exists
   ↓
6. Create Customer:
   - user: from database
   - companyName: from user input
   - serialNumber: from user input
   - external_customer_id: FROM SERIALLINK ← KEY CHANGE!
   ↓
7. Save Customer to database
   ↓
8. Link Customer to SerialLink (mark as VERIFIED)
   ↓
9. Return success with Plan info and external_customer_id
```

---

## 📊 Database Relationships

### **SerialLink → Customer**

```sql
-- SerialLink table (simulated external DB)
CREATE TABLE serial_links (
    id BIGSERIAL PRIMARY KEY,
    serial_number INTEGER UNIQUE NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    external_customer_id VARCHAR(255) UNIQUE,  -- ← NEW FIELD
    customer_id BIGINT REFERENCES customers(id),
    created_at TIMESTAMP,
    verified_at TIMESTAMP
);

-- Customer table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) UNIQUE NOT NULL,
    company_name VARCHAR(255) UNIQUE,
    serial_number INTEGER,
    external_customer_id VARCHAR(255) UNIQUE,  -- ← Copied from SerialLink
    created_at TIMESTAMP
);

-- SMS Balance table (external system)
CREATE TABLE sms_balances (
    id BIGSERIAL PRIMARY KEY,
    external_customer_id VARCHAR(255) UNIQUE,  -- ← Links here!
    total_sms_credits INTEGER,
    used_sms_credits INTEGER,
    remaining_sms_credits INTEGER,
    last_recharged_at TIMESTAMP
);
```

---

## 🧪 Testing

### **Test 1: Create Customer with Valid Serial**

**Request:**
```http
POST http://localhost:7070/api/customers/
Content-Type: application/json

{
  "email": "testuser@example.com",
  "companyName": "Test Company",
  "serialNumber": 101010101
}
```

**Expected Response:**
```json
{
  "message": "customer saved with id: testuser@example.com under plan: Basic Monthly (external_id: cus_external_ellab_001)"
}
```

**Database Result:**
```sql
SELECT * FROM customers WHERE serial_number = 101010101;

-- Result:
id | user_email              | company_name | serial_number | external_customer_id
1  | testuser@example.com    | Test Company | 101010101     | cus_external_ellab_001
```

**SerialLink Status:**
```sql
SELECT serial_number, status, customer_id, external_customer_id 
FROM serial_links 
WHERE serial_number = 101010101;

-- Result:
serial_number | status   | customer_id | external_customer_id
101010101     | VERIFIED | 1           | cus_external_ellab_001
```

---

### **Test 2: Verify SMS Balance Linking**

**Query:**
```sql
-- Find customer's SMS balance using external_customer_id
SELECT 
    c.company_name,
    c.external_customer_id,
    sb.total_sms_credits,
    sb.remaining_sms_credits
FROM customers c
JOIN sms_balances sb ON c.external_customer_id = sb.external_customer_id
WHERE c.id = 1;
```

**Expected Result:**
```
company_name  | external_customer_id  | total_sms_credits | remaining_sms_credits
Test Company  | cus_external_ellab_001| 1000             | 1000
```

---

## 🎯 Benefits

### **1. Data Integrity**
- External IDs come from trusted source (SerialLink/external DB)
- Users cannot fake external customer IDs
- Consistent with external systems

### **2. System Integration**
- Easy to link with SMS Balance system
- Easy to link with payment system (Stripe, etc.)
- Single source of truth for external IDs

### **3. Security**
- External IDs are read-only from user perspective
- System controls ID assignment
- Prevents ID conflicts

### **4. Traceability**
- Clear audit trail: SerialLink → Customer → SMS Balance
- Can trace back to original external system
- Easier debugging

---

## 📚 Available External Customer IDs (Mock Data)

| Serial Number | External Customer ID       | Plan              | Status   |
|---------------|----------------------------|-------------------|----------|
| 101010101     | cus_external_ellab_001     | Basic Monthly     | PENDING  |
| 404040404     | cus_external_notion_002    | Professional      | PENDING  |
| 505050505     | cus_external_startup_003   | Enterprise Yearly | PENDING  |
| 202020202     | cus_external_bbb_004       | Basic Monthly     | VERIFIED |
| 999999999     | cus_external_rejected_005  | Basic Monthly     | REJECTED |

---

## 🔑 Key Takeaways

1. ✅ `external_customer_id` is now populated **automatically** from SerialLink
2. ✅ Users **cannot** set their own external customer ID
3. ✅ This ID is used to link with **SMS Balance** and other external systems
4. ✅ Maintains **data integrity** between NotionPay and external systems
5. ✅ Follows **single source of truth** principle

---

## 🚀 Next Steps

1. ✅ Test customer creation with new flow
2. ✅ Verify external_customer_id is populated correctly
3. ⏭️ Create SMS Balance records using external_customer_id
4. ⏭️ Implement SMS balance tracking
5. ⏭️ Link payments using external_customer_id

---

**Your architecture is now properly integrated with external systems!** 🎉


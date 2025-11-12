# Mock Database Guide - NotionPay

## 🎯 Purpose

The **Mock Database** simulates data from an external database system. This allows you to test the registration verification system without needing access to an actual external database.

---

## 📁 File Structure

```
src/main/java/dat/
├── mockdatabase/
│   └── MigrationDataforPreRegistrationData.java  ← Your mock database populator
├── entities/
│   └── PreRegistrationData.java                  ← Entity representing external DB data
└── Main.java                                      ← Automatically populates on startup
```

---

## 🚀 How to Use

### **Method 1: Automatic Population (Recommended)**

Just run your application normally:

```bash
mvn clean compile exec:java
```

The mock database will automatically populate when the application starts. You'll see:

```
🚀 NotionPay - Starting Application
==================================================

📊 Initializing Mock Database...

🔑 Populating PreRegistration Data (Simulating External DB)
------------------------------------------------------------

✅ Created 5 PreRegistration records:

📋 AVAILABLE FOR REGISTRATION:
   1. ellab@gmail.dk + 101010101 [Ellab A/S]
   2. test.user@notion.io + 404040404 [Notion Technologies]
   3. admin@startup.dk + 505050505 [Startup Denmark IVS]

🚫 ALREADY USED (Will fail registration):
   1. bbb@hotmail.dk + 202020202 [BBB ApS]
   2. cccccc-e2e2@outlook.dk + 303030303 [CCCCCC IVS]
```

---

### **Method 2: Run Migration Separately**

If you want to ONLY populate the database without starting the server:

```bash
mvn exec:java -Dexec.mainClass="dat.mockdatabase.MigrationDataforPreRegistrationData"
```

This is useful for:
- Resetting the database
- Testing the migration script
- Populating data before tests

---

## 📊 Mock Database Records

### Table: `PreRegistrationData`

Your mock database contains **5 records** simulating an external system:

| ID | Email | Serial Number | Company Name | Used | Status |
|----|-------|---------------|--------------|------|---------|
| 1 | ellab@gmail.dk | 101010101 | Ellab A/S | false | ✅ Available |
| 2 | bbb@hotmail.dk | 202020202 | BBB ApS | true | ❌ Already Used |
| 3 | cccccc-e2e2@outlook.dk | 303030303 | CCCCCC IVS | true | ❌ Already Used |
| 4 | test.user@notion.io | 404040404 | Notion Technologies | false | ✅ Available |
| 5 | admin@startup.dk | 505050505 | Startup Denmark IVS | false | ✅ Available |

---

## 🧪 Testing Scenarios

### ✅ Scenario 1: Valid Registration (Should Succeed)

**Test Case:** Register with a valid, unused combination

```http
POST http://localhost:7070/auth/register/

{
    "email": "ellab@gmail.dk",
    "password": "test123",
    "serialNumber": 101010101
}
```

**Expected Result:**
- Status: `201 Created`
- Response:
```json
{
    "token": "eyJhbGc...",
    "email": "ellab@gmail.dk",
    "msg": "Registration successful"
}
```

**After Success:** The record will be marked as `used = true`

---

### ❌ Scenario 2: Already Used Serial Number

**Test Case:** Try to register with an already-used combination

```http
POST http://localhost:7070/auth/register/

{
    "email": "bbb@hotmail.dk",
    "password": "test123",
    "serialNumber": 202020202
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

### ❌ Scenario 3: Invalid Serial Number

**Test Case:** Use a serial number that doesn't exist in mock DB

```http
POST http://localhost:7070/auth/register/

{
    "email": "ellab@gmail.dk",
    "password": "test123",
    "serialNumber": 999999999
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

### ❌ Scenario 4: Wrong Email for Valid Serial

**Test Case:** Email and serial don't match

```http
POST http://localhost:7070/auth/register/

{
    "email": "wrong@email.com",
    "password": "test123",
    "serialNumber": 101010101
}
```

**Expected Result:**
- Status: `403 Forbidden`

---

## 🔄 How It Works

```
┌──────────────────────────────────────────┐
│  1. Application Starts                   │
│     Main.java calls populator            │
└────────────────┬─────────────────────────┘
                 │
                 ↓
┌──────────────────────────────────────────┐
│  2. Mock Database Populates              │
│     MigrationData creates 5 records      │
└────────────────┬─────────────────────────┘
                 │
                 ↓
┌──────────────────────────────────────────┐
│  3. PreRegistrationData Table            │
│     Contains valid email+serial combos   │
└────────────────┬─────────────────────────┘
                 │
                 ↓
┌──────────────────────────────────────────┐
│  4. User Tries to Register               │
│     POST /auth/register/                 │
└────────────────┬─────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
     VALID            INVALID
        │                 │
        ↓                 ↓
┌──────────────┐   ┌─────────────┐
│ Create User  │   │ Return 403  │
│ Mark as used │   │ Forbidden   │
└──────────────┘   └─────────────┘
```

---

## 🛠️ Customizing Mock Data

### Add New Records

Edit `MigrationDataforPreRegistrationData.java`:

```java
// Add a new pre-registration record
PreRegistrationData preUser6 = new PreRegistrationData();
preUser6.setEmail("newuser@company.com");
preUser6.setSerialNumber(606060606);
preUser6.setCompanyName("New Company Ltd");
preUser6.setUsed(false);
em.persist(preUser6);
```

### Clear All Data

The migration class includes a helper method:

```java
// Call this to clear all PreRegistrationData
MigrationDataforPreRegistrationData.clearAllPreRegistrationData();
```

Or add it to your code:

```java
// In Main.java before populating:
MigrationDataforPreRegistrationData.clearAllPreRegistrationData();
MigrationDataforPreRegistrationData.populatePreRegistrationData();
```

---

## 📝 Code Structure

### MigrationDataforPreRegistrationData.java

**Main Components:**

1. **`populatePreRegistrationData()`** - Public static method
   - Creates 5 mock records
   - Can be called from anywhere
   - Prints detailed summary

2. **`clearAllPreRegistrationData()`** - Helper method
   - Deletes all PreRegistrationData records
   - Useful for testing/reset

3. **`main(String[] args)`** - Standalone execution
   - Can run the migration independently
   - Nice formatting and output

---

## 🔍 Verifying Mock Data

### Option 1: Check Console Output

When you start the app, you'll see the list of created records.

### Option 2: Query the Database

```sql
-- Connect to your PostgreSQL database
psql -U postgres -d your_db_name

-- View all PreRegistrationData
SELECT * FROM pre_registration_data;

-- View available records
SELECT * FROM pre_registration_data WHERE used = false;

-- View used records
SELECT * FROM pre_registration_data WHERE used = true;
```

### Option 3: Use HTTP Tests

Open `demoSecurity.http` and run the test cases to see which ones succeed/fail.

---

## 🎨 Integration Flow

```
Main.java
  ↓
  Calls: MigrationDataforPreRegistrationData.populatePreRegistrationData()
  ↓
  Creates records in PreRegistrationData table
  ↓
  VerificationService can now query this table
  ↓
  SecurityController uses VerificationService during registration
  ↓
  User registers successfully OR gets 403 error
```

---

## 🚨 Important Notes

1. **Octal Numbers:** Serial numbers starting with `0` are treated as octal in Java
   - ❌ Bad: `0101010101` 
   - ✅ Good: `101010101`

2. **Database Reset:** If you restart your app with `hibernate.hbm2ddl.auto=create`, all data is wiped and recreated

3. **Production:** In production, replace this mock with a real external database connection

4. **Testing:** The mock database makes it easy to test without external dependencies

---

## 🔐 Security Considerations

- Mock data is for **development and testing only**
- Never use mock data in production
- Real external database should have:
  - Proper authentication
  - Encrypted connections
  - Audit logging
  - Rate limiting

---

## 📚 Related Files

| File | Purpose |
|------|---------|
| `MigrationDataforPreRegistrationData.java` | Populates mock database |
| `PreRegistrationData.java` | Entity definition |
| `VerificationService.java` | Queries PreRegistrationData |
| `SecurityController.java` | Uses verification before registration |
| `demoSecurity.http` | HTTP tests with mock data |

---

## 🆘 Troubleshooting

### Problem: No data in database after startup

**Solution:** Check console output for errors. Make sure:
- Database connection is working
- Hibernate config includes PreRegistrationData entity
- No transaction rollback errors

### Problem: All registrations fail with 403

**Solution:** 
- Check if mock data was actually created
- Query the database: `SELECT * FROM pre_registration_data;`
- Make sure you're using the exact email+serial combinations from the mock data

### Problem: Serial numbers don't match

**Solution:**
- Don't use leading zeros (octal issue)
- Use integers like `101010101` not `0101010101`

---

## 📖 Quick Reference

### Start Application (Auto-populate):
```bash
mvn clean compile exec:java
```

### Run Migration Only:
```bash
mvn exec:java -Dexec.mainClass="dat.mockdatabase.MigrationDataforPreRegistrationData"
```

### Available Test Emails:
- ✅ `ellab@gmail.dk` + `101010101`
- ✅ `test.user@notion.io` + `404040404`
- ✅ `admin@startup.dk` + `505050505`
- ❌ `bbb@hotmail.dk` + `202020202` (already used)
- ❌ `cccccc-e2e2@outlook.dk` + `303030303` (already used)

---

**Last Updated:** November 2025
**Status:** ✅ Production Ready (for testing)


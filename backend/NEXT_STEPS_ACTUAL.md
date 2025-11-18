# 🚀 Next Steps - Based on Actual Code Analysis

**Current Status:** Input Validation & Email/Serial Matching ✅  
**What Needs to Be Done:** Complete Empty DAOs  

---

## 📊 **Actual State of the Code**

### ✅ **Already Implemented:**
- SessionDAO.java - **EMPTY** (needs implementation)
- ActivityLogDAO.java - **STUB with TODOs** (needs full implementation)
- PaymentDAO.java - **STUB with TODOs** (not urgent for Sprint 1)
- ReceiptDAO.java - **EMPTY** (not urgent for Sprint 1)

### ✅ **Already Complete:**
- ValidationUtil.java ✅
- SecurityController with validation ✅
- CustomerController with validation ✅
- Email + Serial matching ✅
- SmsBalanceDAO ✅
- CustomerDAO ✅
- Other DAOs partially done

---

## 🎯 **Actual Next Steps for Sprint 1**

### **Step 1: Implement SessionDAO** (1-2 hours)

**File:** `backend/src/main/java/dat/daos/impl/SessionDAO.java`

Currently it's completely empty. You need to:

1. **Add imports and singleton pattern:**
```java
import dat.daos.IDAO;
import dat.entities.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SessionDAO implements IDAO<Session> {
    private static SessionDAO instance;
    private static EntityManagerFactory emf;
    
    public static SessionDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SessionDAO();
        }
        return instance;
    }
    
    private SessionDAO() {}
```

2. **Implement IDAO methods:**
   - `create(Session session)` - Store a new session
   - `getById(Long id)` - Find session by ID
   - `getAll()` - Get all sessions
   - `update(Session session)` - Update session
   - `delete(Long id)` - Delete session
   - `findByName(String name)` - Not used (return empty)

3. **Add custom business methods:**
```java
public Optional<Session> findByToken(String token)
public Set<Session> getByCustomerId(Long customerId)
public void deactivateSession(Long sessionId)
```

**Pattern to Follow:** Look at `SmsBalanceDAO.java` or `CustomerDAO.java` - they're already implemented!

---

### **Step 2: Implement ActivityLogDAO** (1 hour)

**File:** `backend/src/main/java/dat/daos/impl/ActivityLogDAO.java`

Currently has TODOs. Implement:

1. **All IDAO methods:**
   - `create(ActivityLog log)` ← **Most Important for logging**
   - `getById(Long id)`
   - `getAll()`
   - `update(ActivityLog log)`
   - `delete(Long id)`
   - `findByName(String name)` - Return empty

2. **Custom business methods (already stubbed):**
   - `getByCustomerId(Long customerId)` - Get logs for a specific customer
   - `getByType(String type)` - Filter by LOGIN, LOGOUT, PAYMENT, etc.

**Pattern:** Use the same pattern as other DAOs - look at `ProductDAO.java`

---

### **Step 3: Add Session Storage to Login** (30 mins)

**File:** `backend/src/main/java/dat/security/controllers/SecurityController.java`

In the `login()` method, after successful authentication:
```java
// After: String token = createToken(verifiedUser);

// CREATE SESSION
Session session = new Session();
session.setCustomer(customer);  // Get customer from email
session.setToken(token);
session.setCreatedAt(OffsetDateTime.now());
session.setExpiresAt(OffsetDateTime.now().plusDays(7));
session.setActive(true);
sessionDAO.create(session);

// RETURN RESPONSE
ctx.status(200).json(returnObject
    .put("token", token)
    .put("sessionId", session.getId())  // ← NEW
    .put("email", verifiedUser.getEmail()));
```

---

### **Step 4: Add Activity Logging to Login** (30 mins)

**File:** `backend/src/main/java/dat/security/controllers/SecurityController.java`

In login success:
```java
// After session created
ActivityLog log = new ActivityLog();
log.setCustomer(customer);
log.setType(ActivityLogType.LOGIN);
log.setStatus(ActivityLogStatus.SUCCESS);
log.setMetadata("IP: " + ctx.ip());
log.setTimestamp(OffsetDateTime.now());
activityLogDAO.create(log);
```

In login failure:
```java
catch (ValidationException e) {
    ActivityLog log = new ActivityLog();
    log.setCustomer(customer);  // if customer found
    log.setType(ActivityLogType.LOGIN);
    log.setStatus(ActivityLogStatus.FAILURE);
    log.setMetadata("Reason: " + e.getMessage());
    log.setTimestamp(OffsetDateTime.now());
    activityLogDAO.create(log);
    
    // ... send error response
}
```

---

### **Step 5: Implement Logout Endpoint** (1 hour)

**File:** `backend/src/main/java/dat/security/controllers/SecurityController.java`

Add new method:
```java
@Override
public Handler logout() {
    return (ctx) -> {
        try {
            // Get token from header
            String authHeader = ctx.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).json("No valid token");
                return;
            }
            String token = authHeader.substring(7);
            
            // Find and deactivate session
            Optional<Session> session = sessionDAO.findByToken(token);
            if (session.isPresent()) {
                session.get().setActive(false);
                sessionDAO.update(session.get());
            }
            
            // Log logout activity
            UserDTO user = ctx.attribute("user");
            Optional<Customer> customer = customerDAO.getByUserEmail(user.getEmail());
            if (customer.isPresent()) {
                ActivityLog log = new ActivityLog();
                log.setCustomer(customer.get());
                log.setType(ActivityLogType.LOGOUT);
                log.setStatus(ActivityLogStatus.SUCCESS);
                log.setTimestamp(OffsetDateTime.now());
                activityLogDAO.create(log);
            }
            
            ctx.status(200).json("Logged out successfully");
            
        } catch (Exception e) {
            ctx.status(500).json("Logout failed: " + e.getMessage());
        }
    };
}
```

---

### **Step 6: Add Logout Route** (5 mins)

**File:** `backend/src/main/java/dat/security/routes/SecurityRoutes.java`

In `getSecurityRoutes()` method:
```java
path("/auth", () -> {
    get("/test", ctx -> ctx.json(...), Role.ANYONE);
    post("/login", securityController.login(), Role.ANYONE);
    post("/register", securityController.register(), Role.ANYONE);
    post("/logout", securityController.logout(), Role.USER);  // ← ADD THIS
    post("/user/addrole", securityController.addRole(), Role.USER);
});
```

---

### **Step 7: Update ISecurityController Interface** (5 mins)

**File:** `backend/src/main/java/dat/security/controllers/ISecurityController.java`

Add:
```java
Handler logout();
```

---

## 📋 **Actual Checklist for Next Session**

### **Priority 1 - MUST DO:**
- [ ] Implement `SessionDAO` completely
- [ ] Implement `ActivityLogDAO` completely
- [ ] Add session creation to `login()` method
- [ ] Add activity logging to `login()` method
- [ ] Implement `logout()` handler
- [ ] Add logout route
- [ ] Update interface

### **Priority 2 - NICE TO HAVE:**
- [ ] Implement `PaymentDAO` (for future payments)
- [ ] Implement `ReceiptDAO` (for future receipts)

---

## 🧪 **Test After Implementation**

1. **Registration** → Should work ✅
2. **Login** → Should create session in database
3. **Check database:** 
   ```sql
   SELECT * FROM sessions;
   SELECT * FROM activity_log;
   ```
4. **Logout** → Should deactivate session
5. **Try using old token** → Should fail

---

## 📊 **Remaining Work for Sprint 1**

| Task | Status | Time Est. | Difficulty |
|------|--------|-----------|------------|
| SessionDAO | PENDING | 1-2 hrs | Medium |
| ActivityLogDAO | PENDING | 1 hr | Medium |
| Add to login() | PENDING | 30 min | Easy |
| Logout endpoint | PENDING | 1 hr | Medium |
| Routes update | PENDING | 5 min | Easy |
| **TOTAL** | **5 tasks** | **3.5-4.5 hrs** | **Medium** |

---

## 💡 **Key Pattern for DAOs**

Look at these as templates:
- `SmsBalanceDAO.java` - ✅ Complete implementation
- `ProductDAO.java` - ✅ Complete implementation
- `CustomerDAO.java` - ✅ Complete implementation

All follow the same pattern:
1. Singleton getInstance()
2. IDAO interface methods
3. Custom business methods
4. Try-with-resources for EntityManager

---

**This is the REAL next work! All other DAOs can wait until after Sprint 1.** 🎯


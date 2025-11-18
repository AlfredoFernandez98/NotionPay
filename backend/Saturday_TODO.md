# 🚀 Next Steps - Sprint 1 Continuation

**Last Session Completed:** Input Validation & Email/Serial Matching ✅  
**Next Session Focus:** Session Storage & Activity Logging  
**Estimated Time:** 2-3 hours

---

## 📋 What to Do Next

### **Priority 1: Test Current Implementation** (15-30 mins)
Before continuing, verify everything works:

1. Start backend: `mvn exec:java -Dexec.mainClass="dat.Main"`
2. Open `auth-test.http` in IntelliJ
3. Run tests 1-3 (should all succeed with 201 Created)
4. Run tests 4-10 (should all fail with proper error messages)
5. **Expected:** ✅ Tests 1-3 work, ❌ Tests 4-10 fail correctly

---

### **Priority 2: Task #25 - Session Storage** (1-2 hours)

**Goal:** Store user sessions in database when they register/login

#### **Step 1: Implement SessionDAO Methods**
File: `backend/src/main/java/dat/daos/impl/SessionDAO.java`

Implement these methods:
```java
// Create a new session
public Session create(Session session)

// Find session by token
public Optional<Session> findByToken(String token)

// Deactivate session (set is_active = false)
public void deactivate(String token)

// Get all active sessions for a customer
public Set<Session> getActiveSessionsByCustomerId(Long customerId)
```

#### **Step 2: Update Login Method**
File: `backend/src/main/java/dat/security/controllers/SecurityController.java`

In the `login()` method, after successful authentication:
```java
// Create session
Session session = new Session(user, token, OffsetDateTime.now().plusDays(7));
sessionDAO.create(session);

// Return session ID to client
ctx.json(returnObject
    .put("token", token)
    .put("sessionId", session.getId())  // ← ADD THIS
    .put("email", user.getEmail()));
```

#### **Step 3: Verify Database**
Check that sessions are stored:
```sql
SELECT * FROM sessions;
```

---

### **Priority 3: Task #26 - Activity Logging** (1 hour)

**Goal:** Log all login attempts for security tracking

#### **Step 1: Implement ActivityLogDAO Methods**
File: `backend/src/main/java/dat/daos/impl/ActivityLogDAO.java`

Implement:
```java
public ActivityLog create(ActivityLog log)

public Set<ActivityLog> getByCustomerId(Long customerId)

public Set<ActivityLog> getByType(ActivityLogType type)
```

#### **Step 2: Add Login Logging**
In `SecurityController.login()`:
```java
// After successful login
ActivityLog log = new ActivityLog(
    customer,
    ActivityLogType.LOGIN,
    ActivityLogStatus.SUCCESS,
    "User logged in from IP: " + ctx.ip()
);
activityLogDAO.create(log);
```

#### **Step 3: Add Failed Login Logging**
In login catch block:
```java
catch (ValidationException e) {
    ActivityLog log = new ActivityLog(
        customer,
        ActivityLogType.LOGIN,
        ActivityLogStatus.FAILURE,
        "Failed login attempt: " + e.getMessage()
    );
    activityLogDAO.create(log);
    // ... send error response
}
```

---

### **Priority 4: Task #27-29 - Logout Feature** (1-2 hours)

**Goal:** Allow users to logout and invalidate their session

#### **Step 1: Create Logout Handler**
File: `backend/src/main/java/dat/security/controllers/SecurityController.java`

Add new method:
```java
@Override
public Handler logout() {
    return (ctx) -> {
        try {
            // Get token from Authorization header
            String token = ctx.header("Authorization").split(" ")[1];
            
            // Deactivate session
            sessionDAO.deactivate(token);
            
            // Log logout activity
            UserDTO user = ctx.attribute("user");
            Customer customer = customerDAO.getByUserEmail(user.getEmail()).get();
            ActivityLog log = new ActivityLog(
                customer,
                ActivityLogType.LOGOUT,
                ActivityLogStatus.SUCCESS,
                "User logged out"
            );
            activityLogDAO.create(log);
            
            ctx.status(200).json("Logged out successfully");
            
        } catch (Exception e) {
            ctx.status(500).json("Logout failed: " + e.getMessage());
        }
    };
}
```

#### **Step 2: Add Logout Route**
File: `backend/src/main/java/dat/security/routes/SecurityRoutes.java`

Add to routes:
```java
post("/logout", securityController.logout(), Role.USER);
```

#### **Step 3: Update ISecurityController**
Add logout method to interface:
```java
Handler logout();
```

---

## 📝 Testing Checklist

### Session Storage Tests
- [ ] User registers → Session created in database
- [ ] User login → Session created with token
- [ ] Session has correct expiration time (7 days)
- [ ] Multiple logins create multiple sessions

### Activity Logging Tests
- [ ] Successful login → ActivityLog created with SUCCESS status
- [ ] Failed login → ActivityLog created with FAILURE status
- [ ] Activity log shows correct timestamp
- [ ] Activity log shows correct user

### Logout Tests
- [ ] Logout with valid token → 200 OK
- [ ] Logout without token → 401 Unauthorized
- [ ] Session marked as inactive after logout
- [ ] Cannot use deactivated token for protected routes
- [ ] Logout activity logged

---

## 🛠️ Quick Command Reference

```bash
# Start backend
mvn exec:java -Dexec.mainClass="dat.Main"

# Compile only
mvn compile -DskipTests

# Full build
mvn clean install -DskipTests

# Database check (if psql installed)
psql -U [user] -d [database]
SELECT * FROM sessions;
SELECT * FROM activity_log;
```

---

## 📊 Sprint 1 Remaining

| Task | Status | Time Est. |
|------|--------|-----------|
| ✅ #18 - Input Validation | DONE | - |
| ✅ Email + Serial Match | DONE | - |
| ⏳ #25 - Session Storage | PENDING | 1-2 hrs |
| ⏳ #26 - Activity Logging | PENDING | 1 hr |
| ⏳ #27-29 - Logout | PENDING | 1-2 hrs |
| **TOTAL REMAINING** | **3 tasks** | **3-5 hrs** |

---

## 💡 Tips for Next Session

1. **Start with SessionDAO** - It's the foundation for the other two tasks
2. **Test after each step** - Don't code all three at once
3. **Reuse patterns** - Follow the same structure as login/register
4. **Use the test file** - Add new tests to `auth-test.http` as you go
5. **Check database** - Verify data is actually saved

---

## 🎯 Success Criteria

By end of next session, you should be able to:
- ✅ Register a user with email + serial validation
- ✅ Login and receive a session token
- ✅ See activity logs for login attempts
- ✅ Logout and invalidate session
- ✅ All with proper error handling

---

**See you next time! You're doing great! 🚀**


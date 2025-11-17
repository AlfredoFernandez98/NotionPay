# Sprint 1 TODO - NotionPay Backend
**Sprint Duration:** Nov 10 - Nov 24, 2025  
**Goal:** Complete User Registration, Login, and Logout functionality

---

## ✅ What's Already Implemented

### User Story 1: Opret bruger (Create User)
- ✅ #17 Backend endpoint `POST /api/auth/register` exists
- ✅ #19 Password hashing with BCrypt (in `User` entity constructor)
- ✅ #20 Save user to database (via `SecurityDAO.createUser()`)
- ✅ #21 Return JSON response with JWT token

### User Story 2: Logge ind (Login)
- ✅ #22 Login endpoint `POST /api/auth/login` exists
- ✅ #23 Email/password verification (via `SecurityDAO.getVerifiedUser()`)
- ✅ #24 JWT token generation (in `SecurityController.createToken()`)

---

## 🚧 What Needs to Be Built

### Priority 1: Critical Missing Features

#### Task #16: Create Database Migrations for User
**Status:** ❌ Not Started  
**Priority:** HIGH  
**Description:** Database schema should be created automatically via Hibernate, but we need to verify.

**Action Items:**
- [ ] Check if `User`, `Role`, `Customer`, `SerialLink` tables are created
- [ ] Verify foreign key relationships
- [ ] Test with fresh database
- [ ] Document database schema

**Files to Check:**
- `backend/src/main/java/dat/security/entities/User.java`
- `backend/src/main/java/dat/security/entities/Role.java`
- `backend/src/main/resources/config.properties`

---

#### Task #18: Improve Input Validation
**Status:** ⚠️ Partially Done  
**Priority:** HIGH  
**Description:** Add comprehensive validation for registration and login

**Action Items:**
- [ ] Validate email format (use regex or library)
- [ ] Validate password strength (min 8 chars, at least 1 number)
- [ ] Validate company name (not empty, max length)
- [ ] Validate serial number format
- [ ] Add proper error messages for each validation
- [ ] Return 400 Bad Request for validation errors

**Files to Modify:**
- `backend/src/main/java/dat/security/controllers/SecurityController.java`
- Create: `backend/src/main/java/dat/utils/ValidationUtil.java`

**Example Code:**
```java
// In SecurityController.register()
if (!ValidationUtil.isValidEmail(registerRequest.email)) {
    ctx.status(400);
    ctx.json(returnObject.put("msg", "Invalid email format"));
    return;
}

if (!ValidationUtil.isStrongPassword(registerRequest.password)) {
    ctx.status(400);
    ctx.json(returnObject.put("msg", "Password must be at least 8 characters with 1 number"));
    return;
}
```

---

### Priority 2: Session Management

#### Task #25: Store SESSION in Database
**Status:** ❌ Not Started  
**Priority:** MEDIUM  
**Description:** Store user sessions in database for tracking and logout

**Why?** Currently using JWT tokens (stateless), but for better control we should track sessions.

**Action Items:**
- [ ] Implement `SessionDAO.createSession()` method
- [ ] Create session when user logs in
- [ ] Store: user_id, token, created_at, expires_at, is_active
- [ ] Return session_id with login response

**Files to Modify:**
- `backend/src/main/java/dat/daos/impl/SessionDAO.java` (already exists, needs implementation)
- `backend/src/main/java/dat/security/controllers/SecurityController.java` (update login method)

**Example Code:**
```java
// In login() method, after creating token:
Session session = new Session(user, token, OffsetDateTime.now().plusDays(7));
sessionDAO.create(session);

ctx.json(returnObject
    .put("token", token)
    .put("sessionId", session.getId())
    .put("email", user.getEmail()));
```

---

### Priority 3: Activity Logging

#### Task #26: Add ACTIVITY_LOG for Login
**Status:** ❌ Not Started  
**Priority:** MEDIUM  
**Description:** Log all login attempts for security tracking

**Action Items:**
- [ ] Implement `ActivityLogDAO.create()` method
- [ ] Log successful logins (type=LOGIN, status=SUCCESS)
- [ ] Log failed logins (type=LOGIN, status=FAILED)
- [ ] Store: customer_id, type, status, timestamp, metadata (IP, user agent)

**Files to Modify:**
- `backend/src/main/java/dat/daos/impl/ActivityLogDAO.java` (exists, needs implementation)
- `backend/src/main/java/dat/security/controllers/SecurityController.java`

**Example Code:**
```java
// In login() after successful authentication:
ActivityLog log = new ActivityLog(
    customer,
    ActivityLogType.LOGIN,
    ActivityLogStatus.SUCCESS,
    "User logged in from IP: " + ctx.ip()
);
activityLogDAO.create(log);
```

---

### Priority 4: Logout Functionality

#### Task #27: Implement Logout Endpoint
**Status:** ❌ Not Started  
**Priority:** HIGH  
**Description:** Create `POST /api/auth/logout` endpoint

**Action Items:**
- [ ] Add `logout()` method to `SecurityController`
- [ ] Get session from token
- [ ] Deactivate session (#28)
- [ ] Log activity (#29)
- [ ] Return success response

**Files to Modify:**
- `backend/src/main/java/dat/security/controllers/SecurityController.java`
- `backend/src/main/java/dat/security/controllers/ISecurityController.java`
- `backend/src/main/java/dat/security/routes/SecurityRoutes.java`

**Example Code:**
```java
@Override
public Handler logout() {
    return (ctx) -> {
        try {
            // Get token from header
            String token = ctx.header("Authorization").split(" ")[1];
            
            // Find and deactivate session (#28)
            Optional<Session> session = sessionDAO.findByToken(token);
            if (session.isPresent()) {
                session.get().setActive(false);
                sessionDAO.update(session.get());
            }
            
            // Log logout activity (#29)
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

**Add to SecurityRoutes.java:**
```java
post("/logout", securityController.logout(), Role.USER);
```

---

#### Task #28: Deactivate User's SESSION
**Status:** ❌ Not Started  
**Priority:** HIGH  
**Description:** Mark session as inactive in database

**Action Items:**
- [ ] Add `findByToken()` method to `SessionDAO`
- [ ] Update session `is_active = false`
- [ ] Set `ended_at = NOW()`

**Files to Modify:**
- `backend/src/main/java/dat/daos/impl/SessionDAO.java`

---

#### Task #29: Log ACTIVITY_LOG for Logout
**Status:** ❌ Not Started  
**Priority:** MEDIUM  
**Description:** Track all logout events

**Action Items:**
- [ ] Create activity log entry (type=LOGOUT, status=SUCCESS)
- [ ] Store metadata (timestamp, IP address)

**Files:** Same as #26

---

## 📋 Implementation Order (Recommended)

### Week 1 (Nov 10-17)
1. ✅ Test current registration/login (make sure it works)
2. 🚧 **Task #18: Input Validation** (2-3 hours)
3. 🚧 **Task #16: Verify Database Schema** (1 hour)
4. 🚧 **Task #25: Implement Session Storage** (3-4 hours)

### Week 2 (Nov 18-24)
5. 🚧 **Task #26: Activity Logging for Login** (2 hours)
6. 🚧 **Task #27, #28, #29: Logout Implementation** (3-4 hours)
7. 🧪 **Testing & Bug Fixes** (2-3 hours)
8. 📝 **Documentation** (1 hour)

---

## 🧪 Testing Checklist

### User Story 1: Registration
- [ ] Register with valid data → Success
- [ ] Register with invalid email → 400 Bad Request
- [ ] Register with weak password → 400 Bad Request
- [ ] Register with used serial number → 403 Forbidden
- [ ] Register with duplicate email → 422 Unprocessable

### User Story 2: Login
- [ ] Login with correct credentials → 200 OK + token
- [ ] Login with wrong password → 401 Unauthorized
- [ ] Login with non-existent email → 401 Unauthorized
- [ ] Verify activity log created → Check database

### User Story 3: Logout
- [ ] Logout with valid token → 200 OK
- [ ] Logout without token → 401 Unauthorized
- [ ] Verify session deactivated → Check database
- [ ] Verify activity log created → Check database
- [ ] Try using deactivated token → 401 Unauthorized

---

## 📁 Files That Need Work

### To Implement:
```
backend/src/main/java/dat/
├── daos/impl/
│   ├── SessionDAO.java          ← Implement CRUD methods
│   ├── ActivityLogDAO.java      ← Implement CRUD methods
│
├── security/controllers/
│   └── SecurityController.java  ← Add logout(), improve validation
│
├── security/routes/
│   └── SecurityRoutes.java      ← Add logout route
│
└── utils/
    └── ValidationUtil.java      ← CREATE NEW (for input validation)
```

### To Test:
```
backend/src/main/resources/http/
└── security.http                ← CREATE NEW (test all auth endpoints)
```

---

## 📊 Sprint Progress Tracking

| User Story | Tasks | Status | Completion |
|------------|-------|--------|------------|
| US1: Opret bruger | 5 tasks | 4/5 done | 80% |
| US2: Logge ind | 3 tasks | 2.5/3 done | 83% |
| US3: Logge ud | 3 tasks | 0/3 done | 0% |
| **TOTAL** | **11 tasks** | **6.5/11** | **59%** |

---

## 🎯 Sprint Goal

By Nov 24, users should be able to:
1. ✅ Register an account with serial number validation
2. ✅ Login and receive JWT token
3. ⚠️ Have all actions logged for security tracking
4. ❌ Logout and invalidate their session

---

## 💡 Quick Start

**To continue working on Sprint 1:**

1. **Start with Input Validation (Task #18):**
   ```bash
   # Create ValidationUtil.java
   # Add validation methods
   # Update SecurityController to use validations
   ```

2. **Then Implement Sessions (Task #25):**
   ```bash
   # Open SessionDAO.java
   # Implement create(), findByToken(), update()
   # Update login to store session
   ```

3. **Finally Add Logout (Tasks #27-29):**
   ```bash
   # Add logout() to SecurityController
   # Update routes
   # Test with Postman/HTTP file
   ```

---

**Last Updated:** November 14, 2025  
**Next Review:** November 17, 2025 (mid-sprint check-in)


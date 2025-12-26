# 🧪 NotionPay Login Testing Guide

## Quick Start

### 1. Start Backend
```bash
export JAVA_HOME=/Users/alfredofernandez/Library/Java/JavaVirtualMachines/openjdk-21.0.2/Contents/Home
cd /Users/alfredofernandez/Documents/HovedetOpgave/NotionPay/backend
mvn compile && mvn exec:java -Dexec.mainClass="dat.Main"
```

Wait for: `Server started successfully. API available at: http://localhost:7070/api`

### 2. Start Frontend
```bash
cd /Users/alfredofernandez/Documents/HovedetOpgave/NotionPay/frontend
npm run dev
```

Wait for: `➜  Local:   http://localhost:3001/`

## 🎯 Test Scenarios

### Scenario 1: Register New User (Backend API)

```bash
# Register Diana
curl -X POST http://localhost:7070/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "diana@company-d.com",
    "password": "MySecurePass123",
    "companyName": "Company D Services",
    "serialNumber": 202020202
  }'
```

**Expected:** 
- Status: 201 Created
- Response includes: token, email, customerId, subscriptionId, planName

### Scenario 2: Login with Correct Password (Backend API)

```bash
# Login Diana
curl -X POST http://localhost:7070/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "diana@company-d.com",
    "password": "MySecurePass123"
  }'
```

**Expected:**
- Status: 200 OK
- Response: `{"token":"eyJ...","email":"diana@company-d.com","sessionID":1}`

### Scenario 3: Login with Wrong Password (Backend API)

```bash
# Try wrong password
curl -X POST http://localhost:7070/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "diana@company-d.com",
    "password": "WrongPassword"
  }'
```

**Expected:**
- Status: 401 Unauthorized
- Response: `{"msg":"Wrong password"}`

### Scenario 4: Frontend Login Flow

1. **Open Browser:** Navigate to `http://localhost:3001/login`

2. **Register a User First** (if signup page exists):
   - Go to signup page
   - Use one of the available serial numbers:
     - Email: `test@company-a.com`
     - Password: `TestPass123` (min 8 chars)
     - Company Name: `Test Company A`
     - Serial Number: `101010101`

3. **Login:**
   - Go to: `http://localhost:3001/login`
   - Enter credentials
   - Click "Sign In"

**Expected:**
- Console shows: `✅ Login successful! Redirecting to dashboard...`
- Redirected to: `http://localhost:3001/dashboard`
- Token stored in localStorage

4. **Test Wrong Password:**
   - Go to login page
   - Enter correct email, wrong password
   - Click "Sign In"

**Expected:**
- Error message displayed: "Invalid email or password"
- Stays on login page
- Console shows: `❌ Login error:`

### Scenario 5: Validate Token

```bash
# First, get token from login
TOKEN="<paste_your_token_here>"

# Validate it
curl -X POST http://localhost:7070/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

**Expected:**
- Status: 200 OK
- Response: `{"msg":"Token is valid","email":"...","sessionID":1,"expiresAt":"..."}`

### Scenario 6: Logout

```bash
# Logout
curl -X POST http://localhost:7070/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

**Expected:**
- Status: 200 OK
- Response: `{"msg":"Logged out successfully"}`

## 🔍 Debugging

### Check Backend Logs
```bash
tail -f /tmp/backend_output.log
```

### Check Frontend Logs
```bash
tail -f /tmp/frontend_output.log
```

### Browser Console
Open browser DevTools (F12) and check:
- **Console tab**: For JavaScript errors and log messages
- **Network tab**: For API requests and responses
- **Application tab > Local Storage**: For stored tokens

### Test API Connectivity
```bash
curl http://localhost:7070/api/auth/test
```

**Expected:** `{"msg":"Hello from Open"}`

## 🎨 Frontend Testing Checklist

- [ ] Can access login page
- [ ] Login form accepts email and password input
- [ ] Login button submits form
- [ ] Successful login redirects to dashboard
- [ ] Failed login shows error message
- [ ] Error message clears on new attempt
- [ ] Loading state shows during API call
- [ ] Token is stored in localStorage
- [ ] Can logout successfully
- [ ] Protected routes redirect to login when not authenticated

## 🔧 Common Issues & Solutions

### Issue: "Connection refused" in frontend
**Cause:** Backend not running  
**Solution:** Start backend first, wait for "Server started successfully"

### Issue: CORS error in browser
**Cause:** Backend CORS not configured  
**Solution:** Already fixed in ApplicationConfig.java - restart backend

### Issue: "Wrong password" for correct password
**Cause:** User registered before fix  
**Solution:** Restart backend (database recreates) and re-register

### Issue: Token not working
**Cause:** Token expired (30 min default)  
**Solution:** Login again to get new token

### Issue: Frontend shows "Network Error"
**Cause:** Backend URL misconfigured  
**Solution:** Check apiFacade.js uses `http://localhost:7070/api`

## 📊 Expected Behavior Summary

| Action | Backend Response | Frontend Action |
|--------|------------------|-----------------|
| Register (valid) | 201 + token | Store token, redirect to dashboard |
| Register (invalid serial) | 403 Forbidden | Show error message |
| Login (correct) | 200 + token + sessionID | Store token, redirect to dashboard |
| Login (wrong password) | 401 Unauthorized | Show error message |
| Login (no user) | 401 Unauthorized | Show error message |
| Logout | 200 OK | Clear token, redirect to home |
| Validate (valid token) | 200 OK | Continue |
| Validate (invalid token) | 401 Unauthorized | Clear token, redirect to login |

## 🎓 Testing Tips

1. **Always start backend first** - Frontend depends on it
2. **Use different browsers** - Test in Chrome, Firefox, Safari
3. **Check browser console** - Errors will show there
4. **Clear localStorage** - If testing gets confused, clear it
5. **Restart services** - When in doubt, restart both backend and frontend

## 📝 Test Data

**Available Serial Numbers:**
- `101010101` - Basic Monthly (100 SMS)
- `404040404` - Professional Monthly (500 SMS)
- `505050505` - Enterprise Yearly (1000 SMS)
- `202020202` - Basic Monthly (100 SMS)
- `999999999` - Basic Monthly (100 SMS)

**Password Requirements:**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number

**Example Credentials:**
```
Email: test@example.com
Password: TestPass123
Company: Test Company
Serial: 101010101
```

---

Happy Testing! 🚀

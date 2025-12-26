# Login Authentication Test Guide

## 🔍 How to Test Login is Working Correctly

### Step 1: Open Browser DevTools
1. Open your browser (Chrome/Firefox)
2. Go to: `http://localhost:3001/login`
3. Press **F12** or **Cmd+Option+I** (Mac) to open DevTools
4. Go to **Console** tab

### Step 2: Clear Any Existing Session
In the Console, paste this:
```javascript
localStorage.clear();
location.reload();
```

### Step 3: Try Wrong Password
1. Email: `alice@company-a.com`
2. Password: `wrongpassword123`
3. Click "Sign In"

**Expected Behavior:**
- ❌ Should see error message: "Invalid email or password"
- ❌ Should NOT redirect to dashboard
- Console should show: `❌ Login error:` with details
- Should stay on login page

### Step 4: Try Correct Password
1. Email: `alice@company-a.com`  
2. Password: Use the correct password you registered with
3. Click "Sign In"

**Expected Behavior:**
- ✅ Console shows: `✅ Login successful! Redirecting to dashboard...`
- ✅ Redirects to `/dashboard`
- ✅ Dashboard loads with your data

---

## 🐛 If Login Still Bypasses Backend

### Check 1: Verify Backend is Running
```bash
curl -X POST http://localhost:7070/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"wrong@test.com","password":"wrong"}'
```

**Should return:**
```json
{"msg":"No user found with email: wrong@test.com"}
```

### Check 2: Check Frontend .env File
File: `frontend/.env`
Should contain:
```
VITE_API_URL=http://localhost:7070/api
```

If missing, create it and restart frontend:
```bash
cd frontend
echo "VITE_API_URL=http://localhost:7070/api" > .env
npm run dev
```

### Check 3: Check Browser Console Logs
When you try to login, you should see:
```
=== LOGIN ATTEMPT ===
Email: alice@company-a.com
Backend URL: http://localhost:7070/api
```

If Backend URL is wrong, your .env file is not being read.

### Check 4: Verify localStorage is Not Pre-filled
In browser console:
```javascript
console.log('Token:', localStorage.getItem('jwtToken'));
console.log('User:', localStorage.getItem('notionpay_user'));
```

If you see old tokens, clear them:
```javascript
localStorage.clear();
```

---

## 📊 Test Checklist

- [ ] Backend running on port 7070
- [ ] Frontend running (usually port 3001)
- [ ] `.env` file exists with correct API URL
- [ ] localStorage cleared
- [ ] Wrong password shows error
- [ ] Wrong password does NOT redirect to dashboard
- [ ] Correct password redirects to dashboard
- [ ] Console shows correct logs

---

## 🔐 Test Users

From backend mock data:

| Email | Serial Number | Plan |
|-------|---------------|------|
| alice@company-a.com | 101010101 | Basic |
| bob@company-b.com | 202020202 | Pro |
| charlie@company-c.com | 303030303 | Enterprise |

**Note:** You need to register these users first via the signup page!

---

## 🚨 Common Issues

### Issue: "Can login with any password"
**Cause:** Old session in localStorage or frontend not connecting to backend

**Fix:**
1. Clear localStorage: `localStorage.clear()`
2. Check console for "Backend URL" - should be `http://localhost:7070/api`
3. Verify backend is running: `curl http://localhost:7070/api/auth/test`

### Issue: "Error but still redirects to dashboard"
**Cause:** Old auth data in localStorage

**Fix:**
```javascript
localStorage.removeItem('jwtToken');
localStorage.removeItem('notionpay_token');
localStorage.removeItem('notionpay_user');
location.reload();
```

### Issue: "CORS error"
**Cause:** Backend not allowing frontend requests

**Fix:** Backend should have CORS enabled (already done)

---

**Last Updated:** December 26, 2025

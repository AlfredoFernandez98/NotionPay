# Security Best Practices - NotionPay

## Password Security

### UserDTO Password Handling

The `UserDTO` class has been hardened with the following security measures:

#### 1. **Password Field Protection**
```java
@ToString.Exclude        // Excluded from toString() output
@EqualsAndHashCode.Exclude  // Not used in comparisons
@JsonIgnore              // Never serialized to JSON responses
private String password;
```

#### 2. **Custom toString() Implementation**
The password is replaced with `[PROTECTED]` in any toString() output:
```java
@Override
public String toString() {
    return "UserDTO{" +
            "email='" + email + '\'' +
            ", roles=" + roles +
            ", password=[PROTECTED]" +
            '}';
}
```

#### 3. **equals() and hashCode()**
Password is explicitly excluded from equality checks - users are compared by email only.

---

## Security Guidelines

### ✅ DO:

1. **Use the no-password constructor when returning user data:**
   ```java
   UserDTO safeUser = new UserDTO(user.getEmail(), user.getRoles());
   ```

2. **Clear passwords from memory after use** (where critical):
   ```java
   // For extra security in critical sections:
   String password = userDTO.getPassword();
   // ... use password ...
   password = null;
   ```

3. **Always hash passwords before storage:**
   ```java
   this.password = BCrypt.hashpw(userPass, BCrypt.gensalt());
   ```

4. **Use HTTPS in production** to encrypt passwords in transit

5. **Validate password requirements:**
   - Minimum 8 characters
   - Mix of letters, numbers, and special characters
   - Check against common password lists

### ❌ DON'T:

1. **Never log UserDTO objects that might contain passwords:**
   ```java
   // BAD - even though protected, avoid this pattern
   logger.info("Incoming user: " + userDTO);
   
   // GOOD - log only non-sensitive fields
   logger.info("Login attempt for: " + userDTO.getEmail());
   ```

2. **Never return UserDTO with passwords in API responses:**
   ```java
   // BAD
   return userDTO;
   
   // GOOD
   return new UserDTO(email, roles); // No password
   ```

3. **Never store passwords in plain text** - always hash them

4. **Never include passwords in error messages or exceptions**

5. **Don't serialize UserDTO objects with passwords to external systems**

---

## JWT Token Security

### Token Configuration

Ensure these properties are set securely in `config.properties`:

```properties
SECRET_KEY=your-256-bit-secret-key-min-32-chars
TOKEN_EXPIRE_TIME=3600000
ISSUER=NotionPay
DB_NAME=your_db_name
```

### Production Environment Variables

Set these in your production environment (never commit to git):

```bash
export SECRET_KEY="production-secret-key-change-this-to-a-long-random-string"
export TOKEN_EXPIRE_TIME="3600000"  # 1 hour in milliseconds
export ISSUER="NotionPay"
export DEPLOYED="true"
export DB_USERNAME="your_db_user"
export DB_PASSWORD="your_db_password"
export CONNECTION_STR="jdbc:postgresql://your-host:5432/"
export DB_NAME="production_db"
```

### Token Best Practices

1. **Keep tokens short-lived** (1-24 hours)
2. **Use refresh tokens** for longer sessions (implement if needed)
3. **Rotate SECRET_KEY** periodically in production
4. **Use a strong SECRET_KEY** (minimum 256 bits / 32 characters)
5. **Validate tokens on every protected request**
6. **Check token expiration** before processing requests

---

## Database Security

### User Entity

The `User` entity stores hashed passwords using BCrypt:

```java
public User(String email, String userPass) {
    this.email = email;
    this.password = BCrypt.hashpw(userPass, BCrypt.gensalt());
}
```

### Database Best Practices

1. **Use strong database passwords**
2. **Limit database user permissions** to only what's needed
3. **Enable SSL/TLS** for database connections in production
4. **Regular backups** with encryption
5. **Never expose database credentials** in logs or error messages

---

## API Security Checklist

- [x] Passwords excluded from DTO toString()
- [x] Passwords excluded from JSON serialization (@JsonIgnore)
- [x] Passwords hashed with BCrypt before storage
- [x] JWT tokens with expiration
- [x] Token signature verification
- [x] Role-based access control (RBAC)
- [x] Email-based authentication (not username)
- [ ] Rate limiting (implement if needed)
- [ ] CORS configuration for production
- [ ] HTTPS enforcement in production
- [ ] Input validation and sanitization
- [ ] SQL injection prevention (using JPA/Hibernate)
- [ ] XSS protection
- [ ] CSRF protection (if using session-based auth alongside JWT)

---

## Future Security Enhancements

Consider implementing:

1. **Password Strength Validation** - Reject weak passwords at registration
2. **Account Lockout** - Lock accounts after N failed login attempts
3. **Password Reset Flow** - Secure password reset with email verification
4. **Two-Factor Authentication (2FA)** - Extra security layer
5. **Refresh Tokens** - Separate short-lived access tokens and long-lived refresh tokens
6. **Audit Logging** - Log all authentication events
7. **Rate Limiting** - Prevent brute force attacks
8. **IP Whitelisting/Blacklisting** - For admin endpoints
9. **Security Headers** - Add security headers to HTTP responses
10. **Penetration Testing** - Regular security audits

---

## Incident Response

If a security breach is suspected:

1. **Immediately rotate** all SECRET_KEYs
2. **Invalidate all active tokens** (implement token blacklist if needed)
3. **Force password reset** for all affected users
4. **Review logs** for suspicious activity
5. **Notify affected users** if personal data was compromised
6. **Patch vulnerabilities** identified
7. **Document the incident** and lessons learned

---

## Contact

For security concerns or to report vulnerabilities, contact the development team immediately.

**Last Updated:** November 2025


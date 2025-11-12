# 🏗️ NotionPay Architecture Guide
## Understanding Your Application - A Complete Refresher

*"Like a master craftsman remembering their tools and techniques"*

---

## 📚 Table of Contents

1. [The Big Picture - What Are We Building?](#1-the-big-picture)
2. [The Foundation - Database & JPA](#2-the-foundation)
3. [The Layers - How Code is Organized](#3-the-layers)
4. [The Flow - Request to Response](#4-the-flow)
5. [The Patterns - Why We Do It This Way](#5-the-patterns)
6. [Real Example - User Registration](#6-real-example)
7. [Quick Reference](#7-quick-reference)

---

## 1. The Big Picture

### **What Are We Building?**

NotionPay is a **subscription billing platform** for a SaaS product. Think of it like a mini-Stripe.

```
Customer → Registers → Gets a Plan → Pays monthly/yearly → Uses the service
```

### **The Main Features:**

1. **User Registration** ✅ (DONE!)
   - Customer registers with email + serial number
   - System verifies serial number is valid
   - Creates user account + customer profile
   - Assigns them to a Plan (Basic/Pro/Enterprise)

2. **Subscription Management** (NEXT)
   - Track which plan customer has
   - Handle billing dates
   - Upgrade/downgrade/cancel

3. **Payment Processing** (FUTURE)
   - Store payment methods (credit cards)
   - Charge customers
   - Generate receipts

4. **Product Purchases** (FUTURE)
   - Buy SMS packages
   - Track SMS balance

---

## 2. The Foundation

### **What is a Database?**

Think of it as **Excel spreadsheets** that can talk to each other:

```
Users Table          Customers Table        Plans Table
+------------------+ +-------------------+  +-------------+
| email (PK)      | | id (PK)           |  | id (PK)     |
| password        | | user_email (FK)   |  | name        |
| roles           | | company_name      |  | price       |
+------------------+ | serial_number     |  | period      |
                     +-------------------+  +-------------+
```

**PK** = Primary Key (unique identifier)  
**FK** = Foreign Key (points to another table)

---

### **What is JPA/Hibernate?**

**The Problem:**
Writing SQL by hand is tedious and error-prone:
```sql
-- You DON'T want to write this everywhere:
INSERT INTO customers (user_email, company_name, serial_number, created_at) 
VALUES ('user@example.com', 'Acme Corp', 12345, NOW());
```

**The Solution: JPA (Java Persistence API)**
- Lets you work with **Java objects** instead of SQL
- Hibernate is the tool that makes it work

**Simple Analogy:**
```
You speak Danish → Google Translate → Computer understands English

You write Java objects → Hibernate → Database understands SQL
```

---

### **What is EntityManagerFactory?**

This is the **"connection pool"** to your database.

**Analogy:** Think of a restaurant kitchen:

```
Kitchen = Database
Chef = EntityManager (does the actual work)
Chef Manager = EntityManagerFactory (hires and manages chefs)
```

**Why do we need it?**
- Opening database connections is SLOW (like hiring a chef each time)
- EntityManagerFactory creates a POOL of connections (like having chefs ready to work)
- Each request gets a connection, uses it, returns it

**In your code:**
```java
// HibernateConfig.java creates ONE factory for entire app
private static EntityManagerFactory emf;

public static EntityManagerFactory getEntityManagerFactory() {
    if (emf == null)
        emf = createEMF(getTest());
    return emf;
}
```

**Key Rule:** 
- ✅ ONE EntityManagerFactory per application (singleton)
- ❌ DON'T close EntityManagerFactory (it's reused)
- ✅ DO close EntityManager after each operation

---

## 3. The Layers

Your application has **4 main layers**. Think of it like a restaurant:

```
┌─────────────────────────────────────────┐
│  CONTROLLER (Waiter)                    │  ← Takes orders from customers
│  "What does the customer want?"         │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  SERVICE (Head Chef)                    │  ← Business logic & decisions
│  "How do we make this dish?"            │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  DAO (Line Cook)                        │  ← Talks to database
│  "Get ingredients, store ingredients"   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  ENTITY (Ingredients)                   │  ← The actual data
│  "The raw materials"                    │
└─────────────────────────────────────────┘
```

Let me explain each layer in detail:

---

### **LAYER 1: ENTITY (The Data)**

**What is it?**
A Java class that represents a **row in a database table**.

**Example: Customer Entity**

```java
@Entity  // ← "Hey Hibernate, this is a database table!"
@Getter  // ← Lombok creates getters automatically
@Setter  // ← Lombok creates setters automatically
public class Customer {
    @Id  // ← Primary key
    @GeneratedValue  // ← Database auto-generates this
    private Long id;
    
    @OneToOne  // ← One Customer has ONE User
    @JoinColumn(name = "user_email")
    private User user;
    
    @Column(unique = true)  // ← This field must be unique
    private String companyName;
    
    private Integer serialNumber;
}
```

**Think of it as:**
```
Entity = Blueprint for a database row

Customer customer = new Customer();  // Creates object in memory
em.persist(customer);                // Saves it to database
```

**Common Annotations:**
- `@Entity` - "This class = database table"
- `@Id` - "This field = primary key"
- `@Column` - "This field = database column"
- `@OneToOne`, `@ManyToOne` - "This field connects to another table"

---

### **LAYER 2: DAO (Data Access Object)**

**What is it?**
The **only place** that talks to the database. Like a waiter who's the only one allowed in the kitchen.

**Why?**
- Keeps database code in ONE place
- If database changes, you only update DAOs
- Makes code testable

**Example: CustomerDAO**

```java
public class CustomerDAO {
    private EntityManagerFactory emf;  // ← Connection to database
    
    // Get customer by ID
    public Optional<Customer> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Customer customer = em.find(Customer.class, id);
            return Optional.ofNullable(customer);
        }
    }
    
    // Save new customer
    public Customer create(Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();      // ← Start transaction
            em.persist(customer);              // ← Save to database
            em.getTransaction().commit();      // ← Commit transaction
            return customer;
        }
    }
}
```

**Key Concepts:**

**1. Try-with-resources:**
```java
try (EntityManager em = emf.createEntityManager()) {
    // Do work
}  // ← EntityManager automatically closes here!
```

**2. Transactions:**
```java
em.getTransaction().begin();    // ← "Start recording changes"
em.persist(customer);           // ← "Add this change"
em.getTransaction().commit();   // ← "Save all changes to database"
```

Think of it like **Git**:
- `begin()` = git add
- `persist()` = stage changes
- `commit()` = git commit

**3. JPQL (Java Persistence Query Language):**
```java
// Instead of SQL:
"SELECT * FROM customers WHERE email = 'user@example.com'"

// You write JPQL (looks like SQL but uses Java classes):
"SELECT c FROM Customer c WHERE c.user.email = :email"
```

---

### **LAYER 3: SERVICE (Business Logic)**

**What is it?**
Contains **business rules** and **orchestrates** multiple DAOs.

**Example: SerialLinkVerificationService**

```java
public class SerialLinkVerificationService {
    private EntityManagerFactory emf;
    
    // Business rule: "Serial must be PENDING to be valid"
    public boolean verifySerialNumber(Integer serialNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            SerialLink link = em.createQuery(
                "SELECT s FROM SerialLink s WHERE s.serialNumber = :num AND s.status = :status",
                SerialLink.class
            )
            .setParameter("num", serialNumber)
            .setParameter("status", Status.PENDING)
            .getSingleResult();
            
            // Business logic: Also check if customer is null
            return link.getCustomer() == null;
        } catch (NoResultException e) {
            return false;
        }
    }
}
```

**When to use Service vs DAO?**

```java
// DAO: Simple database operations
customerDAO.getById(1L);
customerDAO.create(customer);

// SERVICE: Complex business logic involving multiple steps
// Example: Registration involves:
// 1. Verify serial number (SerialLinkService)
// 2. Create user (SecurityDAO)
// 3. Create customer (CustomerDAO)
// 4. Link serial to customer (SerialLinkService)
```

---

### **LAYER 4: CONTROLLER (The API)**

**What is it?**
Handles **HTTP requests** from the frontend (or Postman, or mobile app).

**Example: SecurityController**

```java
@Override
public Handler register() {
    return (ctx) -> {  // ← ctx = "context" = incoming HTTP request
        
        // 1. Parse JSON from request body
        RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);
        
        // 2. Business logic (call service)
        boolean valid = serialLinkService.verifySerialNumber(req.serialNumber);
        if (!valid) {
            ctx.status(403);  // ← HTTP status code
            ctx.json(returnObject.put("msg", "Invalid serial"));
            return;
        }
        
        // 3. Save to database (call DAO)
        User user = securityDAO.createUser(req.email, req.password);
        Customer customer = customerDAO.createCustomer(user, req.companyName, req.serialNumber);
        
        // 4. Return JSON response
        ctx.status(201);  // ← 201 = Created
        ctx.json(returnObject
            .put("token", token)
            .put("email", user.getEmail()));
    };
}
```

**Key Concepts:**

**1. HTTP Methods:**
```
GET    /api/customers/123     ← Read data
POST   /api/customers          ← Create new data
PUT    /api/customers/123     ← Update data
DELETE /api/customers/123     ← Delete data
```

**2. HTTP Status Codes:**
```
200 OK                  ← Success
201 Created             ← Successfully created resource
400 Bad Request         ← Client sent invalid data
401 Unauthorized        ← Not logged in
403 Forbidden           ← Logged in but not allowed
404 Not Found           ← Resource doesn't exist
422 Unprocessable       ← Data validation failed
500 Internal Error      ← Server crashed
```

**3. Context (ctx):**
```java
ctx.bodyAsClass(RegisterRequest.class);  // ← Parse JSON from request
ctx.header("Authorization");              // ← Get header value
ctx.status(201);                          // ← Set response status
ctx.json(object);                         // ← Return JSON response
```

---

## 4. The Flow

Let's trace a **complete registration request**:

### **The Journey of a Request:**

```
1. Frontend sends POST request
   ↓
2. Javalin receives it
   ↓
3. Routes.java directs to SecurityController.register()
   ↓
4. Controller parses JSON into RegisterRequest DTO
   ↓
5. Controller calls SerialLinkVerificationService
   ↓
6. Service queries database via EntityManager
   ↓
7. Service returns true/false
   ↓
8. Controller calls SecurityDAO.createUser()
   ↓
9. DAO saves User entity to database
   ↓
10. Controller calls CustomerDAO.createCustomer()
    ↓
11. DAO saves Customer entity to database
    ↓
12. Controller creates JWT token
    ↓
13. Controller returns JSON response
    ↓
14. Javalin sends HTTP response to frontend
```

### **Visual Flow:**

```
┌──────────────┐
│   FRONTEND   │  "POST /api/users/register"
│  (Browser)   │  { email, password, serialNumber }
└──────┬───────┘
       │
       ▼
┌──────────────────────────────────────────────┐
│           JAVALIN (Web Server)               │
└──────┬───────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────┐
│        CONTROLLER (SecurityController)       │
│  - Parse request                             │
│  - Validate input                            │
│  - Call services                             │
└──────┬───────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────┐
│     SERVICE (SerialLinkVerificationService)  │
│  - Business logic                            │
│  - Verify serial is valid                    │
└──────┬───────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────┐
│          DAO (SecurityDAO, CustomerDAO)      │
│  - Talk to database                          │
│  - Save User                                 │
│  - Save Customer                             │
└──────┬───────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────┐
│         DATABASE (PostgreSQL)                │
│  - Store data permanently                    │
└──────────────────────────────────────────────┘
```

---

## 5. The Patterns

### **Pattern 1: Singleton**

**Problem:** Creating multiple DAO instances wastes memory and connections.

**Solution:** Singleton ensures ONE instance per DAO.

```java
public class CustomerDAO {
    private static CustomerDAO instance;  // ← ONE instance for whole app
    private static EntityManagerFactory emf;
    
    // Private constructor - can't create with "new"
    private CustomerDAO() {}
    
    // Get the ONE instance
    public static CustomerDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new CustomerDAO();
        }
        return instance;
    }
}
```

**Usage:**
```java
// First time: creates instance
CustomerDAO dao1 = CustomerDAO.getInstance(emf);

// Second time: returns SAME instance
CustomerDAO dao2 = CustomerDAO.getInstance(emf);

// dao1 == dao2  ← TRUE!
```

---

### **Pattern 2: DTO (Data Transfer Object)**

**Problem:** Entities have TOO MUCH information (passwords, internal IDs, relationships).

**Solution:** DTOs are **simple objects** with only data you want to send/receive.

```java
// ❌ DON'T send Entity to frontend
public class User {
    private String email;
    private String password;  // ← DANGEROUS! Never expose password!
    private Set<Role> roles;  // ← Too much detail
}

// ✅ DO send DTO to frontend
public class UserDTO {
    public String email;
    public Set<String> roles;  // ← Just role names
    // No password!
}
```

**Usage:**
```java
// Controller receives DTO from frontend
RegisterRequest dto = ctx.bodyAsClass(RegisterRequest.class);

// Controller converts Entity → DTO before sending to frontend
UserDTO userDto = new UserDTO(user.getEmail(), user.getRoles());
ctx.json(userDto);
```

---

### **Pattern 3: Try-with-Resources**

**Problem:** Forgetting to close resources causes memory leaks.

**Solution:** Try-with-resources **automatically closes** resources.

```java
// ❌ Old way (manual close)
EntityManager em = emf.createEntityManager();
try {
    // Do work
} finally {
    em.close();  // ← Easy to forget!
}

// ✅ New way (auto close)
try (EntityManager em = emf.createEntityManager()) {
    // Do work
}  // ← Automatically closes here!
```

---

### **Pattern 4: Optional**

**Problem:** `null` causes NullPointerException (the most common Java error).

**Solution:** `Optional<T>` makes it clear when something might not exist.

```java
// ❌ Old way
public Customer getById(Long id) {
    return em.find(Customer.class, id);  // ← Might return null!
}

Customer customer = dao.getById(999L);
customer.getName();  // ← CRASH! NullPointerException

// ✅ New way
public Optional<Customer> getById(Long id) {
    Customer customer = em.find(Customer.class, id);
    return Optional.ofNullable(customer);
}

Optional<Customer> maybeCustomer = dao.getById(999L);
if (maybeCustomer.isPresent()) {
    Customer customer = maybeCustomer.get();
    customer.getName();  // ← Safe!
} else {
    // Handle not found
}

// Or use lambda:
maybeCustomer.ifPresent(customer -> {
    System.out.println(customer.getName());
});
```

---

## 6. Real Example - User Registration

Let's trace **YOUR actual registration code**:

### **Step-by-Step Breakdown:**

```java
@Override
public Handler register() {
    return (ctx) -> {
        ObjectNode returnObject = objectMapper.createObjectNode();
        
        try {
            // STEP 1: Parse incoming JSON
            RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);
            // Frontend sent: { email: "user@example.com", password: "test123", serialNumber: 101010101 }

            // STEP 2: Verify serial number is valid
            boolean isValid = serialLinkService.verifySerialNumber(registerRequest.serialNumber);
            if (!isValid) {
                ctx.status(HttpStatus.FORBIDDEN);
                ctx.json(returnObject.put("msg", "Invalid or already used serial number"));
                return;  // ← Stop here if invalid
            }

            // STEP 3: Get the Plan for this serial
            Plan eligiblePlan = serialLinkService.getPlanForSerialNumber(registerRequest.serialNumber);
            // Serial 101010101 → Basic Plan
            
            // STEP 4: Get full SerialLink entity
            SerialLink serialLink = serialLinkService.getSerialLink(registerRequest.serialNumber);
            
            // STEP 5: Create User (with hashed password)
            User user = securityDAO.createUser(registerRequest.email, registerRequest.password);
            // User constructor automatically hashes password with BCrypt!
            
            // STEP 6: Create Customer
            Customer customer = customerDAO.createCustomer(
                user,                         // ← Links to User
                registerRequest.companyName,  // ← "Acme Corp"
                registerRequest.serialNumber  // ← 101010101
            );
            
            // STEP 7: Link SerialLink to Customer (marks as VERIFIED)
            serialLinkService.linkCustomerToSerialLink(registerRequest.serialNumber, customer);
            // Updates: status = VERIFIED, customer_id = 1, verified_at = NOW()
            
            // STEP 8: Create JWT token for authentication
            String token = createToken(new UserDTO(user.getEmail(), Set.of("USER")));
            
            // STEP 9: Return success response
            ctx.status(HttpStatus.CREATED);  // ← 201 Created
            ctx.json(objectMapper.createObjectNode()
                    .put("token", token)
                    .put("email", user.getEmail())
                    .put("customerId", customer.getId())
                    .put("planName", eligiblePlan.getName())
                    .put("msg", "Registration successful! You are subscribed to " + eligiblePlan.getName()));
                    
        } catch (EntityExistsException e) {
            // User with this email already exists
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);  // ← 422
            ctx.json(returnObject.put("msg", "User with this email already exists"));
            
        } catch (Exception e) {
            // Something went wrong
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);  // ← 500
            ctx.json(returnObject.put("msg", "Registration failed: " + e.getMessage()));
            logger.error("Registration error: ", e);
        }
    };
}
```

### **What Happens in the Database:**

**Before:**
```sql
-- SerialLink table
id | customer_id | serial_number | plan_id | status
1  | NULL        | 101010101     | 2       | PENDING

-- User table: empty
-- Customer table: empty
```

**After successful registration:**
```sql
-- User table
email              | password (hashed)                    | roles
user@example.com   | $2a$10$xyzabc...                       | ["USER"]

-- Customer table
id | user_email         | company_name | serial_number | created_at
1  | user@example.com   | Acme Corp    | 101010101     | 2025-11-12 10:30:00

-- SerialLink table (updated)
id | customer_id | serial_number | plan_id | status   | verified_at
1  | 1           | 101010101     | 2       | VERIFIED | 2025-11-12 10:30:00
```

---

## 7. Quick Reference

### **Common EntityManager Operations:**

```java
// CREATE (INSERT)
em.getTransaction().begin();
em.persist(entity);
em.getTransaction().commit();

// READ (SELECT by ID)
Customer customer = em.find(Customer.class, 1L);

// READ (SELECT with query)
List<Customer> customers = em.createQuery(
    "SELECT c FROM Customer c WHERE c.companyName = :name", 
    Customer.class
)
.setParameter("name", "Acme Corp")
.getResultList();

// UPDATE
em.getTransaction().begin();
customer.setCompanyName("New Name");
em.merge(customer);
em.getTransaction().commit();

// DELETE
em.getTransaction().begin();
em.remove(customer);
em.getTransaction().commit();
```

---

### **HTTP Request/Response Cycle:**

```java
// Parse request
MyDTO dto = ctx.bodyAsClass(MyDTO.class);

// Get query parameter
String name = ctx.queryParam("name");

// Get path parameter
Long id = Long.parseLong(ctx.pathParam("id"));

// Set response status
ctx.status(200);  // or HttpStatus.OK

// Return JSON
ctx.json(myObject);

// Return error
ctx.status(400);
ctx.json(Map.of("error", "Bad request"));
```

---

### **When to Use What:**

```
┌─────────────────────────────────────────────────────────┐
│  ENTITY:                                                │
│  - Represents database table                            │
│  - Has JPA annotations (@Entity, @Id, etc.)            │
│  - Never sent directly to frontend                      │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  DTO:                                                   │
│  - Simple data container                                │
│  - No business logic                                    │
│  - Safe to send to frontend                             │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  DAO:                                                   │
│  - CRUD operations (Create, Read, Update, Delete)       │
│  - Simple queries                                       │
│  - One DAO per Entity                                   │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  SERVICE:                                               │
│  - Complex business logic                               │
│  - Orchestrates multiple DAOs                           │
│  - Validates business rules                             │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  CONTROLLER:                                            │
│  - Handles HTTP requests                                │
│  - Calls Services/DAOs                                  │
│  - Returns HTTP responses                               │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 Key Takeaways

1. **EntityManagerFactory** = Connection pool (create once, use everywhere)
2. **EntityManager** = Database worker (create per operation, close after)
3. **Entity** = Java object ↔ Database row
4. **DAO** = Talks to database (CRUD operations)
5. **Service** = Business logic (orchestrates multiple DAOs)
6. **Controller** = HTTP handler (receives requests, returns responses)
7. **DTO** = Safe data transfer (no sensitive info)

---

## 🧪 Testing Your Understanding

Try to answer these questions:

1. **Why do we close EntityManager but not EntityManagerFactory?**
2. **What's the difference between `em.persist()` and `em.merge()`?**
3. **Why use DTO instead of sending Entity directly?**
4. **When should logic go in Service vs DAO?**
5. **What happens if you forget `@Transactional` or begin/commit?**

*(Answers in your head - review sections above if unsure!)*

---

**Take your time with this guide. Read it section by section. Try to connect it with YOUR code.** 

**When you're ready, we'll move to the next step!** 🚀


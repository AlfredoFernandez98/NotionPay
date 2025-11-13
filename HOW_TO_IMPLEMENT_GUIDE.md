# 🛠️ How to Implement - Step by Step Guide

*Learn by doing! This guide shows you how to implement your own features.*

---

## 📚 Table of Contents

1. [Understanding the Pattern](#1-understanding-the-pattern)
2. [Example: Creating a Customer (ALREADY DONE)](#2-example-creating-a-customer)
3. [Your Turn: Implement Payment Creation](#3-your-turn-implement-payment-creation)
4. [Common Patterns Cheat Sheet](#4-common-patterns-cheat-sheet)
5. [Testing Your Implementation](#5-testing-your-implementation)

---

## 1. Understanding the Pattern

### **Every feature follows this flow:**

```
1. Frontend sends HTTP request
   ↓
2. Controller receives request
   ↓
3. Controller validates input
   ↓
4. Controller calls Service (if complex logic)
   OR
   Controller calls DAO directly (if simple CRUD)
   ↓
5. DAO talks to database
   ↓
6. Controller returns HTTP response
```

### **The Files You'll Touch:**

```
For a "Create Customer" feature:

1. CustomerDTO.java       ← Data transfer (what comes from frontend)
2. Customer.java          ← Entity (what goes to database)
3. CustomerDAO.java       ← Database operations
4. CustomerController.java ← HTTP handler
5. Routes.java            ← Register endpoint
```

---

## 2. Example: Creating a Customer (ALREADY DONE)

Let me show you how `Customer` creation works. **This is already implemented** - use it as your reference!

### **Step 1: The Entity (Customer.java)**

```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_email", referencedColumnName = "email")
    private User user;
    
    @Column(unique = true, nullable = false)
    private String companyName;
    
    @Column(unique = true, nullable = false)
    private Integer serialNumber;
    
    // Constructors, getters, setters
}
```

**Key Points:**
- `@Entity` - Tells Hibernate this is a database table
- `@Id` + `@GeneratedValue` - Auto-incrementing primary key
- `@OneToOne` - Relationship to User entity
- `@Column` - Constraints (unique, nullable, etc.)

---

### **Step 2: The DTO (CustomerDTO.java)**

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String userEmail;
    private String companyName;
    private Integer serialNumber;
    private String externalCustomerId;
    private OffsetDateTime createdAt;
}
```

**Why DTO?**
- Safe to send to frontend (no sensitive data)
- Flat structure (easier for JSON)
- Can combine multiple entities

---

### **Step 3: The DAO (CustomerDAO.java)**

Look at your `CustomerDAO.java` - here's the pattern:

```java
public class CustomerDAO implements IDAO<Customer> {
    private static CustomerDAO instance;
    private static EntityManagerFactory emf;
    
    // SINGLETON PATTERN
    public static CustomerDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new CustomerDAO();
        }
        return instance;
    }
    
    // CREATE METHOD
    @Override
    public Customer create(Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(customer);
            em.getTransaction().commit();
            return customer;
        }
    }
    
    // CUSTOM BUSINESS METHOD
    public Customer createCustomer(User user, String companyName, Integer serialNumber) {
        Customer customer = new Customer(user, companyName, serialNumber);
        return create(customer);
    }
}
```

**Pattern Breakdown:**

1. **Singleton:** Only one DAO instance per application
2. **Try-with-resources:** `EntityManager` auto-closes
3. **Transaction:** `begin()` → `persist()` → `commit()`
4. **Custom methods:** Add business logic on top of CRUD

---

### **Step 4: The Controller (CustomerController.java)**

**CHALLENGE FOR YOU:** Your `CustomerController.java` is empty! Let's implement it together.

Here's the pattern (based on `SecurityController.register()`):

```java
public class CustomerController implements IController {
    private final CustomerDAO customerDAO;
    
    public CustomerController(EntityManagerFactory emf) {
        this.customerDAO = CustomerDAO.getInstance(emf);
    }
    
    /**
     * Create customer endpoint: POST /api/customers
     */
    public Handler createCustomer() {
        return (ctx) -> {
            try {
                // 1. Parse incoming JSON to DTO
                CustomerDTO dto = ctx.bodyAsClass(CustomerDTO.class);
                
                // 2. Validate input
                if (dto.getCompanyName() == null || dto.getCompanyName().isEmpty()) {
                    ctx.status(400); // Bad Request
                    ctx.json(Map.of("error", "Company name is required"));
                    return;
                }
                
                // 3. Get User entity (customer must be linked to a user)
                // TODO: You need to query User first!
                User user = ...; // Get from SecurityDAO or UserDAO
                
                // 4. Create Customer via DAO
                Customer customer = customerDAO.createCustomer(
                    user,
                    dto.getCompanyName(),
                    dto.getSerialNumber()
                );
                
                // 5. Convert Entity → DTO for response
                CustomerDTO responseDto = new CustomerDTO(
                    customer.getId(),
                    customer.getUser().getEmail(),
                    customer.getCompanyName(),
                    customer.getSerialNumber(),
                    customer.getExternalCustomerId(),
                    customer.getCreatedAt()
                );
                
                // 6. Return success response
                ctx.status(201); // Created
                ctx.json(responseDto);
                
            } catch (Exception e) {
                ctx.status(500); // Internal Server Error
                ctx.json(Map.of("error", e.getMessage()));
            }
        };
    }
}
```

**Pattern Breakdown:**

1. **Parse:** `ctx.bodyAsClass(DTO.class)` - JSON → Java object
2. **Validate:** Check required fields, format, etc.
3. **Business Logic:** Get related entities, call DAO
4. **Convert:** Entity → DTO (safe for frontend)
5. **Respond:** Set status code, return JSON

---

### **Step 5: Register Route (Routes.java)**

Add your endpoint to `Routes.java`:

```java
public class Routes {
    public static void routes(Javalin app, EntityManagerFactory emf) {
        CustomerController customerController = new CustomerController(emf);
        
        // Customer routes
        app.post("/api/customers", customerController.createCustomer());
        app.get("/api/customers/{id}", customerController.getCustomer());
        app.get("/api/customers", customerController.getAllCustomers());
    }
}
```

---

## 3. Your Turn: Implement Payment Creation

Now **YOU** implement the Payment creation feature! I've created skeleton classes for you.

### **🎯 Challenge: Create a Payment**

**Acceptance Criteria:**
- Endpoint: `POST /api/payments`
- Request body: `{ "customerId": 1, "amount": 99.00, "currency": "DKK" }`
- Response: `201 Created` with payment details

---

### **Step-by-Step Instructions:**

#### **Step 1: Implement PaymentDAO.create()**

Open `PaymentDAO.java` and implement the `create()` method:

```java
@Override
public Payment create(Payment payment) {
    try (EntityManager em = emf.createEntityManager()) {
        // 1. Start transaction
        em.getTransaction().begin();
        
        // 2. Persist entity
        em.persist(payment);
        
        // 3. Commit transaction
        em.getTransaction().commit();
        
        // 4. Return saved entity (now has ID)
        return payment;
    }
}
```

**Test it:**
- Can you see the pattern from CustomerDAO?
- What does `persist()` do?
- Why do we need `begin()` and `commit()`?

---

#### **Step 2: Implement PaymentDAO.getById()**

```java
@Override
public Optional<Payment> getById(Long id) {
    try (EntityManager em = emf.createEntityManager()) {
        // Find entity by primary key
        Payment payment = em.find(Payment.class, id);
        
        // Return Optional (safe null handling)
        return Optional.ofNullable(payment);
    }
}
```

**Understanding Optional:**
```java
// Without Optional (old way)
Payment payment = dao.getById(1L);
if (payment == null) {  // Easy to forget!
    // Handle not found
}

// With Optional (new way)
Optional<Payment> maybePayment = dao.getById(1L);
if (maybePayment.isPresent()) {
    Payment payment = maybePayment.get();
    // Use payment
} else {
    // Handle not found
}
```

---

#### **Step 3: Implement PaymentDAO.getAll()**

```java
@Override
public Set<Payment> getAll() {
    try (EntityManager em = emf.createEntityManager()) {
        // Create JPQL query
        return em.createQuery("SELECT p FROM Payment p", Payment.class)
                .getResultList()
                .stream()
                .collect(Collectors.toSet());
    }
}
```

**Understanding JPQL:**
```java
// SQL (talks to database tables):
"SELECT * FROM payments"

// JPQL (talks to Java entities):
"SELECT p FROM Payment p"
//       ↑ alias   ↑ Entity class name (not table name!)
```

---

#### **Step 4: Implement Custom Method**

Add a custom method to get payments by customer:

```java
public Set<Payment> getByCustomerId(Long customerId) {
    try (EntityManager em = emf.createEntityManager()) {
        return em.createQuery(
            "SELECT p FROM Payment p WHERE p.subscription.customer.id = :customerId", 
            Payment.class
        )
        .setParameter("customerId", customerId)  // ← Prevents SQL injection!
        .getResultList()
        .stream()
        .collect(Collectors.toSet());
    }
}
```

**Understanding Relationships in JPQL:**
```java
// Payment → Subscription → Customer
"p.subscription.customer.id"
//    ↑ OneToOne    ↑ ManyToOne
```

---

#### **Step 5: Implement PaymentController.createPayment()**

Open `PaymentController.java`:

```java
public Handler createPayment() {
    return ctx -> {
        try {
            // 1. Parse DTO
            PaymentDTO dto = ctx.bodyAsClass(PaymentDTO.class);
            
            // 2. Validate
            if (dto.getAmount() == null || dto.getAmount() <= 0) {
                ctx.status(400);
                ctx.json(Map.of("error", "Amount must be positive"));
                return;
            }
            
            // 3. Get related entities
            // TODO: You need SubscriptionDAO to get subscription!
            Subscription subscription = ...;
            
            // 4. Create Payment entity
            Payment payment = new Payment();
            payment.setSubscription(subscription);
            payment.setAmount(dto.getAmount());
            payment.setCurrency(dto.getCurrency());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreatedAt(OffsetDateTime.now());
            
            // 5. Save via DAO
            Payment savedPayment = paymentDAO.create(payment);
            
            // 6. Convert to DTO and return
            PaymentDTO responseDto = new PaymentDTO();
            responseDto.setId(savedPayment.getId());
            responseDto.setAmount(savedPayment.getAmount());
            responseDto.setStatus(savedPayment.getStatus().toString());
            
            ctx.status(201);
            ctx.json(responseDto);
            
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("error", e.getMessage()));
        }
    };
}
```

---

#### **Step 6: Register Route**

Add to `Routes.java`:

```java
PaymentController paymentController = new PaymentController(emf);
app.post("/api/payments", paymentController.createPayment());
```

---

## 4. Common Patterns Cheat Sheet

### **Pattern 1: Creating an Entity**

```java
// DAO method
public Customer create(Customer customer) {
    try (EntityManager em = emf.createEntityManager()) {
        em.getTransaction().begin();
        em.persist(customer);        // INSERT INTO ...
        em.getTransaction().commit();
        return customer;
    }
}
```

---

### **Pattern 2: Getting by ID**

```java
public Optional<Customer> getById(Long id) {
    try (EntityManager em = emf.createEntityManager()) {
        Customer customer = em.find(Customer.class, id);
        return Optional.ofNullable(customer);
    }
}
```

---

### **Pattern 3: Querying with JPQL**

```java
public Set<Customer> getByCompanyName(String companyName) {
    try (EntityManager em = emf.createEntityManager()) {
        return em.createQuery(
            "SELECT c FROM Customer c WHERE c.companyName = :name",
            Customer.class
        )
        .setParameter("name", companyName)  // Prevents SQL injection
        .getResultList()
        .stream()
        .collect(Collectors.toSet());
    }
}
```

---

### **Pattern 4: Updating an Entity**

```java
public Customer update(Customer customer) {
    try (EntityManager em = emf.createEntityManager()) {
        em.getTransaction().begin();
        Customer updated = em.merge(customer);  // UPDATE ...
        em.getTransaction().commit();
        return updated;
    }
}
```

**persist() vs merge():**
```java
persist(entity);  // INSERT - for new entities
merge(entity);    // UPDATE - for existing entities
```

---

### **Pattern 5: Deleting an Entity**

```java
public void delete(Long id) {
    try (EntityManager em = emf.createEntityManager()) {
        em.getTransaction().begin();
        Customer customer = em.find(Customer.class, id);
        if (customer != null) {
            em.remove(customer);  // DELETE FROM ...
        }
        em.getTransaction().commit();
    }
}
```

---

### **Pattern 6: Controller Handler**

```java
public Handler createEntity() {
    return ctx -> {
        try {
            // 1. Parse request
            MyDTO dto = ctx.bodyAsClass(MyDTO.class);
            
            // 2. Validate
            if (dto.getName() == null) {
                ctx.status(400);
                ctx.json(Map.of("error", "Name required"));
                return;
            }
            
            // 3. Business logic
            MyEntity entity = dao.create(dto.toEntity());
            
            // 4. Return response
            ctx.status(201);
            ctx.json(entity.toDTO());
            
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("error", e.getMessage()));
        }
    };
}
```

---

## 5. Testing Your Implementation

### **Manual Testing with HTTP Files**

Create `payment.http`:

```http
### Create Payment
POST http://localhost:7070/api/payments
Content-Type: application/json

{
  "subscriptionId": 1,
  "amount": 99.00,
  "currency": "DKK"
}

### Get Payment
GET http://localhost:7070/api/payments/1

### Get Customer Payments
GET http://localhost:7070/api/customers/1/payments
```

---

### **Testing with curl**

```bash
# Create payment
curl -X POST http://localhost:7070/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionId": 1,
    "amount": 99.00,
    "currency": "DKK"
  }'

# Get payment
curl http://localhost:7070/api/payments/1
```

---

### **Debugging Checklist**

When something doesn't work:

1. **Check logs** - Look for exceptions
2. **Verify database** - Did the data save?
3. **Test DAO separately** - Does the query work?
4. **Check JSON** - Is the request body valid?
5. **Verify relationships** - Are foreign keys correct?

---

## 🎓 Learning Exercises

### **Exercise 1: Complete PaymentDAO**
Implement all methods in `PaymentDAO.java`
- ✅ create()
- ✅ getById()
- ✅ getAll()
- ✅ update()
- ✅ delete()
- ✅ getByCustomerId()

### **Exercise 2: Complete PaymentController**
Implement all handlers in `PaymentController.java`
- ✅ createPayment()
- ✅ getPayment()
- ✅ getAllPayments()
- ✅ getCustomerPayments()

### **Exercise 3: Add Update Payment Endpoint**
- Create `updatePayment()` handler
- Support PATCH /api/payments/{id}
- Allow updating status only

### **Exercise 4: Add Receipt Generation**
- When payment is COMPLETED, create Receipt
- Use `ReceiptDAO` to save receipt
- Return receipt in payment response

---

## 💡 Pro Tips

1. **Start simple** - Implement basic CRUD first
2. **Copy patterns** - Look at working code (CustomerDAO, SecurityController)
3. **Test frequently** - Don't write too much before testing
4. **Use Optional** - Avoid NullPointerException
5. **Validate input** - Always check user data
6. **Handle errors** - Wrap in try-catch, return meaningful messages
7. **Log everything** - Use logger for debugging

---

## 🆘 Common Errors & Solutions

### **Error 1: "No EntityManager with actual transaction available"**
**Cause:** Forgot to call `begin()` before `persist()`/`merge()`

**Solution:**
```java
em.getTransaction().begin();  // ← Add this!
em.persist(entity);
em.getTransaction().commit();
```

---

### **Error 2: "Entity not found"**
**Cause:** Wrong ID or entity doesn't exist

**Solution:**
```java
Optional<Entity> maybe = dao.getById(id);
if (maybe.isEmpty()) {
    ctx.status(404);
    ctx.json(Map.of("error", "Entity not found"));
    return;
}
```

---

### **Error 3: "LazyInitializationException"**
**Cause:** Trying to access relationship outside transaction

**Solution:** Use fetch joins in JPQL:
```java
"SELECT c FROM Customer c JOIN FETCH c.user WHERE c.id = :id"
```

---

### **Error 4: "Cannot parse JSON"**
**Cause:** DTO field names don't match JSON keys

**Solution:** Check your DTO:
```java
// JSON: { "companyName": "Acme" }
// DTO must have:
private String companyName;  // ← Exact match (camelCase)
```

---

## 🚀 Next Steps

1. **Complete the skeleton classes** I created
2. **Follow the patterns** from CustomerDAO and SecurityController
3. **Test each feature** as you build it
4. **Ask questions** when you get stuck!

**Remember:** Programming is like learning a craft. You learn by **doing**, **failing**, and **trying again**! 💪

Good luck! 🎉


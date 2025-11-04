# 🚀 NotionPay - Quick Start Guide

## Prerequisites
- ✅ PostgreSQL running on `localhost:5432`
- ✅ Database: `doctor` with user `postgres`/`postgres`
- ✅ IntelliJ IDEA (recommended) or Java 17

---

## Run the Test Suite

### In IntelliJ IDEA ⭐ (Recommended)
1. Open project in IntelliJ
2. Right-click `src/main/java/dat/Main.java`
3. Click **"Run 'Main.main()'"**
4. Watch the tests pass! ✅

### From Terminal
```bash
# Make sure PostgreSQL is running first
mvn clean compile exec:java -Dexec.mainClass="dat.Main"
```

---

## What Gets Tested?

✅ **Hibernate 6.6.3** - Configuration & EntityManagerFactory  
✅ **PostgreSQL** - Database connection & CRUD operations  
✅ **Lombok 1.18.36** - @Getter, @Setter, @NoArgsConstructor, etc.  
✅ **Entity Relationships** - User ↔ Role Many-to-Many  
✅ **Password Hashing** - BCrypt integration  

**Optional:**  
✅ **Javalin 6.3.0** - Web server (uncomment in Main.java line 31)

---

## PostgreSQL Setup (if needed)

### Create Database
```sql
CREATE DATABASE doctor;
```

### Or use Docker
```bash
docker run --name postgres-notionpay \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=doctor \
  -p 5432:5432 \
  -d postgres:15
```

---

## Start the Web Server

1. Open `src/main/java/dat/Main.java`
2. Uncomment line 31:
   ```java
   testJavalinServer();
   ```
3. Run the application
4. Visit: http://localhost:7070/api/routes

---

## Project Structure

```
NotionPay/
├── src/main/java/dat/
│   ├── Main.java              ← Test suite (START HERE)
│   ├── config/
│   │   ├── HibernateConfig.java    ← Hibernate setup
│   │   └── ApplicationConfig.java  ← Javalin config
│   ├── security/
│   │   ├── entities/
│   │   │   ├── User.java          ← User entity (Lombok)
│   │   │   └── Role.java          ← Role entity
│   │   ├── controllers/           ← Auth controllers
│   │   └── routes/                ← Security routes
│   └── routes/
│       └── Routes.java            ← API routes
└── src/main/resources/
    └── config.properties          ← Configuration
```

---

## Useful Maven Commands

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Build JAR file
mvn clean package

# Run the application
mvn exec:java -Dexec.mainClass="dat.Main"

# Clean everything
mvn clean
```

---

## Database Configuration

**Development:** `src/main/resources/config.properties`
```properties
SECRET_KEY=4c9f92b04b1e85fa56e7b7b0a34f2de4f5b08cd9bb4dfe8ac4d73b4f7f6ef37b
ISSUER=Alfredo Fernandez
TOKEN_EXPIRE_TIME=1800000
DB_NAME=doctor
```

**Connection:** `jdbc:postgresql://localhost:5432/doctor`

---

## Common Issues & Solutions

### ❌ "Unable to create EntityManagerFactory"
**Solution:** Make sure PostgreSQL is running and the `doctor` database exists.

### ❌ "Port 7070 already in use"
**Solution:** Change the port in `Main.java` line 163 or stop the other application.

### ❌ "cannot find symbol: method getUsername()"
**Solution:** Make sure Lombok is installed in IntelliJ:
- Settings → Plugins → Search "Lombok" → Install
- Enable annotation processing: Settings → Build → Compiler → Annotation Processors → ✅ Enable

### ❌ Java version mismatch
**Solution:** IntelliJ manages this automatically. For command line, install Java 17:
```bash
brew install --cask corretto17
```

---

## Next Steps

1. ✅ Run the test suite to verify setup
2. 📖 Read `UPGRADE_NOTES.md` for detailed changes
3. 🔐 Review security setup in `security/` package
4. 🌐 Start building your API endpoints
5. 🧪 Write proper unit tests in `src/test/java/`

---

## Need Help?

- **Detailed Changes:** See `UPGRADE_NOTES.md`
- **Dependencies:** See `pom.xml`
- **Hibernate Config:** See `src/main/java/dat/config/HibernateConfig.java`

---

**Authors:**
- Alfredo M. Fernandez - cph-af201@stud.ek.dk
- Masih Bijan Kabiri - cph-mk330@stud.ek.dk

---

✨ **Everything is configured and ready to go!** ✨


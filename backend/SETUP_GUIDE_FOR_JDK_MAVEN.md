# NotionPay Backend Setup Guide

This guide will help you set up the development environment for the NotionPay backend on a new PC.

---

## Prerequisites Installation

### 1. Install Java Development Kit (JDK)

**Recommended: Java 21 LTS** (Long-Term Support, stable until 2029)

**Download:**
1. Go to: https://adoptium.net/temurin/releases/
2. Select:
   - **Version:** 21 - LTS (or 23 for newest)
   - **Operating System:** Windows
   - **Architecture:** x64
   - **Package Type:** JDK
3. Download the **`.msi` installer** (easier than .zip)

**Installation:**
1. Run the downloaded `.msi` file
2. **IMPORTANT:** During installation, check these boxes:
   - ✅ **Set JAVA_HOME variable**
   - ✅ **Add to PATH**
   - ✅ **JavaSoft (Oracle) registry keys**
3. Click through and complete the installation

**Verify Installation:**
```bash
java --version
```
You should see output like:
```
openjdk 21.x.x 2024-xx-xx LTS
OpenJDK Runtime Environment Temurin-21...
```

---

### 2. Install Apache Maven

**Download:**
1. Go to: https://maven.apache.org/download.cgi
2. Under **Files**, download: `apache-maven-3.9.11-bin.zip` (Binary zip archive)

**Installation:**
1. Extract the zip file to: `C:\Program Files\apache-maven-3.9.11`
2. Add Maven to your system PATH:
   - Open **Environment Variables** (Search in Windows Start Menu)
   - Under **System Variables** (or User Variables), find `Path`
   - Click **Edit** → **New**
   - Add: `C:\Program Files\apache-maven-3.9.11\bin`
   - Click **OK** to save

**Verify Installation:**
```bash
mvn --version
```
You should see output like:
```
Apache Maven 3.9.11
Maven home: C:\Program Files\apache-maven-3.9.11
Java version: 21.x.x, vendor: Eclipse Adoptium
```

---

## Project Setup

### 3. Clone the Repository

```bash
# Navigate to your workspace
cd C:\Users\[YourUsername]\Desktop\Hovedopg

# Clone the repository (if not already done)
git clone [your-repo-url]
cd NotionPay
```

---

### 4. Configure the Project (if using Java 21)

If you installed **Java 21 LTS** instead of Java 23, update the `pom.xml`:

**File:** `backend/pom.xml`

Change these lines:
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <!-- ... rest stays the same ... -->
</properties>
```

And in the compiler plugin:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <source>21</source>
        <target>21</target>
        <!-- ... rest stays the same ... -->
    </configuration>
</plugin>
```

---

### 5. Install Dependencies & Build

```bash
# Navigate to backend directory
cd backend

# Clean and install all Maven dependencies
mvn clean install -DskipTests

# Or just compile without tests
mvn compile
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXX s
```

---

## Running the Application

### Option A: Using Maven (Command Line)

```bash
cd backend
mvn exec:java -Dexec.mainClass="dat.Main"
```

### Option B: Using IntelliJ IDEA

1. **Open Project:**
   - `File` → `Open` → Select `backend` folder
   - Wait for IntelliJ to import Maven dependencies

2. **Configure JDK (if needed):**
   - `File` → `Project Structure` → `Project`
   - Set **SDK** to Java 21 (or 23)
   - Set **Language Level** to match

3. **Run Application:**
   - Find `Main.java` in `src/main/java/dat/Main.java`
   - Right-click → `Run 'Main'`

---

## Troubleshooting

### Issue: "java: command not found"
**Solution:** Java is not in your PATH. Restart your terminal/IDE after installation, or add Java manually to PATH:
- Add: `C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot\bin`

### Issue: "mvn: command not found"
**Solution:** Maven is not in your PATH. Add Maven's `bin` directory to PATH as described in Step 2.

### Issue: "JAVA_HOME is not set"
**Solution:** 
1. Open Environment Variables
2. Create new **System Variable**:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot`

### Issue: Compilation errors
**Solution:** Make sure your `pom.xml` Java version matches your installed JDK version.

---

## Quick Reference

| Component | Version | Location |
|-----------|---------|----------|
| Java JDK | 21 LTS or 23 | https://adoptium.net/temurin/releases/ |
| Maven | 3.9.11 | https://maven.apache.org/download.cgi |
| Project Config | Java 23 (current) | `backend/pom.xml` |

---

## Summary Checklist

- [ ] Java JDK installed (21 or 23)
- [ ] Maven installed (3.9.11)
- [ ] `java --version` works
- [ ] `mvn --version` works
- [ ] Repository cloned
- [ ] `mvn clean install -DskipTests` runs successfully
- [ ] Can run `Main.java` from IntelliJ or command line

---

**Last Updated:** November 14, 2025  
**Current Java Version:** 23  
**Current Maven Version:** 3.9.11


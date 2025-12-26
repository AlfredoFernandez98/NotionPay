# Fix Java Version Issue

## Problem
Your system is using Java 23, but we need Java 21 for Lombok compatibility.

## Solution

### Step 1: Install Java 21 (if not installed)
```bash
brew install openjdk@21
```

### Step 2: Find Java 21 path
```bash
/usr/libexec/java_home -V
```

This will show all installed Java versions. Look for Java 21.

### Step 3: Set Java 21 as active (Temporary - for this terminal session)
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Step 4: Verify it's working
```bash
java -version
# Should show: openjdk version "21.x.x"

mvn -version
# Should show: Java version: 21.x.x
```

### Step 5: Now compile the backend
```bash
cd /Users/alfredofernandez/Documents/HovedetOpgave/NotionPay/backend
mvn clean install
mvn exec:java -Dexec.mainClass="dat.Main"
```

---

## Alternative: Set Java 21 Permanently

Add this to your `~/.zshrc` file:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

Then reload your shell:
```bash
source ~/.zshrc
```

---

## Quick Test Commands

Run these in your terminal to fix immediately:

```bash
# 1. Check if Java 21 is installed
/usr/libexec/java_home -V

# 2. If Java 21 is there, set it
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 3. Verify
java -version

# 4. Go to backend and compile
cd ~/Documents/HovedetOpgave/NotionPay/backend
mvn clean install

# 5. Run backend
mvn exec:java -Dexec.mainClass="dat.Main"
```

---

## ✅ What Changed

Your code is **100% ready** - no code changes needed!

Only `pom.xml` was updated:
- ✅ Java 23 → Java 21
- ✅ Lombok 1.18.34 → 1.18.36
- ✅ Removed Java 23 compatibility flags
- ✅ Simplified compiler configuration

Everything else (all your Java code) remains exactly the same!

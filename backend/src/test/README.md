# Testing

## Manual Testing (Recommended)

Use the HTTP test file:
```
backend/src/main/resources/http/sprint2-test.http
```

### How to test:
1. Start server: `mvn clean compile exec:java`
2. Open `sprint2-test.http` in IntelliJ
3. Run tests one by one
4. Check responses

## Why no unit tests?
Unit tests require Docker for Testcontainers (test database).
For now, HTTP tests are simpler and faster.

## What to test:
- ✅ View plans
- ✅ Subscribe to plan
- ✅ Add payment card
- ✅ Buy SMS package
- ✅ Check SMS balance

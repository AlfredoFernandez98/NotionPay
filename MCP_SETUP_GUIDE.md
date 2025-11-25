# MCP Setup Guide for NotionPay PostgreSQL Database

## Overview
This guide helps you set up Model Context Protocol (MCP) to connect to your PostgreSQL database.

## Database Information
- **Database Name:** notionpay
- **Host:** localhost
- **Port:** 5432
- **Username:** postgres
- **Password:** postgres

## Setup Instructions

### Option 1: Cursor IDE Configuration

1. **Open Cursor Settings:**
   - Press `Ctrl+,` (or `Cmd+,` on Mac)
   - Search for "MCP" or "Model Context Protocol"

2. **Add MCP Server:**
   Add the following configuration to your Cursor MCP settings:

```json
{
  "mcpServers": {
    "postgres-notionpay": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-postgres",
        "postgresql://postgres:postgres@localhost:5432/notionpay"
      ],
      "env": {
        "PGDATABASE": "notionpay",
        "PGHOST": "localhost",
        "PGPORT": "5432",
        "PGUSER": "postgres",
        "PGPASSWORD": "postgres"
      }
    }
  }
}
```

3. **Restart Cursor:**
   - Close and reopen Cursor for changes to take effect

### Option 2: Manual Configuration File

If your Cursor version supports it, you can place the configuration in:
- **Windows:** `%APPDATA%\Cursor\User\mcp.json`
- **Mac:** `~/Library/Application Support/Cursor/User/mcp.json`
- **Linux:** `~/.config/Cursor/User/mcp.json`

Use the same JSON configuration as above.

### Option 3: Using claude_desktop_config.json

If you're using Claude Desktop app:

1. **Locate config file:**
   - **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
   - **Mac:** `~/Library/Application Support/Claude/claude_desktop_config.json`
   - **Linux:** `~/.config/Claude/claude_desktop_config.json`

2. **Add the MCP server configuration:**

```json
{
  "mcpServers": {
    "postgres-notionpay": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-postgres",
        "postgresql://postgres:postgres@localhost:5432/notionpay"
      ]
    }
  }
}
```

3. **Restart Claude Desktop**

## Verifying the Connection

Once configured, you should be able to:

1. Ask the AI to query your database:
   - "Show me all tables in the notionpay database"
   - "What customers do we have?"
   - "Show me the schema for the subscription table"

2. The AI can now:
   - View database schema
   - Query tables
   - Analyze data relationships
   - Help with SQL queries

## Database Schema Overview

Your NotionPay database includes these main entities:

### Security & Users
- `user` - User accounts
- `role` - User roles and permissions

### Core Business
- `customer` - Customer information
- `plan` - Subscription plans
- `subscription` - Customer subscriptions
- `seriallink` - Serial link verification

### Products
- `product` - Base product information
- `smsproduct` - SMS product details
- `smsbalance` - SMS balance tracking

### Payments
- `paymentmethod` - Payment methods
- `payment` - Payment transactions
- `receipt` - Payment receipts

### System
- `session` - User sessions
- `activitylog` - Activity logging

## Troubleshooting

### Connection Issues
1. **Ensure PostgreSQL is running:**
   ```powershell
   # Check if PostgreSQL service is running
   Get-Service -Name postgresql*
   ```

2. **Verify database exists:**
   ```powershell
   psql -U postgres -l
   ```

3. **Test connection:**
   ```powershell
   psql -U postgres -d notionpay
   ```

### MCP Not Working
1. Make sure Node.js is installed (required for npx)
2. Verify the MCP configuration path is correct
3. Check Cursor/Claude logs for errors
4. Restart the application after configuration changes

## Security Note

⚠️ **Important:** The current configuration uses default PostgreSQL credentials. For production:
1. Use strong passwords
2. Don't commit credentials to version control
3. Consider using environment variables
4. Implement proper database access controls

## Next Steps

After setup, you can:
- Ask AI to explain database structure
- Generate SQL queries
- Analyze data patterns
- Create reports
- Debug database issues

## Support

For issues with:
- **MCP Protocol:** https://github.com/modelcontextprotocol
- **PostgreSQL Server:** https://github.com/modelcontextprotocol/servers
- **Cursor IDE:** https://cursor.sh/docs




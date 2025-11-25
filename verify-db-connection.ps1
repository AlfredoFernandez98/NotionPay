# PostgreSQL Database Connection Verification Script
# For NotionPay Database

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "NotionPay DB Connection Verifier" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Check if PostgreSQL service is running
Write-Host "1. Checking PostgreSQL Service..." -ForegroundColor Yellow
$pgService = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue

if ($pgService) {
    if ($pgService.Status -eq "Running") {
        Write-Host "   ✓ PostgreSQL service is running" -ForegroundColor Green
    } else {
        Write-Host "   ✗ PostgreSQL service is NOT running" -ForegroundColor Red
        Write-Host "   Status: $($pgService.Status)" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "   ⚠ PostgreSQL service not found" -ForegroundColor Yellow
    Write-Host "   This might be okay if PostgreSQL is installed differently" -ForegroundColor Gray
}

Write-Host ""

# Check if psql command is available
Write-Host "2. Checking psql availability..." -ForegroundColor Yellow
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue

if ($psqlPath) {
    Write-Host "   ✓ psql command found at: $($psqlPath.Source)" -ForegroundColor Green
} else {
    Write-Host "   ✗ psql command not found in PATH" -ForegroundColor Red
    Write-Host "   You may need to add PostgreSQL bin directory to your PATH" -ForegroundColor Yellow
}

Write-Host ""

# Test database connection
Write-Host "3. Testing connection to 'notionpay' database..." -ForegroundColor Yellow
$env:PGPASSWORD = "postgres"

if ($psqlPath) {
    try {
        $result = & psql -U postgres -d notionpay -c "\dt" -t 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✓ Successfully connected to notionpay database" -ForegroundColor Green
            Write-Host ""
            Write-Host "Tables found:" -ForegroundColor Cyan
            & psql -U postgres -d notionpay -c "\dt"
        } else {
            Write-Host "   ✗ Failed to connect to database" -ForegroundColor Red
            Write-Host "   Error: $result" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "   ✗ Error connecting to database: $($_.Exception.Message)" -ForegroundColor Red
    }
}
else {
    Write-Host "   ⊗ Skipped (psql not available)" -ForegroundColor Gray
}

Write-Host ""

# Check if Node.js is installed (required for MCP)
Write-Host "4. Checking Node.js installation (required for MCP)..." -ForegroundColor Yellow
$nodePath = Get-Command node -ErrorAction SilentlyContinue

if ($nodePath) {
    $nodeVersion = & node --version
    Write-Host "   ✓ Node.js found: $nodeVersion" -ForegroundColor Green
} else {
    Write-Host "   ✗ Node.js not found" -ForegroundColor Red
    Write-Host "   Node.js is required for MCP. Install from: https://nodejs.org/" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Verification Complete!" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Review the MCP_SETUP_GUIDE.md file for setup instructions" -ForegroundColor White
Write-Host "2. Add the MCP configuration to Cursor settings" -ForegroundColor White
Write-Host "3. Restart Cursor to enable database access" -ForegroundColor White
Write-Host ""


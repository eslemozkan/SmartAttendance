# Test script for reset-password edge function
# PowerShell script to test if the edge function is deployed and working

$functionUrl = "https://oubvhffqbsxsnbtinzbl.functions.supabase.co/reset-password"
$testEmail = "test@example.com"  # Test email (change this to a real email in your system)

Write-Host "Testing reset-password edge function..." -ForegroundColor Cyan
Write-Host "URL: $functionUrl" -ForegroundColor Gray
Write-Host "Email: $testEmail" -ForegroundColor Gray
Write-Host ""

# Test 1: OPTIONS request (CORS preflight)
Write-Host "Test 1: CORS Preflight (OPTIONS)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $functionUrl -Method OPTIONS -ErrorAction Stop
    if ($response.StatusCode -eq 204) {
        Write-Host "✓ CORS preflight successful" -ForegroundColor Green
    } else {
        Write-Host "✗ CORS preflight failed: Status $($response.StatusCode)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ CORS preflight failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: POST request with email
Write-Host "Test 2: Password Reset Request (POST)..." -ForegroundColor Yellow
$body = @{
    email = $testEmail
} | ConvertTo-Json

$headers = @{
    "Content-Type" = "application/json"
    "apikey" = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
}

try {
    $response = Invoke-RestMethod -Uri $functionUrl -Method POST -Body $body -Headers $headers -ErrorAction Stop
    Write-Host "✓ Request successful!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10 | Write-Host
    
    if ($response.ok) {
        Write-Host "`n✓ Function is working correctly!" -ForegroundColor Green
        if ($response.resetLink) {
            Write-Host "Reset link generated: $($response.resetLink)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "`n✗ Function returned error: $($response.error)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Request failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body: $responseBody" -ForegroundColor Red
    }
    Write-Host "`nPossible issues:" -ForegroundColor Yellow
    Write-Host "1. Edge function is not deployed" -ForegroundColor Gray
    Write-Host "2. Service Role Key is not set as secret" -ForegroundColor Gray
    Write-Host "3. Function URL is incorrect" -ForegroundColor Gray
}

Write-Host "`n---" -ForegroundColor Gray
Write-Host "To check logs:" -ForegroundColor Cyan
Write-Host "supabase functions logs reset-password" -ForegroundColor White
Write-Host "Or check Dashboard > Edge Functions > reset-password > Logs" -ForegroundColor White



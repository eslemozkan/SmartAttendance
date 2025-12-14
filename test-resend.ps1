# Resend API Test Script
# Bu script edge function'ın çalışıp çalışmadığını test eder

$edgeFunctionUrl = "https://oubvhffqbsxsnbtinzbl.functions.supabase.co/reset-password"
$anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"

Write-Host "Testing Edge Function: reset-password" -ForegroundColor Cyan
Write-Host ""

# Test email (kayıtlı bir email kullanın)
$testEmail = Read-Host "Test email adresini girin (kayitli olmali)"

if ([string]::IsNullOrWhiteSpace($testEmail)) {
    Write-Host "Email adresi bos olamaz!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Sending request to: $edgeFunctionUrl" -ForegroundColor Yellow
Write-Host "Email: $testEmail" -ForegroundColor Yellow
Write-Host ""

try {
    $body = @{
        email = $testEmail
    } | ConvertTo-Json

    $headers = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $anonKey"
        "apikey" = $anonKey
    }

    $response = Invoke-RestMethod -Uri $edgeFunctionUrl -Method Post -Headers $headers -Body $body -ErrorAction Stop

    Write-Host "Response received!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10

    if ($response.ok -eq $true) {
        Write-Host ""
        Write-Host "SUCCESS! Password reset request processed." -ForegroundColor Green
        if ($response.emailSent -eq $true) {
            Write-Host "Email sent via Resend API!" -ForegroundColor Green
        } else {
            Write-Host "Email not sent (check Resend API key)" -ForegroundColor Yellow
        }
    } else {
        Write-Host ""
        Write-Host "FAILED: $($response.error)" -ForegroundColor Red
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Kontrol edin:" -ForegroundColor Yellow
    Write-Host "   1. Edge function deploy edildi mi?" -ForegroundColor Yellow
    Write-Host "   2. RESEND_API_KEY secret eklendi mi?" -ForegroundColor Yellow
    Write-Host "   3. Supabase Dashboard -> Edge Functions -> reset-password -> Logs" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Sonraki adimlar:" -ForegroundColor Cyan
Write-Host "   1. Email'inizi kontrol edin (spam klasorunu de!)" -ForegroundColor White
Write-Host "   2. Resend Dashboard'da kontrol edin: https://resend.com/emails" -ForegroundColor White
Write-Host "   3. Supabase Dashboard -> Edge Functions -> reset-password -> Logs" -ForegroundColor White


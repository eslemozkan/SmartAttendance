# Test Password Reset API
$headers = @{
    "apikey" = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
    "Content-Type" = "application/json"
}

# Email adresini buraya yaz
$email = "220541002@firat.edu.tr"

$body = @{
    email = $email
    redirect_to = "https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page"
} | ConvertTo-Json

Write-Host "=== Password Reset API Test ==="
Write-Host "Email: $email"
Write-Host "Redirect URL: https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page"
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/recover" -Method POST -Headers $headers -Body $body
    Write-Host "✅ Response Code: $($response.StatusCode)"
    Write-Host "Response Content: $($response.Content)"
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error Response: $responseBody"
    }
}







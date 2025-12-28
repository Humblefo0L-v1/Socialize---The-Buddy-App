Write-Host "==================================" -ForegroundColor Cyan
Write-Host "   COMPLETE SYSTEM TEST SUITE    " -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$testsPassed = 0
$testsFailed = 0

# Helper: wait for an HTTP endpoint to respond (status 200 or actuator health UP)
function Wait-For-Endpoint {
    param(
        [string]$Uri,
        [int]$TimeoutSeconds = 120,
        [int]$IntervalSeconds = 3
    )

    $endTime = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $endTime) {
        try {
            $resp = Invoke-RestMethod -Uri $Uri -Method GET -TimeoutSec 10 -ErrorAction Stop
            # If actuator health returns object with status UP, consider ready
            if ($resp -is [System.Collections.IDictionary] -and $resp.ContainsKey('status')) {
                if ($resp.status -eq 'UP') { return $true }
            } else {
                return $true
            }
        } catch {
            Start-Sleep -Seconds $IntervalSeconds
        }
    }
    return $false
}

# Wait for discovery and services to be ready before running tests
Write-Host "`nWaiting for services to become ready..." -ForegroundColor Cyan
$ready = $true
if (-not (Wait-For-Endpoint -Uri "http://localhost:8761/actuator/health" -TimeoutSeconds 120)) {
    Write-Host "Warning: discovery-service not ready on http://localhost:8761/actuator/health" -ForegroundColor Yellow
    $ready = $false
}
if (-not (Wait-For-Endpoint -Uri "http://localhost:8081/actuator/health" -TimeoutSeconds 120)) {
    Write-Host "Warning: user-service not ready on http://localhost:8081/actuator/health" -ForegroundColor Yellow
    $ready = $false
}
if (-not (Wait-For-Endpoint -Uri "$baseUrl/actuator/health" -TimeoutSeconds 120)) {
    Write-Host "Warning: api-gateway not ready on $baseUrl/actuator/health" -ForegroundColor Yellow
    $ready = $false
}

if (-not $ready) {
    Write-Host "Some services did not become ready in time — continuing tests may fail." -ForegroundColor Yellow
}

# Test 1: Gateway Health Check
Write-Host "`n[TEST 1] Gateway Health Check" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health"
    if ($health.status -eq "UP") {
        Write-Host "[PASSED] Gateway is healthy" -ForegroundColor Green
        $testsPassed++
    }
} catch {
    Write-Host "❌ FAILED: Gateway health check failed" -ForegroundColor Red
    $testsFailed++
}

# Test 2: Register User Through Gateway
Write-Host "`n[TEST 2] User Registration via Gateway" -ForegroundColor Yellow
$randomEmail = "test$(Get-Random -Minimum 1000 -Maximum 9999)@example.com"
$registerBody = @{
    email = $randomEmail
    password = "password123"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "1234567890"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $registerBody -ContentType "application/json"
    
    if ($registerResponse.success -eq $true) {
        Write-Host "[PASSED] User registered successfully" -ForegroundColor Green
        Write-Host "   User ID: $($registerResponse.data.userId)" -ForegroundColor Gray
        Write-Host "   Email: $($registerResponse.data.email)" -ForegroundColor Gray
        $token = $registerResponse.data.token
        $userId = $registerResponse.data.userId
        $testsPassed++
    } else {
        Write-Host "[FAILED] Registration returned success=false" -ForegroundColor Red
        $testsFailed++
        exit
    }
} catch {
    Write-Host "[FAILED] $_" -ForegroundColor Red
    $testsFailed++
    exit
}

# Test 3: Login Through Gateway
Write-Host "`n[TEST 3] User Login via Gateway" -ForegroundColor Yellow
$loginBody = @{
    email = $randomEmail
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    
    if ($loginResponse.success -eq $true) {
        Write-Host "[PASSED] Login successful" -ForegroundColor Green
        $testsPassed++
    } else {
        Write-Host "[FAILED] Login failed" -ForegroundColor Red
        $testsFailed++
    }
} catch {
    Write-Host "[FAILED] $_" -ForegroundColor Red
    $testsFailed++
}

# Test 4: Access Protected Endpoint WITHOUT Token (Should Fail)
Write-Host "`n[TEST 4] Access Protected Endpoint Without Token" -ForegroundColor Yellow
try {
    $unauthorized = Invoke-RestMethod -Uri "$baseUrl/api/users/me" -Method GET
    Write-Host "[FAILED] Should have been blocked!" -ForegroundColor Red
    $testsFailed++
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "[PASSED] Correctly blocked unauthorized request" -ForegroundColor Green
        $testsPassed++
    } else {
        Write-Host "[FAILED] Wrong error code" -ForegroundColor Red
        $testsFailed++
    }
}

# Test 5: Get User Profile WITH Token
Write-Host "`n[TEST 5] Get User Profile (Authenticated)" -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
}

try {
    $profile = Invoke-RestMethod -Uri "$baseUrl/api/users/me" -Method GET -Headers $headers
    
    if ($profile.success -eq $true -and $profile.data.id -eq $userId) {
        Write-Host "[PASSED] Profile retrieved successfully" -ForegroundColor Green
        Write-Host "   Name: $($profile.data.firstName) $($profile.data.lastName)" -ForegroundColor Gray
        $testsPassed++
    } else {
        Write-Host "[FAILED] Profile data incorrect" -ForegroundColor Red
        $testsFailed++
    }
} catch {
    Write-Host "[FAILED] $_" -ForegroundColor Red
    $testsFailed++
}

# Test 6: Update User Profile
Write-Host "`n[TEST 6] Update User Profile" -ForegroundColor Yellow
$updateBody = @{
    firstName = "Updated"
    lastName = "Name"
    bio = "This is my test bio"
    phoneNumber = "9876543210"
} | ConvertTo-Json

try {
    $updated = Invoke-RestMethod -Uri "$baseUrl/api/users/me" -Method PUT -Body $updateBody -ContentType "application/json" -Headers $headers
    
    if ($updated.data.firstName -eq "Updated" -and $updated.data.lastName -eq "Name") {
        Write-Host "[PASSED] Profile updated successfully" -ForegroundColor Green
        Write-Host "   New name: $($updated.data.firstName) $($updated.data.lastName)" -ForegroundColor Gray
        $testsPassed++
    } else {
        Write-Host "[FAILED] Profile not updated correctly" -ForegroundColor Red
        $testsFailed++
    }
} catch {
    Write-Host "[FAILED] $_" -ForegroundColor Red
    $testsFailed++
}

# Test 7: Update Location
Write-Host "`n[TEST 7] Update User Location" -ForegroundColor Yellow
$locationBody = @{
    latitude = 17.385044
    longitude = 78.486671
    address = "Hyderabad, Telangana, India"
} | ConvertTo-Json

try {
    $locationResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/me/location" -Method POST -Body $locationBody -ContentType "application/json" -Headers $headers
    
    if ($locationResponse.data.latitude -eq 17.385044) {
        Write-Host "[PASSED] Location updated successfully" -ForegroundColor Green
        Write-Host "   Location: $($locationResponse.data.address)" -ForegroundColor Gray
        $testsPassed++
    } else {
        Write-Host "[FAILED] Location not updated correctly" -ForegroundColor Red
        $testsFailed++
    }
} catch {
    Write-Host "[FAILED] $_" -ForegroundColor Red
    $testsFailed++
}

# Test 8: Get User Location
Write-Host "`n[TEST 8] Get User Location" -ForegroundColor Yellow
try {
    $location = Invoke-RestMethod -Uri "$baseUrl/api/users/me/location" -Method GET -Headers $headers
    
    if ($location.data.latitude -eq 17.385044) {
        Write-Host "[PASSED] Location retrieved successfully" -ForegroundColor Green
        Write-Host "   Coordinates: $($location.data.latitude), $($location.data.longitude)" -ForegroundColor Gray
        $testsPassed++
    } else {
        Write-Host "[FAILED] Location data incorrect" -ForegroundColor Red
        $testsFailed++
    }
} catch {
    Write-Host "[FAILED] $_" -ForegroundColor Red
    $testsFailed++
}

# Test 9: Get Another User's Profile
Write-Host "`n[TEST 9] Get User by ID" -ForegroundColor Yellow
try {
    $userById = Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -Method GET -Headers $headers
    
    if ($userById.data.id -eq $userId) {
        Write-Host "[PASSED] User retrieved by ID" -ForegroundColor Green
        $testsPassed++
    } else {
        Write-Host "[FAILED] User data incorrect" -ForegroundColor Red
        $testsFailed++
    }
} catch {
    Write-Host "[FAILED] $_" -ForegroundColor Red
    $testsFailed++
}

# Final Results
Write-Host "`n==================================" -ForegroundColor Cyan
Write-Host "         TEST RESULTS             " -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Passed: $testsPassed" -ForegroundColor Green
Write-Host "Failed: $testsFailed" -ForegroundColor Red
Write-Host "Total: $($testsPassed + $testsFailed)" -ForegroundColor White
Write-Host "==================================" -ForegroundColor Cyan

if ($testsFailed -eq 0) {
    Write-Host "`nALL TESTS PASSED! System is working perfectly!" -ForegroundColor Green
} else {
    Write-Host "`nSome tests failed. Please review the errors above." -ForegroundColor Yellow
}
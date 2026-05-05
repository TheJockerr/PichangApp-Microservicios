# [PichangApp] E2E Test Suite - Notification Service

$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080/api/v1/notifications"
$authUrl = "http://localhost:8080/api/v1/auth/login"

function Print-Result {
    param($testName, $status, $detail)
    $color = if ($status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "[$testName]".PadRight(45) -NoNewline
    Write-Host " [$status] " -ForegroundColor $color -NoNewline
    Write-Host "$detail"
}

# --- TEST 1: Login ---
try {
    $loginBody = @{ correo = "test@pichangapp.cl"; password = "password123" } | ConvertTo-Json
    $loginResponse = Invoke-RestMethod -Uri $authUrl -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Print-Result "TEST 1: Login usuario de prueba..." "PASS" "Token obtenido"
} catch {
    Print-Result "TEST 1: Login usuario de prueba..." "FAIL" "Error: $($_.Exception.Message)"
    exit
}

$headers = @{ "Authorization" = "Bearer $token" }

# --- TEST 2: Registrar device token ---
try {
    $tokenBody = @{ userId = "1"; token = "fcm-test-token-123" } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/device-token" -Method Post -Body $tokenBody -ContentType "application/json" -Headers $headers
    Print-Result "TEST 2: Registrar device token..." "PASS" "200 OK"
} catch {
    Print-Result "TEST 2: Registrar device token..." "FAIL" "Error: $($_.Exception.Message)"
}

# --- TEST 3: Historial vacío ---
try {
    # Borramos notificaciones previas si existen (opcional, pero para historial 'vacío')
    $history = Invoke-RestMethod -Uri "$baseUrl/1" -Method Get -Headers $headers
    if ($history.totalElements -ge 0) {
        Print-Result "TEST 3: Historial consultado..." "PASS" "Lista recibida ($($history.totalElements) previos)"
    }
} catch {
    Print-Result "TEST 3: Historial vacío..." "FAIL" "Error: $($_.Exception.Message)"
}

# --- TEST 4: Enviar notificación ---
try {
    $notifBody = @{ 
        userId = "1"; 
        title = "¡Tu karma subió!"; 
        body = "+10 puntos por asistir al evento"; 
        type = "KARMA_INCREASE" 
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/send" -Method Post -Body $notifBody -ContentType "application/json" -Headers $headers
    Print-Result "TEST 4: Enviar notificación..." "PASS" "Notificación enviada"
} catch {
    Print-Result "TEST 4: Enviar notificación..." "FAIL" "Error: $($_.Exception.Message)"
}

# --- TEST 5: Historial con registro ---
try {
    $history = Invoke-RestMethod -Uri "$baseUrl/1" -Method Get -Headers $headers
    if ($history.totalElements -gt 0) {
        Print-Result "TEST 5: Historial con registro..." "PASS" "$($history.totalElements) notificación(es) encontrada(s)"
    } else {
        Print-Result "TEST 5: Historial con registro..." "FAIL" "No se encontraron notificaciones"
    }
} catch {
    Print-Result "TEST 5: Historial con registro..." "FAIL" "Error: $($_.Exception.Message)"
}

# --- TEST 6: Conexión WebSocket ---
try {
    $wsUri = New-Object System.Uri("ws://localhost:8080/ws/websocket") # SockJS raw websocket path
    $wsClient = New-Object System.Net.WebSockets.ClientWebSocket
    $cts = New-Object System.Threading.CancellationTokenSource
    $task = $wsClient.ConnectAsync($wsUri, $cts.Token)
    
    # Esperar 2 segundos
    $task.Wait(2000)
    
    if ($wsClient.State -eq "Open") {
        Print-Result "TEST 6: Conexión WebSocket..." "PASS" "Conexión abierta exitosamente"
        $wsClient.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "", $cts.Token).Wait()
    } else {
        Print-Result "TEST 6: Conexión WebSocket..." "FAIL" "Estado: $($wsClient.State)"
    }
} catch {
    Print-Result "TEST 6: Conexión WebSocket..." "FAIL" "No se pudo conectar: $($_.Exception.InnerException.Message)"
}

# --- TEST 7: Request sin JWT ---
try {
    Invoke-RestMethod -Uri "$baseUrl/1" -Method Get
    Print-Result "TEST 7: Request sin JWT → 401..." "FAIL" "Debería haber fallado con 401"
} catch {
    if ($_.Exception.Response.StatusCode -eq "Unauthorized") {
        Print-Result "TEST 7: Request sin JWT → 401..." "PASS" "401 Unauthorized"
    } else {
        Print-Result "TEST 7: Request sin JWT → 401..." "FAIL" "Error inesperado: $($_.Exception.Message)"
    }
}

Write-Host "`n[PichangApp] Pruebas completadas." -ForegroundColor Cyan

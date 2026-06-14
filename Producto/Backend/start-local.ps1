param()
$Root   = $PSScriptRoot
$LogDir = "$env:TEMP\pichangapp-logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$JWT    = "change_this_secret_for_dev_only_change_in_prod_very_long_key"
$DBUser = "root"
$DBPass = ""

$services = @(
    [PSCustomObject]@{
        Name = "karma-service"; Dir = "karma_service"; Port = 8081
        Env  = @{
            DATABASE_URL = "jdbc:mysql://localhost:3306/pichangapp_karma?useSSL=false&serverTimezone=UTC"
            DB_USERNAME  = $DBUser; DB_PASSWORD = $DBPass; JWT_SECRET = $JWT
        }
    },
    [PSCustomObject]@{
        Name = "notification-service"; Dir = "notification-service"; Port = 8082
        Env  = @{
            DATABASE_URL = "jdbc:mysql://localhost:3306/pichangapp_notifications?useSSL=false&serverTimezone=UTC"
            DB_USERNAME  = $DBUser; DB_PASSWORD = $DBPass; JWT_SECRET = $JWT
            FCM_PROJECT_ID = "pichangapp-local"; GOOGLE_APPLICATION_CREDENTIALS = ""
        }
    },
    [PSCustomObject]@{
        Name = "users-service"; Dir = "users-service"; Port = 8083
        Env  = @{
            DATABASE_URL     = "jdbc:mysql://localhost:3306/pichangapp?useSSL=false&serverTimezone=UTC"
            DB_USERNAME      = $DBUser; DB_PASSWORD = $DBPass; JWT_SECRET = $JWT
            SENDGRID_API_KEY = "SG.local_fake"
        }
    },
    [PSCustomObject]@{
        Name = "events-service"; Dir = "events-service"; Port = 8084
        Env  = @{
            DATABASE_URL             = "jdbc:mysql://localhost:3306/pichangapp_events?useSSL=false&serverTimezone=UTC"
            DB_USERNAME              = $DBUser; DB_PASSWORD = $DBPass; JWT_SECRET = $JWT
            KARMA_SERVICE_URL        = "http://localhost:8081"
            USERS_SERVICE_URL        = "http://localhost:8083"
            NOTIFICATION_SERVICE_URL = "http://localhost:8082"
            SERVICE_INTERNAL_TOKEN   = "internal_token_dev"
        }
    },
    [PSCustomObject]@{
        Name = "api-gateway"; Dir = "api-gateway"; Port = 8080
        Env  = @{
            JWT_SECRET               = $JWT
            USERS_SERVICE_URL        = "http://localhost:8083"
            KARMA_SERVICE_URL        = "http://localhost:8081"
            NOTIFICATION_SERVICE_URL = "http://localhost:8082"
            EVENTS_SERVICE_URL       = "http://localhost:8084"
        }
    }
)

Write-Host "Iniciando $($services.Count) servicios..." -ForegroundColor Cyan

foreach ($svc in $services) {
    $svcDir  = Join-Path $Root $svc.Dir
    $logOut  = "$LogDir\$($svc.Name).log"
    $logErr  = "$LogDir\$($svc.Name)-err.log"
    Set-Content -Path $logOut -Value ""
    Set-Content -Path $logErr -Value ""

    # Crear un script .cmd temporal para este servicio
    $cmdFile = "$LogDir\run-$($svc.Name).cmd"
    $lines   = @("@echo off", "cd /d `"$svcDir`"")
    foreach ($kv in $svc.Env.GetEnumerator()) {
        $lines += "set `"$($kv.Key)=$($kv.Value)`""
    }
    $mvnwFull = Join-Path $svcDir "mvnw.cmd"
    $lines += "call `"$mvnwFull`" spring-boot:run -Dmaven.compiler.useIncrementalCompilation=false"
    $lines | Set-Content -Path $cmdFile -Encoding ASCII

    Start-Process -FilePath "cmd.exe" `
        -ArgumentList "/c", "`"$cmdFile`"" `
        -RedirectStandardOutput $logOut `
        -RedirectStandardError  $logErr `
        -WindowStyle Hidden

    Write-Host "  Lanzado: $($svc.Name)" -ForegroundColor Gray
}

Write-Host "Esperando arranque (max 3 min)..." -ForegroundColor Yellow

$timeout  = 180
$interval = 8
$elapsed  = 0
$readySvc = New-Object System.Collections.Generic.HashSet[string]

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds $interval
    $elapsed += $interval

    foreach ($svc in $services) {
        if ($readySvc.Contains($svc.Name)) { continue }
        $content = Get-Content "$LogDir\$($svc.Name).log" -Raw -ErrorAction SilentlyContinue
        if ($content -match "Started\s+\w+Application") {
            [void]$readySvc.Add($svc.Name)
            Write-Host "  OK $($svc.Name) ($elapsed s)" -ForegroundColor Green
        }
    }
    if ($readySvc.Count -eq $services.Count) { break }
    Write-Host "  ... $elapsed s - $($readySvc.Count)/$($services.Count) listos" -ForegroundColor DarkGray
}

Write-Host "Resultado:" -ForegroundColor Cyan
foreach ($svc in $services) {
    if ($readySvc.Contains($svc.Name)) {
        Write-Host "  OK   $($svc.Name.PadRight(22)) http://localhost:$($svc.Port)" -ForegroundColor Green
    } else {
        Write-Host "  WAIT $($svc.Name.PadRight(22)) http://localhost:$($svc.Port)" -ForegroundColor Yellow
    }
}
Write-Host "Logs: $LogDir" -ForegroundColor DarkGray

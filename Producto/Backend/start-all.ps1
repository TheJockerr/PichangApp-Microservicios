# [PichangApp] Script de arranque de microservicios

Write-Host "[PichangApp] Iniciando microservicios..." -ForegroundColor Cyan

# Definir rutas y puertos
$services = @(
    @{ Name = "users-service"; Path = "users-service"; Port = 8080 },
    @{ Name = "karma_service"; Path = "karma_service"; Port = 8081 },
    @{ Name = "notification-service"; Path = "notification-service"; Port = 8082 }
)

foreach ($service in $services) {
    Write-Host "[...] Lanzando $($service.Name) en puerto $($service.Port)..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $($service.Path); .\mvnw.cmd spring-boot:run" -WindowStyle Normal
}

Write-Host "[PichangApp] Esperando 15 segundos a que los servicios inicien..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

$allOk = $true
foreach ($service in $services) {
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:$($service.Port)/actuator/health" -Method Get -TimeoutSec 2
        if ($health.status -eq "UP") {
            Write-Host "[OK] $($service.Name.PadRight(20)) → http://localhost:$($service.Port)" -ForegroundColor Green
        } else {
            Write-Host "[WARN] $($service.Name.PadRight(20)) → Respondio pero no esta UP ($($health.status))" -ForegroundColor Yellow
            $allOk = $false
        }
    } catch {
        Write-Host "[FAIL] $($service.Name.PadRight(20)) → No responde en http://localhost:$($service.Port)" -ForegroundColor Red
        $allOk = $false
    }
}

if ($allOk) {
    Write-Host "[PichangApp] Todos los servicios están corriendo." -ForegroundColor Cyan
} else {
    Write-Host "[PichangApp] Algunos servicios no iniciaron correctamente. Revisa las ventanas de log." -ForegroundColor Red
}

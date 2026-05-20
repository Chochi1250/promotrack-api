param(
    [int]$Rounds = 10,
    [int]$DelayMilliseconds = 150,
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$IncludeServerErrors,
    [string]$ServerErrorEndpoint = "/internal/demo/error"
)

$validEndpoints = @(
    "$BaseUrl/api/offers",
    "$BaseUrl/api/offers/today",
    "$BaseUrl/api/offers/upcoming",
    "$BaseUrl/api/offers/expiring-soon",
    "$BaseUrl/api/offers/expiring-soon?days=1",
    "$BaseUrl/api/offers/expiring-soon?days=7",
    "$BaseUrl/api/offers/expiring-soon?days=30",
    "$BaseUrl/api/supermarkets"
)

$clientErrorEndpoints = @(
    "$BaseUrl/api/no-existe",
    "$BaseUrl/api/offers/999999",
    "$BaseUrl/api/supermarkets/999999",
    "$BaseUrl/actuator/env"
)

$serverErrorUrl = "$BaseUrl$ServerErrorEndpoint"

Write-Host ""
Write-Host "Simulando trafico para PromoTrack API..."
Write-Host "Base URL: $BaseUrl"
Write-Host "Rondas: $Rounds"
Write-Host "Delay: $DelayMilliseconds ms"
Write-Host "Incluir errores 5xx: $IncludeServerErrors"

if ($IncludeServerErrors) {
    Write-Host "Endpoint 5xx: $serverErrorUrl"
}

Write-Host ""

for ($round = 1; $round -le $Rounds; $round++) {
    Write-Host "Ronda $round de $Rounds"

    1..10 | ForEach-Object {
        $random = Get-Random -Minimum 1 -Maximum 101

        # Distribucion aproximada:
        # - 70% trafico valido
        # - 20% errores 4xx controlados
        # - 10% errores 5xx, solo si IncludeServerErrors esta activo
        #
        # Ademas, si IncludeServerErrors esta activo, se fuerza al menos
        # un 500 en la ultima request de la ultima ronda. Esto facilita
        # la demo incluso con -Rounds 1.
        $isLastRequest = ($round -eq $Rounds -and $_ -eq 10)

        if ($IncludeServerErrors -and ($isLastRequest -or $random -gt 90)) {
            $url = $serverErrorUrl
        }
        elseif ($random -gt 70) {
            $url = Get-Random -InputObject $clientErrorEndpoints
        }
        else {
            $url = Get-Random -InputObject $validEndpoints
        }

        try {
            $response = Invoke-WebRequest -Uri $url -Method GET -ErrorAction Stop
            Write-Host "OK  $($response.StatusCode) -> $url"
        }
        catch {
            $statusCode = $null

            if ($_.Exception.Response -ne $null) {
                $statusCode = [int]$_.Exception.Response.StatusCode
            }

            if ($statusCode) {
                Write-Host "ERR $statusCode -> $url"
            }
            else {
                Write-Host "ERR ---- -> $url"
            }
        }

        Start-Sleep -Milliseconds $DelayMilliseconds
    }
}

Write-Host ""
Write-Host "Simulacion finalizada."
Write-Host "Revisar metricas en:"
Write-Host "- Prometheus: http://localhost:9090"
Write-Host "- Grafana:    http://localhost:3000"
Write-Host ""

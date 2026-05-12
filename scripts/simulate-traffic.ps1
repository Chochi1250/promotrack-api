param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Rounds = 10,
    [int]$DelayMilliseconds = 250
)

$ErrorActionPreference = "Stop"

$validPaths = @(
    "/",
    "/api/supermarkets",
    "/api/offers",
    "/api/offers/today",
    "/api/offers/upcoming",
    "/api/offers/expiring-soon",
    "/api/offers/calendar?from=2026-01-01&to=2026-12-31",
    "/actuator/health"
)

$notFoundPaths = @(
    "/api/demo/not-found",
    "/api/offers/999999",
    "/api/supermarkets/999999"
)

function Invoke-DemoRequest {
    param(
        [string]$Path,
        [bool]$ExpectedNotFound = $false
    )

    $uri = "$BaseUrl$Path"

    try {
        $response = Invoke-WebRequest -Method GET -Uri $uri -MaximumRedirection 5
        Write-Host ("{0} GET {1}" -f [int]$response.StatusCode, $Path)
    }
    catch {
        $statusCode = $null

        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }

        if ($ExpectedNotFound -and $statusCode -eq 404) {
            Write-Host ("404 GET {0} (esperado para demo)" -f $Path)
            return
        }

        if ($statusCode) {
            Write-Host ("{0} GET {1} (error)" -f $statusCode, $Path)
            return
        }

        Write-Host ("ERROR GET {0}: {1}" -f $Path, $_.Exception.Message)
    }
}

Write-Host "Generando trafico de demo contra $BaseUrl"
Write-Host "Rondas: $Rounds"

for ($round = 1; $round -le $Rounds; $round++) {
    Write-Host ""
    Write-Host ("Ronda {0}/{1}" -f $round, $Rounds)

    foreach ($path in $validPaths) {
        Invoke-DemoRequest -Path $path
        Start-Sleep -Milliseconds $DelayMilliseconds
    }

    if ($round % 2 -eq 0) {
        foreach ($path in $notFoundPaths) {
            Invoke-DemoRequest -Path $path -ExpectedNotFound $true
            Start-Sleep -Milliseconds $DelayMilliseconds
        }
    }
}

Write-Host ""
Write-Host "Trafico de demo finalizado."
Write-Host "Revisar Prometheus en http://localhost:9090 y Grafana en http://localhost:3000."

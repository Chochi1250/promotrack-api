param(
    [int]$Rounds = 10,
    [int]$RequestsPerRound = 10,
    [int]$DelayMilliseconds = 150,
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$IncludeServerErrors,
    [switch]$RenderSafe,
    [string]$ServerErrorEndpoint = "/internal/demo/error"
)

$BaseUrl = $BaseUrl.TrimEnd("/")

if ($Rounds -lt 1) {
    throw "Rounds must be at least 1."
}

if ($RequestsPerRound -lt 1) {
    throw "RequestsPerRound must be at least 1."
}

$includeServerErrorsForRun = $IncludeServerErrors -and -not $RenderSafe

$validEndpoints = @(
    "$BaseUrl/actuator/health",
    "$BaseUrl/api/offers",
    "$BaseUrl/api/offers/today",
    "$BaseUrl/api/offers/upcoming",
    "$BaseUrl/api/offers/expiring-soon?days=7",
    "$BaseUrl/api/supermarkets"
)

$controlledClientErrorEndpoints = @(
    "$BaseUrl/api/offers/expiring-soon?days=0",
    "$BaseUrl/api/offers/expiring-soon?days=31"
)

if (-not $RenderSafe) {
    $validEndpoints += @(
        "$BaseUrl/api/offers/expiring-soon",
        "$BaseUrl/api/offers/expiring-soon?days=1",
        "$BaseUrl/api/offers/expiring-soon?days=30"
    )
    $controlledClientErrorEndpoints += @(
        "$BaseUrl/api/no-existe",
        "$BaseUrl/api/offers/999999",
        "$BaseUrl/api/supermarkets/999999"
    )
}

$serverErrorUrl = "$BaseUrl$ServerErrorEndpoint"

Write-Host ""
Write-Host "Simulando trafico para PromoTrack API..."
Write-Host "Base URL: $BaseUrl"
Write-Host "Rondas: $Rounds"
Write-Host "Requests por ronda: $RequestsPerRound"
Write-Host "Delay: $DelayMilliseconds ms"
Write-Host "Modo seguro Render: $RenderSafe"
Write-Host "Incluir errores 5xx: $includeServerErrorsForRun"

if ($IncludeServerErrors -and $RenderSafe) {
    Write-Host "RenderSafe activo: se omiten errores 5xx dev-only."
}

if ($includeServerErrorsForRun) {
    Write-Host "Endpoint 5xx: $serverErrorUrl"
}

Write-Host ""

for ($round = 1; $round -le $Rounds; $round++) {
    Write-Host "Ronda $round de $Rounds"

    1..$RequestsPerRound | ForEach-Object {
        $random = Get-Random -Minimum 1 -Maximum 101

        # Mezcla aproximada: 70% de tráfico válido, 25% de errores 4xx y
        # 5% de errores 5xx cuando se habilitan fuera del modo RenderSafe.
        # La última solicitud garantiza un 5xx en ejecuciones cortas.
        $isLastRequest = ($round -eq $Rounds -and $_ -eq $RequestsPerRound)

        if ($includeServerErrorsForRun -and ($isLastRequest -or $random -gt 95)) {
            $url = $serverErrorUrl
        }
        elseif ($random -gt 70) {
            $url = Get-Random -InputObject $controlledClientErrorEndpoints
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

if ($RenderSafe) {
    Write-Host "Revisar APM en New Relic para la app configurada en Render."
}
else {
    Write-Host "Revisar metricas en:"
    Write-Host "- Prometheus: http://localhost:9090"
    Write-Host "- Grafana:    http://localhost:3000"
}

Write-Host ""

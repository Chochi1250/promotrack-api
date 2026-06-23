# Observabilidad

PromoTrack API publica métricas con Spring Boot Actuator y Micrometer. El entorno local usa Prometheus y Grafana; el despliegue en Render puede habilitar New Relic APM.

## Actuator

La aplicación expone los siguientes endpoints:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/metrics/{metricName}`
- `GET /actuator/prometheus`

La configuración no publica endpoints sensibles como `env`, `beans`, `heapdump`, `configprops`, `shutdown` o `loggers`. Los detalles del health check tampoco se incluyen en la respuesta pública.

## Prometheus y Grafana

El stack local se inicia con:

```powershell
docker compose up --build
```

Prometheus consulta `api:8080/actuator/prometheus` dentro de la red de Compose. Grafana aprovisiona automáticamente el datasource y el dashboard `PromoTrack API Monitoring` desde:

```text
monitoring/prometheus.yml
monitoring/grafana/provisioning/
monitoring/grafana/promotrack-dashboard.json
```

URLs locales:

- API: `http://localhost:8080`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

## Generación de tráfico

El script `scripts/simulate-traffic.ps1` realiza consultas de lectura y genera respuestas 4xx controladas para alimentar métricas y transacciones.

```powershell
.\scripts\simulate-traffic.ps1
.\scripts\simulate-traffic.ps1 -Rounds 20 -DelayMilliseconds 100
```

En el perfil `dev`, la opción `-IncludeServerErrors` usa `GET /internal/demo/error` para generar respuestas 5xx controladas. Ese endpoint no existe en el perfil `render`.

Para apuntar el script a un despliegue remoto sin usar endpoints internos:

```powershell
.\scripts\simulate-traffic.ps1 `
  -BaseUrl https://<servicio>.onrender.com `
  -RenderSafe `
  -Rounds 20
```

## New Relic en Render

La imagen Docker contiene el Java Agent en `/opt/newrelic/newrelic.jar`, pero no lo activa de forma predeterminada. Para habilitarlo en Render se requieren estas variables:

```text
NEW_RELIC_LICENSE_KEY=<secreto>
NEW_RELIC_APP_NAME=PromoTrack API Render
NEW_RELIC_LOG_FILE_NAME=STDOUT
JAVA_TOOL_OPTIONS=-javaagent:/opt/newrelic/newrelic.jar
```

`NEW_RELIC_LICENSE_KEY` debe mantenerse exclusivamente en la configuración segura del servicio. No debe incluirse en archivos versionados.

La instrumentación agrega los siguientes atributos a consultas de lectura seleccionadas:

- `business.operation`
- `filter.days`
- `result.count`

No registra payloads, credenciales, cadenas de conexión ni datos personales.

## Métricas útiles

- Requests por minuto: `sum(rate(http_server_requests_seconds_count[1m])) * 60`
- Errores 5xx por minuto: `sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) * 60`
- Latencia promedio: `sum(rate(http_server_requests_seconds_sum[1m])) / sum(rate(http_server_requests_seconds_count[1m]))`
- Memoria JVM: `sum(jvm_memory_used_bytes)`
- CPU del proceso: `process_cpu_usage`

## Diagnóstico

Si Prometheus muestra el target `DOWN`:

- comprobar que `docker compose ps` muestre la API como saludable;
- consultar `http://localhost:8080/actuator/prometheus`;
- confirmar que `monitoring/prometheus.yml` apunte a `api:8080`.

Si Grafana no muestra el dashboard:

- comprobar los volúmenes de `monitoring/grafana/` en `docker-compose.yml`;
- buscar la carpeta `PromoTrack` en la sección Dashboards;
- revisar los logs del contenedor de Grafana.

Si New Relic no recibe datos:

- comprobar las variables `NEW_RELIC_LICENSE_KEY`, `NEW_RELIC_APP_NAME` y `JAVA_TOOL_OPTIONS`;
- verificar en los logs de Render que el Java Agent se haya cargado;
- generar tráfico con `-RenderSafe` y confirmar la imagen desplegada.

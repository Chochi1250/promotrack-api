# Monitoreo

PromoTrack API expone metricas con Spring Boot Actuator y Micrometer. El entorno local usa Prometheus y Grafana mediante Docker Compose; la app desplegada en Render se observa con New Relic APM.

## Actuator

Endpoints expuestos:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/metrics/{metricName}`
- `GET /actuator/prometheus`

La exposicion se mantiene acotada desde `src/main/resources/application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: never
  info:
    env:
      enabled: true
```

No se exponen endpoints sensibles como `env`, `beans`, `heapdump`, `threaddump`, `configprops`, `shutdown` o `loggers`.

## Prometheus y Grafana local

El stack local se levanta con:

```powershell
docker compose up --build
```

Servicios:

- API: `http://localhost:8080`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Prometheus usa `monitoring/prometheus.yml` y scrapea la API dentro de la red de Docker:

```yaml
scrape_configs:
  - job_name: promotrack-api
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - api:8080
```

Grafana carga el datasource y el dashboard desde:

```text
monitoring/grafana/provisioning/
monitoring/grafana/promotrack-dashboard.json
```

El dashboard versionado se llama `PromoTrack API Monitoring` y queda en la carpeta `PromoTrack`.

## New Relic APM en Render

La imagen Docker incluye el New Relic Java Agent en:

```text
/opt/newrelic/newrelic.jar
```

El agent queda inactivo por defecto. No se define `JAVA_TOOL_OPTIONS` en el Dockerfile ni en Docker Compose, por lo que el entorno local y los tests no envian datos a New Relic.

Variables requeridas en Render:

```text
NEW_RELIC_LICENSE_KEY=<secret>
NEW_RELIC_APP_NAME=PromoTrack API Render
NEW_RELIC_LOG_FILE_NAME=STDOUT
JAVA_TOOL_OPTIONS=-javaagent:/opt/newrelic/newrelic.jar
```

`NEW_RELIC_LICENSE_KEY` debe cargarse solo como variable de entorno del servicio. No debe versionarse en Dockerfile, README, workflows ni archivos `.env`.

La app agrega instrumentacion minima con New Relic Java API en consultas de lectura relevantes:

- ofertas del dia;
- ofertas proximas;
- ofertas proximas a vencer;
- listado de supermercados.

Atributos custom enviados:

- `business.operation`
- `filter.days`
- `result.count`

No se registran payloads completos, credenciales, cadenas de conexion ni datos personales.

## Script de trafico

El script `scripts/simulate-traffic.ps1` genera requests contra endpoints de lectura y errores 4xx controlados. Sirve para cargar metricas en Prometheus/Grafana y transacciones en New Relic.

Uso local:

```powershell
.\scripts\simulate-traffic.ps1
```

Opciones comunes:

```powershell
.\scripts\simulate-traffic.ps1 -Rounds 20 -DelayMilliseconds 100
.\scripts\simulate-traffic.ps1 -BaseUrl http://localhost:8080
.\scripts\simulate-traffic.ps1 -BaseUrl https://<tu-servicio>.onrender.com -RenderSafe
```

Para generar errores 5xx controlados en el perfil `dev`:

```powershell
.\scripts\simulate-traffic.ps1 -IncludeServerErrors
```

Ese modo usa `GET /internal/demo/error`, un endpoint interno activo solo con perfil `dev`. No esta disponible en el perfil `render`. Cuando se usa `-RenderSafe`, el script evita endpoints dev-only y genera solo errores 4xx controlados.

## Validacion

Validar API y Actuator:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/prometheus
```

Validar Prometheus:

- abrir `http://localhost:9090/targets`;
- confirmar que el job `promotrack-api` este en estado `UP`.

Validar Grafana:

- abrir `http://localhost:3000`;
- entrar a Dashboards;
- buscar la carpeta `PromoTrack` y el dashboard `PromoTrack API Monitoring`.

Validar New Relic en Render:

- confirmar deploy exitoso de la imagen esperada en Render;
- revisar logs de arranque y verificar que se carga `JAVA_TOOL_OPTIONS`;
- generar trafico con `-RenderSafe`;
- revisar APM Summary, Transactions, Errors, Traces, Databases y JVM.

## Metricas utiles

- Requests por minuto: `sum(rate(http_server_requests_seconds_count[1m])) * 60`
- Errores 5xx por minuto: `sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) * 60`
- Latencia promedio: `sum(rate(http_server_requests_seconds_sum[1m])) / sum(rate(http_server_requests_seconds_count[1m]))`
- Memoria JVM: `sum(jvm_memory_used_bytes)`
- CPU del proceso: `process_cpu_usage`

## Rollback

Para desactivar New Relic sin cambiar la imagen:

1. Eliminar o vaciar `JAVA_TOOL_OPTIONS` en Render.
2. Redeployar el servicio.
3. Confirmar que la API sigue respondiendo y que el Java Agent ya no se carga.

Para remover la integracion por completo en una version futura:

1. Quitar la descarga y copia de `/opt/newrelic` en el Dockerfile.
2. Quitar la dependencia `newrelic-api`.
3. Quitar las anotaciones `@Trace` y los atributos custom.
4. Publicar y desplegar una nueva imagen.

## Troubleshooting breve

Prometheus muestra el target `DOWN`:

- verificar que `docker compose ps` muestre la API saludable;
- validar `http://localhost:8080/actuator/prometheus`;
- revisar que `monitoring/prometheus.yml` apunte a `api:8080`.

Grafana no muestra el dashboard:

- revisar que el volumen `monitoring/grafana/provisioning` este montado;
- abrir Dashboards y buscar la carpeta `PromoTrack`;
- importar manualmente `monitoring/grafana/promotrack-dashboard.json` si se usa otra instancia de Grafana.

New Relic no recibe datos:

- verificar `NEW_RELIC_LICENSE_KEY`, `NEW_RELIC_APP_NAME` y `JAVA_TOOL_OPTIONS` en Render;
- revisar logs de Render para confirmar carga del Java Agent;
- generar trafico contra la URL publica con `-RenderSafe`;
- confirmar que la app desplegada corresponde a la imagen publicada esperada.

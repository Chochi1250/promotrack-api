# PromoTrack API

PromoTrack API es una API REST para gestionar supermercados y ofertas de supermercados argentinos.

## Objetivo en el TP DevOps

Este proyecto forma parte de un Trabajo Practico Integrador de DevOps. La API funciona como aplicacion base para aplicar practicas de desarrollo, testing, containerizacion, CI/CD, publicacion de imagen Docker y monitoreo.

## Stack

- Java 25
- Spring Boot 4.0.6
- Spring Web / MVC
- Spring Data JPA
- H2 Database para MVP
- Bean Validation
- Spring Boot Actuator
- Micrometer Prometheus Registry
- Prometheus
- Grafana
- Springdoc OpenAPI / Swagger UI
- JUnit, Mockito y MockMvc
- Maven Wrapper

## Ejecutar Localmente

Desde la raiz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicacion queda disponible en:

```text
http://localhost:8080
```

## Correr Tests

```powershell
.\mvnw.cmd clean test
```

## Integracion Continua

El proyecto incluye un workflow de GitHub Actions en `.github/workflows/ci.yml`.

La CI se ejecuta en `push` y `pull_request` hacia `develop` y `main`. Valida:

- Tests con Maven Wrapper.
- Build del paquete con Maven.
- Construccion de la imagen Docker sin publicarla.

## Publicacion de Imagen Docker en GHCR

La publicacion de imagen Docker permite construir la API como una imagen versionada y subirla a un registry para que pueda descargarse y ejecutarse desde otros entornos.

Este proyecto publica la imagen automaticamente en GitHub Container Registry mediante el workflow `.github/workflows/docker-publish.yml`. El workflow corre en `push` a `develop`, `push` a `main` y tambien puede ejecutarse manualmente con `workflow_dispatch`. No publica imagenes desde `pull_request`.

La imagen queda publicada en:

```text
ghcr.io/<owner>/promotrack-api
```

Tags publicados:

- `develop`: cuando el workflow corre sobre la rama `develop`.
- `latest`: cuando el workflow corre sobre la rama `main`.
- `<sha>`: commit SHA corto para trazabilidad.

Descargar la imagen publicada:

```powershell
docker pull ghcr.io/<owner>/promotrack-api:latest
```

Ejecutar la imagen publicada:

```powershell
docker run --rm -p 8080:8080 ghcr.io/<owner>/promotrack-api:latest
```

Validar que la API responde:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Docker

Construir la imagen:

```powershell
docker build -t promotrack-api .
```

Ejecutar el contenedor:

```powershell
docker run -p 8080:8080 promotrack-api
```

Validar que el contenedor responde:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Tambien se puede abrir Swagger UI en el navegador:

```text
http://localhost:8080/swagger-ui/index.html
```

## Docker Compose

Construir y levantar la API con Prometheus y Grafana:

```powershell
docker compose up --build
```

Detener y remover el contenedor:

```powershell
docker compose down
```

Validar health:

```powershell
docker compose ps
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/prometheus
```

Tambien se puede abrir en el navegador:

```text
http://localhost:8080/actuator/health
```

Abrir Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Monitoreo

El proyecto usa Spring Boot Actuator, Micrometer, Prometheus y Grafana con una exposicion minima y explicita para monitoreo basico local:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

No se exponen endpoints sensibles como `env`, `beans`, `heapdump`, `threaddump`, `configprops`, `shutdown` o `loggers`.

Prometheus scrapea la API desde `monitoring/prometheus.yml` usando el target interno de Docker `api:8080`. Grafana se usa como representacion visual de las metricas y queda conectado a Prometheus mediante provisioning local.

Prometheus:

- UI: `http://localhost:9090`
- Targets: `http://localhost:9090/targets`
- El job `promotrack-api` debe aparecer como `UP`.

Grafana:

- UI: `http://localhost:3000`
- Datasource Prometheus provisionado automaticamente.
- Dashboard provisionado desde `monitoring/grafana/promotrack-dashboard.json`.
- Si hiciera falta importarlo manualmente: abrir Grafana, ir a Dashboards, New, Import y cargar `monitoring/grafana/promotrack-dashboard.json`.

Generar trafico de demo:

```powershell
.\scripts\simulate-traffic.ps1
```

Opcionalmente se puede ajustar la cantidad de rondas:

```powershell
.\scripts\simulate-traffic.ps1 -Rounds 20 -DelayMilliseconds 100
```

Metricas utiles para observar:

- requests HTTP,
- errores 4xx/5xx,
- latencia,
- memoria JVM,
- CPU/proceso.

PromQL sugerido:

```promql
sum(rate(http_server_requests_seconds_count[1m])) * 60
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) * 60
sum(rate(http_server_requests_seconds_sum[1m])) / sum(rate(http_server_requests_seconds_count[1m]))
```

La documentacion completa del monitoreo local esta en `docs/monitoring.md`.

## URLs Utiles

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Actuator Health: `http://localhost:8080/actuator/health`
- Actuator Info: `http://localhost:8080/actuator/info`
- Actuator Metrics: `http://localhost:8080/actuator/metrics`
- Actuator Prometheus: `http://localhost:8080/actuator/prometheus`
- Prometheus UI: `http://localhost:9090`
- Grafana UI: `http://localhost:3000`
- H2 Console: `http://localhost:8080/h2-console`

Credenciales H2 en perfil `dev`:

```text
JDBC URL: jdbc:h2:mem:promotrack
User Name: sa
Password:
```

## Endpoints Principales

Supermercados:

- `GET /api/supermarkets`
- `GET /api/supermarkets/{id}`
- `POST /api/supermarkets`
- `PUT /api/supermarkets/{id}`
- `DELETE /api/supermarkets/{id}`

Ofertas:

- `GET /api/offers`
- `GET /api/offers/{id}`
- `POST /api/offers`
- `PUT /api/offers/{id}`
- `DELETE /api/offers/{id}`
- `GET /api/offers/today`
- `GET /api/offers/upcoming`
- `GET /api/offers/expiring-soon`
- `GET /api/offers/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/offers/supermarket/{supermarketId}`

## Perfil Dev con H2

El perfil activo por defecto es `dev`. Usa una base H2 en memoria configurada en `src/main/resources/application-dev.yml`.

La configuracion actual:

- Usa H2 en memoria.
- Habilita consola H2 en `/h2-console`.
- Ejecuta `data.sql` al iniciar.
- Usa modo compatible con PostgreSQL para facilitar una futura migracion.
- Expone Actuator en `/actuator/health`, `/actuator/info`, `/actuator/metrics` y `/actuator/prometheus`.

## Proximos Pasos

- Agregar PostgreSQL para entorno containerizado.
- Publicar imagen Docker en un registry.
- Evaluar New Relic One como evolucion opcional de monitoreo.

# PromoTrack API

PromoTrack API es una API REST academica desarrollada con Java y Spring Boot para gestionar supermercados y ofertas. El proyecto funciona como base para un Trabajo Practico integrador de DevOps: backend, base de datos, tests automatizados, contenedores, CI/CD, publicacion de imagen, deploy y observabilidad.

## Stack

Backend:

- Java 25
- Spring Boot 4.0.6
- Spring Web / MVC
- Spring Data JPA
- Bean Validation
- Springdoc OpenAPI / Swagger UI

Base de datos:

- PostgreSQL en Docker Compose para el perfil `dev`
- PostgreSQL en Render para la demo desplegada
- PostgreSQL mediante Testcontainers para tests automatizados

DevOps:

- Maven Wrapper
- Dockerfile multi-stage
- Docker Compose
- GitHub Actions
- GitHub Container Registry
- Render mediante deploy hook

Observabilidad:

- Spring Boot Actuator
- Micrometer Prometheus Registry
- Prometheus y Grafana para monitoreo local
- New Relic APM para la app desplegada en Render

## Funcionalidades principales

- Alta, consulta, actualizacion y baja logica de supermercados.
- Alta, consulta, actualizacion y baja logica de ofertas.
- Consulta de ofertas activas, del dia, futuras y proximas a vencer.
- Consulta de ofertas por rango de fechas y por supermercado.
- Validacion de requests con Bean Validation.
- Respuestas de error centralizadas.
- Documentacion OpenAPI disponible con Swagger UI.

## Ejecucion local con Docker Compose

Docker Compose levanta la API, PostgreSQL, Prometheus y Grafana:

```powershell
docker compose up --build
```

Servicios disponibles:

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5434`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Validaciones rapidas:

```powershell
docker compose ps
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/prometheus
```

Para detener el entorno:

```powershell
docker compose down
```

Para recrear la base local desde cero:

```powershell
docker compose down -v
docker compose up --build
```

## Endpoints utiles

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
- `GET /api/offers/expiring-soon?days=7`
- `GET /api/offers/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/offers/supermarket/{supermarketId}`

Soporte:

- `GET /`
- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`
- Swagger UI: `/swagger-ui/index.html`

El parametro `days` de `/api/offers/expiring-soon` es opcional. Si no se informa, usa `3`; el rango permitido es `1..30`.

## Tests

```powershell
.\mvnw.cmd clean test
```

Los tests usan PostgreSQL mediante Testcontainers. Para ejecutarlos localmente, Docker Desktop debe estar iniciado; no requieren una base PostgreSQL instalada manualmente ni dependen de `localhost`.

## CI/CD y registry

El proyecto usa GitHub Actions para validar, publicar imagenes y disparar deploys controlados.

- `.github/workflows/ci.yml`: corre en Pull Requests hacia `develop` y `main`, y tambien por `workflow_dispatch`. Ejecuta tests, genera el package, valida Docker Compose y construye la imagen local sin publicarla.
- `.github/workflows/docker-publish.yml`: corre en push a `develop` y `main`, y tambien por `workflow_dispatch`. Ejecuta tests, construye la imagen y publica en GHCR.
- `.github/workflows/release.yml`: corre al publicar tags `vX.Y.Z`. Valida semver, publica la imagen versionada en GHCR, crea una GitHub Release y dispara Render con `imgURL`.
- `.github/workflows/render-deploy.yml`: deploy manual a Render. Publica una imagen `render-manual-<short-sha>` y la envia al deploy hook.

Imagen en GitHub Container Registry:

```text
ghcr.io/chochi1250/promotrack-api
```

Tags usados:

- `develop` y `develop-<sha>` para integracion.
- `latest` y `main-<sha>` para `main`.
- `vX.Y.Z`, `X.Y`, `X` y `sha-<sha>` para releases.
- `render-manual-<short-sha>` para deploys manuales.

Crear una release versionada, reemplazando `vX.Y.Z` por la version correspondiente:

```powershell
git tag vX.Y.Z
git push origin vX.Y.Z
```

## Deploy en Render

Render despliega una imagen Docker publicada en GHCR. Los workflows de release y deploy manual llaman el deploy hook con el parametro `imgURL`, para que Render use una imagen concreta:

- release formal: `ghcr.io/chochi1250/promotrack-api:vX.Y.Z`
- deploy manual: `ghcr.io/chochi1250/promotrack-api:render-manual-<short-sha>`

Variables principales para Render:

```text
SPRING_PROFILES_ACTIVE=render
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<password>
```

Render define `PORT` automaticamente; fuera de Render, la API mantiene `8080` como fallback. El perfil `render` usa PostgreSQL por variables de entorno, no carga `data.sql` y no expone el endpoint interno de demo del perfil `dev`.

Para cargar datos de demo en la base PostgreSQL de Render se incluye:

```text
docs/db/render-seed.sql
```

Ese script se ejecuta manualmente contra la base de Render cuando se necesita poblar la demo.

## Observabilidad

Actuator expone una lista acotada de endpoints:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

Prometheus scrapea la API dentro de Docker Compose usando `api:8080`, y Grafana carga automaticamente el datasource y el dashboard versionado desde `monitoring/grafana/`.

Para generar trafico local:

```powershell
.\scripts\simulate-traffic.ps1
```

New Relic APM se usa para observar la app desplegada en Render. La imagen Docker incluye el Java Agent en `/opt/newrelic/newrelic.jar`, pero no lo activa por defecto. En Render se habilita con:

```text
NEW_RELIC_LICENSE_KEY=<secret>
NEW_RELIC_APP_NAME=PromoTrack API Render
NEW_RELIC_LOG_FILE_NAME=STDOUT
JAVA_TOOL_OPTIONS=-javaagent:/opt/newrelic/newrelic.jar
```

Para generar trafico seguro contra Render:

```powershell
.\scripts\simulate-traffic.ps1 -BaseUrl https://<tu-servicio>.onrender.com -RenderSafe -Rounds 20
```

La documentacion tecnica de observabilidad esta en `docs/monitoring.md`.

## Estructura breve del repo

```text
.github/workflows/       Workflows de CI, publicacion, release y deploy manual
docs/                    Documentacion tecnica y scripts SQL de apoyo
monitoring/              Prometheus, Grafana provisioning y dashboard
scripts/                 Scripts auxiliares para generar trafico
src/main/java/           Codigo fuente de la API
src/main/resources/      Configuracion por perfil y datos locales
src/test/                Tests automatizados
Dockerfile               Imagen multi-stage de la aplicacion
docker-compose.yml       Entorno local completo
pom.xml                  Configuracion Maven
```

## Variables de entorno principales

Aplicacion:

- `SPRING_PROFILES_ACTIVE`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `PORT`

Render deploy:

- `RENDER_DEPLOY_HOOK_URL`

New Relic:

- `NEW_RELIC_LICENSE_KEY`
- `NEW_RELIC_APP_NAME`
- `NEW_RELIC_LOG_FILE_NAME`
- `JAVA_TOOL_OPTIONS`

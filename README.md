# PromoTrack API

PromoTrack API es una API REST academica desarrollada con Java y Spring Boot para gestionar supermercados y ofertas. La aplicacion es simple a proposito: el foco del Trabajo Practico de DevOps esta en demostrar automatizacion, contenedores, CI/CD, publicacion de imagen Docker, monitoreo y documentacion tecnica defendible.

## Objetivo del TP

El proyecto funciona como aplicacion base para aplicar practicas DevOps sobre un backend real pero acotado:

- validar cambios automaticamente con GitHub Actions;
- construir y ejecutar la aplicacion con Docker;
- levantar un entorno local reproducible con Docker Compose;
- publicar una imagen Docker en GitHub Container Registry;
- exponer healthchecks y metricas con Spring Actuator;
- recolectar y visualizar metricas con Prometheus y Grafana;
- monitorear la demo desplegada con New Relic APM;
- desplegar una demo academica minima en Render;
- documentar el flujo de entrega y las decisiones tecnicas.

## Stack tecnologico

Backend:

- Java 25
- Spring Boot 4.0.6
- Spring Web / MVC
- Spring Data JPA
- Bean Validation
- Springdoc OpenAPI / Swagger UI

Base de datos:

- PostgreSQL en Docker Compose para entorno `dev`
- PostgreSQL mediante Testcontainers para tests automatizados

Testing:

- JUnit
- Mockito
- MockMvc
- Maven Wrapper

DevOps / CI/CD:

- Docker
- Docker Compose
- GitHub Actions
- GitHub Container Registry
- Render para demo academica mediante deploy hook

Observabilidad:

- Spring Boot Actuator
- Micrometer Prometheus Registry
- Prometheus
- Grafana
- New Relic APM para la demo en Render

## Funcionalidades principales

La API permite gestionar un catalogo basico de supermercados y ofertas:

- alta, consulta, actualizacion y baja logica de supermercados;
- alta, consulta, actualizacion y baja logica de ofertas;
- consulta de ofertas activas;
- consulta de ofertas del dia;
- consulta de ofertas futuras;
- consulta de ofertas proximas a vencer, con rango configurable entre 1 y 30 dias;
- consulta de ofertas por rango de fechas;
- consulta de ofertas por supermercado;
- validacion de requests con Bean Validation;
- respuestas de error centralizadas;
- documentacion OpenAPI disponible con Swagger UI.

## Ejecucion local con Maven

Ejecutar tests:

```powershell
.\mvnw.cmd clean test
```

Los tests usan PostgreSQL mediante Testcontainers. No requieren una base PostgreSQL instalada manualmente ni dependen de `localhost`.
Para ejecutarlos localmente, Docker Desktop debe estar iniciado porque Testcontainers crea un contenedor PostgreSQL efimero.

Levantar la aplicacion:

```powershell
.\mvnw.cmd spring-boot:run
```

El perfil por defecto es `dev`, que usa PostgreSQL. Para ejecutar la aplicacion con Maven, primero debe haber una instancia PostgreSQL disponible en `localhost:5434` o se deben configurar las variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

URLs principales:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Healthcheck: `http://localhost:8080/actuator/health`

## Ejecucion con Docker

Construir la imagen local:

```powershell
docker build -t promotrack-api .
```

Ejecutar el contenedor requiere una base PostgreSQL disponible. Para una prueba local simple, se puede levantar solo PostgreSQL con Docker Compose y luego ejecutar la imagen apuntando a ese servicio expuesto en `localhost:5434`:

```powershell
docker compose up -d postgres

docker run --rm -p 8080:8080 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5434/promotrack `
  -e SPRING_DATASOURCE_USERNAME=promotrack `
  -e SPRING_DATASOURCE_PASSWORD=promotrack `
  promotrack-api
```

Validar que la API responde:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Ejecucion con Docker Compose

Docker Compose levanta un entorno local reproducible con la API, PostgreSQL, Prometheus y Grafana:

```powershell
docker compose up --build
```

Detener el entorno:

```powershell
docker compose down
```

Servicios disponibles:

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5434`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Validaciones utiles:

```powershell
docker compose ps
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/prometheus
```

Si se necesita recrear la base local desde cero:

```powershell
docker compose down -v
docker compose up --build
```

Para conectarse desde DBeaver u otro cliente local:

```text
Host: localhost
Port: 5434
Database: promotrack
User: promotrack
Password: promotrack
```

## CI/CD con GitHub Actions

El proyecto usa GitHub Actions para separar validacion, publicacion de imagen, deploy manual y deploy academico por release tag.

Workflow de CI: `.github/workflows/ci.yml`

- Se ejecuta en Pull Requests hacia `develop` y `main`.
- Tambien puede ejecutarse manualmente con `workflow_dispatch`.
- Valida tests con Maven Wrapper.
- Los tests usan PostgreSQL mediante Testcontainers; no se configura un servicio PostgreSQL separado en GitHub Actions.
- Genera el package de la aplicacion.
- Valida la configuracion de Docker Compose.
- Construye la imagen Docker localmente sin publicarla.

Workflow de publicacion: `.github/workflows/docker-publish.yml`

- Se ejecuta en push a `develop`.
- Se ejecuta en push a `main`.
- Tambien puede ejecutarse manualmente con `workflow_dispatch`.
- Ejecuta tests con PostgreSQL mediante Testcontainers.
- Genera el package de la aplicacion con tests omitidos, porque ya fueron validados en el paso anterior.
- Construye la imagen Docker.
- Publica la imagen en GitHub Container Registry.

Workflow de release y deploy demo: `.github/workflows/release.yml`

- Se ejecuta al pushear tags semver con formato `vX.Y.Z`.
- Ejecuta tests con PostgreSQL mediante Testcontainers.
- Genera el package de la aplicacion con tests omitidos, porque ya fueron validados en el paso anterior.
- Construye y publica la imagen Docker en GitHub Container Registry.
- Crea una GitHub Release con `GITHUB_TOKEN`.
- Dispara el deploy en Render mediante el secret `RENDER_DEPLOY_HOOK_URL` y el parametro `imgURL`.
- El `imgURL` apunta a la imagen formal del tag: `ghcr.io/chochi1250/promotrack-api:${GITHUB_REF_NAME}`.

Workflow de deploy manual en Render: `.github/workflows/render-deploy.yml`

- Se ejecuta manualmente con `workflow_dispatch` desde GitHub Actions.
- Ejecuta tests con PostgreSQL mediante Testcontainers.
- Genera el package de la aplicacion con tests omitidos, porque ya fueron validados en el paso anterior.
- Construye y publica la imagen Docker en GitHub Container Registry.
- Publica una imagen especifica con tag `render-manual-<short-sha>`.
- Dispara el deploy en Render mediante el secret `RENDER_DEPLOY_HOOK_URL` y el parametro `imgURL`.
- El `imgURL` apunta exactamente a la imagen manual recien publicada.
- No crea tags Git, no crea GitHub Releases y no reemplaza el flujo de release semver.

Flujo esperado:

```text
feature branch -> Pull Request a develop -> CI sin publicacion
merge a develop -> imagen :develop y :develop-<sha>
merge a main -> imagen :latest y :main-<sha>
tag vX.Y.Z -> imagen :vX.Y.Z, :X.Y, :X, :sha-<sha> -> GitHub Release -> Render deploy hook con imgURL=:vX.Y.Z
Run workflow Manual Deploy - Render -> imagen :render-manual-<short-sha> -> Render deploy hook con imgURL=:render-manual-<short-sha>
```

## Publicacion en GitHub Container Registry

La imagen Docker se publica en GitHub Container Registry:

```text
ghcr.io/chochi1250/promotrack-api
```

Tags usados:

- `develop`: imagen de integracion generada desde la rama `develop`.
- `develop-<sha>`: imagen de integracion trazable a un commit.
- `latest`: imagen estable generada desde la rama `main`.
- `main-<sha>`: imagen estable trazable a un commit.
- `vX.Y.Z`: imagen de release generada desde un tag semver.
- `X.Y` y `X`: alias semver para la ultima release de una linea mayor o menor.
- `sha-<sha>`: tag de release asociado al commit para trazabilidad.
- `render-manual-<short-sha>`: imagen manual trazable a un commit y usada para deploy a demanda.

Crear una release tag:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

Descargar la imagen estable:

```powershell
docker pull ghcr.io/chochi1250/promotrack-api:latest
```

Ejecutar la imagen publicada requiere una base PostgreSQL disponible. Para validar la imagen sin levantar la API desde Compose:

```powershell
docker compose up -d postgres

docker run --rm -p 8080:8080 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5434/promotrack `
  -e SPRING_DATASOURCE_USERNAME=promotrack `
  -e SPRING_DATASOURCE_PASSWORD=promotrack `
  ghcr.io/chochi1250/promotrack-api:latest
```

Validar healthcheck:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Para validar una imagen de integracion:

```powershell
docker pull ghcr.io/chochi1250/promotrack-api:develop
```

## Deploy academico en Render

Render se usa solo como bonus academico para publicar una demo accesible de la API. No reemplaza el entorno local con Docker Compose, no incluye Prometheus/Grafana administrados y no se presenta como una configuracion de produccion real.

La aplicacion es compatible con el puerto dinamico de Render mediante:

```yaml
server.port: ${PORT:8080}
```

Configuracion sugerida en Render:

- crear un Web Service desde el repositorio o desde la imagen Docker publicada en GHCR;
- usar el Dockerfile del proyecto;
- configurar el healthcheck en `/actuator/health`;
- usar una base PostgreSQL administrada de Render o una PostgreSQL externa;
- crear un deploy hook y guardar su URL como secret `RENDER_DEPLOY_HOOK_URL` en GitHub;
- no levantar Docker Compose en Render.

Los workflows no dependen de un tag fijo configurado manualmente en Render. Ambos llaman el Deploy Hook con el parametro `imgURL`, codificado para URL:

- release formal: `imgURL=ghcr.io/chochi1250/promotrack-api:vX.Y.Z`;
- deploy manual: `imgURL=ghcr.io/chochi1250/promotrack-api:render-manual-<short-sha>`.

En la llamada real, `imgURL` se envia como query parameter URL-encoded. Por ejemplo, `ghcr.io/chochi1250/promotrack-api:v1.0.3` se envia codificado dentro del Deploy Hook para evitar problemas con `/` y `:`.

El workflow `Manual Deploy - Render` sirve para redeploys o demos a voluntad, sin crear tags semver ni GitHub Releases.

Variables de entorno necesarias:

```text
SPRING_PROFILES_ACTIVE=render
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<password>
```

Render define `PORT` automaticamente. Si se ejecuta fuera de Render y `PORT` no existe, la API mantiene `8080`.

El perfil `render` es minimo: usa PostgreSQL por variables de entorno, no carga `data.sql`, no expone el endpoint interno de demo del perfil `dev` y usa `spring.jpa.hibernate.ddl-auto=update` para permitir una demo simple sin Flyway. Esta decision es aceptable para el alcance academico del TP, pero no reemplaza migraciones versionadas en un entorno productivo.

El deploy hacia Render puede dispararse al publicar un tag semver `vX.Y.Z` o al ejecutar manualmente el workflow `Manual Deploy - Render`. En ambos casos GitHub Actions publica la imagen en GHCR y Render despliega la imagen concreta recibida por `imgURL`. Los Pull Requests y los merges a `develop` o `main` no despliegan en Render por si solos.

### Datos demo manuales en Render PostgreSQL

Render no carga `src/main/resources/data.sql` en el perfil `render`, porque `spring.sql.init.mode=never` evita insertar datos automaticamente en cada deploy. Para poblar la demo academica una sola vez se versiona el script manual:

```text
docs/db/render-seed.sql
```

Conexion recomendada:

- Para la aplicacion en Render, usar la Internal Database URL de Render como referencia de host, puerto, base y credenciales, configurada en formato JDBC en `SPRING_DATASOURCE_URL`: `jdbc:postgresql://<internal-host>:<port>/<database>`.
- Para DBeaver, `psql` u otro cliente local, usar la External Database URL de Render. La URL interna solo funciona dentro de la red privada de Render.
- Mantener `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` con las credenciales de la base PostgreSQL correspondiente.

Ejecutar el seed desde una terminal local con `psql`:

```powershell
psql "<external-database-url-de-render>" -f docs/db/render-seed.sql
```

Tambien puede ejecutarse desde DBeaver abriendo una consola SQL conectada con la External Database URL y corriendo el contenido de `docs/db/render-seed.sql`.

El script usa `CURRENT_DATE`, fechas relativas y `INSERT ... WHERE NOT EXISTS` para evitar duplicados de los datos demo. No usa IDs explicitos, por lo que no requiere ajustar secuencias y no modifica registros existentes.

Consultas de verificacion:

```sql
SELECT COUNT(*) FROM supermarkets;
SELECT COUNT(*) FROM offers;
SELECT * FROM supermarkets;
SELECT * FROM offers;
```

Validaciones funcionales despues del seed:

```text
GET /api/supermarkets
GET /api/offers
GET /api/offers/today
GET /api/offers/upcoming
GET /api/offers/expiring-soon?days=7
```

Limitaciones del plan gratuito:

- puede haber cold start o spin down despues de inactividad;
- la primera respuesta puede demorar mas;
- los recursos son acotados;
- la base PostgreSQL debe configurarse aparte y respetar las restricciones del plan elegido.

## Healthcheck y Actuator

Actuator expone una lista acotada de endpoints para monitoreo local:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

No se exponen endpoints sensibles como `env`, `beans`, `heapdump`, `threaddump`, `configprops`, `shutdown` o `loggers`.

## Monitoreo local

El monitoreo local se basa en Actuator, Micrometer, Prometheus y Grafana.

- La API expone metricas en `/actuator/prometheus`.
- Prometheus scrapea la API usando el target interno de Docker `api:8080`.
- Grafana se conecta a Prometheus mediante provisioning local.
- El dashboard versionado esta en `monitoring/grafana/promotrack-dashboard.json`.

Para generar trafico de demo:

```powershell
.\scripts\simulate-traffic.ps1
```

Para incluir errores 5xx controlados en perfil `dev`:

```powershell
.\scripts\simulate-traffic.ps1 -IncludeServerErrors
```

La documentacion completa del monitoreo local esta en `docs/monitoring.md`.

## APM en Render

La imagen Docker incluye el New Relic Java Agent en `/opt/newrelic/newrelic.jar`, pero no lo activa por defecto. En Render se habilita con variables de entorno, sin commitear secretos:

```text
NEW_RELIC_LICENSE_KEY=<secret>
NEW_RELIC_APP_NAME=PromoTrack API Render
NEW_RELIC_LOG_FILE_NAME=STDOUT
JAVA_TOOL_OPTIONS=-javaagent:/opt/newrelic/newrelic.jar
```

Para generar trafico seguro contra la app desplegada:

```powershell
.\scripts\simulate-traffic.ps1 -BaseUrl https://<tu-servicio>.onrender.com -RenderSafe -Rounds 20
```

La guia completa de monitoreo local y APM remoto esta en `docs/monitoring.md`.

## Endpoints principales

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

El parametro `days` de `/api/offers/expiring-soon` es opcional. Si no se informa, usa `3` dias por defecto. El rango permitido es `1..30`.

Soporte:

- `GET /`
- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

## Flujo de trabajo con Git

El proyecto usa un flujo simple orientado a Pull Requests:

- `feature/*`: ramas para cambios puntuales.
- `develop`: rama de integracion.
- `main`: rama estable/final.

Flujo recomendado:

```text
feature/* -> Pull Request a develop -> merge a develop -> Pull Request a main -> merge a main
main -> tag vX.Y.Z -> GitHub Release -> deploy demo en Render
Run workflow Manual Deploy - Render -> deploy demo en Render sin release
```

Los Pull Requests permiten ejecutar CI antes de integrar cambios. La publicacion de imagen se realiza al integrar cambios en `develop` o `main`. El deploy academico en Render puede hacerse por tags de release o con el workflow manual, pero no se dispara automaticamente por Pull Requests ni por merges.

## Decisiones tecnicas

- PostgreSQL se usa como base estandar del proyecto.
- Docker Compose provee PostgreSQL para el entorno local `dev`.
- Testcontainers provee PostgreSQL efimero para tests automatizados, sin depender de una base instalada manualmente.
- Dockerfile multi-stage separa build y runtime.
- Docker Compose permite levantar API, PostgreSQL, Prometheus y Grafana con un solo comando.
- GitHub Actions automatiza validacion y publicacion de imagen.
- GHCR se usa como registry integrado con GitHub y trazable por tags.
- Render se usa como despliegue de demo academica disparado por deploy hook desde un tag semver o desde un workflow manual, siempre con `imgURL` para elegir una imagen concreta.
- Prometheus y Grafana cubren monitoreo local sin depender de servicios externos.
- New Relic APM cubre la demo publica en Render y se activa solo con variables de entorno del servicio.
- Swagger/OpenAPI documenta los endpoints disponibles.

Kubernetes, Terraform, OpenTelemetry y alerting formal quedan como roadmap futuro para no sobredimensionar el TP.

## Perfiles de base de datos

El perfil activo por defecto es `dev` por `src/main/resources/application.yml`. Esta configuracion es un baseline academico/local, no una configuracion productiva.

Perfil `dev`:

- usa PostgreSQL;
- queda activo en Docker Compose mediante `SPRING_PROFILES_ACTIVE=dev`;
- en Docker Compose se conecta a `postgres:5432`;
- fuera de Docker Compose se conecta por defecto a `localhost:5434`;
- toma credenciales desde variables de entorno;
- carga datos iniciales desde `data.sql`;
- Actuator limitado a `health`, `info`, `metrics` y `prometheus`.

Variables usadas por el perfil `dev`:

```text
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Perfil `render`:

- usa PostgreSQL administrado o externo;
- queda activo en Render mediante `SPRING_PROFILES_ACTIVE=render`;
- toma conexion y credenciales desde variables de entorno;
- toma el puerto desde `PORT`, con fallback local a `8080`;
- no depende de Docker Compose;
- no carga datos iniciales desde `data.sql`;
- usa `spring.sql.init.mode=never`;
- mantiene Actuator limitado a `health`, `info`, `metrics` y `prometheus`.

Variables usadas por el perfil `render`:

```text
SPRING_PROFILES_ACTIVE=render
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
PORT
```

Perfil `test`:

- usa PostgreSQL mediante Testcontainers;
- queda activo por `src/test/resources/application.yml` y por `@ActiveProfiles("test")` en tests de contexto;
- no requiere PostgreSQL instalado localmente;
- no depende de puertos fijos como `localhost:5432` o `localhost:5434`;
- carga datos iniciales desde `data.sql`;
- valida contra el mismo motor de base de datos que el entorno local.

## Evidencias para defensa

Checklist sugerida:

- GitHub Actions de CI en verde.
- Workflow de publicacion en GHCR en verde.
- Workflow de release en verde para un tag semver `vX.Y.Z`.
- Workflow manual `Manual Deploy - Render` en verde.
- Imagen publicada en `ghcr.io/chochi1250/promotrack-api`.
- `docker pull` funcionando.
- `docker run` funcionando desde la imagen publicada, conectado a PostgreSQL.
- Deploy hook de Render configurado como secret `RENDER_DEPLOY_HOOK_URL`.
- Demo academica en Render respondiendo `/actuator/health`.
- `/actuator/health` respondiendo `UP`.
- Swagger UI funcionando.
- Prometheus con target `promotrack-api` en estado `UP`.
- Grafana mostrando metricas luego de generar trafico.
- New Relic APM mostrando transacciones, latencia, errores y trazas de servicios sobre la app en Render.

## Roadmap futuro

Mejoras posibles fuera del alcance principal de esta entrega:

- agregar Flyway para versionar migraciones de base de datos;
- evaluar entornos separados de deploy para staging y produccion real;
- agregar alertas y SLOs sobre las metricas disponibles;
- incorporar OpenTelemetry para trazas;
- evaluar Kubernetes para orquestacion;
- gestionar infraestructura con Terraform;
- agregar paginacion, busqueda o mas filtros funcionales si el dominio crece;
- separar con mas detalle perfiles `dev` y `prod`.

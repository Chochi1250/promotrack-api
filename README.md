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
- H2 en memoria para tests automatizados

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

Observabilidad:

- Spring Boot Actuator
- Micrometer Prometheus Registry
- Prometheus
- Grafana

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

Los tests usan H2 en memoria mediante el perfil `test`, por lo que no requieren PostgreSQL levantado.

Levantar la aplicacion:

```powershell
.\mvnw.cmd spring-boot:run
```

El perfil por defecto es `dev`, que usa PostgreSQL. Para ejecutar la aplicacion con Maven, primero debe haber una instancia PostgreSQL disponible en `localhost:5432` o se deben configurar las variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

URLs principales:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Healthcheck: `http://localhost:8080/actuator/health`

## Ejecucion con Docker

Construir la imagen local:

```powershell
docker build -t promotrack-api .
```

Ejecutar el contenedor:

```powershell
docker run --rm -p 8080:8080 promotrack-api
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
- PostgreSQL: `localhost:5432`
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

## CI/CD con GitHub Actions

El proyecto usa GitHub Actions para separar validacion y publicacion.

Workflow de CI: `.github/workflows/ci.yml`

- Se ejecuta en Pull Requests hacia `develop` y `main`.
- Tambien se ejecuta en push a `develop` y `main`.
- Valida tests con Maven Wrapper.
- Genera el package de la aplicacion.
- Valida la configuracion de Docker Compose.
- Construye la imagen Docker localmente sin publicarla.

Workflow de publicacion: `.github/workflows/docker-publish.yml`

- Se ejecuta en push a `develop`.
- Se ejecuta en push a `main`.
- Tambien puede ejecutarse manualmente con `workflow_dispatch`.
- Ejecuta tests.
- Construye la imagen Docker.
- Publica la imagen en GitHub Container Registry.

Flujo esperado:

```text
feature branch -> Pull Request -> CI -> develop -> imagen :develop
develop -> Pull Request -> CI -> main -> imagen :latest
```

## Publicacion en GitHub Container Registry

La imagen Docker se publica en GitHub Container Registry:

```text
ghcr.io/chochi1250/promotrack-api
```

Tags usados:

- `develop`: imagen de integracion generada desde la rama `develop`.
- `latest`: imagen estable generada desde la rama `main`.
- SHA corto: tag asociado al commit para trazabilidad.

Descargar la imagen estable:

```powershell
docker pull ghcr.io/chochi1250/promotrack-api:latest
```

Ejecutar la imagen publicada:

```powershell
docker run --rm -p 8080:8080 ghcr.io/chochi1250/promotrack-api:latest
```

Validar healthcheck:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Para validar una imagen de integracion:

```powershell
docker pull ghcr.io/chochi1250/promotrack-api:develop
```

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
```

Los Pull Requests permiten ejecutar CI antes de integrar cambios. La publicacion de imagen se realiza al integrar cambios en `develop` o `main`.

## Decisiones tecnicas

- PostgreSQL se usa en Docker Compose para representar un entorno local mas realista y reproducible.
- H2 se mantiene para tests rapidos y aislados.
- Dockerfile multi-stage separa build y runtime.
- Docker Compose permite levantar API, PostgreSQL, Prometheus y Grafana con un solo comando.
- GitHub Actions automatiza validacion y publicacion de imagen.
- GHCR se usa como registry integrado con GitHub y trazable por tags.
- Prometheus y Grafana cubren monitoreo local sin depender de servicios externos.
- Swagger/OpenAPI documenta los endpoints disponibles.

Kubernetes, Terraform, Render, New Relic, APM y OpenTelemetry no forman parte de la implementacion actual. Quedan como roadmap futuro para no sobredimensionar el TP.

## Perfiles de base de datos

El perfil activo por defecto es `dev`. Esta configuracion es un baseline academico/local, no una configuracion productiva.

Perfil `dev`:

- usa PostgreSQL;
- en Docker Compose se conecta a `postgres:5432`;
- fuera de Docker Compose se conecta por defecto a `localhost:5432`;
- toma credenciales desde variables de entorno;
- carga datos iniciales desde `data-postgres.sql`;
- Actuator limitado a `health`, `info`, `metrics` y `prometheus`.

Variables usadas por el perfil `dev`:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Perfil `test`:

- usa H2 en memoria;
- no requiere servicios externos;
- carga datos iniciales desde `data.sql`;
- mantiene los tests rapidos y aislados.

## Evidencias para defensa

Checklist sugerida:

- GitHub Actions de CI en verde.
- Workflow de publicacion en GHCR en verde.
- Imagen publicada en `ghcr.io/chochi1250/promotrack-api`.
- `docker pull` funcionando.
- `docker run` funcionando desde la imagen publicada.
- `/actuator/health` respondiendo `UP`.
- Swagger UI funcionando.
- Prometheus con target `promotrack-api` en estado `UP`.
- Grafana mostrando metricas luego de generar trafico.

## Roadmap futuro

Mejoras posibles fuera del alcance principal de esta entrega:

- agregar Flyway para versionar migraciones de base de datos;
- desplegar en Render u otro servicio cloud gratuito;
- agregar New Relic o una herramienta APM;
- incorporar OpenTelemetry para trazas;
- evaluar Kubernetes para orquestacion;
- gestionar infraestructura con Terraform;
- agregar paginacion, busqueda o mas filtros funcionales si el dominio crece;
- separar con mas detalle perfiles `dev` y `prod`.

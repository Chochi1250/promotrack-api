# PromoTrack API

PromoTrack API es una API REST para gestionar supermercados y ofertas de supermercados argentinos.

## Objetivo en el TP DevOps

Este proyecto forma parte de un Trabajo Practico Integrador de DevOps. La API funciona como aplicacion base para aplicar practicas de desarrollo, testing, containerizacion, CI/CD, publicacion de imagen Docker y monitoreo.

## Stack

- Java 21
- Spring Boot 3.5.6
- Spring Web / MVC
- Spring Data JPA
- H2 Database para MVP
- Bean Validation
- Spring Boot Actuator
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

## URLs Utiles

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Actuator Health: `http://localhost:8080/actuator/health`
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
- Expone Actuator en `/actuator/health`.

## Proximos Pasos

- Crear `Dockerfile`.
- Crear `docker-compose.yml`.
- Agregar PostgreSQL para entorno containerizado.
- Configurar GitHub Actions para ejecutar tests y build.
- Publicar imagen Docker en un registry.
- Ampliar monitoreo con Actuator y metricas.

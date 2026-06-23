# PromoTrack API

API REST para administrar supermercados y promociones en Argentina. El proyecto es un prototipo académico orientado a prácticas de backend y DevOps: persistencia con PostgreSQL, documentación OpenAPI, pruebas automatizadas, contenedores, CI/CD y observabilidad.

## Funcionalidades

- ABM de supermercados y ofertas con baja lógica.
- Consultas de ofertas vigentes, futuras y próximas a vencer.
- Búsqueda por rango de fechas y por supermercado.
- Validación de solicitudes y respuestas de error uniformes.
- Documentación interactiva mediante Swagger UI.

## Tecnologías

- Java 25 y Spring Boot 4
- Spring Web, Spring Data JPA y Bean Validation
- PostgreSQL 16
- Springdoc OpenAPI
- JUnit, MockMvc y Testcontainers
- Docker y Docker Compose
- Prometheus, Grafana y New Relic
- GitHub Actions y GitHub Container Registry

## Requisitos

- Docker Desktop o un motor Docker compatible con Compose
- Java 25 para ejecutar Maven fuera de Docker

El repositorio incluye Maven Wrapper, por lo que no es necesario instalar Maven.

## Ejecución local

El entorno completo incluye la API, PostgreSQL, Prometheus y Grafana:

```powershell
docker compose up --build
```

Servicios disponibles:

| Servicio | URL |
| --- | --- |
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| PostgreSQL | `localhost:5434` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |

Comprobación rápida:

```powershell
docker compose ps
Invoke-RestMethod http://localhost:8080/actuator/health
```

Para detener el entorno:

```powershell
docker compose down
```

El perfil `dev` recrea el esquema y carga los datos de ejemplo de `src/main/resources/data.sql`. Para reiniciar también el volumen local de PostgreSQL:

```powershell
docker compose down -v
docker compose up --build
```

## API

La especificación completa está disponible en Swagger UI. Endpoints principales:

| Recurso | Operaciones |
| --- | --- |
| Supermercados | `GET`, `POST /api/supermarkets`; `GET`, `PUT`, `DELETE /api/supermarkets/{id}` |
| Ofertas | `GET`, `POST /api/offers`; `GET`, `PUT`, `DELETE /api/offers/{id}` |
| Ofertas vigentes | `GET /api/offers/today` |
| Ofertas futuras | `GET /api/offers/upcoming` |
| Próximos vencimientos | `GET /api/offers/expiring-soon?days=3` |
| Calendario | `GET /api/offers/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD` |
| Ofertas por supermercado | `GET /api/offers/supermarket/{supermarketId}` |

El parámetro `days` es opcional, usa `3` por defecto y admite valores entre `1` y `30`.

## Pruebas

```powershell
.\mvnw.cmd clean test
```

Las pruebas de integración usan PostgreSQL mediante Testcontainers, por lo que Docker debe estar disponible. No dependen de una base local preexistente.

## Configuración

La configuración se divide por perfiles:

- `dev`: PostgreSQL local, creación automática del esquema y carga de datos de ejemplo.
- `render`: conexión mediante variables de entorno, sin carga automática de datos.
- `test`: PostgreSQL efímero administrado por Testcontainers.

Variables requeridas para el perfil `render`:

```text
SPRING_PROFILES_ACTIVE=render
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<contraseña>
```

`PORT` es opcional y usa `8080` como valor predeterminado. Los secretos de despliegue y New Relic deben configurarse en el proveedor de ejecución o en GitHub Actions; no se almacenan en el repositorio.

## Observabilidad

Actuator expone únicamente `health`, `info`, `metrics` y `prometheus`. Docker Compose configura Prometheus y aprovisiona el datasource y el dashboard de Grafana incluidos en `monitoring/`.

Para generar tráfico de prueba local:

```powershell
.\scripts\simulate-traffic.ps1
```

La configuración de New Relic y las instrucciones de diagnóstico están documentadas en [docs/monitoring.md](docs/monitoring.md).

## CI/CD

Los workflows de GitHub Actions cubren:

- validación de pull requests con tests, package y build de Docker;
- publicación de imágenes de `develop` y `main` en GHCR;
- releases versionadas con tags `vX.Y.Z`;
- despliegue manual o asociado a una release en Render.

Imagen publicada: `ghcr.io/chochi1250/promotrack-api`.

## Estructura

```text
.github/workflows/   Integración continua, publicación y despliegue
docs/                Documentación técnica y datos opcionales de ejemplo
monitoring/          Configuración de Prometheus y Grafana
scripts/             Utilidades de desarrollo y observabilidad
src/main/            Código y configuración de la aplicación
src/test/            Pruebas unitarias y de integración
```

## Alcance del prototipo

La API no implementa autenticación ni autorización. El esquema de producción se administra con Hibernate (`ddl-auto=update`) y no incluye migraciones versionadas; ambas decisiones deben revisarse antes de usar el proyecto en un entorno productivo.

## Proximos pasos
- Adaptar Docker para que corra en entornos multiplataforma ( Amd, intel, en entornos mas viejos, asegurarme que corra en distribuciones de linux... etc )
- Agregar Terraform junto a una mejora significativa para la entrega final
- Escalar API y agregarle una interfaz grafica

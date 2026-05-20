# Monitoreo Basico

PromoTrack API usa Spring Boot Actuator, Micrometer, Prometheus y Grafana para un monitoreo local simple y seguro. El objetivo es validar estado, informacion general, metricas tecnicas y visualizacion sin sobredimensionar el proyecto ni publicar datos sensibles.

## Endpoints disponibles

Los endpoints expuestos son:

- `GET /actuator/health`: indica si la aplicacion esta levantada y disponible.
- `GET /actuator/info`: muestra informacion general de la aplicacion.
- `GET /actuator/metrics`: lista las metricas disponibles.
- `GET /actuator/metrics/{metricName}`: muestra el detalle de una metrica concreta.
- `GET /actuator/prometheus`: expone metricas en formato Prometheus.

El healthcheck de Docker Compose usa:

```text
http://localhost:8080/actuator/health
```

## Configuracion aplicada

La configuracion de Actuator expone una lista cerrada de endpoints:

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

`show-details: never` evita publicar detalles internos del estado de la aplicacion o de sus dependencias.

Prometheus usa `monitoring/prometheus.yml` para scrapear la API dentro de la red de Docker Compose:

```yaml
scrape_configs:
  - job_name: promotrack-api
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - api:8080
```

## Estructura de archivos

```text
monitoring/
  prometheus.yml
  grafana/
    promotrack-dashboard.json
    provisioning/
      datasources/
        prometheus.yml
      dashboards/
        promotrack.yml
scripts/
  simulate-traffic.ps1
```

`monitoring/prometheus.yml` define el scrape de Prometheus. `monitoring/grafana/promotrack-dashboard.json` versiona el dashboard de demo. Los archivos de `monitoring/grafana/provisioning/` cargan automaticamente el datasource y el dashboard al iniciar Grafana con Docker Compose.

## Levantar el stack local

Desde la raiz del proyecto:

```powershell
docker compose up --build
```

Esto levanta un stack local de demo con el perfil `dev`. Es el baseline academico del TP y no representa una configuracion productiva.

Esto incluye:

- API en `http://localhost:8080`
- Prometheus en `http://localhost:9090`
- Grafana en `http://localhost:3000`

URLs de validacion:

- API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus endpoint: `http://localhost:8080/actuator/prometheus`
- Prometheus UI: `http://localhost:9090`
- Grafana UI: `http://localhost:3000`

Para validar Prometheus, abrir `http://localhost:9090/targets` y confirmar que el job `promotrack-api` este en estado `UP`.

Para validar Grafana, abrir `http://localhost:3000`, entrar a Dashboards y buscar la carpeta `PromoTrack`. El dashboard provisionado se llama `PromoTrack API Monitoring`.

Si el dashboard no aparece porque se esta usando una instancia externa de Grafana, se puede importar manualmente desde Dashboards, New, Import, cargando `monitoring/grafana/promotrack-dashboard.json`.

## Monitoreo en Render con New Relic APM

New Relic APM complementa el monitoreo local. Prometheus y Grafana demuestran un stack reproducible con Docker Compose; New Relic observa la aplicacion publica desplegada en Render.

La imagen Docker incluye el New Relic Java Agent en:

```text
/opt/newrelic/newrelic.jar
```

El agent queda inactivo por defecto. No se define `JAVA_TOOL_OPTIONS` en el Dockerfile ni en Docker Compose, por lo que el entorno local y los tests no envian datos a New Relic.

Para activarlo en Render, configurar estas variables de entorno en el Web Service:

```text
NEW_RELIC_LICENSE_KEY=<secret>
NEW_RELIC_APP_NAME=PromoTrack API Render
NEW_RELIC_LOG_FILE_NAME=STDOUT
JAVA_TOOL_OPTIONS=-javaagent:/opt/newrelic/newrelic.jar
```

`NEW_RELIC_LICENSE_KEY` debe cargarse solo en Render. No debe commitearse ni agregarse al Dockerfile, README, workflows o archivos `.env` versionados.

La integracion agrega instrumentacion custom minima con New Relic Java API en consultas de lectura relevantes:

- ofertas del dia;
- ofertas proximas;
- ofertas proximas a vencer;
- listado de supermercados.

Los atributos custom son de baja cardinalidad y no contienen datos sensibles:

- `business.operation`
- `filter.days`
- `result.count`

No se registran payloads completos, nombres de usuarios, credenciales, cadenas de conexion ni datos personales.

## Validar New Relic en Render

Pasos sugeridos:

1. Publicar una nueva imagen en GHCR con el Dockerfile actualizado.
2. Ejecutar el workflow manual de deploy a Render o publicar un tag de release.
3. Confirmar que Render redeploya correctamente la imagen.
4. Revisar los logs de Render y buscar el arranque del Java agent.
5. Abrir New Relic APM y confirmar que aparece la aplicacion `PromoTrack API Render`.
6. Generar trafico contra la URL publica de Render.
7. Revisar transacciones web, latencia, errores 4xx controlados, throughput y trazas de metodos instrumentados.

Ejemplo de trafico seguro contra Render:

```powershell
.\scripts\simulate-traffic.ps1 -BaseUrl https://<tu-servicio>.onrender.com -RenderSafe -Rounds 20 -RequestsPerRound 10 -DelayMilliseconds 150
```

`-RenderSafe` evita usar endpoints internos del perfil `dev` y genera solo errores 4xx controlados mediante parametros invalidos como `days=0` y `days=31`.

Capturas recomendadas para la defensa:

- Render con deploy exitoso y variables de entorno configuradas sin revelar el valor de `NEW_RELIC_LICENSE_KEY`.
- Logs de Render mostrando que la aplicacion arranco correctamente con `JAVA_TOOL_OPTIONS`.
- New Relic APM Summary con throughput, response time y error rate.
- Transactions con endpoints como `/api/offers/today`, `/api/offers/upcoming`, `/api/offers/expiring-soon` y `/api/supermarkets`.
- Trace details mostrando segmentos de service y atributos custom como `business.operation`, `filter.days` y `result.count`.

Rollback rapido:

1. Eliminar o vaciar `JAVA_TOOL_OPTIONS` en Render.
2. Redeployar el servicio.
3. Confirmar que la API sigue respondiendo y que ya no se carga el Java agent.

Rollback completo:

1. Revertir el bloque del Dockerfile que descarga y copia `/opt/newrelic`.
2. Revertir la dependencia `newrelic-api` y las anotaciones `@Trace` si se desea remover toda la instrumentacion custom.
3. Publicar y desplegar una nueva imagen.

## Trafico de demo

El script `scripts/simulate-traffic.ps1` genera requests de lectura contra endpoints validos y errores 4xx controlados. Sirve para que Prometheus, Grafana y New Relic muestren trafico durante una demo.

Ejecutar:

```powershell
.\scripts\simulate-traffic.ps1
```

Opciones utiles:

```powershell
.\scripts\simulate-traffic.ps1 -Rounds 20 -DelayMilliseconds 100
.\scripts\simulate-traffic.ps1 -BaseUrl http://localhost:8080
.\scripts\simulate-traffic.ps1 -BaseUrl https://<tu-servicio>.onrender.com -RenderSafe
```

Para validar el panel de errores 5xx, el perfil `dev` incluye el endpoint interno `GET /internal/demo/error`. No es una funcionalidad de negocio: solo lanza un error controlado para que el `GlobalExceptionHandler` devuelva un 500 generico y Prometheus registre la metrica. Se puede incluir en la simulacion con:

```powershell
.\scripts\simulate-traffic.ps1 -IncludeServerErrors
```

Este endpoint esta limitado al perfil `dev` y no debe habilitarse en produccion.

## Por que no se expone todo Actuator

Actuator ofrece endpoints utiles para diagnostico, pero varios pueden revelar informacion interna. Para este TP se expone solo lo necesario para monitoreo basico local:

- estado de disponibilidad,
- informacion general de la app,
- metricas tecnicas,
- metricas en formato Prometheus.

No se exponen endpoints sensibles como:

- `env`
- `beans`
- `heapdump`
- `threaddump`
- `configprops`
- `shutdown`
- `loggers`

Exponer estos endpoints sin controles adicionales puede filtrar variables de entorno, configuracion, estructura interna, informacion de memoria, threads, dependencias o permitir cambios operativos no deseados.

## Diferencia entre health, info, metrics y prometheus

`health` responde si la aplicacion esta disponible. Es el endpoint adecuado para healthchecks de contenedores y validaciones simples de uptime.

`info` publica metadatos no sensibles, como nombre, descripcion y version de la aplicacion.

`metrics` expone mediciones tecnicas de la aplicacion y del runtime, por ejemplo uso de memoria, threads, requests HTTP o tiempos de respuesta cuando esas metricas estan disponibles.

`prometheus` expone metricas en el formato que Prometheus puede scrapear. No reemplaza a `metrics`; lo complementa para recoleccion y consultas PromQL.

Grafana muestra las metricas recolectadas por Prometheus. El datasource queda provisionado desde `monitoring/grafana/provisioning/datasources/prometheus.yml`. Se versiona un dashboard de demo, pero no credenciales.

## Dashboard de Grafana

El dashboard versionado en `monitoring/grafana/promotrack-dashboard.json` permite visualizar el estado de la API durante una demo local. Muestra senales basicas de operacion sin agregar herramientas innecesarias.

Paneles incluidos:

- API Status: estado general observado desde las metricas disponibles.
- HTTP Requests per Minute: volumen de requests por minuto.
- Average HTTP latency: latencia promedio de requests HTTP.
- Requests by HTTP Status: distribucion por codigo HTTP.
- Requests by Endpoint: trafico agrupado por endpoint.
- HTTP 5xx Errors: errores de servidor.
- HTTP 4xx Errors: errores de cliente, utiles para ver los 404 controlados de la demo.
- JVM Memory Used: memoria usada por la JVM.

Metricas observadas:

- `http_server_requests_seconds_count`
- `http_server_requests_seconds_sum`
- `jvm_memory_used_bytes`
- `process_cpu_usage`

Grafana no recolecta metricas por si mismo en esta configuracion: consulta a Prometheus y las muestra en paneles.

## Que metricas mirar

- Requests HTTP: volumen de requests recibidas por la API.
- Errores 4xx/5xx: fallos del cliente y errores del servidor.
- Latencia: tiempo que tardan las requests en responder.
- Memoria JVM: uso de heap y memoria del proceso Java.
- CPU/proceso: consumo del proceso y senales de carga.

## PromQL basico

Requests por minuto:

```promql
sum(rate(http_server_requests_seconds_count[1m])) * 60
```

Errores 5xx por minuto:

```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) * 60
```

Latencia promedio:

```promql
sum(rate(http_server_requests_seconds_sum[1m])) / sum(rate(http_server_requests_seconds_count[1m]))
```

Uso de memoria JVM:

```promql
sum(jvm_memory_used_bytes)
```

Uso de CPU del proceso:

```promql
process_cpu_usage
```

## Conceptos de observabilidad

SLI significa Service Level Indicator. Es una metrica concreta que mide el comportamiento del servicio, por ejemplo porcentaje de respuestas exitosas o latencia p95.

SLO significa Service Level Objective. Es el objetivo interno definido sobre un SLI, por ejemplo que el 95% de las respuestas respondan en menos de 500 ms.

SLA significa Service Level Agreement. Es un compromiso formal con usuarios o clientes. Suele tener consecuencias si no se cumple.

Error budget es el margen de error permitido por un SLO. Si el objetivo es 99% de disponibilidad, el 1% restante es el presupuesto de error disponible.

Metricas son valores numericos medidos en el tiempo. Permiten ver tendencias, detectar degradaciones y comparar el estado actual contra objetivos.

Logs son eventos registrados por la aplicacion. Sirven para entender que paso en un momento especifico.

Traces representan el recorrido de una solicitud entre componentes. En el stack local no se agrega tracing distribuido; en Render, New Relic APM puede mostrar transacciones web y segmentos de metodos instrumentados para las consultas principales.

Latency mide cuanto tarda una operacion o request en responder.

Traffic mide la cantidad de solicitudes o carga que recibe el sistema.

Errors mide fallos, respuestas no exitosas o excepciones.

Saturation mide que tan cerca esta el sistema de agotar sus recursos, por ejemplo CPU, memoria, threads o conexiones.

## Justificacion para el TP

Prometheus y Grafana son una combinacion simple y defendible para este trabajo: la API expone metricas, Prometheus las recolecta y Grafana las visualiza. Todo corre localmente con Docker Compose, sin Kubernetes ni secretos versionados.

La solucion mantiene bajo acoplamiento: si el monitoreo se apaga, la API sigue funcionando; si la API evoluciona, el contrato principal de monitoreo sigue siendo `/actuator/prometheus`.

New Relic agrega una segunda mirada para la demo desplegada en Render: APM real sobre la aplicacion publica, transacciones HTTP, latencia, errores, throughput y trazas de metodos de negocio instrumentados. Esta integracion tambien queda desacoplada porque se activa solo con variables de entorno del servicio Render.

## Evolucion opcional

Como evolucion futura se puede agregar alerting formal sobre New Relic o Grafana Cloud, SLOs, dashboards compartidos por ambiente y OpenTelemetry si el proyecto crece hacia mas servicios.

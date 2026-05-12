# Monitoreo Basico

PromoTrack API usa Spring Boot Actuator para exponer un baseline seguro de monitoreo. El objetivo es validar estado, informacion de la aplicacion y metricas tecnicas sin sobredimensionar el proyecto ni publicar datos sensibles.

## Endpoints disponibles

Los endpoints expuestos son:

- `GET /actuator/health`: indica si la aplicacion esta levantada y disponible.
- `GET /actuator/info`: muestra informacion general de la aplicacion.
- `GET /actuator/metrics`: lista las metricas disponibles.
- `GET /actuator/metrics/{metricName}`: muestra el detalle de una metrica concreta.

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
        include: health,info,metrics
  endpoint:
    health:
      show-details: never
  info:
    env:
      enabled: true
```

`show-details: never` evita publicar detalles internos del estado de la aplicacion o de sus dependencias.

## Por que no se expone todo Actuator

Actuator incluye endpoints utiles para diagnostico, pero varios pueden revelar informacion interna. Para este TP se expone solo lo necesario para monitoreo basico:

- estado de disponibilidad,
- informacion general de la app,
- metricas tecnicas.

No se exponen endpoints sensibles como:

- `env`
- `beans`
- `heapdump`
- `threaddump`
- `configprops`
- `shutdown`
- `loggers`

Exponer estos endpoints sin controles adicionales puede filtrar variables de entorno, configuracion, estructura interna, informacion de memoria, threads, dependencias o permitir cambios operativos no deseados.

## Diferencia entre health, info y metrics

`health` responde si la aplicacion esta disponible. Es el endpoint adecuado para healthchecks de contenedores y validaciones simples de uptime.

`info` publica metadatos no sensibles, como nombre, descripcion y version de la aplicacion.

`metrics` expone mediciones tecnicas de la aplicacion y del runtime, por ejemplo uso de memoria, threads, requests HTTP o tiempos de respuesta cuando esas metricas estan disponibles.

## Conceptos de observabilidad

SLI significa Service Level Indicator. Es una metrica concreta que mide el comportamiento del servicio, por ejemplo porcentaje de respuestas exitosas o latencia p95.

SLO significa Service Level Objective. Es el objetivo interno definido sobre un SLI, por ejemplo que el 95% de las respuestas respondan en menos de 500 ms.

SLA significa Service Level Agreement. Es un compromiso formal con usuarios o clientes. Suele tener consecuencias si no se cumple.

Error budget es el margen de error permitido por un SLO. Si el objetivo es 99% de disponibilidad, el 1% restante es el presupuesto de error disponible.

Metricas son valores numericos medidos en el tiempo. Permiten ver tendencias, detectar degradaciones y comparar el estado actual contra objetivos.

Logs son eventos registrados por la aplicacion. Sirven para entender que paso en un momento especifico.

Traces representan el recorrido de una solicitud entre componentes. Son utiles en sistemas distribuidos, pero no se implementan en este baseline porque PromoTrack API es una aplicacion simple.

Latency mide cuanto tarda una operacion o request en responder.

Traffic mide la cantidad de solicitudes o carga que recibe el sistema.

Errors mide fallos, respuestas no exitosas o excepciones.

Saturation mide que tan cerca esta el sistema de agotar sus recursos, por ejemplo CPU, memoria, threads o conexiones.

## Evolucion opcional

New Relic One puede quedar como evolucion futura para centralizar metricas, dashboards, errores y trazas. No forma parte del baseline actual porque agregaria complejidad innecesaria para el alcance academico del proyecto.

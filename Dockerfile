FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:25-jre AS newrelic-agent

ARG NEW_RELIC_AGENT_VERSION=9.2.0

WORKDIR /opt/newrelic

RUN apt-get update \
    && apt-get install -y --no-install-recommends wget unzip \
    && wget --quiet --output-document=/tmp/newrelic-java.zip "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NEW_RELIC_AGENT_VERSION}/newrelic-java-${NEW_RELIC_AGENT_VERSION}.zip" \
    && unzip -q /tmp/newrelic-java.zip -d /tmp \
    && cp /tmp/newrelic/newrelic.jar /tmp/newrelic/newrelic.yml /opt/newrelic/ \
    && mkdir -p /opt/newrelic/logs \
    && rm -rf /tmp/newrelic /tmp/newrelic-java.zip /var/lib/apt/lists/*

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

RUN addgroup --system spring && adduser --system spring --ingroup spring

COPY --from=build /app/target/*.jar app.jar
COPY --from=newrelic-agent /opt/newrelic /opt/newrelic

RUN chown -R spring:spring /app /opt/newrelic

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

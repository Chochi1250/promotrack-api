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

WORKDIR /tmp/newrelic-download

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends wget unzip; \
    rm -rf /var/lib/apt/lists/*; \
    wget --quiet --output-document=newrelic-java.zip "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NEW_RELIC_AGENT_VERSION}/newrelic-java-${NEW_RELIC_AGENT_VERSION}.zip"; \
    test -s newrelic-java.zip; \
    unzip -t newrelic-java.zip; \
    unzip -q newrelic-java.zip -d extracted; \
    test -s extracted/newrelic/newrelic.jar; \
    test -s extracted/newrelic/newrelic.yml; \
    mkdir -p /opt/newrelic/logs; \
    cp extracted/newrelic/newrelic.jar /opt/newrelic/newrelic.jar; \
    cp extracted/newrelic/newrelic.yml /opt/newrelic/newrelic.yml; \
    java -jar /opt/newrelic/newrelic.jar -v; \
    cd /; \
    rm -rf /tmp/newrelic-download

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

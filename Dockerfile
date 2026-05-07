FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN addgroup --system spring && adduser --system spring --ingroup spring

COPY --from=build /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

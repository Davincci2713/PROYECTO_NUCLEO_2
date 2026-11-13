# Un ARG de módulo elige el jar. Los defaults de localhost viven en los YAML;
# Compose los sobreescribe con variables de entorno.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests package -Djacoco.skip=true

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
ARG MODULO
COPY --from=build /build/${MODULO}/target/${MODULO}-0.1.0.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

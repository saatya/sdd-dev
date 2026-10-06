# ---- build stage -------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Tests run in CI / `make test`; skip here to keep image builds fast.
RUN mvn -q -B package -DskipTests

# ---- runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Non-root user (AC-13). UID matches runAsUser in k8s/deployment.yaml.
RUN groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --no-create-home app

COPY --from=build /src/target/hello-k8s-sidecar-1.0.0.jar /app/app.jar

USER 10001
EXPOSE 8080
# -XX:MaxRAMPercentage sizes the heap from the container memory limit.
# java.io.tmpdir points at a writable emptyDir because the root FS is read-only.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Djava.io.tmpdir=/tmp", "-jar", "/app/app.jar"]

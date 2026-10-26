FROM maven:3.9.15-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp verify

FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system clyvovet && useradd --system --gid clyvovet --create-home clyvovet
WORKDIR /app
COPY --from=build --chown=clyvovet:clyvovet /workspace/target/clyvovet-0.0.1-SNAPSHOT.jar app.jar
USER clyvovet
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

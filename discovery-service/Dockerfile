FROM gradle:8.10.2-jdk21-alpine AS build
WORKDIR /workspace
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

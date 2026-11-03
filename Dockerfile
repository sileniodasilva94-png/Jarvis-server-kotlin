FROM gradle:8.10-jdk17 AS build
WORKDIR /workspace

COPY . .

# Cria a pasta correta e move os arquivos soltos
RUN mkdir -p app/src/main/kotlin/com/teaupp/jarvis && \
    find . -maxdepth 1 -name "*.kt" -exec mv {} app/src/main/kotlin/com/teaupp/jarvis/ \; && \
    mv build.gradle.kts app/ 2>/dev/null || true && \
    mv settings.gradle.kts . 2>/dev/null || true

# Vai para a pasta app e compila
WORKDIR /workspace/app
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

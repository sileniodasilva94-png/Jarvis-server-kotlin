FROM gradle:8.10-jdk17 AS build
WORKDIR /app

# Copia todos os arquivos soltos
COPY . .

# Cria a estrutura de pastas correta que o Kotlin exige
RUN mkdir -p app/src/main/kotlin/com/teaupp/jarvis && \
    mv *.kt app/src/main/kotlin/com/teaupp/jarvis/ 2>/dev/null || true && \
    mv build.gradle.kts app/ 2>/dev/null || true && \
    mv settings.gradle.kts . 2>/dev/null || true

# Constrói o projeto
RUN cd app && gradle build --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

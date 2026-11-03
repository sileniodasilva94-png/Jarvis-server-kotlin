FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Baixa o compilador do Kotlin
RUN apt-get update && apt-get install -y curl unzip && \
    curl -sSL https://github.com/JetBrains/kotlin/releases/download/v1.9.22/kotlin-compiler-1.9.22.zip -o kotlin.zip && \
    unzip kotlin.zip -d /opt && \
    ln -s /opt/kotlinc/bin/kotlinc /usr/local/bin/kotlinc

# Baixa as bibliotecas do Ktor e Logback direto do Maven
RUN mkdir -p libs && cd libs && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-server-core-jvm/2.3.12/ktor-server-core-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-server-netty-jvm/2.3.12/ktor-server-netty-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-server-content-negotiation-jvm/2.3.12/ktor-server-content-negotiation-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-serialization-kotlinx-json-jvm/2.3.12/ktor-serialization-kotlinx-json-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-server-cors-jvm/2.3.12/ktor-server-cors-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-server-status-pages-jvm/2.3.12/ktor-server-status-pages-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-client-core-jvm/2.3.12/ktor-client-core-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-client-cio-jvm/2.3.12/ktor-client-cio-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/io/ktor/ktor-client-content-negotiation-jvm/2.3.12/ktor-client-content-negotiation-jvm-2.3.12.jar && \
    curl -sLO https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-json-jvm/1.7.3/kotlinx-serialization-json-jvm-1.7.3.jar && \
    curl -sLO https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar && \
    curl -sLO https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.6/logback-classic-1.5.6.jar && \
    curl -sLO https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.6/logback-core-1.5.6.jar && \
    curl -sLO https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.8.0/kotlinx-coroutines-core-jvm-1.8.0.jar && \
    curl -sLO https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-core-jvm/1.7.3/kotlinx-serialization-core-jvm-1.7.3.jar

# Copia os arquivos .kt da raiz
COPY *.kt /app/src/

# Compila o Kotlin usando a biblioteca baixada
RUN kotlinc /app/src/*.kt -include-runtime -d /app/jarvis.jar -cp "/app/libs/*"

# Imagem final
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/jarvis.jar .
EXPOSE 8080
CMD ["java", "-jar", "jarvis.jar"]

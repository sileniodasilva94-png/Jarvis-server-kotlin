package com.teaupp.jarvis

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun main() {
    val log = LoggerFactory.getLogger("JarvisServer")
    log.info("Iniciando Jarvis Server na porta ${Config.PORT}")
    
    embeddedServer(Netty, port = Config.PORT, host = "0.0.0.0") {
        install(ContentNegotiation) { 
            json(Json { ignoreUnknownKeys = true; prettyPrint = true }) 
        }
        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowHeader("X-Token")
            allowHeader("X-Device")
        }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                log.error("Erro", cause)
                call.respond(HttpStatusCode.InternalServerError,
                    mapOf("erro" to (cause.message ?: "Erro interno")))
            }
        }
        routing { setupRoutes() }
    }.start(wait = true)
}
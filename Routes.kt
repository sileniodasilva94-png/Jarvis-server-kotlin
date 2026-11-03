package com.teaupp.jarvis

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

fun Route.setupRoutes() {

    get("/") {
        call.respond(mapOf(
            "service" to "Jarvis Server (Kotlin)",
            "status" to "online",
            "model" to Config.OPENAI_MODEL
        ))
    }

    get("/health") {
        call.respond(HealthResponse(
            status = "ok",
            openai = Config.OPENAI_KEY.isNotEmpty(),
            eleven = Config.ELEVEN_KEY.isNotEmpty()
        ))
    }

    post("/chat") {
        val req = call.receive<ChatRequest>()
        if (Config.OPENAI_KEY.isEmpty()) {
            call.respond(ChatResponse("O servidor está sem a chave da OpenAI configurada, senhor."))
            return@post
        }
        val mensagens = mutableListOf(OpenAIMessage("system", systemPrompt()))
        req.facts?.takeIf { it.isNotEmpty() }?.let {
            mensagens.add(OpenAIMessage("system",
                "Fatos que você já aprendeu sobre o senhor:\n- " + it.take(20).joinToString("\n- ")))
        }
        mensagens.add(OpenAIMessage("user", req.mensagem))
        call.respond(ChatResponse(chamarOpenAI(mensagens)))
    }

    post("/vision") {
        val req = call.receive<VisionRequest>()
        if (Config.OPENAI_KEY.isEmpty()) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("erro" to "OpenAI não configurada"))
            return@post
        }
        try {
            val resp: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer ${Config.OPENAI_KEY}")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "model" to "gpt-4o-mini",
                    "messages" to listOf(mapOf(
                        "role" to "user",
                        "content" to listOf(
                            mapOf("type" to "text", "text" to req.prompt),
                            mapOf("type" to "image_url",
                                "image_url" to mapOf("url" to "data:image/jpeg;base64,${req.image}"))
                        )
                    )),
                    "max_tokens" to 500
                ))
            }
            if (resp.status.value !in 200..299) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("erro" to "Falha na visão"))
                return@post
            }
            val texto = resp.body<String>()
            val regex = Regex("\"content\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
            val achado = regex.find(texto)?.groupValues?.get(1)?.replace("\\n", "\n") ?: "Sem resposta"
            call.respond(mapOf("resposta" to achado))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("erro" to "Erro: ${e::class.simpleName}"))
        }
    }

    post("/tts") {
        val req = call.receive<TTSRequest>()
        val voz = req.voice_id ?: Config.ELEVEN_VOICE
        val audio = chamarEleven(req.text, voz)
        if (audio == null) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("erro" to "ElevenLabs não configurada ou falhou"))
        } else {
            call.respondBytes(audio.first, ContentType.parse(audio.second))
        }
    }

    get("/voices") {
        if (Config.ELEVEN_KEY.isEmpty()) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("erro" to "ElevenLabs não configurada"))
            return@get
        }
        try {
            val resp: HttpResponse = httpClient.get("https://api.elevenlabs.io/v1/voices") {
                header("xi-api-key", Config.ELEVEN_KEY)
            }
            if (resp.status.value !in 200..299) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("erro" to "Falha ao listar vozes"))
                return@get
            }
            val texto = resp.body<String>()
            val regex = Regex("\"voice_id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"name\"\\s*:\\s*\"([^\"]+)\"")
            val vozes = regex.findAll(texto).map { Voice(it.groupValues[1], it.groupValues[2]) }.toList()
            call.respond(VoicesResponse(vozes))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("erro" to "Erro: ${e::class.simpleName}"))
        }
    }

    post("/sync") {
        val req = call.receive<SyncRequest>()
        val novos = (req.facts ?: emptyList()).map { it.trim() }
            .filter { it.length in 3..300 }.take(50)
        call.respond(mapOf(
            "ok" to true,
            "received_messages" to (req.messages?.size ?: 0),
            "received_facts" to (req.facts?.size ?: 0),
            "facts" to novos.take(10)
        ))
    }
}
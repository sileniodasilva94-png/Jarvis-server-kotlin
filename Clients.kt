package com.teaupp.jarvis

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object Config {
    val OPENAI_KEY: String = System.getenv("OPENAI_API_KEY") ?: ""
    val OPENAI_MODEL: String = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini"
    val ELEVEN_KEY: String = System.getenv("ELEVEN_API_KEY") ?: ""
    val ELEVEN_VOICE: String = System.getenv("ELEVEN_VOICE_ID") ?: "pNInz6obpgDQGcFmaJgB"
    val SERVER_TOKEN: String = System.getenv("SERVER_TOKEN") ?: ""
    val CREATOR_NAME: String = System.getenv("CREATOR_NAME") ?: "Silenio da Silva"
    val ASSISTANT_NAME: String = System.getenv("ASSISTANT_NAME") ?: "Jarvis"
    val PORT: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
}

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    engine { requestTimeout = 90_000 }
}

fun systemPrompt(): String =
    "Você é ${Config.ASSISTANT_NAME}, assistente pessoal do senhor ${Config.CREATOR_NAME}. " +
    "Responda SEMPRE em português do Brasil, de forma educada, concisa e prática. " +
    "Chame o usuário de 'senhor'. Evite respostas longas. " +
    "Se não souber algo, diga claramente. Nunca invente informações."

suspend fun chamarOpenAI(mensagens: List<OpenAIMessage>): String {
    if (Config.OPENAI_KEY.isEmpty()) {
        return "O servidor está sem a chave da OpenAI configurada, senhor."
    }
    return try {
        val resp: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer ${Config.OPENAI_KEY}")
            contentType(ContentType.Application.Json)
            setBody(OpenAIRequest(Config.OPENAI_MODEL, mensagens))
        }
        if (resp.status.value !in 200..299) {
            "Erro do servidor de IA (${resp.status.value}), senhor."
        } else {
            resp.body<OpenAIResponse>().choices.firstOrNull()?.message?.content?.trim()
                ?: "O cérebro não devolveu resposta, senhor."
        }
    } catch (e: Exception) {
        "Falha interna: ${e::class.simpleName}"
    }
}

suspend fun chamarEleven(texto: String, voz: String): Pair<ByteArray, String>? {
    if (Config.ELEVEN_KEY.isEmpty()) return null
    return try {
        val resp: HttpResponse = httpClient.post("https://api.elevenlabs.io/v1/text-to-speech/$voz") {
            header("xi-api-key", Config.ELEVEN_KEY)
            contentType(ContentType.Application.Json)
            setBody(mapOf("text" to texto, "model_id" to "eleven_multilingual_v2"))
        }
        if (resp.status.value in 200..299) resp.body<ByteArray>() to "audio/mpeg" else null
    } catch (e: Exception) {
        null
    }
}
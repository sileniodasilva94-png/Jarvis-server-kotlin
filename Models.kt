package com.teaupp.jarvis

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val mensagem: String,
    val mode: String? = "online",
    val facts: List<String>? = null
)

@Serializable
data class ChatResponse(val resposta: String)

@Serializable
data class VisionRequest(val image: String, val prompt: String)

@Serializable
data class TTSRequest(val text: String, val voice_id: String? = null)

@Serializable
data class SyncMessage(val ts: Long, val role: String, val text: String)

@Serializable
data class SyncRequest(
    val messages: List<SyncMessage>? = null,
    val facts: List<String>? = null
)

@Serializable
data class HealthResponse(val status: String, val openai: Boolean, val eleven: Boolean)

@Serializable
data class Voice(val id: String, val name: String)

@Serializable
data class VoicesResponse(val voices: List<Voice>)

@Serializable
data class OpenAIMessage(val role: String, val content: String)

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 500
)

@Serializable
data class OpenAIChoice(val message: OpenAIMessage)

@Serializable
data class OpenAIResponse(val choices: List<OpenAIChoice>)
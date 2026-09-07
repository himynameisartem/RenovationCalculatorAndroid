package com.tekhnologiistroitelstva.renovationcalculator.ui.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ChatApiClient {
    private val endpoint = URL("https://cucosinepsiey.beget.app/chat")

    suspend fun send(messages: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val requestMessages = JSONArray().apply {
            messages.forEach { message ->
                put(
                    JSONObject()
                        .put("role", if (message.role == ChatRole.User) "user" else "assistant")
                        .put("content", message.text)
                )
            }
        }
        val body = JSONObject()
            .put("messages", requestMessages)
            .toString()

        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        try {
            connection.outputStream.use { output ->
                output.write(body.toByteArray(Charsets.UTF_8))
            }

            val code = connection.responseCode
            val responseText = if (code in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            }

            if (code !in 200..299) {
                throw IllegalStateException("Server error: $code")
            }

            JSONObject(responseText).optString("answer").ifBlank {
                throw IllegalStateException("Empty answer")
            }
        } finally {
            connection.disconnect()
        }
    }
}

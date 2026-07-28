package com.skfamily.renovationcalculatir.ui.chat

import java.util.UUID

enum class ChatRole {
    Assistant,
    User
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val text: String
)

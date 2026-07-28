package com.skfamily.renovationcalculatir.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.Assistant,
            text = "Здравствуйте. Я помогу с вопросами по ремонту, стоимости работ и услугам компании."
        )
    ),
    val draft: String = "",
    val isSending: Boolean = false,
    val errorText: String? = null
)

class ChatViewModel(
    private val apiClient: ChatApiClient = ChatApiClient()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val maxMessageLength = 500

    fun updateDraft(value: String) {
        _uiState.update {
            it.copy(
                draft = value.take(maxMessageLength),
                errorText = null
            )
        }
    }

    fun send() {
        val text = uiState.value.draft.trim()
        if (text.isEmpty() || uiState.value.isSending) return

        _uiState.update {
            it.copy(
                draft = "",
                isSending = true,
                errorText = null,
                messages = it.messages + ChatMessage(role = ChatRole.User, text = text)
            )
        }

        viewModelScope.launch {
            runCatching { apiClient.send(text) }
                .onSuccess { answer ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            messages = it.messages + ChatMessage(role = ChatRole.Assistant, text = answer)
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorText = "Не удалось получить ответ. Проверьте подключение и попробуйте еще раз."
                        )
                    }
                }
        }
    }
}

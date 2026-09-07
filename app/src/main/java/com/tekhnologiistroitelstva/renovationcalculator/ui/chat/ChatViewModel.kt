package com.tekhnologiistroitelstva.renovationcalculator.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val errorText: String? = null,
    val remainingQuestions: Int = 10
) {
    val isSessionLimitReached: Boolean
        get() = remainingQuestions == 0
}

class ChatViewModel(
    private val apiClient: ChatApiClient = ChatApiClient()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val maxMessageLength = 500
    private val conversationMessages = mutableListOf<ChatMessage>()
    private var lastQuestionAtMillis: Long? = null
    private var expirationJob: Job? = null

    fun updateDraft(value: String) {
        if (uiState.value.isSessionLimitReached) return

        _uiState.update {
            it.copy(
                draft = value.take(maxMessageLength),
                errorText = null
            )
        }
    }

    fun send() {
        refreshSessionState()

        val text = uiState.value.draft.trim()
        if (text.isEmpty() || uiState.value.isSending) return

        if (uiState.value.isSessionLimitReached) {
            _uiState.update {
                it.copy(errorText = "Лимит 10 вопросов исчерпан. Диалог очистится через 5 минут после последнего вопроса.")
            }
            return
        }

        val userMessage = ChatMessage(role = ChatRole.User, text = text)
        conversationMessages += userMessage
        val requestMessages = conversationMessages
            .dropLast(1)
            .takeLast(MAX_HISTORY_MESSAGES) + userMessage

        lastQuestionAtMillis = SystemClock.elapsedRealtime()
        scheduleExpiration()

        _uiState.update {
            it.copy(
                draft = "",
                isSending = true,
                errorText = null,
                remainingQuestions = it.remainingQuestions - 1,
                messages = it.messages + userMessage
            )
        }

        viewModelScope.launch {
            runCatching { apiClient.send(requestMessages) }
                .onSuccess { answer ->
                    val assistantMessage = ChatMessage(role = ChatRole.Assistant, text = answer)
                    conversationMessages += assistantMessage
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            messages = it.messages + assistantMessage
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

    fun refreshSessionState(nowMillis: Long = SystemClock.elapsedRealtime()) {
        val lastQuestionAt = lastQuestionAtMillis ?: return
        if (nowMillis - lastQuestionAt >= SESSION_TIMEOUT_MILLIS) {
            resetSession()
        }
    }

    private fun scheduleExpiration() {
        expirationJob?.cancel()
        expirationJob = viewModelScope.launch {
            delay(SESSION_TIMEOUT_MILLIS)
            refreshSessionState()
        }
    }

    private fun resetSession() {
        expirationJob?.cancel()
        expirationJob = null
        conversationMessages.clear()
        lastQuestionAtMillis = null
        _uiState.value = ChatUiState()
    }

    private companion object {
        const val MAX_HISTORY_MESSAGES = 10
        const val SESSION_TIMEOUT_MILLIS = 5 * 60 * 1000L
    }
}

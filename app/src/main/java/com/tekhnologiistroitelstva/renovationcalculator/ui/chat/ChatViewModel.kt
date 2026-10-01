package com.tekhnologiistroitelstva.renovationcalculator.ui.chat

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

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
    val isSessionLimitReached: Boolean get() = remainingQuestions == 0
}

class ChatViewModel(
    private val apiClient: ChatApiClient = ChatApiClient(),
    private val photoEstimateApiClient: PhotoEstimateApiClient = PhotoEstimateApiClient()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val maxMessageLength = 500
    private val conversationMessages = mutableListOf<ChatMessage>()
    private var lastQuestionAtMillis: Long? = null
    private var expirationJob: Job? = null
    private var photoStep = PhotoStep.Idle
    private var pendingSurfaces: RoomSurfaceAnalysis? = null
    private var pendingArea: Double? = null
    private var pendingImageUris = emptyList<String>()
    private var didShowPhotoHint = false

    fun updateDraft(value: String) {
        if (uiState.value.isSessionLimitReached && photoStep == PhotoStep.Idle) return
        _uiState.update { it.copy(draft = value.take(maxMessageLength), errorText = null) }
    }

    fun send() {
        refreshSessionState()
        val text = uiState.value.draft.trim()
        if (text.isEmpty() || uiState.value.isSending) return

        if (photoStep == PhotoStep.AwaitingArea || photoStep == PhotoStep.AwaitingHeight) {
            sendPhotoParameter(text)
            return
        }
        if (uiState.value.isSessionLimitReached) {
            _uiState.update {
                it.copy(errorText = "Лимит 10 вопросов исчерпан. Диалог очистится через 5 минут после последнего вопроса.")
            }
            return
        }

        val userMessage = ChatMessage(role = ChatRole.User, text = text)
        conversationMessages += userMessage
        val requestMessages = conversationMessages.dropLast(1).takeLast(MAX_HISTORY_MESSAGES) + userMessage
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
                    val assistant = ChatMessage(role = ChatRole.Assistant, text = answer)
                    conversationMessages += assistant
                    _uiState.update { it.copy(isSending = false, messages = it.messages + assistant) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorText = "Не удалось получить ответ. Проверьте подключение и попробуйте еще раз.",
                            messages = it.messages + ChatMessage(
                                role = ChatRole.Assistant,
                                text = "Не удалось получить ответ. Проверьте подключение и попробуйте еще раз."
                            )
                        )
                    }
                }
        }
    }

    fun consumePhotoHint(): Boolean {
        if (didShowPhotoHint) return false
        didShowPhotoHint = true
        return true
    }

    fun beginPhotoEstimate(context: Context, imageUris: List<Uri>) {
        if (imageUris.isEmpty() || uiState.value.isSending) return
        val addingDetails = photoStep == PhotoStep.AwaitingArea && pendingImageUris.isNotEmpty()
        val combined = ((if (addingDetails) pendingImageUris else emptyList()) + imageUris.map(Uri::toString)).take(3)
        if (!addingDetails) {
            pendingSurfaces = null
            pendingArea = null
        }
        pendingImageUris = combined
        photoStep = PhotoStep.Analyzing
        _uiState.update {
            it.copy(
                draft = "",
                errorText = null,
                isSending = true,
                messages = it.messages + ChatMessage(
                    role = ChatRole.User,
                    text = "",
                    imageUris = imageUris.map(Uri::toString)
                )
            )
        }
        viewModelScope.launch {
            runCatching {
                MobileCvService.getInstance(context).analyze(combined.map(Uri::parse))
            }.onSuccess { surfaces ->
                pendingSurfaces = surfaces
                photoStep = PhotoStep.AwaitingArea
                val missing = missingSurfaceNames(surfaces)
                val answer = if (missing.isEmpty()) {
                    "Введите площадь помещения в м²."
                } else {
                    "Для более точного результата добавьте фото, где лучше видны ${joinSurfaceNames(missing)}. Либо сразу введите площадь помещения — расчёт будет выполнен по имеющимся фотографиям."
                }
                val promptMessage = ChatMessage(role = ChatRole.Assistant, text = answer)
                conversationMessages += ChatMessage(
                    role = ChatRole.User,
                    text = photoAnalysisContext(surfaces)
                )
                conversationMessages += promptMessage
                _uiState.update {
                    it.copy(isSending = false, messages = it.messages + promptMessage)
                }
            }.onFailure { error ->
                pendingSurfaces = null
                pendingArea = null
                pendingImageUris = emptyList()
                photoStep = PhotoStep.Idle
                val answer = when (error) {
                    MobileCvException.NotRoom -> "На снимке не удалось увидеть помещение. Добавьте фото комнаты общим планом, чтобы были видны стены и пол или потолок."
                    MobileCvException.NoSurfaces -> "По этому снимку не удалось оценить состояние комнаты. Добавьте другое фото общим планом."
                    else -> "Не удалось распознать поверхности на фотографиях. Попробуйте выбрать другие снимки."
                }
                val failureMessage = ChatMessage(role = ChatRole.Assistant, text = answer)
                conversationMessages += ChatMessage(
                    role = ChatRole.User,
                    text = when (error) {
                        MobileCvException.NotRoom -> "Пользователь загрузил фотографию, но CV не смог подтвердить помещение и не определил материалы."
                        MobileCvException.NoSurfaces -> "Пользователь загрузил фотографию комнаты, но CV не определил материалы поверхностей."
                        else -> "При анализе загруженной фотографии произошла ошибка."
                    }
                )
                conversationMessages += failureMessage
                _uiState.update {
                    it.copy(isSending = false, errorText = error.localizedMessage, messages = it.messages + failureMessage)
                }
            }
        }
    }

    private fun sendPhotoParameter(text: String) {
        val value = firstNumber(text)
        if (value == null || value <= 0) {
            sendPhotoFollowUp(text)
            return
        }
        _uiState.update { it.copy(draft = "", errorText = null, messages = it.messages + ChatMessage(role = ChatRole.User, text = text)) }
        if (photoStep == PhotoStep.AwaitingArea) {
            pendingArea = value
            photoStep = PhotoStep.AwaitingHeight
            _uiState.update {
                it.copy(messages = it.messages + ChatMessage(role = ChatRole.Assistant, text = "Введите высоту потолка в метрах."))
            }
            return
        }
        val surfaces = pendingSurfaces ?: run { photoStep = PhotoStep.Idle; return }
        val area = pendingArea ?: run { photoStep = PhotoStep.Idle; return }
        photoStep = PhotoStep.Estimating
        _uiState.update { it.copy(isSending = true) }
        viewModelScope.launch {
            runCatching {
                photoEstimateApiClient.estimate(surfaces, "other", "Помещение", area, value)
            }.onSuccess { response ->
                val assistant = ChatMessage(role = ChatRole.Assistant, text = response.answer)
                conversationMessages += ChatMessage(
                    role = ChatRole.User,
                    text = photoConversationContext(surfaces, area, value, response.answer)
                )
                conversationMessages += assistant
                _uiState.update { it.copy(isSending = false, messages = it.messages + assistant) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorText = error.localizedMessage,
                        messages = it.messages + ChatMessage(
                            role = ChatRole.Assistant,
                            text = "Не удалось получить расчёт. Проверьте подключение и попробуйте ещё раз."
                        )
                    )
                }
            }
            pendingSurfaces = null
            pendingArea = null
            pendingImageUris = emptyList()
            photoStep = PhotoStep.Idle
        }
    }

    private fun sendPhotoFollowUp(text: String) {
        val userMessage = ChatMessage(role = ChatRole.User, text = text)
        conversationMessages += userMessage
        val requestMessages = conversationMessages.dropLast(1).takeLast(MAX_HISTORY_MESSAGES) + userMessage
        val continuation = if (photoStep == PhotoStep.AwaitingArea) {
            "Чтобы продолжить расчёт, введите площадь помещения в м²."
        } else {
            "Чтобы продолжить расчёт, введите высоту потолка в метрах."
        }
        lastQuestionAtMillis = SystemClock.elapsedRealtime()
        scheduleExpiration()
        _uiState.update {
            it.copy(
                draft = "",
                isSending = true,
                errorText = null,
                remainingQuestions = (it.remainingQuestions - 1).coerceAtLeast(0),
                messages = it.messages + userMessage
            )
        }
        viewModelScope.launch {
            runCatching { apiClient.send(requestMessages) }
                .onSuccess { answer ->
                    val assistant = ChatMessage(
                        role = ChatRole.Assistant,
                        text = "$answer\n\n$continuation"
                    )
                    conversationMessages += assistant
                    _uiState.update { it.copy(isSending = false, messages = it.messages + assistant) }
                }
                .onFailure {
                    val assistant = ChatMessage(role = ChatRole.Assistant, text = continuation)
                    conversationMessages += assistant
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorText = "Не удалось получить ответ. Проверьте подключение и попробуйте еще раз.",
                            messages = it.messages + assistant
                        )
                    }
                }
        }
    }

    private fun photoConversationContext(
        surfaces: RoomSurfaceAnalysis,
        area: Double,
        height: Double,
        estimateAnswer: String
    ): String {
        fun materials(items: List<DetectedMaterial>) = if (items.isEmpty()) "не определены" else items.joinToString(", ") {
            "${it.material}=${String.format(Locale.US, "%.3f", it.confidence)}"
        }
        return "[PHOTO_OBJECT_CONTEXT] Подтверждённый результат фото-расчёта для текущего объекта: " +
            "$estimateAnswer Параметры объекта: площадь $area м², высота $height м. " +
            "Результат CV: стены: ${materials(surfaces.walls)}; пол: ${materials(surfaces.floor)}; " +
            "потолок: ${materials(surfaces.ceiling)}. Значения уверенности не выше 0.80 считай предположениями."
    }

    private fun photoAnalysisContext(surfaces: RoomSurfaceAnalysis): String {
        fun materials(items: List<DetectedMaterial>) = if (items.isEmpty()) "не определены" else items.take(2).joinToString(", ") {
            "${it.material}=${String.format(Locale.US, "%.3f", it.confidence)}"
        }
        return "Пользователь загрузил фото комнаты. CV: стены: ${materials(surfaces.walls)}; " +
            "пол: ${materials(surfaces.floor)}; потолок: ${materials(surfaces.ceiling)}. " +
            "Уверенность не выше 0.80 — предположение."
    }

    private fun firstNumber(text: String): Double? = Regex("\\d+(?:[.,]\\d+)?")
        .find(text)?.value?.replace(',', '.')?.toDoubleOrNull()

    private fun missingSurfaceNames(surfaces: RoomSurfaceAnalysis): List<String> = buildList {
        if ("walls" !in surfaces.visibleSurfaces) add("стены")
        if ("floor" !in surfaces.visibleSurfaces) add("пол")
        if ("ceiling" !in surfaces.visibleSurfaces) add("потолок")
    }

    private fun joinSurfaceNames(names: List<String>): String = when (names.size) {
        0 -> "все поверхности"
        1 -> names.first()
        else -> names.dropLast(1).joinToString(", ") + " и " + names.last()
    }

    fun refreshSessionState(nowMillis: Long = SystemClock.elapsedRealtime()) {
        val lastQuestionAt = lastQuestionAtMillis ?: return
        if (nowMillis - lastQuestionAt >= SESSION_TIMEOUT_MILLIS) resetSession()
    }

    fun clearConversation() {
        resetSession()
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
        pendingSurfaces = null
        pendingArea = null
        pendingImageUris = emptyList()
        photoStep = PhotoStep.Idle
        _uiState.value = ChatUiState()
    }

    private enum class PhotoStep { Idle, Analyzing, AwaitingArea, AwaitingHeight, Estimating }

    private companion object {
        const val MAX_HISTORY_MESSAGES = 10
        const val SESSION_TIMEOUT_MILLIS = 5 * 60 * 1000L
    }
}

package com.tekhnologiistroitelstva.renovationcalculator.ui.chat

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ChatBubbleOverlay(
    modifier: Modifier = Modifier,
    cardsBottomPx: Float,
    presentationKey: Int,
    viewModel: ChatViewModel = viewModel()
) {
    var isOpen by remember { mutableStateOf(false) }
    var isHintExpanded by remember { mutableStateOf(false) }
    var isHintDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            viewModel.refreshSessionState()
        }
    }

    LaunchedEffect(presentationKey) {
        isHintDismissed = false
        isHintExpanded = false
        delay(250)

        while (isActive && !isHintDismissed) {
            isHintExpanded = true
            delay(7_000)
            if (isHintDismissed) break

            isHintExpanded = false
            delay(5_000)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val controlHeight = 58.dp
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val controlHeightPx = with(density) { controlHeight.toPx() }
        val freeSpacePx = containerHeightPx - cardsBottomPx - controlHeightPx
        val bottomPadding = if (cardsBottomPx > 0f && freeSpacePx >= 0f) {
            with(density) { (freeSpacePx / 2f).toDp() }
        } else {
            16.dp
        }

        if (isOpen) {
            ChatCard(
                viewModel = viewModel,
                onClose = { isOpen = false },
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 86.dp)
                    .imePadding()
            )
        } else {
            ChatHintButton(
                expanded = isHintExpanded,
                expandedWidth = maxWidth - 32.dp,
                bottomPadding = bottomPadding,
                onOpenChat = {
                    isHintDismissed = true
                    isHintExpanded = false
                    isOpen = true
                },
                onDismissHint = {
                    isHintDismissed = true
                    isHintExpanded = false
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun ChatHintButton(
    expanded: Boolean,
    expandedWidth: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onOpenChat: () -> Unit,
    onDismissHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedWidth by animateDpAsState(
        targetValue = if (expanded) expandedWidth else 58.dp,
        animationSpec = if (expanded) spring() else tween(durationMillis = 250),
        label = "chat_hint_width"
    )

    Box(
        modifier = modifier
            .padding(end = 16.dp, bottom = bottomPadding)
            .width(animatedWidth)
            .height(58.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(Color(0xFF3F7BE3), Color(0xFF5BA6F2)))
            )
    ) {
        if (expanded) {
            IconButton(
                onClick = onDismissHint,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть подсказку",
                    tint = Color.White.copy(alpha = 0.86f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (expanded) {
            Text(
                text = "Есть вопрос по ремонту?\nСпросите ИИ-помощника",
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 52.dp, end = 66.dp)
                    .clickable(onClick = onOpenChat)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(58.dp)
                .clickable(onClick = onOpenChat),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubble,
                contentDescription = "ИИ помощник",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ChatCard(
    viewModel: ChatViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showClearConversationDialog by remember { mutableStateOf(false) }
    var showPhotoHint by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.beginPhotoEstimate(context, uris.take(3))
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = cameraUri
        if (success && uri != null) viewModel.beginPhotoEstimate(context, listOf(uri))
    }

    LaunchedEffect(Unit) {
        showPhotoHint = viewModel.consumePhotoHint()
    }

    LaunchedEffect(uiState.messages.size, uiState.isSending) {
        val lastIndex = uiState.messages.lastIndex + if (uiState.isSending) 1 else 0
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp, max = 500.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ChatHeader(
                canClear = uiState.messages.size > 1,
                onClear = { showClearConversationDialog = true },
                onClose = {
                    focusManager.clearFocus()
                    onClose()
                },
                onTap = { focusManager.clearFocus() }
            )

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { focusManager.clearFocus() }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageRow(message = message)
                }

                if (uiState.isSending) {
                    item(key = "typing") {
                        TypingRow()
                    }
                }
            }

            ChatInputBar(
                uiState = uiState,
                onDraftChange = viewModel::updateDraft,
                onSend = viewModel::send,
                onAddPhoto = { showPhotoSourceDialog = true },
                showPhotoHint = showPhotoHint,
                onDismissPhotoHint = { showPhotoHint = false }
            )
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Добавить фото помещения") },
            text = { Text("Можно добавить от одной до трёх фотографий.") },
            confirmButton = {
                IconButton(onClick = {
                    showPhotoSourceDialog = false
                    val file = File.createTempFile("room_", ".jpg", context.cacheDir)
                    cameraUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    cameraLauncher.launch(cameraUri!!)
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Камера")
                }
            },
            dismissButton = {
                IconButton(onClick = {
                    showPhotoSourceDialog = false
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Галерея")
                }
            }
        )
    }

    if (showClearConversationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConversationDialog = false },
            title = { Text("Очистить диалог?") },
            text = { Text("Сообщения и результат анализа фотографий будут удалены.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConversationDialog = false
                    showPhotoHint = false
                    cameraUri = null
                    focusManager.clearFocus()
                    viewModel.clearConversation()
                }) {
                    Text("Очистить", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConversationDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ChatHeader(
    canClear: Boolean,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onTap: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() }
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF3F7BE3).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF3F7BE3)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = "ИИ помощник",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101114)
            )
            Text(
                text = "Отвечает по вопросам ремонта",
                fontSize = 12.sp,
                color = Color(0xFF7A7F8A)
            )
        }

        IconButton(onClick = onClear, enabled = canClear) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F1F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Очистить диалог",
                    tint = Color(0xFF7A7F8A).copy(alpha = if (canClear) 1f else 0.35f),
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        IconButton(onClick = onClose) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F1F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть чат",
                    tint = Color(0xFF7A7F8A),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatMessageRow(message: ChatMessage) {
    val isUser = message.role == ChatRole.User

    Row(modifier = Modifier.fillMaxWidth()) {
        if (isUser) {
            Spacer(modifier = Modifier.weight(1f))
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isUser) Color(0xFF0A84FF) else Color(0xFFF1F2F7),
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
            ) {
                if (message.imageUris.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        message.imageUris.take(3).forEach { uri ->
                            ChatImageThumbnail(uri)
                        }
                    }
                }
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = if (isUser) Color.White else Color(0xFF101114)
                    )
                }
            }
        }

        if (!isUser) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ChatImageThumbnail(uriString: String) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, uriString) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uriString))?.use(BitmapFactory::decodeStream)
            }.getOrNull()
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Фото помещения",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(74.dp)
                .clip(RoundedCornerShape(10.dp))
        )
    }
}

@Composable
private fun TypingRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF1F2F7)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Печатает...",
                    fontSize = 13.sp,
                    color = Color(0xFF7A7F8A)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ChatInputBar(
    uiState: ChatUiState,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onAddPhoto: () -> Unit,
    showPhotoHint: Boolean,
    onDismissPhotoHint: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(14.dp)
    ) {
        uiState.errorText?.let { errorText ->
            Text(
                text = errorText,
                color = Color(0xFFD32F2F),
                fontSize = 12.sp
            )
        }

        if (uiState.isSessionLimitReached) {
            Text(
                text = "Лимит 10 вопросов достигнут. Диалог очистится через 5 минут после последнего вопроса.",
                color = Color(0xFF7A7F8A),
                fontSize = 12.sp
            )
        }

        if (showPhotoHint) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEAF2FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Для более точного рассчета можно загрузить до трех фотографий помещения.",
                        fontSize = 12.sp,
                        color = Color(0xFF2E5FA7),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissPhotoHint, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", modifier = Modifier.size(15.dp))
                    }
                }
            }
        }

        Text(
            text = "Ответы генерирует ИИ, он может ошибаться. Проверяйте важную информацию у менеджера.",
            color = Color(0xFF7A7F8A),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onAddPhoto,
                enabled = !uiState.isSending,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Добавить фото",
                    tint = Color(0xFF0A84FF)
                )
            }

            OutlinedTextField(
                value = uiState.draft,
                onValueChange = onDraftChange,
                enabled = !uiState.isSessionLimitReached,
                placeholder = { Text("Ваш вопрос") },
                minLines = 1,
                maxLines = 3,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF1F2F7),
                    unfocusedContainerColor = Color(0xFFF1F2F7)
                ),
                modifier = Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = {
                    if (uiState.draft.isNotBlank() && !uiState.isSending && !uiState.isSessionLimitReached) {
                        onSend()
                    }
                },
                containerColor = if (
                    uiState.draft.isBlank() || uiState.isSending || uiState.isSessionLimitReached
                ) {
                    Color(0xFFB8BBC2)
                } else {
                    Color(0xFF0A84FF)
                },
                contentColor = Color.White,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Отправить",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

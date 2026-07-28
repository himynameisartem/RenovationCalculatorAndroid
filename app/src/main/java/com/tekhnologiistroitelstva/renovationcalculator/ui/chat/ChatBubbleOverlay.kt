package com.tekhnologiistroitelstva.renovationcalculator.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatBubbleOverlay(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    var isOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
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
            FloatingActionButton(
                onClick = { isOpen = true },
                containerColor = Color.Transparent,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(end = 22.dp, bottom = 76.dp)
                    .size(58.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3F7BE3), Color(0xFF5BA6F2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "AI помощник"
                    )
                }
            }
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
                onSend = viewModel::send
            )
        }
    }
}

@Composable
private fun ChatHeader(
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
            Text(
                text = message.text,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = if (isUser) Color.White else Color(0xFF101114),
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
            )
        }

        if (!isUser) {
            Spacer(modifier = Modifier.weight(1f))
        }
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
    onSend: () -> Unit
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
            OutlinedTextField(
                value = uiState.draft,
                onValueChange = onDraftChange,
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
                onClick = onSend,
                containerColor = if (uiState.draft.isBlank() || uiState.isSending) {
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

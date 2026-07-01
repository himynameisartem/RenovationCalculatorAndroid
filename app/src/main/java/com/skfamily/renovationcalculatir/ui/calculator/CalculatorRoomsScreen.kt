package com.skfamily.renovationcalculatir.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.skfamily.renovationcalculatir.ui.models.RoomDraftInput
import com.skfamily.renovationcalculatir.ui.onboarding.OnboardingKeys
import com.skfamily.renovationcalculatir.ui.onboarding.OnboardingOverlay
import com.skfamily.renovationcalculatir.ui.onboarding.OnboardingPage
import com.skfamily.renovationcalculatir.ui.onboarding.OnboardingPrefs
import androidx.compose.runtime.LaunchedEffect

private enum class RoomType(val title: String) {
    LIVING("Жилая"),
    KITCHEN("Кухня"),
    BATHROOM("Санузел"),
    HALLWAY("Прихожая"),
}

@Stable
private class RoomDraft(
    val id: String,
    val type: RoomType,
) {
    var title by mutableStateOf(if (id.endsWith("_1")) type.title else "${type.title} ${id.substringAfter('_')}")
    var areaText by mutableStateOf("")
    var heightText by mutableStateOf("2.7")
}

@Composable
fun CalculatorRoomsScreen(
    onSkip: () -> Unit,
    onContinue: (List<RoomDraftInput>) -> Unit,
) {
    val context = LocalContext.current
    var livingCount by remember { mutableStateOf(0) }
    var kitchenCount by remember { mutableStateOf(0) }
    var bathroomCount by remember { mutableStateOf(0) }
    var hallwayCount by remember { mutableStateOf(0) }
    var showOnboarding by remember { mutableStateOf(false) }
    val rooms = remember { mutableStateListOf<RoomDraft>() }
    val onboardingPrefs = remember(context) { OnboardingPrefs(context) }
    val onboardingPages = remember {
        listOf(
            OnboardingPage(
                title = "Шаг 1. Помещения",
                description = "Укажите количество комнат, затем заполните названия и площадь для расчета работ по помещениям."
            ),
            OnboardingPage(
                title = "Гибкий старт",
                description = "Если комнаты пока не нужны, можно нажать «Пропустить» и перейти к выбору работ без них."
            ),
            OnboardingPage(
                title = "Что дальше",
                description = "После заполнения комнат нажмите «Продолжить», чтобы перейти к категориям работ и собрать смету."
            )
        )
    }

    LaunchedEffect(Unit) {
        if (onboardingPrefs.shouldShow(OnboardingKeys.CALCULATOR)) {
            showOnboarding = true
            onboardingPrefs.markShown(OnboardingKeys.CALCULATOR)
        }
    }

    fun syncRooms(type: RoomType, count: Int) {
        val current = rooms.filter { it.type == type }
        when {
            current.size < count -> {
                repeat(count - current.size) {
                    val index = current.size + it + 1
                    rooms.add(
                        RoomDraft(
                            id = "${type.name}_$index",
                            type = type
                        )
                    )
                }
            }

            current.size > count -> {
                val toDelete = current.takeLast(current.size - count).map { it.id }.toSet()
                rooms.removeAll { it.id in toDelete }
            }
        }
    }

    syncRooms(RoomType.LIVING, livingCount)
    syncRooms(RoomType.KITCHEN, kitchenCount)
    syncRooms(RoomType.BATHROOM, bathroomCount)
    syncRooms(RoomType.HALLWAY, hallwayCount)

    val isContinueEnabled = rooms.isNotEmpty()
    val totalArea = rooms.sumOf { it.areaText.replace(",", ".").toDoubleOrNull() ?: 0.0 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Калькулятор",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                IconButton(
                    onClick = { showOnboarding = true },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Подсказки",
                        tint = Color(0xFF101114)
                    )
                }
            }
            StepsHeader()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Укажите помещения",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Выберите количество комнат и заполните параметры.",
                            color = Color(0xFF6E737D)
                        )
                    }

                    item {
                        RoomCountCard(
                            title = "Жилые комнаты",
                            icon = Icons.Default.Home,
                            iconTint = Color(0xFF5B93EA),
                            iconBackgroundColor = Color(0x1F5B93EA),
                            count = livingCount,
                            onMinus = { if (livingCount > 0) livingCount-- },
                            onPlus = { livingCount++ }
                        )
                    }
                    item {
                        RoomCountCard(
                            title = "Кухня",
                            icon = Icons.Default.Restaurant,
                            iconTint = Color(0xFF4CAF6E),
                            iconBackgroundColor = Color(0x1F4CAF6E),
                            count = kitchenCount,
                            onMinus = { if (kitchenCount > 0) kitchenCount-- },
                            onPlus = { kitchenCount++ }
                        )
                    }
                    item {
                        RoomCountCard(
                            title = "Санузел",
                            icon = Icons.Default.Bathtub,
                            iconTint = Color(0xFF8264C8),
                            iconBackgroundColor = Color(0x1F8264C8),
                            count = bathroomCount,
                            onMinus = { if (bathroomCount > 0) bathroomCount-- },
                            onPlus = { bathroomCount++ }
                        )
                    }
                    item {
                        RoomCountCard(
                            title = "Прихожая",
                            icon = Icons.Default.MeetingRoom,
                            iconTint = Color(0xFFE07A4E),
                            iconBackgroundColor = Color(0x1FE07A4E),
                            count = hallwayCount,
                            onMinus = { if (hallwayCount > 0) hallwayCount-- },
                            onPlus = { hallwayCount++ }
                        )
                    }

                    item {
                        if (rooms.isNotEmpty()) {
                            Text(
                                text = "Параметры комнат",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    if (rooms.isNotEmpty()) {
                        items(rooms, key = { it.id }) { room ->
                            RoomParamsCard(room = room)
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(Color(0xFFEAF1FF), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SquareFoot,
                                            contentDescription = null,
                                            tint = Color(0xFF2A6FF3),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text("Общая площадь")
                                }
                                Text(String.format("%.1f м²", totalArea), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(96.dp)) }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color(0xFFD7D8DD),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Пропустить", modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val mapped = rooms.mapNotNull { room ->
                                    val area = room.areaText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    if (area > 0) RoomDraftInput(room.title, area) else null
                                }
                                onContinue(mapped)
                            },
                            enabled = isContinueEnabled,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Продолжить", modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        if (showOnboarding) {
            OnboardingOverlay(
                pages = onboardingPages,
                onDismiss = { showOnboarding = false }
            )
        }
    }
}

@Composable
private fun StepsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StepItem(number = 1, title = "Помещения", active = true)
        StepItem(number = 2, title = "Работы", active = false)
        StepItem(number = 3, title = "Итог", active = false)
    }
    HorizontalDivider(color = Color(0xFFE3E4E8))
}

@Composable
private fun StepItem(number: Int, title: String, active: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    color = if (active) Color(0xFF2A6FF3) else Color(0xFFD0D2D8),
                    shape = RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = if (active) Color.White else Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = title,
            color = if (active) Color(0xFF2A6FF3) else Color(0xFF8A8E98),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RoomCountCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackgroundColor: Color,
    count: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(iconBackgroundColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(title, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onMinus) { Icon(Icons.Default.Remove, contentDescription = null) }
            Text("$count", fontWeight = FontWeight.Bold)
            IconButton(onClick = onPlus) { Icon(Icons.Default.Add, contentDescription = null) }
        }
    }
}

@Composable
private fun RoomParamsCard(room: RoomDraft) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = room.title,
                onValueChange = { room.title = it },
                label = { Text("Название комнаты") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                    OutlinedTextField(
                        value = room.areaText,
                        onValueChange = { room.areaText = it },
                        label = { Text("Площадь, м²") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = room.heightText,
                        onValueChange = { room.heightText = it },
                        label = { Text("Высота, м") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

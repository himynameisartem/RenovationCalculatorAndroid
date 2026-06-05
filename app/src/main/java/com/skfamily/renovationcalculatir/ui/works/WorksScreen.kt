package com.skfamily.renovationcalculatir.ui.works

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skfamily.renovationcalculatir.data.CatalogCategory
import com.skfamily.renovationcalculatir.data.CatalogItem
import com.skfamily.renovationcalculatir.data.CatalogRepository
import com.skfamily.renovationcalculatir.ui.models.RoomDraftInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WorksScreen(
    rooms: List<RoomDraftInput>,
    onBackToRooms: () -> Unit,
    onFinish: (List<SummaryLine>, Int) -> Unit,
    vm: WorksViewModel = viewModel()
) {
    val state = vm.state
    val selectedCategory = state.categories.getOrNull(state.selectedCategoryIndex)
    val categoryTabsState = rememberLazyListState()
    var selectedItemForDialog by remember { mutableStateOf<CatalogItem?>(null) }
    var showSummary by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        TopBackBar(onBackToRooms)
        StepsHeader()
        CategoryTabs(
            categories = state.categories,
            selected = state.selectedCategoryIndex,
            listState = categoryTabsState,
            onSelect = vm::selectCategory
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Ошибка загрузки: ${state.error}", color = Color.Red) }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(6.dp)) }
                        selectedCategory?.sections?.forEach { section ->
                            item(section.id) {
                                WorksSectionCard(
                                    section = section,
                                    expanded = vm.isExpanded(section.id),
                                    onToggle = { vm.toggleExpanded(section.id) },
                                    selectedQuantities = vm.selectedQuantities,
                                    onAddClick = { selectedItemForDialog = it }
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(140.dp)) }
                    }
                }
            }

            BottomActions(
                modifier = Modifier.align(Alignment.BottomCenter),
                total = vm.totalSum(),
                canNext = vm.canGoNextInCurrentCategory(),
                isLastCategory = vm.isLastCategory(),
                onOpenSummary = { showSummary = true },
                onNext = { vm.nextCategory() },
                onFinish = { onFinish(vm.summaryLines(), vm.totalSum()) }
            )
        }
    }

    selectedItemForDialog?.let { item ->
        QuantityDialog(
            item = item,
            rooms = rooms,
            onDismiss = { selectedItemForDialog = null },
            onAdd = { qty ->
                vm.addItem(item, qty)
                selectedItemForDialog = null
            }
        )
    }

    if (showSummary) {
        SummaryDialog(
            lines = vm.summaryLines(),
            total = vm.totalSum(),
            onDismiss = { showSummary = false },
            onRemove = vm::removeItem,
            onResetAll = vm::resetAll
        )
    }

    LaunchedEffect(state.selectedCategoryIndex, state.categories.size) {
        if (state.categories.isNotEmpty()) {
            val targetIndex = state.selectedCategoryIndex.coerceIn(0, state.categories.lastIndex)
            categoryTabsState.animateScrollToItem(targetIndex)
        }
    }

    LaunchedEffect(Unit) { vm.loadIfNeeded() }
}

@Composable
private fun TopBackBar(onBackToRooms: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White, CircleShape)
                .clickable { onBackToRooms() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color(0xFF3D424B))
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
        StepItem(1, "Помещения", false)
        StepItem(2, "Работы", true)
        StepItem(3, "Итог", false)
    }
    HorizontalDivider(color = Color(0xFFE3E4E8))
}

@Composable
private fun StepItem(number: Int, title: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(if (active) Color(0xFF2A6FF3) else Color(0xFFD0D2D8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), color = if (active) Color.White else Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(title, color = if (active) Color(0xFF2A6FF3) else Color(0xFF8A8E98), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategoryTabs(
    categories: List<CatalogCategory>,
    selected: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(modifier = Modifier.size(16.dp)) }
        items(categories.indices.toList()) { index ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .background(if (active) Color(0xFF2A6FF3) else Color.White, RoundedCornerShape(16.dp))
                    .clickable { onSelect(index) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(categories[index].title, color = if (active) Color.White else Color(0xFF2A2D34), fontSize = 13.sp)
            }
        }
        item { Spacer(modifier = Modifier.size(16.dp)) }
    }
}

@Composable
private fun WorksSectionCard(
    section: com.skfamily.renovationcalculatir.data.CatalogSection,
    expanded: Boolean,
    onToggle: () -> Unit,
    selectedQuantities: Map<String, Double>,
    onAddClick: (CatalogItem) -> Unit
) {
    androidx.compose.material3.Card(shape = RoundedCornerShape(16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(section.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(0.85f))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Color(0xFF8A8E98))
            }

            if (expanded) {
                HorizontalDivider(color = Color(0xFFE8E9ED))
                section.items.forEachIndexed { idx, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(0.82f)) {
                            Column {
                                Text(item.title, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                val qty = selectedQuantities[item.id] ?: 0.0
                                if (qty > 0.0) {
                                    Text("${String.format("%.1f", qty)} ${item.unit} • ${(qty * item.price).toInt()} ₽", color = Color(0xFF2A6FF3), fontSize = 12.sp)
                                } else {
                                    Text("${item.price.toInt()} ₽ / ${item.unit}", color = Color(0xFF8A8E98), fontSize = 12.sp)
                                }
                            }
                        }
                        IconButton(onClick = { onAddClick(item) }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2A6FF3))
                        }
                    }
                    if (idx != section.items.lastIndex) HorizontalDivider(color = Color(0xFFF0F1F4))
                }
            }
        }
    }
}

@Composable
private fun QuantityDialog(
    item: CatalogItem,
    rooms: List<RoomDraftInput>,
    onDismiss: () -> Unit,
    onAdd: (Double) -> Unit
) {
    val isAreaUnit = remember(item.unit) {
        val u = item.unit.lowercase()
        u.contains("кв") || u.contains("м2") || u.contains("м²")
    }
    val roomSelection = remember(rooms, item.id) { rooms.associate { it.name to false }.toMutableMap() }
    var qtyText by remember(item.id) { mutableStateOf("1") }
    val qtyValue = qtyText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val subtotal = qtyValue * item.price

    fun recalcFromRooms() {
        val sum = rooms.filter { roomSelection[it.name] == true }.sumOf { it.area }
        qtyText = if (sum <= 0.0) "0" else String.format("%.1f", sum)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Text("Добавить", color = Color(0xFF2A6FF3), modifier = Modifier.clickable {
                val qty = qtyText.replace(",", ".").toDoubleOrNull() ?: 0.0
                if (qty > 0) onAdd(qty)
            })
        },
        dismissButton = { Text("Отмена", modifier = Modifier.clickable(onClick = onDismiss)) },
        title = { Text(item.title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${item.price.toInt()} ₽ / ${item.unit}", color = Color(0xFF8A8E98))
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Количество (${item.unit})") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Сумма: ${subtotal.toInt()} ₽",
                    color = Color(0xFF2A6FF3),
                    fontWeight = FontWeight.SemiBold
                )

                if (isAreaUnit) {
                    Text("Комнаты:", fontWeight = FontWeight.SemiBold)
                    if (rooms.isEmpty()) {
                        Text("Комнаты не выбраны", color = Color(0xFF8A8E98))
                    } else {
                        rooms.forEach { room ->
                            val selected = roomSelection[room.name] == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        roomSelection[room.name] = !selected
                                        recalcFromRooms()
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.fillMaxWidth(0.82f)) {
                                    Text("${room.name} • ${String.format("%.1f", room.area)} м²")
                                }
                                Icon(
                                    imageVector = if (selected) Icons.Default.Remove else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (selected) Color(0xFFE53935) else Color(0xFF2A6FF3)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

data class SummaryLine(
    val itemId: String,
    val title: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val subtotal: Double,
)

@Composable
private fun SummaryDialog(
    lines: List<SummaryLine>,
    total: Int,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit,
    onResetAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Смета") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (lines.isEmpty()) {
                    Text("Смета пустая", color = Color(0xFF8A8E98))
                } else {
                    lines.forEach { line ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.fillMaxWidth(0.74f)) {
                                Column {
                                    Text(line.title, fontSize = 14.sp)
                                    Text("${String.format("%.1f", line.quantity)} ${line.unit} × ${line.unitPrice.toInt()} ₽", fontSize = 12.sp, color = Color(0xFF8A8E98))
                                }
                            }
                            Text("${line.subtotal.toInt()} ₽", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            IconButton(onClick = { onRemove(line.itemId) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53935))
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF0F1F4))
                    }
                }
                Text("Итого: $total ₽", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Text("Закрыть", color = Color(0xFF2A6FF3), modifier = Modifier.clickable(onClick = onDismiss))
        },
        dismissButton = {
            Text("Сбросить все", color = Color(0xFFE53935), modifier = Modifier.clickable {
                onResetAll()
                onDismiss()
            })
        }
    )
}

@Composable
private fun BottomActions(
    modifier: Modifier = Modifier,
    total: Int,
    canNext: Boolean,
    isLastCategory: Boolean,
    onOpenSummary: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (total > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A6FF3), RoundedCornerShape(16.dp))
                    .clickable { onOpenSummary() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Сумма: $total ₽", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(if (canNext && !isLastCategory) Color(0xFFD7D8DD) else Color(0xFFE9EAEE), RoundedCornerShape(16.dp))
                    .clickable(enabled = canNext && !isLastCategory) { onNext() },
                contentAlignment = Alignment.Center
            ) {
                Text("Далее", fontWeight = FontWeight.SemiBold, color = Color(0xFF2A2D34))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(if (total > 0) Color(0xFF2A6FF3) else Color(0xFF9AB9F7), RoundedCornerShape(16.dp))
                    .clickable(enabled = total > 0) { onFinish() },
                contentAlignment = Alignment.Center
            ) {
                Text("Завершить", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

class WorksViewModel : ViewModel() {
    var state by mutableStateOf(WorksUiState())
        private set

    val selectedQuantities = mutableStateMapOf<String, Double>()
    private val expandedSections = mutableStateMapOf<String, Boolean>()

    private val repo = CatalogRepository()

    fun loadIfNeeded() {
        if (state.categories.isNotEmpty() || state.isLoading) return
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { repo.loadCatalog() } }
                .onSuccess { categories -> state = state.copy(isLoading = false, categories = categories) }
                .onFailure { state = state.copy(isLoading = false, error = it.message ?: "Unknown error") }
        }
    }

    fun selectCategory(index: Int) {
        state = state.copy(selectedCategoryIndex = index.coerceIn(0, (state.categories.size - 1).coerceAtLeast(0)))
    }

    fun isExpanded(sectionId: String): Boolean = expandedSections[sectionId] == true

    fun toggleExpanded(sectionId: String) {
        expandedSections[sectionId] = !(expandedSections[sectionId] == true)
    }

    fun addItem(item: CatalogItem, quantity: Double) {
        selectedQuantities[item.id] = quantity
    }

    fun removeItem(itemId: String) {
        selectedQuantities.remove(itemId)
    }

    fun resetAll() {
        selectedQuantities.clear()
    }

    fun nextCategory() {
        val next = (state.selectedCategoryIndex + 1).coerceAtMost((state.categories.size - 1).coerceAtLeast(0))
        state = state.copy(selectedCategoryIndex = next)
    }

    fun isLastCategory(): Boolean = state.selectedCategoryIndex >= (state.categories.size - 1).coerceAtLeast(0)

    fun canGoNextInCurrentCategory(): Boolean {
        val cat = state.categories.getOrNull(state.selectedCategoryIndex) ?: return false
        val ids = cat.sections.flatMap { it.items }.map { it.id }.toSet()
        return selectedQuantities.any { (id, qty) -> id in ids && qty > 0 }
    }

    fun totalSum(): Int {
        val byId = state.categories.flatMap { it.sections }.flatMap { it.items }.associateBy { it.id }
        return selectedQuantities.entries.sumOf { (id, qty) -> ((byId[id]?.price ?: 0.0) * qty).toInt() }
    }

    fun summaryLines(): List<SummaryLine> {
        val byId = state.categories.flatMap { it.sections }.flatMap { it.items }.associateBy { it.id }
        return selectedQuantities.entries.mapNotNull { (id, qty) ->
            val item = byId[id] ?: return@mapNotNull null
            SummaryLine(
                itemId = id,
                title = item.title,
                quantity = qty,
                unit = item.unit,
                unitPrice = item.price,
                subtotal = qty * item.price
            )
        }
    }
}

data class WorksUiState(
    val isLoading: Boolean = false,
    val categories: List<CatalogCategory> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val error: String? = null,
)

package com.skfamily.renovationcalculatir.ui.finalestimate

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skfamily.renovationcalculatir.R
import com.skfamily.renovationcalculatir.ui.works.SummaryLine
import com.skfamily.renovationcalculatir.ui.request.RequestFormSheet

data class FinalCompany(
    val name: String,
    val logoRes: Int,
    val websiteUrl: String,
    val phoneUrl: String,
    val phoneLabel: String,
    val accent: Color,
)

@Composable
fun FinalEstimateScreen(
    lines: List<SummaryLine>,
    total: Int,
    onBackToWorks: () -> Unit,
    onSaveEstimate: () -> String,
) {
    var infoVisible by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<FinalCompany?>(null) }
    var showContactsSheet by remember { mutableStateOf(false) }
    var showRequestForm by remember { mutableStateOf(false) }
    var saveDialogText by remember { mutableStateOf<String?>(null) }

    val companies = remember {
        listOf(
            FinalCompany(
                name = "Remstar",
                logoRes = R.drawable.remstar_logo,
                websiteUrl = "https://remstar-remont.ru",
                phoneUrl = "tel:+79454874972",
                phoneLabel = "+7 495 487-49-72",
                accent = Color(0xFF2A6FF3)
            ),
            FinalCompany(
                name = "Легион",
                logoRes = R.drawable.legion_logo,
                websiteUrl = "https://legionremont.ru",
                phoneUrl = "tel:+79158303600",
                phoneLabel = "+7 915 830-36-00",
                accent = Color(0xFF3D7BE3)
            ),
            FinalCompany(
                name = "СК Фемели",
                logoRes = R.drawable.femily_logo,
                websiteUrl = "https://skfamily.moscow",
                phoneUrl = "tel:+79158303600",
                phoneLabel = "+7 915 830-36-00",
                accent = Color(0xFF2D9CDB)
            ),
            FinalCompany(
                name = "ТЛР Групп",
                logoRes = R.drawable.trl_group_logo,
                websiteUrl = "https://tlr-stroy.ru",
                phoneUrl = "tel:+74950217123",
                phoneLabel = "+7 495 021-71-23",
                accent = Color(0xFF5BA6F2)
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderBar(
                infoVisible = infoVisible,
                onBackToWorks = onBackToWorks,
                onToggleInfo = { infoVisible = !infoVisible }
            )

            StepBar()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                CompaniesSection(
                    companies = companies,
                    selectedCompany = selectedCompany,
                    onCompanySelected = { selectedCompany = it }
                )

                TotalCard(
                    total = total,
                    onSaveClick = {
                        saveDialogText = onSaveEstimate()
                    }
                )

                Text(
                    text = "Детализация сметы",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (lines.isEmpty()) {
                        Text(
                            text = "Смета пустая",
                            color = Color(0xFF8A8E98),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(2.dp)) }
                            items(lines) { line ->
                                EstimateLineCard(line = line)
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }

                ActionBanner(
                    selectedCompany = selectedCompany,
                    onRequestClick = { showRequestForm = true },
                    onContactsClick = { showContactsSheet = true }
                )
            }
        }

        if (infoVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { infoVisible = false }
            )

            InfoBubble(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 54.dp, end = 16.dp)
                    .clickable { infoVisible = false }
            )
        }
    }

    if (showRequestForm) {
        RequestFormSheet(
            estimateLinesText = estimateLinesForRequest(lines),
            onDismiss = { showRequestForm = false }
        )
    }

    if (showContactsSheet) {
        val company = selectedCompany
        if (company != null) {
            CompanyContactsSheet(
                company = company,
                onDismiss = { showContactsSheet = false }
            )
        } else {
            showContactsSheet = false
        }
    }

    saveDialogText?.let { message ->
        AlertDialog(
            onDismissRequest = { saveDialogText = null },
            confirmButton = {
                Button(onClick = { saveDialogText = null }) {
                    Text("Ок")
                }
            },
            title = { Text("Сохранение") },
            text = { Text(message) }
        )
    }
}

@Composable
private fun HeaderBar(
    infoVisible: Boolean,
    onBackToWorks: () -> Unit,
    onToggleInfo: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackToWorks) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color(0xFF2A2D34)
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Итог",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        IconButton(onClick = onToggleInfo) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (infoVisible) Color(0xFF2A6FF3) else Color(0xFF2A2D34)
            )
        }
    }
}

@Composable
private fun StepBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StepItem(1, "Помещения", StepState.Done)
        StepItem(2, "Работы", StepState.Done)
        StepItem(3, "Итог", StepState.Active)
    }
    HorizontalDivider(color = Color(0xFFE3E4E8))
}

@Composable
private fun StepItem(number: Int, title: String, state: StepState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(stepCircleColor(state), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (state == StepState.Done) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    color = if (state == StepState.Inactive) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = title,
            color = stepTextColor(state),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private enum class StepState {
    Done,
    Active,
    Inactive,
}

private fun stepCircleColor(state: StepState): Color = when (state) {
    StepState.Done, StepState.Active -> Color(0xFF2A6FF3)
    StepState.Inactive -> Color(0xFFD0D2D8)
}

private fun stepTextColor(state: StepState): Color = when (state) {
    StepState.Done, StepState.Active -> Color(0xFF2A6FF3)
    StepState.Inactive -> Color(0xFF8A8E98)
}

@Composable
private fun CompaniesSection(
    companies: List<FinalCompany>,
    selectedCompany: FinalCompany?,
    onCompanySelected: (FinalCompany) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Список компаний",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = "Ниже приведены примеры компаний для ориентира. Вы можете выбрать любую другую компанию или подрядчика по своему усмотрению.",
            fontSize = 11.sp,
            lineHeight = 12.sp,
            color = Color(0xFF8A8E98),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            companies.forEach { company ->
                CompanyCard(
                    company = company,
                    selected = selectedCompany?.name == company.name,
                    onClick = { onCompanySelected(company) }
                )
            }
        }
    }
}

@Composable
private fun CompanyCard(
    company: FinalCompany,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F8FB), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = company.logoRes),
                    contentDescription = company.name,
                    modifier = Modifier.size(42.dp)
                )

                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(company.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = company.name,
            fontSize = 9.sp,
            lineHeight = 8.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(62.dp)
        )
    }
}

@Composable
private fun TotalCard(
    total: Int,
    onSaveClick: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFEAF1FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF2A6FF3))
            }

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Итого по смете:",
                    fontSize = 11.sp,
                    color = Color(0xFF8A8E98)
                )
                Text(
                    text = "$total ₽",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2A6FF3)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Сохранить смету", color = Color(0xFF2A2D34), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EstimateLineCard(line: SummaryLine) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(line.title, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${line.quantity.format1()} ${line.unit} × ${line.unitPrice.format0()} ₽",
                    fontSize = 12.sp,
                    color = Color(0xFF8A8E98)
                )
            }

            Text(
                text = "${line.subtotal.format0()} ₽",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActionBanner(
    selectedCompany: FinalCompany?,
    onRequestClick: () -> Unit,
    onContactsClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = null,
                tint = Color(0xFF2A6FF3)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "Готовы начать ремонт?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Выберите компанию и мы свяжемся с вами. Так же вы можете самостоятельно связаться с нами по ссылке в контактах.",
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    color = Color(0xFF8A8E98)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onRequestClick,
                    modifier = Modifier
                        .width(122.dp)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Заказать\nзвонок",
                        fontSize = 11.sp,
                        lineHeight = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        softWrap = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = onContactsClick,
                    enabled = selectedCompany != null,
                    modifier = Modifier
                        .width(122.dp)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedCompany != null) Color(0xFF2A6FF3) else Color(0xFFD0D2D8)
                    )
                ) {
                    Text("Контакты", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun estimateLinesForRequest(lines: List<SummaryLine>): String? {
    if (lines.isEmpty()) return null

    return lines.joinToString(separator = "\n") { line ->
        val quantity = String.format("%.1f", line.quantity)
        val unitPrice = String.format("%.0f", line.unitPrice)
        val subtotal = String.format("%.0f", line.subtotal)
        "${line.title}: $quantity ${line.unit} × $unitPrice ₽ = $subtotal ₽"
    }
}

@Composable
private fun InfoBubble(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color(0xFFF2F4F8), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Указанные компании приведены в качестве примеров и не ограничивают ваш выбор подрядчика. Итоговая стоимость уточняется напрямую у выбранной компании.",
            fontSize = 12.sp,
            color = Color(0xFF2A2D34)
        )
    }
}

private fun Double.format0(): String = String.format("%.0f", this)
private fun Double.format1(): String = String.format("%.1f", this)

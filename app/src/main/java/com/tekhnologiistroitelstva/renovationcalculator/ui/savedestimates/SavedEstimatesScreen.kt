package com.tekhnologiistroitelstva.renovationcalculator.ui.savedestimates

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tekhnologiistroitelstva.renovationcalculator.data.SavedEstimate
import com.tekhnologiistroitelstva.renovationcalculator.data.SavedEstimatesStore
import com.tekhnologiistroitelstva.renovationcalculator.ui.onboarding.OnboardingKeys
import com.tekhnologiistroitelstva.renovationcalculator.ui.onboarding.OnboardingOverlay
import com.tekhnologiistroitelstva.renovationcalculator.ui.onboarding.OnboardingPage
import com.tekhnologiistroitelstva.renovationcalculator.ui.onboarding.OnboardingPrefs
import androidx.compose.ui.platform.LocalContext
import com.tekhnologiistroitelstva.renovationcalculator.R

@Composable
fun SavedEstimatesScreen(
    store: SavedEstimatesStore,
    onOpenNewEstimate: () -> Unit,
    onOpenEstimate: (SavedEstimate) -> Unit,
) {
    val context = LocalContext.current
    val onboardingPrefs = remember(context) { OnboardingPrefs(context) }
    var showOnboarding by remember { mutableStateOf(false) }
    val onboardingPages = remember {
        listOf(
            OnboardingPage(
                title = "Сохраненные сметы",
                description = "Здесь хранятся ваши сохраненные расчеты. Можно открыть детали, что-то изменить, заказать звонок или удалить.",
                imageRes = R.drawable.estimate_guide
            )
        )
    }

    LaunchedEffect(Unit) {
        store.reload()
        if (onboardingPrefs.shouldShow(OnboardingKeys.ESTIMATES)) {
            showOnboarding = true
            onboardingPrefs.markShown(OnboardingKeys.ESTIMATES)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Сохраненные сметы",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showOnboarding = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Подсказки",
                        tint = Color(0xFF101114)
                    )
                }
            }

            when {
                store.isLoading && !store.hasSavedEstimates -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                !store.hasSavedEstimates -> {
                    EmptyEstimatesState(onOpenNewEstimate = onOpenNewEstimate)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(2.dp)) }
                        items(store.estimates, key = { it.id }) { estimate ->
                            EstimateCard(
                                estimate = estimate,
                                onOpen = { onOpenEstimate(estimate) },
                                onDelete = { store.delete(estimate.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(12.dp)) }
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
private fun EmptyEstimatesState(onOpenNewEstimate: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Сохраненных смет пока нет",
                color = Color(0xFF8A8E98),
                fontSize = 15.sp
            )

            FilledTonalButton(onClick = onOpenNewEstimate) {
                Text("Сделать новый расчет")
            }
        }
    }
}

@Composable
private fun EstimateCard(
    estimate: SavedEstimate,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = estimate.createdAt.toDisplayDate(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${estimate.total} ₽",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53935))
            }
        }

        Text(
            text = "${estimate.lines.size} поз.",
            fontSize = 12.sp,
            color = Color(0xFF8A8E98)
        )

        Spacer(modifier = Modifier.height(8.dp))

        estimate.lines.take(3).forEachIndexed { index, line ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = line.title,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${line.subtotal.toInt()} ₽",
                    fontSize = 12.sp,
                    color = Color(0xFF8A8E98)
                )
            }

            if (index != estimate.lines.take(3).lastIndex) {
                HorizontalDivider(color = Color(0xFFF0F1F4))
            }
        }
    }
}

private fun Long.toDisplayDate(): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.forLanguageTag("ru-RU"))
    return sdf.format(java.util.Date(this))
}

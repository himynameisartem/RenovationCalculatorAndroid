package com.skfamily.renovationcalculatir.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.skfamily.renovationcalculatir.R
import com.skfamily.renovationcalculatir.ui.onboarding.OnboardingKeys
import com.skfamily.renovationcalculatir.ui.onboarding.OnboardingOverlay
import com.skfamily.renovationcalculatir.ui.onboarding.OnboardingPage
import com.skfamily.renovationcalculatir.ui.onboarding.OnboardingPrefs
import com.skfamily.renovationcalculatir.ui.request.RequestFormSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun HomeScreen(
    onOpenCalculator: () -> Unit,
    onOpenRequest: () -> Unit,
    onOpenPrice: () -> Unit,
) {
    var showRequestForm by remember { mutableStateOf(false) }
    var showPriceConfirm by remember { mutableStateOf(false) }
    var isDownloadingPrice by remember { mutableStateOf(false) }
    var downloadErrorText by remember { mutableStateOf<String?>(null) }
    var showDownloadError by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val onboardingPrefs = remember(context) { OnboardingPrefs(context) }
    val onboardingPages = remember {
        listOf(
            OnboardingPage(
                title = "Добро пожаловать",
                description = "На главном экране собраны быстрые сценарии: калькулятор, заявка на ремонт и актуальный прайс."
            ),
            OnboardingPage(
                title = "Переход к расчету",
                description = "Откройте калькулятор, чтобы выбрать помещения, добавить работы и собрать итоговую смету."
            ),
            OnboardingPage(
                title = "Дополнительные действия",
                description = "Здесь же можно оставить заявку на ремонт и скачать актуальный прайс в PDF."
            )
        )
    }

    LaunchedEffect(Unit) {
        if (onboardingPrefs.shouldShow(OnboardingKeys.HOME)) {
            showOnboarding = true
            onboardingPrefs.markShown(OnboardingKeys.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeroBlock(
                onHelpClick = { showOnboarding = true }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HomeActionCard(
                    title = "Калькулятор",
                    subtitle = "Рассчитайте стоимость ремонта квартиры",
                    icon = Icons.Default.Calculate,
                    onClick = onOpenCalculator,
                    primary = true,
                    cardBackground = Brush.horizontalGradient(
                        listOf(Color(0xFF3F7BE3), Color(0xFF5BA6F2))
                    ),
                    titleColor = Color.White,
                    subtitleColor = Color.White.copy(alpha = 0.82f),
                    iconBackgroundColor = Color.White.copy(alpha = 0.16f),
                    iconTint = Color.White,
                    arrowTint = Color(0xFF3778F2),
                    decorativeImageRes = R.drawable.pages_icon,
                    decorativeImageAlpha = 0.12f
                )

                HomeActionCard(
                    title = "Заявка на ремонт",
                    subtitle = "Оставьте заявку и мы свяжемся с вами",
                    icon = Icons.Default.Checklist,
                    onClick = {
                        onOpenRequest()
                        showRequestForm = true
                    },
                    cardBackground = Brush.horizontalGradient(listOf(Color.White, Color.White)),
                    titleColor = Color(0xFF101114),
                    subtitleColor = Color(0xFF6E737D),
                    iconBackgroundColor = Color(0xFFEAF1FF),
                    iconTint = Color(0xFF2A6FF3),
                    arrowTint = Color(0xFF3778F2),
                    decorativeImageRes = R.drawable.docs_icon,
                    decorativeImageAlpha = 0.12f
                )

                HomeActionCard(
                    title = "Актуальный прайс",
                    subtitle = "Посмотрите актуальные цены на работы",
                    icon = Icons.Default.Payments,
                    onClick = {
                        onOpenPrice()
                        showPriceConfirm = true
                    },
                    cardBackground = Brush.horizontalGradient(listOf(Color.White, Color.White)),
                    titleColor = Color(0xFF101114),
                    subtitleColor = Color(0xFF6E737D),
                    iconBackgroundColor = Color(0xFFEAF7EA),
                    iconTint = Color(0xFF6CAB5F),
                    arrowTint = Color(0xFF6AAF57),
                    decorativeImageRes = R.drawable.coins_icon,
                    decorativeImageAlpha = 0.12f
                )
            }
        }

        if (showRequestForm) {
            RequestFormSheet(
                estimateLinesText = null,
                onDismiss = { showRequestForm = false }
            )
        }

        if (showPriceConfirm) {
            AlertDialog(
                onDismissRequest = { showPriceConfirm = false },
                title = { Text("Скачать прайс?") },
                text = { Text("Будет загружен актуальный прайс в PDF.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showPriceConfirm = false
                            isDownloadingPrice = true
                            scope.launch {
                                val result = downloadPricePdf(context)
                                isDownloadingPrice = false
                                result.fold(
                                    onSuccess = { file ->
                                        runCatching { openPdf(context, file) }
                                            .onFailure {
                                                downloadErrorText = it.message ?: "Не удалось открыть PDF"
                                                showDownloadError = true
                                            }
                                    },
                                    onFailure = { error ->
                                        downloadErrorText = error.message ?: "Не удалось скачать прайс"
                                        showDownloadError = true
                                    }
                                )
                            }
                        }
                    ) {
                        Text("Скачать")
                    }
                },
                dismissButton = {
                    Button(onClick = { showPriceConfirm = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (isDownloadingPrice) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Загрузка прайса...")
                }
            }
        }

        if (showDownloadError) {
            AlertDialog(
                onDismissRequest = { showDownloadError = false },
                title = { Text("Ошибка загрузки") },
                text = { Text(downloadErrorText ?: "Не удалось скачать файл.") },
                confirmButton = {
                    Button(onClick = { showDownloadError = false }) {
                        Text("Ок")
                    }
                }
            )
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
private fun HeroBlock(onHelpClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.main_page_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Ремонт\nквартир",
                fontSize = 34.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101114)
            )
            Text(
                text = "Рассчитайте стоимость\nи выберите подходящий\nвариант ремонта",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666A73)
            )
        }

        IconButton(
            onClick = onHelpClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.78f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = "Подсказки",
                tint = Color(0xFF101114)
            )
        }
    }
}

private suspend fun downloadPricePdf(context: Context): Result<File> = withContext(Dispatchers.IO) {
    runCatching {
        val url = URL("https://skfamily.moscow/price.pdf")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            instanceFollowRedirects = true
        }
        val code = connection.responseCode
        if (code !in 200..299) {
            throw IllegalStateException("Статус: $code")
        }

        val outFile = File(context.cacheDir, "price.pdf")
        connection.inputStream.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        outFile
    }
}

private fun openPdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Открыть прайс"))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Поделиться прайсом"))
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    primary: Boolean = false,
    cardBackground: Brush = Brush.horizontalGradient(listOf(Color.White, Color.White)),
    titleColor: Color = Color(0xFF101114),
    subtitleColor: Color = Color(0xFF6E737D),
    iconBackgroundColor: Color = Color(0xFFE9EEF9),
    iconTint: Color = Color(0xFF2A6FF3),
    arrowTint: Color = Color(0xFF4E535E),
    decorativeImageRes: Int? = null,
    decorativeImageAlpha: Float = 0.1f
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBackground)
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            if (decorativeImageRes != null) {
                Image(
                    painter = painterResource(id = decorativeImageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alpha = decorativeImageAlpha,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(96.dp)
                        .padding(end = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint)
                }

                Spacer(modifier = Modifier.size(14.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .widthIn(max = 168.dp)
                    ) {
                        Text(
                            title,
                            color = titleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            subtitle,
                            color = subtitleColor,
                            fontSize = 10.5.sp,
                            lineHeight = 12.sp,
                            maxLines = 3,
                            softWrap = true
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape, clip = false)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = arrowTint
                    )
                }
            }
        }
    }
}

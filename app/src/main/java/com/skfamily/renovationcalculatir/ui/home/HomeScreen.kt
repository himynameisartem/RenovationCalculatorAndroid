package com.skfamily.renovationcalculatir.ui.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onOpenCalculator: () -> Unit,
    onOpenRequest: () -> Unit,
    onOpenPrice: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeroBlock()

        HomeActionCard(
            title = "Калькулятор",
            subtitle = "Рассчитайте стоимость ремонта квартиры",
            icon = Icons.Default.Calculate,
            onClick = onOpenCalculator,
            primary = true
        )

        HomeActionCard(
            title = "Заявка на ремонт",
            subtitle = "Оставьте заявку и мы свяжемся с вами",
            icon = Icons.Default.Checklist,
            onClick = onOpenRequest
        )

        HomeActionCard(
            title = "Актуальный прайс",
            subtitle = "Посмотрите актуальные цены на работы",
            icon = Icons.Default.Payments,
            onClick = onOpenPrice
        )
    }
}

@Composable
private fun HeroBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.White,
                        Color(0xFFF4F5F8),
                        Color(0xFFEDEFF4)
                    )
                )
            )
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.TopStart)
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
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    primary: Boolean = false
) {
    val background = if (primary) {
        Brush.horizontalGradient(listOf(Color(0xFF3F7BE3), Color(0xFF5BA6F2)))
    } else {
        Brush.horizontalGradient(listOf(Color.White, Color.White))
    }
    val titleColor = if (primary) Color.White else Color(0xFF101114)
    val subtitleColor = if (primary) Color.White.copy(alpha = 0.8f) else Color(0xFF6E737D)
    val iconBg = if (primary) Color.White.copy(alpha = 0.2f) else Color(0xFFE9EEF9)
    val iconColor = if (primary) Color.White else Color(0xFF2A6FF3)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor)
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 25.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = subtitleColor, fontSize = 16.sp, lineHeight = 18.sp)
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = if (primary) Color(0xFF2A6FF3) else Color(0xFF4E535E)
                )
            }
        }
    }
}

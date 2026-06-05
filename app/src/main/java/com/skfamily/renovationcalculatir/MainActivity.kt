package com.skfamily.renovationcalculatir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skfamily.renovationcalculatir.ui.calculator.CalculatorRoomsScreen
import com.skfamily.renovationcalculatir.ui.finalestimate.FinalEstimateScreen
import com.skfamily.renovationcalculatir.ui.home.HomeScreen
import com.skfamily.renovationcalculatir.ui.models.RoomDraftInput
import com.skfamily.renovationcalculatir.ui.theme.RenovationCalculatirTheme
import com.skfamily.renovationcalculatir.ui.works.SummaryLine
import com.skfamily.renovationcalculatir.ui.works.WorksScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RenovationCalculatirTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    var showLaunch by remember { mutableStateOf(true) }
    var showLaunchProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3000)
        showLaunchProgress = true
        delay(350)
        showLaunch = false
    }

    if (showLaunch) {
        LaunchLoadingScreen(showProgress = showLaunchProgress)
    } else {
        MainTabsScreen()
    }
}

@Composable
private fun MainTabsScreen() {
    val navController = rememberNavController()
    var infoDialogText by remember { mutableStateOf<String?>(null) }
    var roomsForWorks by remember { mutableStateOf<List<RoomDraftInput>>(emptyList()) }
    var finalEstimateLines by remember { mutableStateOf<List<SummaryLine>>(emptyList()) }
    var finalEstimateTotal by remember { mutableStateOf(0) }

    fun navigateToCalculatorStart() {
        val popped = navController.popBackStack("calculator", inclusive = false)
        if (!popped) {
            navController.navigate("calculator") {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val tabs = listOf(
        MainTab("home", "Главная", Icons.Default.Home),
        MainTab("calculator", "Расчет", Icons.Default.Calculate),
        MainTab("estimates", "Сметы", Icons.Default.Checklist),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            when (tab.route) {
                                "calculator" -> navigateToCalculatorStart()
                                else -> navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onOpenCalculator = {
                        navigateToCalculatorStart()
                    },
                    onOpenRequest = {
                        infoDialogText = "Форма заявки будет следующим шагом. Сейчас подключили рабочий каркас."
                    },
                    onOpenPrice = {
                        infoDialogText = "Загрузка PDF-прайса будет следующим шагом Android-версии."
                    }
                )
            }
            composable("calculator") {
                CalculatorRoomsScreen(
                    onSkip = {
                        roomsForWorks = emptyList()
                        navController.navigate("works")
                    },
                    onContinue = { rooms ->
                        roomsForWorks = rooms
                        navController.navigate("works")
                    }
                )
            }
            composable("works") {
                WorksScreen(
                    rooms = roomsForWorks,
                    onBackToRooms = { navController.popBackStack() },
                    onFinish = { lines, total ->
                        finalEstimateLines = lines
                        finalEstimateTotal = total
                        navController.navigate("final_estimate") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("final_estimate") {
                FinalEstimateScreen(
                    lines = finalEstimateLines,
                    total = finalEstimateTotal,
                    onBackToWorks = { navController.popBackStack() },
                    onSaveEstimate = { "Смета сохранена. Следующим шагом подключим реальное сохранение." }
                )
            }
            composable("estimates") { PlaceholderScreen("Сметы") }
        }

        infoDialogText?.let { message ->
            AlertDialog(
                onDismissRequest = { infoDialogText = null },
                title = { Text("Информация") },
                text = { Text(message) },
                confirmButton = {
                    Button(onClick = { infoDialogText = null }) {
                        Text("Ок")
                    }
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}

@Composable
private fun LaunchLoadingScreen(showProgress: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "RenovationCalculator")
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(visible = showProgress) {
                CircularProgressIndicator()
            }
        }
    }
}

private data class MainTab(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Preview(showBackground = true)
@Composable
private fun AppRootPreview() {
    RenovationCalculatirTheme {
        AppRoot()
    }
}

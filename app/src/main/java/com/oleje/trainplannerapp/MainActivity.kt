package com.oleje.trainplannerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oleje.trainplannerapp.ui.MainViewModel
import com.oleje.trainplannerapp.ui.RouteSearchScreen
import com.oleje.trainplannerapp.ui.SettingsScreen
import com.oleje.trainplannerapp.ui.TicketsScreen
import com.oleje.trainplannerapp.ui.theme.*
import com.oleje.trainplannerapp.ui.theme.TrainPlannerAppTheme
class MainActivity : ComponentActivity() {



    @Preview(showBackground = true, backgroundColor = 0xFF0B132B)
    @Composable
    fun MainActivityPreview() {
        TrainPlannerAppTheme {
            MainAppScreen(viewModel = MainViewModel())
        }
    }
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainPlannerAppTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

sealed class NavTab(val title: String, val iconText: String) {
    object Search : NavTab("Пошук", "🔍")
    object Tickets : NavTab("Квитки", "🎟️")
    object Settings : NavTab("Налаштування", "⚙️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf<NavTab>(NavTab.Search) }
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "УКРЗАЛІЗНИЦЯ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "MAGA • Train Planner",
                            fontSize = 11.sp,
                            color = UzAccentYellow,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UzCardHeader)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = UzCardBg,
                contentColor = UzTextPrimary
            ) {
                listOf(NavTab.Search, NavTab.Tickets, NavTab.Settings).forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = { Text(text = tab.iconText, fontSize = 20.sp) },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) UzAccentYellow else UzTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = UzCardHeader
                        )
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = UzCardHeader,
                    contentColor = UzTextPrimary,
                    snackbarData = data
                )
            }
        },
        containerColor = UzBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                NavTab.Search -> RouteSearchScreen(viewModel = viewModel)
                NavTab.Tickets -> TicketsScreen(viewModel = viewModel)
                NavTab.Settings -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

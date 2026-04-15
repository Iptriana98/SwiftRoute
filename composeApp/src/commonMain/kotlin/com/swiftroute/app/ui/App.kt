package com.swiftroute.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swiftroute.app.shared.data.FirebaseRouteRepository
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.ui.screens.auth.AuthScreen
import com.swiftroute.app.ui.screens.history.HistoryScreen
import com.swiftroute.app.ui.screens.home.HomeScreen
import com.swiftroute.app.ui.screens.route.CreateRouteScreen
import com.swiftroute.app.ui.screens.route.RouteOverviewScreen
import com.swiftroute.app.ui.screens.execution.ActiveRouteScreen
import com.swiftroute.app.ui.screens.execution.RouteSummaryScreen
import com.swiftroute.app.ui.screens.settings.SettingsScreen
import com.swiftroute.app.ui.screens.stop.AddStopScreen
import com.swiftroute.app.ui.screens.optimization.OptimizationScreen
import com.swiftroute.app.ui.theme.SwiftRouteNightTheme

/**
 * Navigation destinations
 */
sealed class AppDestination {
    data object Home : AppDestination()
    data class CreateRoute(val routeId: String?) : AppDestination()
    data class AddStop(val routeId: String, val stopId: String?) : AppDestination()
    data class RouteOverview(val routeId: String) : AppDestination()
    data class ActiveRoute(val routeId: String) : AppDestination()
    data class RouteSummary(val routeId: String) : AppDestination()
    data class Optimization(val routeId: String) : AppDestination()
}

/**
 * Main App composable with state-based navigation
 */
@Composable
fun App(
    routeRepository: RouteRepository = FirebaseRouteRepository()
) {
    SwiftRouteNightTheme {
        var isLoggedIn by remember { mutableStateOf(false) }
        var userId by remember { mutableStateOf<String?>(null) }
        var userEmail by remember { mutableStateOf<String?>(null) }
        var selectedTab by remember { mutableIntStateOf(0) }
        
        // Navigation state
        var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Home) }
        var navigationStack by remember { mutableStateOf(listOf<AppDestination>()) }

        // Navigation functions
        fun navigateTo(destination: AppDestination) {
            navigationStack = navigationStack + currentDestination
            currentDestination = destination
        }

        fun navigateBack() {
            if (navigationStack.isNotEmpty()) {
                currentDestination = navigationStack.last()
                navigationStack = navigationStack.dropLast(1)
            } else {
                currentDestination = AppDestination.Home
            }
        }

        // Handle destination-based rendering
        when (val destination = currentDestination) {
            is AppDestination.Home -> {
                if (!isLoggedIn) {
                    AuthScreen(
                        onLoginSuccess = { newUserId ->
                            userId = newUserId
                            userEmail = "user@example.com" // Would come from auth
                            isLoggedIn = true
                            currentDestination = AppDestination.Home
                        }
                    ).Content()
                } else {
                    // Main App with Bottom Navigation
                    MainScaffold(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        userEmail = userEmail,
                        onLogout = {
                            userId = null
                            isLoggedIn = false
                            currentDestination = AppDestination.Home
                        }
                    ) {
                        HomeScreen(
                            userId = userId ?: "",
                            routeRepository = routeRepository,
                            onNavigateToCreateRoute = { routeId ->
                                navigateTo(AppDestination.CreateRoute(routeId))
                            },
                            onNavigateToRouteOverview = { routeId ->
                                navigateTo(AppDestination.RouteOverview(routeId))
                            }
                        ).Content()
                    }
                }
            }
            
            is AppDestination.CreateRoute -> {
                CreateRouteScreen(
                    userId = userId ?: "",
                    routeId = destination.routeId,
                    routeRepository = routeRepository,
                    onNavigateBack = { navigateBack() },
                    onNavigateToAddStop = { routeId, stopId ->
                        navigateTo(AppDestination.AddStop(routeId, stopId))
                    },
                    onNavigateToOptimization = { routeId ->
                        navigateTo(AppDestination.Optimization(routeId))
                    }
                ).Content()
            }
            
            is AppDestination.AddStop -> {
                AddStopScreen(
                    routeId = destination.routeId,
                    stopId = destination.stopId,
                    routeRepository = routeRepository,
                    onNavigateBack = { navigateBack() }
                ).Content()
            }
            
            is AppDestination.RouteOverview -> {
                RouteOverviewScreen(
                    userId = userId ?: "",
                    routeId = destination.routeId,
                    routeRepository = routeRepository,
                    onNavigateBack = { navigateBack() },
                    onStartRoute = { routeId ->
                        navigateTo(AppDestination.ActiveRoute(routeId))
                    },
                    onEditRoute = { routeId ->
                        navigateTo(AppDestination.CreateRoute(routeId))
                    }
                ).Content()
            }
            
            is AppDestination.ActiveRoute -> {
                ActiveRouteScreen(
                    userId = userId ?: "",
                    routeId = destination.routeId,
                    routeRepository = routeRepository,
                    onNavigateToSummary = {
                        navigateTo(AppDestination.RouteSummary(destination.routeId))
                    }
                ).Content()
            }
            
            is AppDestination.RouteSummary -> {
                RouteSummaryScreen(
                    userId = userId ?: "",
                    routeId = destination.routeId,
                    routeRepository = routeRepository,
                    onStartAgain = { routeId ->
                        // Clear navigation stack and start route again
                        navigationStack = emptyList()
                        currentDestination = AppDestination.ActiveRoute(routeId)
                    },
                    onGoHome = {
                        navigationStack = emptyList()
                        currentDestination = AppDestination.Home
                    }
                ).Content()
            }
            
            is AppDestination.Optimization -> {
                OptimizationScreen(
                    routeId = destination.routeId,
                    routeRepository = routeRepository,
                    onNavigateBack = { navigateBack() },
                    onOptimizationComplete = { navigateBack() }
                ).Content()
            }
        }
    }
}

/**
 * Main scaffold with bottom navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userEmail: String?,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    var currentUserId by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                // Routes Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = { 
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = "Routes"
                        ) 
                    },
                    label = { Text("Routes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF44DDC1),
                        selectedTextColor = Color(0xFF44DDC1),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color(0xFF00BFA5).copy(alpha = 0.3f)
                    )
                )
                
                // History Tab
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History"
                        ) 
                    },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF44DDC1),
                        selectedTextColor = Color(0xFF44DDC1),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color(0xFF00BFA5).copy(alpha = 0.3f)
                    )
                )
                
                // Settings Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        ) 
                    },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF44DDC1),
                        selectedTextColor = Color(0xFF44DDC1),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color(0xFF00BFA5).copy(alpha = 0.3f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> content()
                1 -> HistoryScreen(currentUserId ?: "", onLogout = onLogout).Content()
                2 -> SettingsScreen(
                    userEmail = userEmail,
                    onLogout = onLogout
                ).Content()
            }
        }
    }
}

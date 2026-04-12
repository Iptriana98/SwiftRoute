package com.swiftroute.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen

import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.swiftroute.app.ui.theme.SwiftRouteNightTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    SwiftRouteNightTheme {
        TabNavigator(RoutesTab) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        tonalElevation = NavigationBarDefaults.Elevation
                    ) {
                        TabNavigationItem(RoutesTab)
                        TabNavigationItem(HistoryTab)
                        TabNavigationItem(SettingsTab)
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    CurrentScreen()
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
        label = { Text(tab.options.title) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    )
}

// Tabs Definitions

internal object RoutesTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.List)
            return TabOptions(index = 0u, title = "Routes", icon = icon)
        }

    @Composable
    override fun Content() {
        Box(Modifier.fillMaxSize()) { Text("Routes Screen Placeholder", Modifier.padding(16.dp)) }
    }
}

internal object HistoryTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Refresh)
            return TabOptions(index = 1u, title = "History", icon = icon)
        }

    @Composable
    override fun Content() {
        Box(Modifier.fillMaxSize()) { Text("History Screen Placeholder", Modifier.padding(16.dp)) }
    }
}

internal object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Settings)
            return TabOptions(index = 2u, title = "Settings", icon = icon)
        }

    @Composable
    override fun Content() {
        Box(Modifier.fillMaxSize()) { Text("Settings Screen Placeholder", Modifier.padding(16.dp)) }
    }
}

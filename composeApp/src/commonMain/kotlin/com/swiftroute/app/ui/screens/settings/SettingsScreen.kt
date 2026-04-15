package com.swiftroute.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.swiftroute.app.shared.data.AuthService
import com.swiftroute.app.shared.data.MockAuthService

/**
 * Settings screen
 * @param authService AuthService for logout (optional, uses MockAuthService if not provided)
 * @param userEmail User email to display (optional)
 * @param onLogout Callback for logout action (optional)
 */
@OptIn(ExperimentalMaterial3Api::class)
class SettingsScreen(
    private val authService: AuthService = MockAuthService(),
    private val userEmail: String? = null,
    private val onLogout: () -> Unit = {}
) : Screen {

    @Composable
    override fun Content() {
        // TODO: Load from ViewModel/Repository
        var preferredNavApp by remember { mutableStateOf("google_maps") }
        var defaultServiceTime by remember { mutableStateOf("15") }
        var displayEmail by remember { mutableStateOf(userEmail ?: "user@example.com") }
        var showLogoutDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Account section
                SettingsSection(title = "Account") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayEmail,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Signed in",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Navigation preferences
                SettingsSection(title = "Navigation") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            NavAppOption(
                                app = "google_maps",
                                name = "Google Maps",
                                icon = Icons.Default.Map,
                                isSelected = preferredNavApp == "google_maps",
                                onSelect = { preferredNavApp = "google_maps" }
                            )
                            HorizontalDivider()
                            NavAppOption(
                                app = "apple_maps",
                                name = "Apple Maps",
                                icon = Icons.Default.Map,
                                isSelected = preferredNavApp == "apple_maps",
                                onSelect = { preferredNavApp = "apple_maps" }
                            )
                            HorizontalDivider()
                            NavAppOption(
                                app = "waze",
                                name = "Waze",
                                icon = Icons.Default.Navigation,
                                isSelected = preferredNavApp == "waze",
                                onSelect = { preferredNavApp = "waze" }
                            )
                        }
                    }
                    Text(
                        text = "Preferred app for turn-by-turn navigation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                }

                // Route defaults
                SettingsSection(title = "Route Defaults") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Default Service Time",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Time at each stop",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = defaultServiceTime,
                                    onValueChange = {
                                        if (it.all { c -> c.isDigit() }) {
                                            defaultServiceTime = it
                                        }
                                    },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true,
                                    suffix = { Text("min") }
                                )
                            }
                        }
                    }
                }

                // About section
                SettingsSection(title = "About") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            SettingsItem(
                                icon = Icons.Default.Info,
                                title = "App Version",
                                subtitle = "1.0.0"
                            )
                            HorizontalDivider()
                            SettingsItem(
                                icon = Icons.Default.Description,
                                title = "Terms of Service",
                                onClick = { /* TODO */ }
                            )
                            HorizontalDivider()
                            SettingsItem(
                                icon = Icons.Default.PrivacyTip,
                                title = "Privacy Policy",
                                onClick = { /* TODO */ }
                            )
                            HorizontalDivider()
                            SettingsItem(
                                icon = Icons.Default.Help,
                                title = "Help & Support",
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }

                // Sign out
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Logout confirmation dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Sign Out") },
                text = { Text("Are you sure you want to sign out?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Call real logout from AuthService
                            authService.signOut()
                            showLogoutDialog = false
                            onLogout()
                        }
                    ) {
                        Text("Sign Out", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Settings section header
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

/**
 * Navigation app option
 */
@Composable
fun NavAppOption(
    app: String,
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )
    }
}

/**
 * Settings item
 */
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onClick?.invoke() },
            enabled = onClick != null
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

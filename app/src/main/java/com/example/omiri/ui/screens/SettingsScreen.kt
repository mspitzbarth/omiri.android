package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onMyStoresClick: () -> Unit = {},
    onMembershipCardsClick: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val shoppingListNotifications by viewModel.shoppingListNotifications.collectAsState()
    val monitorAllLists by viewModel.monitorAllLists.collectAsState()
    val debugMode by viewModel.debugMode.collectAsState()
    val selectedStoresCount by viewModel.selectedStoresCount.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Header with back button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(vertical = Spacing.xxs)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF6B7280)
                    )
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.lg))

            // NOTIFICATIONS Section
            SectionHeader("NOTIFICATIONS")
            Spacer(Modifier.height(Spacing.md))

            SettingItemWithSwitch(
                icon = Icons.Outlined.Notifications,
                iconBackgroundColor = Color(0xFFFFEDDB),
                iconTint = Color(0xFFEA580B),
                title = "Shopping List Notifications",
                subtitle = "Get alerts for list items",
                checked = shoppingListNotifications,
                onCheckedChange = { viewModel.toggleShoppingListNotifications() }
            )

            Spacer(Modifier.height(Spacing.md))

            SettingItemWithSwitch(
                icon = Icons.Outlined.Visibility,
                iconBackgroundColor = Color(0xFFDBEAFE),
                iconTint = Color(0xFF3B82F6),
                title = "Monitor All Lists",
                subtitle = "Track all shopping lists",
                checked = monitorAllLists,
                onCheckedChange = { viewModel.toggleMonitorAllLists() }
            )

            Spacer(Modifier.height(Spacing.xl))

            // PREFERENCES Section
            SectionHeader("PREFERENCES")
            Spacer(Modifier.height(Spacing.md))

            SettingItemNavigable(
                icon = Icons.Outlined.Store,
                iconBackgroundColor = Color(0xFFD1FAE5),
                iconTint = Color(0xFF10B981),
                title = "My Stores",
                subtitle = "$selectedStoresCount stores selected",
                onClick = onMyStoresClick
            )

            Spacer(Modifier.height(Spacing.md))

            SettingItemNavigable(
                icon = Icons.Outlined.CreditCard,
                iconBackgroundColor = Color(0xFFFFF7ED),
                iconTint = Color(0xFFEA580B),
                title = "Cards & Memberships",
                subtitle = "Loyalty cards",
                onClick = onMembershipCardsClick
            )

            Spacer(Modifier.height(Spacing.md))

            SettingItemNavigable(
                icon = Icons.Outlined.Language,
                iconBackgroundColor = Color(0xFFEDE9FE),
                iconTint = Color(0xFF8B5CF6),
                title = "Language",
                subtitle = selectedLanguage,
                onClick = { /* Navigate to language selection */ }
            )

            Spacer(Modifier.height(Spacing.xl))

            // ADVANCED Section
            SectionHeader("ADVANCED")
            Spacer(Modifier.height(Spacing.md))

            SettingItemWithSwitch(
                icon = Icons.Outlined.BugReport,
                iconBackgroundColor = Color(0xFFFEE2E2),
                iconTint = Color(0xFFEF4444),
                title = "Debug Mode",
                subtitle = "Developer options",
                checked = debugMode,
                onCheckedChange = { viewModel.toggleDebugMode() }
            )

            Spacer(Modifier.height(Spacing.xl))

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PennyPal v1.2.4",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "© 2024 PennyPal. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Color(0xFF6B7280),
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun SettingItemWithSwitch(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = iconBackgroundColor,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text content
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Switch
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFEA580B),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD1D5DB)
                )
            )
        }
    }
}

@Composable
private fun SettingItemNavigable(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = iconBackgroundColor,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text content
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

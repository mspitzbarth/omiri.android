package com.example.omiri.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.SettingsViewModel
import com.example.omiri.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onMyStoresClick: () -> Unit = {},
    onMembershipCardsClick: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val shoppingListNotifications by viewModel.shoppingListNotifications.collectAsState()
    val debugMode by viewModel.debugMode.collectAsState()
    val selectedStoresCount by viewModel.selectedStoresCount.collectAsState()
    
    // Dummy state for new UI demo
    var emailNotifications by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Header
        ScreenHeader(
            title = "Settings",
            onBackClick = onBackClick
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.lg))

            // PREFERENCES
            SettingsGroup(title = "PREFERENCES") {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    iconColor = Color(0xFFFFEDDB), // Orange bg
                    iconTint = Color(0xFFEA580B), // Orange icon
                    title = "Push Notifications",
                    trailingContent = {
                        Switch(
                            checked = shoppingListNotifications,
                            onCheckedChange = { viewModel.toggleShoppingListNotifications() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEA580B),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                )
                
                HorizontalDivider(color = Color(0xFFF3F4F6))
                
                SettingsItem(
                    icon = Icons.Outlined.Email,
                    iconColor = Color(0xFFDBEAFE), // Blue bg
                    iconTint = Color(0xFF3B82F6), // Blue icon
                    title = "Email Notifications",
                    trailingContent = {
                        Switch(
                            checked = emailNotifications,
                            onCheckedChange = { emailNotifications = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEA580B),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                )
                
                HorizontalDivider(color = Color(0xFFF3F4F6))
                
                SettingsItem(
                    icon = Icons.Outlined.LocationOn,
                    iconColor = Color(0xFFCCFBF1), // Teal bg
                    iconTint = Color(0xFF14B8A6), // Teal icon
                    title = "Location",
                    subtitle = if (selectedStoresCount > 0) "$selectedStoresCount stores selected" else "Select stores",
                    onClick = onMyStoresClick
                )

                HorizontalDivider(color = Color(0xFFF3F4F6))
                
                SettingsItem(
                    icon = Icons.Outlined.Favorite,
                    iconColor = Color(0xFFFEF3C7), // Yellow bg
                    iconTint = Color(0xFFD97706), // Yellow icon
                    title = "Interests",
                    onClick = { showToast("Interests coming soon!") }
                )
            }
            
            Spacer(Modifier.height(Spacing.xl))
            
            // SUPPORT
            SettingsGroup(title = "SUPPORT") {
                SettingsItem(
                    icon = Icons.Outlined.Help,
                    iconColor = Color(0xFFCCFBF1),
                    iconTint = Color(0xFF0D9488), 
                    title = "Help Center",
                    onClick = { showToast("Help Center coming soon!") }
                )
                
                HorizontalDivider(color = Color(0xFFF3F4F6))
                
                SettingsItem(
                    icon = Icons.Outlined.HeadsetMic,
                    iconColor = Color(0xFFFCE7F3),
                    iconTint = Color(0xFFDB2777),
                    title = "Contact Support",
                    onClick = { showToast("Support contact coming soon!") }
                )
                
                HorizontalDivider(color = Color(0xFFF3F4F6))
                
                SettingsItem(
                    icon = Icons.Outlined.Star,
                    iconColor = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF16A34A),
                    title = "Rate Omiri",
                    onClick = { showToast("Rating feature coming soon!") }
                )
            }
            
            Spacer(Modifier.height(Spacing.xl))

            // LEGAL
            SettingsGroup(title = "LEGAL") {
                SettingsItem(
                    icon = Icons.Outlined.Security,
                    iconColor = Color(0xFFE5E7EB),
                    iconTint = Color(0xFF4B5563), 
                    title = "Privacy Policy",
                    onClick = { showToast("Privacy Policy coming soon!") }
                )
                
                HorizontalDivider(color = Color(0xFFF3F4F6))
                
                SettingsItem(
                    icon = Icons.Outlined.Description,
                    iconColor = Color(0xFFE5E7EB),
                    iconTint = Color(0xFF4B5563),
                    title = "Terms of Service",
                    onClick = { showToast("Terms of Service coming soon!") }
                )
            }

            Spacer(Modifier.height(Spacing.xl))
            
            // DEVELOPER
            SettingsGroup(title = "DEVELOPER") {
                 SettingsItem(
                    icon = Icons.Outlined.BugReport,
                    iconColor = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFDC2626), 
                    title = "Debug Mode",
                    trailingContent = {
                        Switch(
                            checked = debugMode,
                            onCheckedChange = { viewModel.toggleDebugMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEA580B),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                )
            }
            
            if (debugMode) {
                Spacer(Modifier.height(Spacing.md))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = "Test Notifications",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Button(
                                onClick = { viewModel.triggerFlashSaleNotification() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { 
                                Text("Flash Sale", maxLines = 1, fontSize = 12.sp) 
                            }
                            
                            Button(
                                onClick = { viewModel.triggerPriceDropNotification() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { 
                                Text("Price Drop", maxLines = 1, fontSize = 12.sp) 
                            }
                            
                            Button(
                                onClick = { viewModel.triggerListUpdateNotification() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { 
                                Text("List Update", maxLines = 1, fontSize = 12.sp) 
                            }
                        }
                    }
                }
            }
            
            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Omiri v1.2.4",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "© 2024 Omiri. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF)
                )
            }
            
            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Spacing.sm, start = Spacing.xs)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val modifier = Modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
        .padding(horizontal = Spacing.md, vertical = 12.dp) // Adjusted padding for balance
        
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column {
                 Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
        
        if (trailingContent != null) {
            trailingContent()
        } else {
            // Default trailing is chevron
             Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

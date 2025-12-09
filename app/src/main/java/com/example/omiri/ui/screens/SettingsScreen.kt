package com.example.omiri.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.omiri.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.SettingsViewModel
import com.example.omiri.ui.components.ScreenHeader
import com.example.omiri.ui.components.SettingsGroup
import com.example.omiri.ui.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onMyStoresClick: () -> Unit = {},
    onMembershipCardsClick: () -> Unit = {},
    onOnboardingClick: () -> Unit = {},
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

    var versionClickCount by remember { mutableIntStateOf(0) }
    var showEasterEgg by remember { mutableStateOf(false) }

    LaunchedEffect(showEasterEgg) {
        if (showEasterEgg) {
            delay(5000)
            showEasterEgg = false
            versionClickCount = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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

                // PREFERENCES (Same as before)
                SettingsGroup(title = "PREFERENCES") {
                    SettingsItem(
                        icon = Icons.Outlined.Notifications,
                        iconColor = Color(0xFFFFEDDB), // Orange bg
                        iconTint = Color(0xFFFE8357), // Orange icon
                        title = "Push Notifications",
                        trailingContent = {
                            Switch(
                                checked = shoppingListNotifications,
                                onCheckedChange = { viewModel.toggleShoppingListNotifications() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFE8357),
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
                                    checkedTrackColor = Color(0xFFFE8357),
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
                        icon = Icons.Outlined.CreditCard,
                        iconColor = Color(0xFFE0E7FF), // Indigo bg
                        iconTint = Color(0xFF4338CA), // Indigo icon
                        title = "Membership Cards",
                        subtitle = "Manage your loyalty cards",
                        onClick = onMembershipCardsClick
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
                                    checkedTrackColor = Color(0xFFFE8357),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                    )
                    
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    
                    SettingsItem(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        iconColor = Color(0xFFE0F2FE), // Light Blue
                        iconTint = Color(0xFF0284C7), // Blue
                        title = "Show Dummy Chat",
                        trailingContent = {
                            val dummyData by viewModel.showDummyData.collectAsState()
                            Switch(
                                checked = dummyData,
                                onCheckedChange = { viewModel.toggleShowDummyData() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFE8357),
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
                            
                            Spacer(Modifier.height(Spacing.sm))
                            
                            Button(
                                onClick = onOnboardingClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                            ) {
                                Text("Trigger Onboarding Flow")
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
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            versionClickCount++
                            if (versionClickCount >= 7) {
                                showEasterEgg = true
                                versionClickCount = 0
                            }
                        }
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
        
        // Easter Egg Overlay
        if (showEasterEgg) {
            EasterEggOverlay()
        }
    }
}

@Composable
fun EasterEggOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {}, // Swallow clicks
        contentAlignment = Alignment.Center
    ) {
        // Falling Sakura Container - Aligned TopStart so offsets are absolute
        Box(modifier = Modifier.fillMaxSize()) { 
             val density = LocalDensity.current
             val configuration = LocalConfiguration.current
             val screenHeight = configuration.screenHeightDp.dp
             val screenWidth = configuration.screenWidthDp.dp
             
             // Create 80 falling items
             repeat(80) {
                 key(it) {
                     FallingEmoji(
                         maxHeight = screenHeight,
                         maxWidth = screenWidth
                     )
                 }
             }
        }
        
        // Panda/Bubu and Name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bubu),
                    contentDescription = "Bubu",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "SAMO",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 48.sp
            )
        }
    }
}

@Composable
fun FallingEmoji(maxHeight: Dp, maxWidth: Dp) {
    // Sakura emojis
    val emoji = remember { listOf("🌸", "💮", "🌸", "❤️").random() }
    
    val durationMillis = remember { Random.nextInt(3000, 6000) }
    val startDelay = remember { Random.nextInt(0, 2000) }
    val startX = remember { Random.nextFloat() } // 0.0 to 1.0 multiplier
    val emojiSize = remember { Random.nextInt(20, 45).sp }
    
    // Vertical Drop
    val animatedY = remember { Animatable(-100f) }
    
    // Rotation
    val rotation = remember { Animatable(0f) }
    val targetRotation = remember { Random.nextInt(180, 720).toFloat() * if(Random.nextBoolean()) 1 else -1 }
    
    LaunchedEffect(Unit) {
        delay(startDelay.toLong())
        launch {
            animatedY.animateTo(
                targetValue = maxHeight.value + 100f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing
                )
            )
        }
        launch {
            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing
                )
            )
        }
    }
    
    // Horizontal Sway (Sine wave based on Y progress)
    // We approximate sway by simple offset based on time or just randomized linear drift?
    // Let's do a simple randomized drift + sine sway
    // Since we don't have frame time easily here without checking frame, 
    // we can assume Y correlates to time.
    // Normalized Y (0 to 1) -> 
    val currentY = animatedY.value
    val normalizedY = (currentY + 100f) / (maxHeight.value + 200f)
    val swayAmplitude = 50f // px sway
    val swayFrequency = 2.0 * Math.PI // Full sine wave
    
    // Calculate sway offset: sin(progress * freq) * amp
    val swayOffset = (kotlin.math.sin(normalizedY * swayFrequency * 3) * swayAmplitude).toFloat()
    
    val xPos = (maxWidth.value * startX).dp + swayOffset.dp
    
    Text(
        text = emoji,
        fontSize = emojiSize,
        modifier = Modifier
            .absoluteOffset(x = xPos, y = animatedY.value.dp)
            .graphicsLayer {
                rotationZ = rotation.value
            }
    )
}




package com.example.omiri.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.R
import com.example.omiri.ui.components.*
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.MyStoresViewModel
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.launch

enum class OnboardingStep {
    WELCOME,
    STORES,
    NOTIFICATIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    storesViewModel: MyStoresViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Store State
    val selectedCountry by storesViewModel.selectedCountry.collectAsState()
    val availableStores by storesViewModel.availableStores.collectAsState()
    val selectedStores by storesViewModel.selectedStores.collectAsState()
    val storeLocations by storesViewModel.storeLocations.collectAsState()
    val isLoading by storesViewModel.isLoading.collectAsState()
    
    // Location modal state
    val locationModalStore by storesViewModel.locationModalStore.collectAsState()
    val availableLocations by storesViewModel.availableLocations.collectAsState()
    val isLoadingLocations by storesViewModel.isLoadingLocations.collectAsState()
    var selectedZipcodes by remember { mutableStateOf(setOf<String>()) }
    
    // UI Local State for modals
    var showCountryPicker by remember { mutableStateOf(false) }

    // Update selected zipcodes when modal opens
    LaunchedEffect(locationModalStore) {
        locationModalStore?.let { store ->
            selectedZipcodes = storeLocations[store.id] ?: emptySet()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF9FAFB) // Light gray bg like Settings
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Back Button (except Welcome)
            if (currentStep != OnboardingStep.WELCOME) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            currentStep = when (currentStep) {
                                OnboardingStep.STORES -> OnboardingStep.WELCOME
                                OnboardingStep.NOTIFICATIONS -> OnboardingStep.STORES
                                else -> OnboardingStep.WELCOME
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                    
                    // Progress Bar Centered
                    Box(modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.6f)) {
                         OnboardingProgressBar(
                            currentStep = currentStep.ordinal,
                            totalSteps = 3
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
            }

            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> {
                        WelcomeContent(
                            onNext = { 
                                storesViewModel.loadStores() // Ensure loaded
                                currentStep = OnboardingStep.STORES 
                            }
                        )
                    }
                    OnboardingStep.STORES -> {
                        StoreSelectionContent(
                            selectedCountry = selectedCountry,
                            availableStores = availableStores,
                            selectedStores = selectedStores,
                            storeLocations = storeLocations,
                            isLoading = isLoading,
                            onCountryClick = { showCountryPicker = true },
                            onStoreToggle = { store ->
                                if (store.hasMultipleLocations) {
                                    storesViewModel.openLocationModal(store)
                                } else {
                                    storesViewModel.toggleStore(store.id, false)
                                }
                            },
                            onNext = { currentStep = OnboardingStep.NOTIFICATIONS }
                        )
                    }
                    OnboardingStep.NOTIFICATIONS -> {
                        NotificationsContent(
                            onFinish = onFinish,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
    
    // Modals
    if (showCountryPicker) {
        CountryPickerDialog(
            selectedCountry = selectedCountry,
            onCountrySelected = { 
                storesViewModel.selectCountry(it)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }

    // Location Modal
    locationModalStore?.let { store ->
        StoreLocationModal(
            storeName = store.retailer,
            locations = availableLocations,
            selectedZipcodes = selectedZipcodes,
            isLoading = isLoadingLocations,
            onLocationToggle = { zipcode ->
                selectedZipcodes = if (selectedZipcodes.contains(zipcode)) {
                    selectedZipcodes - zipcode
                } else {
                    if (selectedZipcodes.size < 5) {
                        selectedZipcodes + zipcode
                    } else {
                        selectedZipcodes
                    }
                }
            },
            onSave = {
                storesViewModel.saveStoreLocations(store.id, selectedZipcodes)
            },
            onDismiss = {
                storesViewModel.closeLocationModal()
            }
        )
    }
}

@Composable
fun OnboardingProgressBar(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index <= currentStep) Color(0xFFFE8357) else Color(0xFFE5E7EB)
                    )
            )
        }
    }
}

@Composable
fun WelcomeContent(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(Modifier.weight(1f))
        
        // Logo
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFFE8357)),
            contentAlignment = Alignment.Center
        ) {
             Icon(
                painter = painterResource(id = R.drawable.ic_omiri_logo),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        Text(
            text = "Omiri",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        
        Spacer(Modifier.height(Spacing.xl))
        
        Text(
            text = "Smarter shopping,\neffortless savings.",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF111827)
        )
        
        Spacer(Modifier.height(Spacing.md))
        
        Text(
            text = "Track deals, plan shopping trips, and\nsave money weekly.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFF6B7280)
        )
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Get started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(Spacing.lg))
    }
}

@Composable
fun StoreSelectionContent(
    selectedCountry: String,
    availableStores: List<com.example.omiri.data.api.models.StoreListResponse>,
    selectedStores: Set<String>,
    storeLocations: Map<String, Set<String>>,
    isLoading: Boolean,
    onCountryClick: () -> Unit,
    onStoreToggle: (com.example.omiri.data.api.models.StoreListResponse) -> Unit,
    onNext: () -> Unit
) {
    // Validation Logic
    val isValid = selectedStores.isNotEmpty() && selectedStores.all { storeId ->
        // If the store is selected, check if it needs locations.
        // We need to look up if the store hasMultipleLocations. 
        // Iterate availableStores to find the store object
        val store = availableStores.find { it.id == storeId }
        if (store?.hasMultipleLocations == true) {
            // Must have at least 1 location selected
            val locations = storeLocations[storeId]
            !locations.isNullOrEmpty()
        } else {
            true // Single location stores are valid just by being selected
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Select your stores",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = "Pick the stores you shop at to see relevant deals.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
        
        Spacer(Modifier.height(Spacing.lg))
        
        CountrySelector(
            selectedCountry = selectedCountry,
            onClick = onCountryClick
        )
        
        Spacer(Modifier.height(Spacing.md))
        
        // List of stores
        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFE8357))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(availableStores) { store ->
                    StoreItem(
                        store = store,
                        isSelected = selectedStores.contains(store.id),
                        selectedLocationCount = storeLocations[store.id]?.size ?: 0,
                        onToggle = { onStoreToggle(store) }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isValid, 
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFE8357),
                disabledContainerColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isValid) Color.White else Color(0xFF9CA3AF)
            )
        }
        
        Spacer(Modifier.height(Spacing.md))


        
        Spacer(Modifier.height(Spacing.lg))
    }
}

@Composable
fun NotificationsContent(
    onFinish: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    // Toggles state
    var notificationsEnabled by remember { mutableStateOf(false) }
    var priceDrops by remember { mutableStateOf(true) }
    var weeklyFlyers by remember { mutableStateOf(true) }
    var expiringSoon by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        notificationsEnabled = isGranted
        if (!isGranted) {
             scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Notifications permission denied. Please enable them in settings.",
                    duration = SnackbarDuration.Short
                )
             }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make scrollable
    ) {
        Spacer(Modifier.height(Spacing.md))

        // Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFFFF7ED), RoundedCornerShape(20.dp))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = null,
                tint = Color(0xFFFE8357),
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(Modifier.height(Spacing.lg))
        
        Text(
            text = "Don't miss the best\ndeals",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF111827),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(Spacing.sm))
        
         Text(
            text = "Enable alerts for price drops, new flyers, and expiring offers.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color(0xFF6B7280),
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)
        )
        
        Spacer(Modifier.height(Spacing.xl))

        // Settings Groups style
        SettingsGroup(title = "GENERAL") {
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                iconColor = Color(0xFFFFEDDB), // Orange bg
                iconTint = Color(0xFFFE8357),
                title = "Push Notifications",
                subtitle = "Enable critical alerts about deals",
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                        notificationsEnabled = true
                                    } else {
                                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    notificationsEnabled = true
                                }
                            } else {
                                notificationsEnabled = false
                            }
                        },
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

        // Sub Options (Visible only if main is enabled)
        AnimatedVisibility(
            visible = notificationsEnabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(Modifier.height(Spacing.lg))
                SettingsGroup(title = "CUSTOMIZE ALERTS") {
                    SettingsItem(
                        icon = Icons.Outlined.TrendingDown,
                        iconColor = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFEF4444),
                        title = "Price drops",
                        subtitle = "Get alerts when items go on sale",
                        trailingContent = {
                             Switch(
                                checked = priceDrops,
                                onCheckedChange = { priceDrops = it },
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
                        icon = Icons.Outlined.Newspaper,
                        iconColor = Color(0xFFDBEAFE),
                        iconTint = Color(0xFF3B82F6),
                        title = "New weekly flyers",
                        subtitle = "Stay updated with fresh deals",
                        trailingContent = {
                             Switch(
                                checked = weeklyFlyers,
                                onCheckedChange = { weeklyFlyers = it },
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
                        icon = Icons.Outlined.Timer,
                        iconColor = Color(0xFFF3E8FF),
                        iconTint = Color(0xFFA855F7),
                        title = "Expiring soon",
                        subtitle = "Never miss limited-time offers",
                         trailingContent = {
                             Switch(
                                checked = expiringSoon,
                                onCheckedChange = { expiringSoon = it },
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
            }
        }
        
        Spacer(Modifier.height(Spacing.xxl))
        
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Finish Setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(Spacing.md))
    }
}

@Composable
fun NotificationToggleItem(
    icon: ImageVector,
    iconColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), // Light gray bg
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            
            Spacer(Modifier.width(Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFFE8357),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD1D5DB)
                )
            )
        }
    }
}

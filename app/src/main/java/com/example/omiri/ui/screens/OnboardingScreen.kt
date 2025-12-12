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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    PERSONALIZATION,
    STORES,
    CREATE_LIST,
    NOTIFICATIONS,
    COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    storesViewModel: MyStoresViewModel = viewModel(),
    shoppingListViewModel: com.example.omiri.viewmodels.ShoppingListViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val userPreferences = remember { com.example.omiri.data.local.UserPreferences(context) }

    // Store State
    val selectedCountry by storesViewModel.selectedCountry.collectAsState()
    val availableStores by storesViewModel.availableStores.collectAsState()
    val selectedStores by storesViewModel.selectedStores.collectAsState()
    val storeLocations by storesViewModel.storeLocations.collectAsState()
    val isLoading by storesViewModel.isLoading.collectAsState()
    
    // Shopping List State
    val currentListId by shoppingListViewModel.currentListId.collectAsState()
    
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
            // Header with Back Button (except Welcome and Completed)
            if (currentStep != OnboardingStep.WELCOME && currentStep != OnboardingStep.COMPLETED) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            currentStep = when (currentStep) {
                                OnboardingStep.PERSONALIZATION -> OnboardingStep.WELCOME
                                OnboardingStep.STORES -> OnboardingStep.PERSONALIZATION
                                OnboardingStep.CREATE_LIST -> OnboardingStep.STORES
                                OnboardingStep.NOTIFICATIONS -> OnboardingStep.CREATE_LIST
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
                            totalSteps = 5
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
                                currentStep = OnboardingStep.PERSONALIZATION 
                            }
                        )
                    }
                    OnboardingStep.PERSONALIZATION -> {
                        PersonalizationContent(
                            onNext = {
                                storesViewModel.loadStores() // Ensure loaded
                                currentStep = OnboardingStep.STORES
                            },
                            userPreferences = userPreferences
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
                            onNext = { currentStep = OnboardingStep.CREATE_LIST }
                        )
                    }
                    OnboardingStep.CREATE_LIST -> {
                        CreateListContent(
                            onNext = { name ->
                                // Rename default list or create new
                                val listId = currentListId
                                if (listId != null && name.isNotBlank()) {
                                    // Direct repo call since VM doesn't expose rename yet
                                    com.example.omiri.data.repository.ShoppingListRepository.renameList(listId, name)
                                }
                                currentStep = OnboardingStep.NOTIFICATIONS
                            }
                        )
                    }
                    OnboardingStep.NOTIFICATIONS -> {
                        NotificationsContent(
                            onFinish = {
                                currentStep = OnboardingStep.COMPLETED
                            },
                            snackbarHostState = snackbarHostState
                        )
                    }
                    OnboardingStep.COMPLETED -> {
                        CompletedContent(
                            onFinish = onFinish
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

@Composable
fun PersonalizationContent(
    onNext: () -> Unit,
    userPreferences: com.example.omiri.data.local.UserPreferences
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Data Loading
    var categories by remember { mutableStateOf<List<PersonalizationCategory>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("personalization_options.json").bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonString)
                val loadedCategories = mutableListOf<PersonalizationCategory>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val category = obj.getString("category")
                    val key = obj.getString("key")
                    val optionsArray = obj.getJSONArray("options")
                    val options = mutableListOf<String>()
                    for (j in 0 until optionsArray.length()) {
                        options.add(optionsArray.getString(j))
                    }
                    loadedCategories.add(PersonalizationCategory(category, key, options))
                }
                categories = loadedCategories
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // State for selections
    // We map "key" -> Set of selected options
    var selectedOptions by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    
    // Demographics State
    var selectedGender by remember { mutableStateOf("") }
    var selectedAgeRange by remember { mutableStateOf("") }
    
    val genderOptions = listOf("Male", "Female", "Non-binary", "Prefer not to say")
    val ageOptions = listOf("18-24", "25-34", "35-44", "45-54", "55-64", "65+")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Tell us about you",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = "Help us personalize your deals and recommendations. All fields are optional.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
        
        Spacer(Modifier.height(Spacing.lg))
        
        // Dynamic Categories
        if (categories.isEmpty()) {
             Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFE8357))
            }
        } else {
            categories.forEach { category ->
                Text(
                    text = category.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(Spacing.sm))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    category.options.forEach { option ->
                        val currentSelections = selectedOptions[category.key] ?: emptySet()
                        val isSelected = currentSelections.contains(option)
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                val newSet = if (isSelected) {
                                    currentSelections - option
                                } else {
                                    currentSelections + option
                                }
                                selectedOptions = selectedOptions + (category.key to newSet)
                            },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFE8357),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF374151)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
            }
        }

        // Demographics at the end
        // Gender Section
        Text(
            text = "Gender (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(Modifier.height(Spacing.sm))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            genderOptions.forEach { gender ->
                val isSelected = selectedGender == gender
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGender = if (isSelected) "" else gender },
                    label = { Text(gender) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFE8357), // Brand Orange
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color(0xFF374151)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                        borderWidth = 1.dp
                    )
                )
            }
        }
        
        Spacer(Modifier.height(Spacing.lg))
        
        // Age Section
        Text(
            text = "Age Range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(Modifier.height(Spacing.sm))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            ageOptions.forEach { age ->
                val isSelected = selectedAgeRange == age
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedAgeRange = if (isSelected) "" else age },
                    label = { Text(age) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFE8357),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color(0xFF374151)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                        borderWidth = 1.dp
                    )
                )
            }
        }
        
        Spacer(Modifier.height(Spacing.xxl))
        
        Button(
            onClick = {
                scope.launch {
                    // Save preferences
                    if (selectedGender.isNotBlank()) userPreferences.saveUserGender(selectedGender)
                    if (selectedAgeRange.isNotBlank()) userPreferences.saveUserAgeRange(selectedAgeRange)
                    
                    // Flatten all selected options into one set of interests for now
                    val allInterests = selectedOptions.values.flatten().toSet()
                    if (allInterests.isNotEmpty()) userPreferences.saveUserInterests(allInterests)
                    
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(Spacing.md))
    }
}


data class PersonalizationCategory(
    val category: String,
    val key: String,
    val options: List<String>
)

@Composable
fun CreateListContent(onNext: (String) -> Unit) {
    var listName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFEFF6FF), RoundedCornerShape(20.dp)) // Blue bg
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                tint = Color(0xFF3B82F6), // Blue
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(Modifier.height(Spacing.lg))
        
        Text(
            text = "Name your list",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        
        Spacer(Modifier.height(Spacing.sm))
        
        Text(
            text = "Give your first shopping list a name.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
        
        Spacer(Modifier.height(Spacing.xl))
        
        OutlinedTextField(
            value = listName,
            onValueChange = { listName = it },
            label = { Text("List Name") },
            placeholder = { Text("e.g., Weekly Groceries") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFE8357),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedLabelColor = Color(0xFFFE8357),
                cursorColor = Color(0xFFFE8357)
            )
        )
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = { onNext(listName.trim().ifEmpty { "My List" }) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(Spacing.md))
    }
}

@Composable
fun CompletedContent(onFinish: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(Modifier.weight(1f))
        
        // Success Icon/Circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)), // Light Green
            contentAlignment = Alignment.Center
        ) {
             Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color(0xFF16A34A), // Green
                modifier = Modifier.size(50.dp)
            )
        }
        
        Spacer(Modifier.height(Spacing.xl))
        
        Text(
            text = "You are now all set up",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(Spacing.md))
        
        Text(
            text = "Start tracking deals and saving money with Omiri.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFF6B7280)
        )
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Start using App",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(Spacing.lg))
    }
}

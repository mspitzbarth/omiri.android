package com.example.omiri.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.omiri.data.local.UserPreferences

enum class OnboardingStep {
    STEP_1_GOALS,
    STEP_2_DEALS,
    STEP_3_DIETARY,
    STORES,
    CREATE_LIST,
    COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    storesViewModel: MyStoresViewModel = viewModel(),
    shoppingListViewModel: com.example.omiri.viewmodels.ShoppingListViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.STEP_1_GOALS) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }

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

    // List Name State
    var listName by remember { mutableStateOf("") }

    // Personalization Data Loading
    var categories by remember { mutableStateOf<List<PersonalizationCategory>>(emptyList()) }
    var selectedOptions by remember { mutableStateOf(mapOf<String, Set<String>>()) }

    // Update selected zipcodes when modal opens
    LaunchedEffect(locationModalStore) {
        locationModalStore?.let { store ->
            selectedZipcodes = storeLocations[store.id] ?: emptySet()
        }
    }

    // Load Personalization Options
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

    fun updateSelection(key: String, option: String, isSelected: Boolean) {
        val currentSet = selectedOptions[key] ?: emptySet()
        val newSet = if (isSelected) currentSet + option else currentSet - option
        selectedOptions = selectedOptions + (key to newSet)
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF9FAFB), // Light gray bg
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo + Text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         if (currentStep != OnboardingStep.STEP_1_GOALS && currentStep != OnboardingStep.COMPLETED) {
                             IconButton(onClick = {
                                 currentStep = when(currentStep) {
                                     OnboardingStep.STEP_2_DEALS -> OnboardingStep.STEP_1_GOALS
                                     OnboardingStep.STEP_3_DIETARY -> OnboardingStep.STEP_2_DEALS
                                     OnboardingStep.STORES -> OnboardingStep.STEP_3_DIETARY
                                     OnboardingStep.CREATE_LIST -> OnboardingStep.STORES
                                     else -> currentStep
                                 }
                             }) {
                                 Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF111827))
                             }
                         }

                         Box(
                             modifier = Modifier
                                 .size(32.dp)
                                 .clip(CircleShape)
                                 .background(Color(0xFFFE8357)),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 painter = painterResource(id = R.drawable.ic_omiri_logo),
                                 contentDescription = null,
                                 tint = Color.White,
                                 modifier = Modifier.size(20.dp)
                             )
                         }
                         Spacer(Modifier.width(Spacing.sm))
                         Text(
                             text = "Omiri",
                             style = MaterialTheme.typography.titleLarge,
                             fontWeight = FontWeight.Bold,
                             color = Color(0xFF111827)
                         )
                    }

                    // Skip Button Logic
                    val progressStep = when(currentStep) {
                        OnboardingStep.STEP_1_GOALS -> 0
                        OnboardingStep.STEP_2_DEALS -> 1
                        OnboardingStep.STEP_3_DIETARY -> 2
                        OnboardingStep.STORES -> 3
                        OnboardingStep.CREATE_LIST -> 4
                        else -> -1
                    }

                    if (progressStep != -1 && progressStep < 3) { 
                        TextButton(onClick = {
                            currentStep = when(currentStep) {
                                OnboardingStep.STEP_1_GOALS -> OnboardingStep.STEP_2_DEALS
                                OnboardingStep.STEP_2_DEALS -> OnboardingStep.STEP_3_DIETARY
                                OnboardingStep.STEP_3_DIETARY -> {
                                    storesViewModel.loadStores()
                                    OnboardingStep.STORES
                                }
                                else -> currentStep
                            }
                        }) {
                            Text("Skip", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                        }
                    } else if (currentStep != OnboardingStep.COMPLETED) {
                         Spacer(Modifier.width(48.dp))
                    }
                }
                
                // Linear Progress Indicator
                val progressStep = when(currentStep) {
                        OnboardingStep.STEP_1_GOALS -> 0
                        OnboardingStep.STEP_2_DEALS -> 1
                        OnboardingStep.STEP_3_DIETARY -> 2
                        OnboardingStep.STORES -> 3
                        OnboardingStep.CREATE_LIST -> 4
                        else -> -1
                }
                
                if (progressStep != -1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (index <= progressStep) Color(0xFFFE8357) else Color(0xFFE5E7EB)
                                    )
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
             if (currentStep != OnboardingStep.COMPLETED) {
                 Surface(
                     color = Color.White,
                     shadowElevation = 8.dp, // Slight shadow for separation
                     modifier = Modifier.fillMaxWidth()
                 ) {
                     Column(modifier = Modifier.padding(Spacing.md)) {
                         
                        // Determine button state and action
                        val (isEnabled, buttonText, onAction) = when(currentStep) {
                             OnboardingStep.STEP_1_GOALS -> Triple(true, "Continue") { currentStep = OnboardingStep.STEP_2_DEALS }
                             OnboardingStep.STEP_2_DEALS -> Triple(true, "Continue") { currentStep = OnboardingStep.STEP_3_DIETARY }
                             OnboardingStep.STEP_3_DIETARY -> Triple(true, "Continue") { 
                                 storesViewModel.loadStores()
                                 currentStep = OnboardingStep.STORES 
                             }
                             OnboardingStep.STORES -> {
                                 val valid = selectedStores.isNotEmpty()
                                 Triple(valid, "Continue") { currentStep = OnboardingStep.CREATE_LIST }
                             }
                             OnboardingStep.CREATE_LIST -> {
                                 Triple(listName.isNotBlank(), "Create List") { 
                                     shoppingListViewModel.createList(listName)
                                     currentStep = OnboardingStep.COMPLETED
                                 }
                             }
                             else -> Triple(true, "Continue", {})
                        }

                        Button(
                            onClick = onAction,
                            enabled = isEnabled,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFE8357),
                                disabledContainerColor = Color(0xFFE5E7EB)
                            )
                        ) {
                            Text(text = buttonText, fontWeight = FontWeight.Bold, color = if(isEnabled) Color.White else Color(0xFF9CA3AF))
                        }
                     }
                 }
             }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.md)
                .background(Color(0xFFF9FAFB))
        ) {
            when(currentStep) {
                OnboardingStep.STEP_1_GOALS -> OnboardingStep1(
                    categories = categories,
                    selectedOptions = selectedOptions,
                    onOptionToggle = ::updateSelection
                )
                OnboardingStep.STEP_2_DEALS -> OnboardingStep2(
                    categories = categories,
                    selectedOptions = selectedOptions,
                    onOptionToggle = ::updateSelection,
                    userPreferences = userPreferences
                )
                OnboardingStep.STEP_3_DIETARY -> OnboardingStep3(
                    categories = categories,
                    selectedOptions = selectedOptions,
                    onOptionToggle = ::updateSelection
                )
                OnboardingStep.STORES -> StoreSelectionContent(
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
                    }
                )
                OnboardingStep.CREATE_LIST -> CreateListContent(
                    listName = listName,
                    onNameChange = { listName = it }
                )
                OnboardingStep.COMPLETED -> CompletedContent(
                    onFinish = onFinish
                )
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
fun OnboardingDotsIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        repeat(totalSteps) { index ->
            val color = if (index == currentStep) Color(0xFFFE8357) else Color(0xFFD1D5DB)
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun OnboardingStep1(
    categories: List<PersonalizationCategory>,
    selectedOptions: Map<String, Set<String>>,
    onOptionToggle: (String, String, Boolean) -> Unit
) {
    val shoppingGoals = categories.find { it.key == "shopping_goals" }
    val shoppingMode = categories.find { it.key == "shopping_mode" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Light Orange
            border = BorderStroke(1.dp, Color(0xFFFE8357))
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFE8357),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Personalized for you",
                        color = Color(0xFFFE8357),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "Let's find your best deals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Pick what matters most — we'll tailor prices, stores, and alerts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4B5563)
                )
            }
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        // Shopping Goals
        shoppingGoals?.let { category ->
            SettingsGroup(title = "What are you shopping for?") {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                     Text("Choose up to 3 -- we'll optimize your feed.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                     Spacer(Modifier.height(Spacing.md))
                     FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        category.options.forEach { option ->
                            val isSelected = selectedOptions[category.key]?.contains(option) == true
                            FilterChip(
                                selected = isSelected,
                                onClick = { onOptionToggle(category.key, option, !isSelected) },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFE8357),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                 }
            }
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        // Shopping Mode
        shoppingMode?.let { category ->
            SettingsGroup(title = "How do you shop?") {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                     Text("So we can show the right deals at the right time.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                     
                     Spacer(Modifier.height(Spacing.md))

                     // Primary Modes
                    val mainModes = listOf("In-store", "Pickup", "Delivery")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        mainModes.forEach { mode ->
                             val isSelected = selectedOptions[category.key]?.contains(mode) == true
                             val isAny = selectedOptions[category.key]?.contains("Any") == true
                             val visuallySelected = isSelected || (mode == "In-store" && isAny)
                             
                             Box(modifier = Modifier.weight(1f)) {
                                 FilterChip(
                                    selected = visuallySelected,
                                    onClick = { onOptionToggle(category.key, mode, !visuallySelected) },
                                    label = { Text(mode, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFE8357),
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                             }
                        }
                    }
                    
                    Spacer(Modifier.height(Spacing.md))
                    
                    FilterChip(
                        selected = selectedOptions[category.key]?.contains("Any") == true,
                        onClick = { onOptionToggle(category.key, "Any", !(selectedOptions[category.key]?.contains("Any") ?: false)) },
                        label = { Text("Any", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        shape = RoundedCornerShape(16.dp),
                         colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFE8357),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(Spacing.lg))
                    
                    // Toggles for specialized options
                    val switchOptions = listOf("Willing to switch stores", "Prefer one main store")
                    switchOptions.forEach { opt ->
                        val isChecked = selectedOptions[category.key]?.contains(opt) == true
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = opt + if(opt.contains("switch")) " for better deals" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151),
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isChecked,
                                onCheckedChange = { onOptionToggle(category.key, opt, it) },
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
            }
        }
        // Button removed (handled by parent Scaffold)
        Spacer(Modifier.height(Spacing.xxl))
    }
}

@Composable
fun OnboardingStep2(
    categories: List<PersonalizationCategory>,
    selectedOptions: Map<String, Set<String>>,
    onOptionToggle: (String, String, Boolean) -> Unit,
    userPreferences: UserPreferences
) {
    val dealTypes = categories.find { it.key == "deal_types" }
    
    // Notifications State (Local for this step - dummy for now as requesting ui only)
    var notificationsEnabled by remember { mutableStateOf(true) }
    var weeklyDigest by remember { mutableStateOf(false) }
    var bigDiscounts by remember { mutableStateOf(true) }
    
    // Max alerts slider/selector
    var maxAlerts by remember { mutableIntStateOf(3) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
         // Deal Types
        dealTypes?.let { category ->
            SettingsGroup(title = "What kinds of deals do you want?") {
                Column(modifier = Modifier.padding(Spacing.md)) {
                     Text("Pick what you actually use.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                     Spacer(Modifier.height(Spacing.md))
                     FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        category.options.forEach { option ->
                            val isSelected = selectedOptions[category.key]?.contains(option) == true
                            FilterChip(
                                selected = isSelected,
                                onClick = { onOptionToggle(category.key, option, !isSelected) },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFE8357),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        // Deal Alerts
        SettingsGroup(title = "Deal alerts") {
             Column(modifier = Modifier.padding(Spacing.md)) {
                Text("Only what's useful — no spam.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                // Switches
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notify me when items on my list go on sale", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFFE8357))
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Weekly deals digest", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(
                        checked = weeklyDigest,
                        onCheckedChange = { weeklyDigest = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFFE8357))
                    )
                }
                
                 // Big discounts
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Only big discounts (20%+)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(
                        checked = bigDiscounts,
                        onCheckedChange = { bigDiscounts = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFFE8357))
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF3F4F6))

                Text("Max alerts/day", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Spacing.sm))
                // Max Alerts Chips
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    listOf(1, 3, 5).forEach { amount ->
                        val selected = maxAlerts == amount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) Color.White else Color.Transparent)
                                .clickable { maxAlerts = amount },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = amount.toString(), 
                                fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal,
                                color = if(selected) Color.Black else Color.Gray
                            )
                        }
                    }
                }
             }
        }
        
        Spacer(Modifier.height(Spacing.xxl))
    }
}

@Composable
fun OnboardingStep3(
    categories: List<PersonalizationCategory>,
    selectedOptions: Map<String, Set<String>>,
    onOptionToggle: (String, String, Boolean) -> Unit
) {
    val dietary = categories.find { it.key == "dietary_style" }
    val budget = categories.find { it.key == "weekly_budget" }
    
    // Hardcoded options if not in JSON
    val genderOptions = listOf("Male", "Female", "Non-binary", "Prefer not to say")
    val ageOptions = listOf("18-24", "25-34", "35-44", "45-54", "55-64", "65+")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        
        // Dietary Preferences
        dietary?.let { category ->
            SettingsGroup(title = "Food preferences (optional)") {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                     Text("Helps recipes + grocery matches.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                     Spacer(Modifier.height(Spacing.md))
                     FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        category.options.forEach { option ->
                            val isSelected = selectedOptions[category.key]?.contains(option) == true
                            FilterChip(
                                selected = isSelected,
                                onClick = { onOptionToggle(category.key, option, !isSelected) },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFE8357),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                 }
            }
        }
        
        Spacer(Modifier.height(Spacing.md))

        // Demographics Section
        SettingsGroup(title = "About you (optional)") {
            Column(modifier = Modifier.padding(Spacing.md)) {
                
                // Weekly Budget
                budget?.let { category ->
                    Text("Weekly Budget", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         items(category.options) { option ->
                            val isSelected = selectedOptions[category.key]?.contains(option) == true
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    selectedOptions[category.key]?.forEach { onOptionToggle(category.key, it, false) }
                                    onOptionToggle(category.key, option, true)
                                },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFE8357),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))
                }
                
                // Gender
                Text("Gender", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     items(genderOptions) { option ->
                        val isSelected = selectedOptions["gender"]?.contains(option) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                 selectedOptions["gender"]?.forEach { onOptionToggle("gender", it, false) }
                                 onOptionToggle("gender", option, true)
                            },
                            label = { Text(option) },
                             colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFE8357),
                                    selectedLabelColor = Color.White
                                )
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))
                
                // Age
                Text("Age Range", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     items(ageOptions) { option ->
                        val isSelected = selectedOptions["age_range"]?.contains(option) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                 selectedOptions["age_range"]?.forEach { onOptionToggle("age_range", it, false) }
                                 onOptionToggle("age_range", option, true)
                            },
                            label = { Text(option) },
                             colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFE8357),
                                    selectedLabelColor = Color.White
                                )
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(Spacing.lg))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Securely stored locally. Improves deal relevance.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        Spacer(Modifier.weight(1f))
        
        // No local buttons needed
        Spacer(Modifier.height(Spacing.xl))
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
    onStoreToggle: (com.example.omiri.data.api.models.StoreListResponse) -> Unit
) {
    // Dummy Data for Preview/Fallback if no API data
    val dummyStores = remember {
        listOf(
            com.example.omiri.data.api.models.StoreListResponse("1", "Walmart", "US", 100, 100, true, emptyList(), emptyList(), emptyList()),
            com.example.omiri.data.api.models.StoreListResponse("2", "Target", "US", 50, 50, true, emptyList(), emptyList(), emptyList()),
            com.example.omiri.data.api.models.StoreListResponse("3", "Whole Foods", "US", 20, 20, false, emptyList(), emptyList(), emptyList()),
            com.example.omiri.data.api.models.StoreListResponse("4", "Costco", "US", 30, 30, true, emptyList(), emptyList(), emptyList()),
            com.example.omiri.data.api.models.StoreListResponse("5", "Kroger", "US", 40, 40, false, emptyList(), emptyList(), emptyList()),
            com.example.omiri.data.api.models.StoreListResponse("6", "Aldi", "US", 10, 10, false, emptyList(), emptyList(), emptyList())
        )
    }
    
    val storesToDisplay = if (availableStores.isEmpty() && !isLoading) dummyStores else availableStores

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
         // Header Card (matching Step 1 style)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Light Orange
            border = BorderStroke(1.dp, Color(0xFFFE8357))
        ) {
             Column(modifier = Modifier.padding(Spacing.lg)) {
                 Text(
                    text = "Select your stores",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Pick the stores you shop at to see relevant deals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4B5563)
                )
             }
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        // Country Selector
        SettingsGroup(title = "Region") {
             Box(modifier = Modifier.padding(Spacing.md)) {
                 CountrySelector(
                    selectedCountry = selectedCountry,
                    onClick = onCountryClick
                )
             }
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        // List of stores
        SettingsGroup(title = "Available Stores") {
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFE8357))
                }
            } else {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                    storesToDisplay.forEach { store ->
                        StoreItem(
                            store = store,
                            isSelected = selectedStores.contains(store.id),
                            selectedLocationCount = storeLocations[store.id]?.size ?: 0,
                            onToggle = { onStoreToggle(store) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    
                    if (storesToDisplay == dummyStores) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Showing demo stores (No API connection)", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                 }
            }
        }
        
        Spacer(Modifier.height(Spacing.xxl))
    }
}

data class PersonalizationCategory(
    val category: String,
    val key: String,
    val options: List<String>
)

@Composable
fun CreateListContent(
    listName: String,
    onNameChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(Spacing.xl))
        
        SettingsGroup(title = "Name your first list") {
             Column(
                 modifier = Modifier.padding(Spacing.lg),
                 horizontalAlignment = Alignment.CenterHorizontally
             ) {
                Text(
                     text = "Start your savings journey.", 
                     style = MaterialTheme.typography.bodyMedium, 
                     color = Color.Gray,
                     modifier = Modifier.padding(bottom = Spacing.lg)
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFFFF7ED), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = Color(0xFFFE8357),
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(Modifier.height(Spacing.lg))
                
                OutlinedTextField(
                    value = listName,
                    onValueChange = onNameChange,
                    placeholder = { Text("e.g., Weekly Groceries") },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFE8357),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    singleLine = true
                )
             }
        }
        
        Spacer(Modifier.weight(1f))
        
        // Button handled in parent
        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
fun CompletedContent(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFECFDF5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color(0xFF10B981), // Success Green
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(Modifier.height(Spacing.xl))
        
        Text(
            text = "All set!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        
        Spacer(Modifier.height(Spacing.md))
        
        Text(
            text = "Your preferences have been saved.\nLet's start shopping!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFF6B7280)
        )
        
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
                text = "Start Browsing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

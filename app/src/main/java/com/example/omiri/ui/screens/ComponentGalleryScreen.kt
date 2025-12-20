package com.example.omiri.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.components.*
import com.example.omiri.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentGalleryScreen() {
    val scrollState = rememberScrollState()
    var textFieldValue by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("Option 1") }
    var switchChecked by remember { mutableStateOf(true) }
    var checkboxChecked by remember { mutableStateOf(true) }
    var radioSelected by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Omiri UI Kit Gallery") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            GallerySection("Navigation") {
                OmiriBreadcrumb(
                    items = listOf(
                        OmiriBreadcrumbItem("Home", {}),
                        OmiriBreadcrumbItem("Categories", {}),
                        OmiriBreadcrumbItem("Groceries", {}, isLast = true)
                    )
                )
            }

            GallerySection("Buttons") {
                OmiriButton("Primary Button", {})
                Spacer(Modifier.height(8.dp))
                OmiriButton("Secondary Button", {}, style = OmiriButtonStyle.Secondary)
                Spacer(Modifier.height(8.dp))
                OmiriButton("Soft Button", {}, style = OmiriButtonStyle.Soft)
                Spacer(Modifier.height(8.dp))
                OmiriButton("Neutral Button", {}, style = OmiriButtonStyle.Neutral)
                Spacer(Modifier.height(8.dp))
                OmiriButton("Gradient Button", {}, style = OmiriButtonStyle.Gradient)
                Spacer(Modifier.height(8.dp))
                OmiriButton("Danger Button", {}, style = OmiriButtonStyle.Danger)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OmiriButton("Small", {}, size = OmiriButtonSize.Small, fullWidth = false)
                    OmiriButton("Medium", {}, size = OmiriButtonSize.Medium, fullWidth = false)
                    OmiriIconButton(Icons.Default.Add, {}, style = OmiriButtonStyle.Primary)
                }
            }

            GallerySection("Badges & Chips") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OmiriStatusBadge("Verified", color = AppColors.Success)
                    OmiriStatusBadge("Pending", color = AppColors.Info, softBackground = true)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OmiriDiscountBadge("50% OFF")
                    OmiriDiscountBadge("Ends Soon", isHot = true)
                    OmiriTimeBadge("2 days left")
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OmiriFilterChip("Selected", true, {})
                    OmiriFilterChip("Unselected", false, {})
                    OmiriCategoryTag("Organic", Icons.Default.CheckCircle, iconColor = AppColors.Success)
                }
            }

            GallerySection("Inputs & Selection") {
                OmiriTextField(value = textFieldValue, onValueChange = { textFieldValue = it }, label = "Standard Input", placeholder = "Type something...")
                Spacer(Modifier.height(16.dp))
                OmiriPasswordInput(value = textFieldValue, onValueChange = { textFieldValue = it }, label = "Password Input")
                Spacer(Modifier.height(16.dp))
                OmiriTextArea(value = textFieldValue, onValueChange = { textFieldValue = it }, label = "Text Area", placeholder = "Multiple lines here...")
                Spacer(Modifier.height(16.dp))
                OmiriSelectDropdown(
                    options = listOf("Option 1", "Option 2", "Option 3"),
                    selectedOption = selectedOption,
                    onOptionSelected = { selectedOption = it },
                    label = "Dropdown"
                )
                Spacer(Modifier.height(16.dp))
                OmiriSegmentedControl(
                    options = listOf("Left", "Center", "Right"),
                    selectedOption = "Center",
                    onOptionSelected = {}
                )
                Spacer(Modifier.height(16.dp))
                OmiriSwitch(checked = switchChecked, onCheckedChange = { switchChecked = it })
                OmiriCheckbox(checked = checkboxChecked, onCheckedChange = { checkboxChecked = it }, label = "Checkbox Label")
                OmiriRadioButton(selected = radioSelected, onClick = { radioSelected = !radioSelected }, label = "Radio Label")
            }

            GallerySection("Generic Cards") {
                OmiriCard {
                    Text("Basic Omiri Card Content", modifier = Modifier.padding(16.dp))
                }
                Spacer(Modifier.height(16.dp))
                OmiriIconCard(
                    title = "Settings", 
                    subtitle = "Manage your preferences", 
                    icon = Icons.Default.Settings
                )
                Spacer(Modifier.height(16.dp))
                OmiriListItemCard(
                    title = "Personal Information",
                    subtitle = "Update your profile data",
                    icon = { Icon(Icons.Default.Person, null, tint = AppColors.BrandOrange) },
                    onClick = {}
                )
            }

            GallerySection("Product Cards") {
                OmiriVerticalProductCard(
                    title = "Organic Milk 2L",
                    storeName = "Costco",
                    price = "€3.99",
                    oldPrice = "€10.39",
                    savePercentage = "62%",
                    timeLeft = "2 days left"
                )
                Spacer(Modifier.height(16.dp))
                OmiriHorizontalProductCard(
                    title = "Paper Towels 12-Pack",
                    storeName = "Target",
                    price = "€15.99",
                    oldPrice = "€20.19",
                    discountBadge = "21%",
                    timeLeft = "Ends today",
                    isOnList = true
                )
            }

            GallerySection("Promotion & Summary") {
                NextWeekDealsCard(
                    dateRange = "Dec 15 - 21",
                    dealCount = 89,
                    potentialSavings = "€125",
                    onPreviewClick = {}
                )
                Spacer(Modifier.height(16.dp))
                HomeScreenListCard(
                    title = "Weekly Groceries",
                    icon = Icons.Default.ShoppingCart,
                    itemCount = 24,
                    matchedDealsCount = 1,
                    completionPercentage = 16,
                    onClick = {}
                )
            }

            GallerySection("List Items") {
                SimpleListItem(title = "Account Security", icon = Icons.Default.Security)
                AvatarListItem(title = "John Doe", subtitle = "john.doe@example.com")
                ToggleListItem(title = "Dark Mode", icon = Icons.Default.Brightness4, checked = true, onCheckedChange = {})
                Spacer(Modifier.height(8.dp))
                SavingsListItem(
                    storeInitial = "C",
                    storeColor = Color(0xFF3B82F6),
                    storeName = "Costco",
                    productName = "Organic Milk 2L",
                    savingsText = "€6.40",
                    onAddClick = {}
                )
            }

            GallerySection("Misc & Tools") {
                var qty by remember { mutableStateOf(1) }
                OmiriQuantitySelector(quantity = qty, onQuantityChange = { qty = it })
                Spacer(Modifier.height(24.dp))
                OmiriCountdownTimer("02", "14", "45", "12")
                Spacer(Modifier.height(24.dp))
                OmiriSocialShareRow(onShareClick = {})
                Spacer(Modifier.height(24.dp))
                OmiriStoreBadge("A", AppColors.BrandOrange)
            }

            GallerySection("Alert & Progress") {
                OmiriAlert(
                    type = OmiriAlertType.Success,
                    title = "Success Message",
                    message = "Your list has been updated successfully.",
                    onDismiss = {}
                )
                Spacer(Modifier.height(8.dp))
                OmiriAlert(
                    type = OmiriAlertType.Error,
                    title = "Error Message",
                    message = "Something went wrong. Please try again.",
                    onDismiss = {}
                )
                Spacer(Modifier.height(16.dp))
                OmiriProgressBar(progress = 0.65f, isGradient = true)
                Spacer(Modifier.height(16.dp))
                OmiriStepIndicator(steps = listOf("Cart", "Address", "Payment"), currentStepIndex = 1)
                Spacer(Modifier.height(24.dp))
                OmiriSpinner()
            }
        }
    }
}

@Composable
fun GallerySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.Neutral900,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
        Divider(modifier = Modifier.padding(top = 24.dp), color = AppColors.Neutral200)
    }
}

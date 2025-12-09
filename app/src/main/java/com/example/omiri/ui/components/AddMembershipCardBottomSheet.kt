package com.example.omiri.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.MembershipCardViewModel
import com.example.omiri.viewmodels.MyStoresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembershipCardBottomSheet(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MembershipCardViewModel = viewModel(),
    myStoresViewModel: MyStoresViewModel = viewModel()
) {
    var storeName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var memberName by remember { mutableStateOf("") }
    var tier by remember { mutableStateOf("") }
    var selectedStoreId by remember { mutableStateOf<String?>(null) }
    
    // Image State
    var cardImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cardImageUri by remember { mutableStateOf<Uri?>(null) }
   
    // Input Method State
    var selectedMethod by remember { mutableStateOf(InputMethod.MANUAL) }

    // Available stores
    val selectedStores by myStoresViewModel.selectedStores.collectAsState()
    val availableStores by myStoresViewModel.availableStores.collectAsState()
    val myStoreOptions = availableStores.filter { selectedStores.contains(it.id) }
    
    // Launcher for camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            cardImageBitmap = bitmap
            cardImageUri = null
            // In a real app, we would run OCR here to extract number
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = null,
        modifier = modifier.fillMaxHeight(0.95f) // Make it almost full screen
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.md),
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111827)
                    )
                }
                
                Text(
                    text = "Add Membership Card",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    modifier = Modifier.align(Alignment.Center)
                )
                
                TextButton(
                    onClick = {
                        if (storeName.isNotBlank()) {
                            viewModel.addCard(storeName, cardNumber, tier, selectedStoreId, cardImageBitmap, cardImageUri)
                            onSave()
                        }
                    },
                    enabled = storeName.isNotBlank(),
                    modifier = Modifier.align(Alignment.CenterEnd),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFE8357),
                        disabledContentColor = Color(0xFF9CA3AF)
                    )
                ) {
                    Text(
                        "Save",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg)
            ) {
                // 1. Orange Card Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFE8357), // Orange-600
                                    Color(0xFFF97316)  // Orange-500
                                )
                            )
                        )
                ) {
                    // Background Circles for decoration
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .offset(x = 180.dp, y = (-50).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .offset(x = (-40).dp, y = 140.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Store Icon Placeholder
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Store,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // TIER DISPLAY
                            Text(
                                text = if (tier.isBlank()) "MEMBER CARD" else tier.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (storeName.isBlank()) "Store Name" else storeName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (memberName.isBlank()) "Member Name" else memberName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Card Number",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (cardNumber.isBlank()) "**** **** **** 1234" else cardNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            // QR Icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(Spacing.xl))
                
                // 2. Add Card Information (Methods)
                Text(
                    text = "Add Card Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                
                Spacer(Modifier.height(Spacing.md))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    MethodCard(
                        icon = Icons.Outlined.CameraAlt,
                        label = "Scan Card",
                        isSelected = selectedMethod == InputMethod.SCAN,
                        onClick = { 
                            selectedMethod = InputMethod.SCAN
                            // Launch camera for 'Scan Card' simulation
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    MethodCard(
                        icon = Icons.Outlined.QrCodeScanner,
                        label = "QR Code",
                        isSelected = selectedMethod == InputMethod.QR,
                        onClick = { selectedMethod = InputMethod.QR }, // Placeholder for QR
                        modifier = Modifier.weight(1f)
                    )
                    
                    MethodCard(
                        icon = Icons.Outlined.Keyboard,
                        label = "Manual",
                        isSelected = selectedMethod == InputMethod.MANUAL,
                        onClick = { selectedMethod = InputMethod.MANUAL },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(Modifier.height(Spacing.xl))

                // 3. Inputs
                
                // Store Name
                InputField(
                    label = "Store Name",
                    value = storeName,
                    onValueChange = { storeName = it },
                    placeholder = "Enter store name"
                )
                
                Spacer(Modifier.height(Spacing.md))
                
                // Card Number - UPDATED to allow symbols (KeyboardType.Text)
                InputField(
                    label = "Card Number",
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    placeholder = "Enter card number",
                    keyboardType = KeyboardType.Text
                )
                
                Spacer(Modifier.height(Spacing.md))
                
                // Member Name
                InputField(
                    label = "Member Name",
                    value = memberName,
                    onValueChange = { memberName = it },
                    placeholder = "Enter your name"
                )

                Spacer(Modifier.height(Spacing.md))

                // Membership Tier / Notes - NEW
                InputField(
                    label = "Membership Tier / Notes",
                    value = tier,
                    onValueChange = { tier = it },
                    placeholder = "e.g. Gold Member, Premium"
                )
                
                Spacer(Modifier.height(Spacing.xl))
                
                // Optional: Store Link
                 if (myStoreOptions.isNotEmpty()) {
                    Text(
                        "Link to My Store",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )
                    
                    // Simple dropdown or horizontal list for store mapping
                    // For brevity, using a simple chip-like selection or just the first match logic?
                    // Let's keep it simple: Just a text showing which store it might link to or a small list
                     myStoreOptions.forEach { store ->
                        val isSelected = selectedStoreId == store.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStoreId = if (isSelected) null else store.id }
                                .padding(vertical = 8.dp)
                        ) {
                             Checkbox(
                                checked = isSelected,
                                onCheckedChange = { isChecked ->
                                    selectedStoreId = if (isChecked) store.id else null
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFE8357))
                            )
                            Text(
                                text = store.retailer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF374151)
                            )
                        }
                    }
                 }

                Spacer(Modifier.height(Spacing.xxxl))
            }
        }
    }
}

enum class InputMethod {
    SCAN, QR, MANUAL
}

@Composable
private fun MethodCard(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFFFFCCAA) else Color(0xFFE5E7EB) // Light orange vs Light Gray
    val containerColor = if (isSelected) Color(0xFFFFF7ED) else Color.White // Orange-50 vs White
    val iconColor = if (isSelected) Color(0xFFFE8357) else Color(0xFF6B7280) // Orange-600 vs Gray-500
    val textColor = if (isSelected) Color(0xFFFE8357) else Color(0xFF4B5563)

    Card(
        modifier = modifier
            .aspectRatio(1f) // Square
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF374151)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFE8357), // Orange focus
                unfocusedBorderColor = Color.Transparent, // No border when unfocused (style from mock)
                focusedContainerColor = Color(0xFFF9FAFB),
                unfocusedContainerColor = Color(0xFFF9FAFB)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            )
        )
    }
}

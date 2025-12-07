package com.example.omiri.ui.components

import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.MembershipCardViewModel
import com.example.omiri.viewmodels.MyStoresViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembershipCardBottomSheet(
    onDismiss: () -> Unit,
    onSave: () -> Unit, // Callback after successful save
    modifier: Modifier = Modifier,
    viewModel: MembershipCardViewModel = viewModel(),
    myStoresViewModel: MyStoresViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var selectedStoreId by remember { mutableStateOf<String?>(null) }
    
    // Image State
    var cardImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cardImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Available stores selection
    val selectedStores by myStoresViewModel.selectedStores.collectAsState()
    val availableStores by myStoresViewModel.availableStores.collectAsState()
    
    // Launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            cardImageBitmap = bitmap
            cardImageUri = null
            showImageSourceDialog = false
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            cardImageUri = uri
            cardImageBitmap = null
            showImageSourceDialog = false
        }
    }
    
    // Only show stores the user has selected
    val myStoreOptions = availableStores.filter { selectedStores.contains(it.id) }

    // Image Source Dialog - Helper
    if (showImageSourceDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showImageSourceDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF3E8FF), // Light purple tone from screenshot
                modifier = Modifier.fillMaxWidth().padding(Spacing.md)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    Text(
                        text = "Add Card Image",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(Modifier.height(Spacing.xs))

                    // Take Photo
                    TextButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEA580B))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "Take Photo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Choose from Gallery
                    TextButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEA580B))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Photo,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "Choose from Gallery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(Modifier.height(Spacing.md))
                    
                    // Cancel
                    TextButton(
                        onClick = { showImageSourceDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = null,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
                .navigationBarsPadding()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    Text(
                        text = "Add Membership Card",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    // Card Image Section (Updated Style)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        onClick = { showImageSourceDialog = true }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (cardImageBitmap != null) {
                                Image(
                                    bitmap = cardImageBitmap!!.asImageBitmap(),
                                    contentDescription = "Card Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (cardImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(cardImageUri),
                                    contentDescription = "Card Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEA580B).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            tint = Color(0xFFEA580B)
                                        )
                                    }
                                    Spacer(Modifier.height(Spacing.sm))
                                    Text(
                                        "Add Card Image",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF374151)
                                    )
                                }
                            }
                        }
                    }

                    // Inputs
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Card Name") },
                        placeholder = { Text("e.g. IKEA Family") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEA580B),
                            focusedLabelColor = Color(0xFFEA580B),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                         shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text("Card Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEA580B),
                            focusedLabelColor = Color(0xFFEA580B),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Store Selection
                     if (myStoreOptions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Text(
                                "Link to Store (Optional)",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF111827)
                            )
                            
                            myStoreOptions.forEach { store ->
                                val isSelected = selectedStoreId == store.id
                                Card(
                                    onClick = { selectedStoreId = if (isSelected) null else store.id },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(Spacing.md),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Store Icon
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFEF4444)), // Red brand color
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Store,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        
                                        Spacer(Modifier.width(Spacing.md))
                                        
                                        Text(
                                            text = store.retailer,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF111827)
                                        )
                                        
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { isChecked ->
                                                selectedStoreId = if (isChecked) store.id else null
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFFEA580B),
                                                uncheckedColor = Color(0xFFD1D5DB)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(Spacing.sm))

                    // Buttons
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addCard(name, number, selectedStoreId, cardImageBitmap, cardImageUri)
                                onSave()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEA580B),
                            disabledContainerColor = Color(0xFFE5E7EB), // Grey when disabled
                            contentColor = Color.White,
                            disabledContentColor = Color(0xFF9CA3AF) // Grey text
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text(
                            text = "Save Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

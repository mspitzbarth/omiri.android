package com.example.omiri.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.ChatMessage
import com.example.omiri.viewmodels.ChatViewModel

import com.example.omiri.ui.components.simpleVerticalScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigateToShoppingList: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    var messageText by rememberSaveable { mutableStateOf("") }

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = { /* OmiriHeader in content */ },
        bottomBar = {
            ChatInputBar(
                value = messageText,
                onValueChange = { messageText = it },
                isLoading = isLoading,
                error = error,
                onDismissError = { viewModel.clearError() },
                onFocus = {
                    scope.launch {
                        kotlinx.coroutines.delay(300)
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size)
                        }
                    }
                },
                onSend = {
                    val trimmed = messageText.trim()
                    if (trimmed.isNotEmpty() && !isLoading) {
                        viewModel.sendMessage(trimmed)
                        messageText = ""
                    }
                },
                isOnline = isOnline
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(com.example.omiri.ui.theme.AppColors.Bg)
        ) {
            com.example.omiri.ui.components.OmiriHeader(
                startContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         // Avatar/Icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(com.example.omiri.ui.theme.AppColors.BrandOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.omiri.R.drawable.ic_omiri_logo),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Spacer(Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "H.A.N.S",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (isOnline) Color(0xFF10B981) else Color(0xFF9CA3AF),
                                            CircleShape
                                        )
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "Online" else "Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOnline) Color(0xFF10B981) else Color(0xFF6B7280),
                                    fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp)
                                )
                            }
                        }
                    }
                },
                notificationCount = 2, // Ideally from ViewModel check, but hardcoded in prev example too
                onNotificationClick = onNotificationsClick,
                onProfileClick = onProfileClick,
                customAction = {
                    Surface(
                         shape = CircleShape,
                         color = Color.White,
                         shadowElevation = 2.dp,
                         modifier = Modifier
                             .size(40.dp)
                             .clip(CircleShape)
                             .clickable { viewModel.resetConversation() }
                     ) {
                         Box(contentAlignment = Alignment.Center) {
                             Icon(
                                 imageVector = Icons.Outlined.Delete,
                                 contentDescription = "Clear Chat",
                                 tint = Color(0xFF1F2937),
                                 modifier = Modifier.size(22.dp)
                             )
                         }
                     }
                }
            )

            val networkErrorType by viewModel.networkErrorType.collectAsState()
            
            LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .simpleVerticalScrollbar(listState),
                    contentPadding = PaddingValues(bottom = Spacing.md), 
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Explicit larger gap
                ) {
                    // Spacer for top padding
                    item { Spacer(Modifier.height(Spacing.md)) }

                    // Date Header
                    if (messages.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE5E7EB),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Today",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF4B5563),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (messages.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.md),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF7ED)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
                            ) {
                                Column(
                                    modifier = Modifier.padding(Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    Text(
                                        text = "💬 AI Shopping Assistant",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFE8357)
                                    )
                                    Text(
                                        text = "Ask me about deals, recipes, or help with your shopping list. I can help you find the best prices and plan your meals!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }

                    // Safe items block
                    items(messages) { message ->
                        ChatBubble(message, onNavigateToShoppingList)
                    }

                    if (isLoading && messages.lastOrNull()?.isUser == true) {
                        item { TypingIndicator() }
                    }
                }

        }
    }
}

// ... ChatInputBar ...

// ... ChatBubble ...

@Composable
fun ChatBubble(
    message: ChatMessage,
    onNavigateToShoppingList: () -> Unit = {}
) {
    val bubbleColor = if (message.isUser) Color(0xFFFE8357) else Color.White
    val textColor = if (message.isUser) Color.White else Color(0xFF1F2937)
    val borderStroke = if (message.isUser) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
    }
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.9f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md), // Add outer padding to Row
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = maxBubbleWidth)
        ) {
            if (message.text.isNotBlank()) {
                Surface(
                    color = bubbleColor,
                    shape = shape,
                    border = borderStroke,
                    shadowElevation = 0.dp
                ) {
                    Column {
                        if (message.isUser) {
                            Text(
                                text = message.text,
                                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 4.dp),
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // Use Markdown for bot
                            com.example.omiri.ui.components.MarkdownText(
                                markdown = message.text,
                                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 4.dp),
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        // Timestamp
                        val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                        val timeString = timeFormat.format(java.util.Date(message.timestamp))
                        
                        Text(
                            text = timeString,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (message.isUser) Color.White.copy(alpha = 0.7f) else Color(0xFF9CA3AF),
                            fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    }
                }
            }
            
            // Attachment Cards
            if (message.attachmentType != null) {
                Spacer(Modifier.height(8.dp))
                when (message.attachmentType) {
                    com.example.omiri.viewmodels.AttachmentType.SHOPPING_LIST_SUMMARY -> {
                        val data = message.attachmentData as? Map<*, *>
                        val count = (data?.get("count") as? Int) ?: 0
                        val deals = (data?.get("deals") as? Int) ?: 0
                        
                        com.example.omiri.ui.components.ShoppingListChatCard(
                            count = count,
                            dealCount = deals,
                            onClick = onNavigateToShoppingList
                        )
                    }
                    com.example.omiri.viewmodels.AttachmentType.SHOPPING_LIST_UPDATE -> {
                        com.example.omiri.ui.components.ShoppingListUpdateCard(
                            data = message.attachmentData as? Map<String, Any> ?: emptyMap(),
                            onViewList = onNavigateToShoppingList
                        )
                    }
                    com.example.omiri.viewmodels.AttachmentType.DEALS_MATCHED -> {
                        com.example.omiri.ui.components.DealsMatchedCard(
                            data = message.attachmentData as? Map<String, Any> ?: emptyMap()
                        )
                    }
                    com.example.omiri.viewmodels.AttachmentType.FOUND_DEALS -> {
                        com.example.omiri.ui.components.FoundDealsCard(
                            data = message.attachmentData as? Map<String, Any> ?: emptyMap()
                        )
                    }
                    com.example.omiri.viewmodels.AttachmentType.STORE_ROUTE -> {
                        com.example.omiri.ui.components.RecommendedStoreRunCard(
                            data = message.attachmentData as? Map<String, Any> ?: emptyMap()
                        )
                    }
                    com.example.omiri.viewmodels.AttachmentType.RECIPE_IDEAS -> {
                        com.example.omiri.ui.components.RecipeIdeasCard(
                            data = message.attachmentData as? Map<String, Any> ?: emptyMap()
                        )
                    }
                    com.example.omiri.viewmodels.AttachmentType.SUGGESTIONS -> {
                        com.example.omiri.ui.components.SuggestionsRow(
                            data = message.attachmentData as? Map<String, Any> ?: emptyMap(),
                            onSuggestionClick = { suggestion: String -> 
                                if (suggestion.startsWith("Show")) onNavigateToShoppingList()
                                // Handle others or just send as text
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

// Typing Indicator and Dot remain here
@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "TypeIndicator")

    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        @Composable
        fun bounceAnimation(delay: Int): State<Float> {
            return transition.animateFloat(
                initialValue = 0f,
                targetValue = -10f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 600 // Faster duration
                        0f at 0
                        0f at delay
                        -8f at delay + 150 with FastOutSlowInEasing // Peak
                        0f at delay + 300 // Return
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "Bounce"
            )
        }

        val offset1 by bounceAnimation(0)
        val offset2 by bounceAnimation(100)
        val offset3 by bounceAnimation(200)

        Dot(offset1)
        Dot(offset2)
        Dot(offset3)
    }
}

@Composable
fun Dot(offsetY: Float) {
    Box(
        modifier = Modifier
            .graphicsLayer { translationY = offsetY }
            .size(8.dp)
            .background(Color(0xFFFE8357), androidx.compose.foundation.shape.CircleShape) // Orange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean,
    isOnline: Boolean = true,
    error: String?,
    onDismissError: () -> Unit,
    onFocus: () -> Unit = {},
    onSend: () -> Unit
) {
    // Voice Search Launcher
    val voiceLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText: String? = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (spokenText != null) {
                onValueChange(spokenText)
                // Optional: Auto-send if desired? User asked for "better input" so let's populate it.
            }
        }
    }

    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .padding(vertical = 12.dp, horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {

            // Error Card
            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Error,
                            contentDescription = null,
                            tint = Color(0xFFDC2626)
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF7F1D1D),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill Shaped Input Field
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(26.dp), // Fully rounded pill
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { if (it.isFocused) onFocus() },
                            singleLine = true,
                            enabled = !isLoading && isOnline,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = if (isOnline) com.example.omiri.ui.theme.AppColors.BrandInk else com.example.omiri.ui.theme.AppColors.SubtleText
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(com.example.omiri.ui.theme.AppColors.BrandOrange),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSend() }),
                            decorationBox = { innerTextField ->
                                if (value.isEmpty()) {
                                    Text(
                                        text = if (isOnline) "Ask about deals…" else "Offline",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        
                        // Vertical divider/spacer if needed, or just icons
                        if (value.isEmpty()) {
                             Spacer(Modifier.width(8.dp))
                             IconButton(
                                onClick = {
                                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak to chat...")
                                    }
                                    try {
                                        voiceLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                                modifier = Modifier.size(24.dp),
                                enabled = isOnline
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Outlined.Mic,
                                    contentDescription = "Voice Input",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(Spacing.sm))

                // Send Button
                FilledIconButton(
                    onClick = onSend,
                    enabled = value.isNotBlank() && !isLoading && isOnline,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                        disabledContainerColor = com.example.omiri.ui.theme.AppColors.PastelGrey,
                        contentColor = Color.White,
                        disabledContentColor = com.example.omiri.ui.theme.AppColors.MutedText
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    // Removed Spinner per request
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}





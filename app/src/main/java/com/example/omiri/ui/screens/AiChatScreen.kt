package com.example.omiri.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.ChatMessage
import com.example.omiri.viewmodels.ChatViewModel

import com.example.omiri.ui.components.simpleVerticalScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    onNotificationsClick: () -> Unit = {},
    onNavigateToShoppingList: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    var messageText by rememberSaveable { mutableStateOf("") }

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    val listState = rememberLazyListState()

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
                onSend = {
                    val trimmed = messageText.trim()
                    if (trimmed.isNotEmpty() && !isLoading) {
                        viewModel.sendMessage(trimmed)
                        messageText = ""
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(Color.White)
        ) {
            com.example.omiri.ui.components.OmiriHeader(
                startContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         // Avatar/Icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEA580B), CircleShape),
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
                                text = "AI Assistant",
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
                customAction = {
                    IconButton(onClick = { viewModel.resetConversation() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Outlined.DeleteOutline,
                            contentDescription = "Clear Chat",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }
            )

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
                                    color = Color(0xFFEA580B)
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
    val bubbleColor = if (message.isUser) Color(0xFFEA580B) else Color(0xFFF3F4F6) // Greyish for bot
    val textColor = if (message.isUser) Color.White else Color(0xFF1F2937)
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
    }
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.8f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = if (message.isUser) 0.dp else 1.dp,
            modifier = Modifier
                .widthIn(max = maxBubbleWidth) // Constrain width to 80%
                .padding(horizontal = Spacing.md) // Ensure it doesn't touch edge
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
        
        // Attachment Cards
        if (message.attachmentType == com.example.omiri.viewmodels.AttachmentType.SHOPPING_LIST_SUMMARY) {
            Spacer(Modifier.height(4.dp))
            val data = message.attachmentData as? Map<*, *>
            val count = (data?.get("count") as? Int) ?: 0
            val deals = (data?.get("deals") as? Int) ?: 0
            
            com.example.omiri.ui.components.ShoppingListChatCard(
                count = count,
                dealCount = deals,
                onClick = onNavigateToShoppingList
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val dotSize = 8.dp
    val delayUnit = 300 
    
    val transition = rememberInfiniteTransition(label = "TypeIndicator")

    @Composable
    fun animateDot(offset: Int): androidx.compose.runtime.State<Float> {
        return transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = delayUnit * 4
                    0f at 0
                    1f at (offset * delayUnit) using LinearEasing // Jump up
                    0f at (offset * delayUnit + delayUnit) // Jump down
                }
            ),
            label = "Dot$offset"
        )
    }

    val dot1Alpha by animateDot(0)
    val dot2Alpha by animateDot(1)
    val dot3Alpha by animateDot(2)

    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // We use simple alpha or offset animation. 
        // Let's use Offset Y for "Bounce"
        
        @Composable
        fun bounceAnimation(delay: Int): State<Float> {
            return transition.animateFloat(
                initialValue = 0f,
                targetValue = -10f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0f at 0
                        0f at delay
                        -6f at delay + 300 with FastOutSlowInEasing
                        0f at delay + 600
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "Bounce"
            )
        }

        val offset1 by bounceAnimation(0)
        val offset2 by bounceAnimation(200)
        val offset3 by bounceAnimation(400)

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
            .background(Color(0xFF9CA3AF), androidx.compose.foundation.shape.CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onDismissError: () -> Unit,
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
        shadowElevation = 6.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = Spacing.md, end = Spacing.md, top = 8.dp, bottom = 0.dp), // Zero bottom padding to keep it low
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
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp), // Fully rounded pill
                    color = Color(0xFFF3F4F6), // Light grey like SearchBar
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !isLoading,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF111827)
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEA580B)),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSend() }),
                            decorationBox = { innerTextField ->
                                if (value.isEmpty()) {
                                    Text(
                                        text = "Ask about deals…",
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
                                modifier = Modifier.size(24.dp)
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
                    enabled = value.isNotBlank() && !isLoading,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFEA580B),
                        disabledContainerColor = Color(0xFFE5E7EB),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}





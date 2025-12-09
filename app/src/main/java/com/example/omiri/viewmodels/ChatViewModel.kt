package com.example.omiri.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.omiri.data.api.models.ConversationResponse
import com.example.omiri.data.api.models.MessageResponse
import com.example.omiri.data.repository.MistralRepository
import com.example.omiri.data.repository.ProductRepository
import com.example.omiri.data.local.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI representation of a chat message
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentType: AttachmentType? = null,
    val attachmentData: Any? = null
)

enum class AttachmentType {
    SHOPPING_LIST_SUMMARY,
    PRODUCT_CARD,
    SHOPPING_LIST_UPDATE,
    DEALS_MATCHED,
    STORE_ROUTE,
    RECIPE_IDEAS
}

/**
 * ViewModel for managing Mistral AI chat conversations
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MistralRepository()
    private val productRepository = ProductRepository()
    private val userPreferences = UserPreferences(application)

    // UI State
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _networkErrorType = MutableStateFlow<com.example.omiri.utils.NetworkErrorType?>(null)
    val networkErrorType: StateFlow<com.example.omiri.utils.NetworkErrorType?> = _networkErrorType.asStateFlow()

    private val _conversationId = MutableStateFlow<String?>(null)
    val conversationId: StateFlow<String?> = _conversationId.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        checkOnlineStatus()
        viewModelScope.launch {
            userPreferences.showDummyChatData.collect { show ->
                if (show) {
                    loadDummyData()
                } else {
                    // If disabling dummy data, clear chat to avoid confusion or mixed state
                    // _messages.value = emptyList() 
                    // Commented out to prevent wiping real chat if user just toggles off. 
                    // But usually debug toggles are disruptive.
                }
            }
        }
    }
    
    private fun loadDummyData() {
        val now = System.currentTimeMillis()
        val mockMessages = mutableListOf<ChatMessage>()
        
        // 1. Intro
        mockMessages.add(ChatMessage(
            text = "Hi! I'm your shopping assistant. I can help you find deals, organize lists, suggest recipes, and plan your shopping trips. What would you like to do today?",
            isUser = false,
            timestamp = now - 60000
        ))
        
        // 2. User Request
        mockMessages.add(ChatMessage(
            text = "I need to create a shopping list for dinner tomorrow. Can you help me find deals on ingredients for pasta?",
            isUser = true,
            timestamp = now - 50000
        ))
        
        // 3. Bot Response text
        mockMessages.add(ChatMessage(
            text = "Great! I've found some pasta recipes and matched them with current deals. Let me create a list for you.",
            isUser = false,
            timestamp = now - 45000
        ))
        
        // 4. Shopping List Update Card (Bot)
        mockMessages.add(ChatMessage(
            text = "", // Empty text for card-only message
            isUser = false,
            timestamp = now - 44000,
            attachmentType = AttachmentType.SHOPPING_LIST_UPDATE,
            attachmentData = mapOf(
                "addedCount" to 4,
                "status" to "Synced",
                "items" to listOf("Spaghetti pasta", "Ground beef", "Tomato sauce", "+1 more")
            )
        ))
        
        // 5. Deals Matched Card (Bot)
        mockMessages.add(ChatMessage(
            text = "",
            isUser = false,
            timestamp = now - 43000,
            attachmentType = AttachmentType.DEALS_MATCHED,
            attachmentData = mapOf(
                "count" to 3,
                "badge" to "Best Saves",
                "items" to listOf(
                    mapOf("name" to "Barilla Spaghetti", "price" to "€1.99", "oldPrice" to "€2.49", "discount" to "-20%", "icon" to "wheat"),
                    mapOf("name" to "Ground Beef 1lb", "price" to "€4.99", "oldPrice" to "€6.99", "discount" to "-29%", "icon" to "meat"),
                    mapOf("name" to "Hunts Tomato Sauce", "price" to "€1.49", "oldPrice" to "€1.89", "discount" to "-21%", "icon" to "bottle")
                )
            )
        ))
        
        // 6. Bot Follow-up
        mockMessages.add(ChatMessage(
            text = "Perfect! I found great deals that save you €2.89. Would you like me to plan the best route to get these items?",
            isUser = false,
            timestamp = now - 30000
        ))
        
        // 7. User Reply
        mockMessages.add(ChatMessage(
            text = "Yes, plan my route please!",
            isUser = true,
            timestamp = now - 20000
        ))
        
        // 8. Store Route Card
        mockMessages.add(ChatMessage(
            text = "",
            isUser = false,
            timestamp = now - 15000,
            attachmentType = AttachmentType.STORE_ROUTE,
            attachmentData = mapOf(
                "stops" to 2,
                "savings" to "€2.89",
                "badge" to "Fewest stores",
                "steps" to listOf(
                    mapOf("store" to "Target", "desc" to "Pasta, Ground beef • 1.2 mi", "price" to "€1.99", "color" to "red"),
                    mapOf("store" to "Walmart", "desc" to "Tomato sauce, Cheese • 2.1 mi", "price" to "€0.90", "color" to "blue")
                )
            )
        ))
        
        // 9. Recipe Ideas
        mockMessages.add(ChatMessage(
            text = "",
            isUser = false,
            timestamp = now - 10000,
            attachmentType = AttachmentType.RECIPE_IDEAS,
            attachmentData = mapOf(
                "badge" to "Uses your list",
                "recipes" to listOf(
                    mapOf("name" to "Classic Spaghetti", "difficulty" to "Easy", "time" to "25 min", "color" to "orange"),
                    mapOf("name" to "Meat Pasta Bake", "difficulty" to "Medium", "time" to "45 min", "color" to "yellow")
                )
            )
        ))
        
        _messages.value = mockMessages
    }

    fun checkOnlineStatus() {
        viewModelScope.launch {
            val result = repository.checkHealth()
            _isOnline.value = result.getOrDefault(false)
            
            result.onFailure {
                 _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(it)
            }.onSuccess {
                 _networkErrorType.value = null // Clear error if success
            }
        }
    }

    /**
     * Send a message to the AI
     */
    fun sendMessage(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isBlank()) return

        Log.d(TAG, "Sending message: $trimmed")

        // Add user message immediately
        _messages.value = _messages.value + ChatMessage(text = trimmed, isUser = true)

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val currentConversationId = _conversationId.value

                if (currentConversationId.isNullOrBlank()) {
                    Log.d(TAG, "Starting new conversation...")
                    startNewConversation(trimmed)
                } else {
                    Log.d(TAG, "Continuing conversation: $currentConversationId")
                    continueConversation(currentConversationId, trimmed)
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to send message: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
                _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(e)

                _messages.value = _messages.value + ChatMessage(
                    text = "Sorry, I'm having trouble connecting right now. Please try again.",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Start a new conversation with the AI
     */
    private suspend fun startNewConversation(initialMessage: String) {
        val context = getShoppingListContext()
        Log.d(TAG, "Starting conversation with context: $context")
        
        val result = repository.startConversation(
            initialMessage = initialMessage, 
            systemContext = context
        )

        result.onSuccess { response ->
            val convId = response.conversationId

            if (convId.isNullOrBlank()) {
                Log.e(TAG, "Missing conversation_id from startConversation response")
                throw IllegalStateException("Missing conversation_id")
            }

            _conversationId.value = convId
            Log.d(TAG, "Conversation started: $convId")

            val aiText = extractAssistantTextFromStart(response)

            _messages.value = _messages.value + ChatMessage(
                text = aiText ?: "I'm here to help! What would you like to know?",
                isUser = false
            )

            Log.d(TAG, "AI start response: ${aiText ?: "<fallback>"}")
        }.onFailure { error ->
            Log.e(TAG, "Failed to start conversation", error)
            throw error
        }
    }

    /**
     * Continue an existing conversation
     */
    private suspend fun continueConversation(conversationId: String, message: String) {
        // Always include latest context to ensure AI knows about changes
        val context = getShoppingListContext()
        val messageWithContext = "[System Context: $context] User Question: $message"
        
        Log.d(TAG, "Sending message (with hidden context): $messageWithContext")
        
        val result = repository.sendMessage(conversationId, messageWithContext)

        result.onSuccess { response ->
            val aiText = extractAssistantTextFromMessage(response)

            _messages.value = _messages.value + ChatMessage(
                text = aiText ?: "I received your message. How can I help you?",
                isUser = false
            )

            Log.d(TAG, "AI message response: ${aiText ?: "<fallback>"}")
        }.onFailure { error ->
            Log.e(TAG, "Failed to send message", error)
            throw error
        }
    }
    
    private fun getShoppingListContext(): String {
        val list = com.example.omiri.data.repository.ShoppingListRepository.shoppingLists.value.find { 
            it.id == com.example.omiri.data.repository.ShoppingListRepository.currentListId.value 
        }
        
        return if (list != null && list.items.isNotEmpty()) {
            val items = list.items.joinToString(", ") { 
                "${it.name} (${if(it.isDone) "Done" else "To Buy"}${if(it.isInDeals) ", DEAL AVAILABLE" else ""})" 
            }
            "Current User Shopping List '${list.name}': [$items]. When answering, check this list."
        } else {
            "User's shopping list is currently empty."
        }
    }

    /**
     * Extract assistant text from ConversationResponse
     */
    private fun extractAssistantTextFromStart(response: ConversationResponse): String? {
        return response.response?.content
    }

    /**
     * Extract assistant text from MessageResponse
     */
    private fun extractAssistantTextFromMessage(response: MessageResponse): String? {
        // Check for tool calls first!
        // Top-level check for client tools
        if (response.isClientToolCall) {
            handleToolCalls(response)
            return null // Return null so we don't display a text message if it was just a tool call
        }

        val msg = response.messages?.lastOrNull()
        if (msg?.toolCalls != null) {
            handleToolCallsFromContent(msg)
        }
        
        // Try top-level content first (MessageResponse has it)
        if (!response.content.isNullOrBlank()) {
            return response.content
        }
        
        // Fallback to nested response object
        return response.response?.content
    }

    /**
     * Parse and execute tool calls if present
     */
    private fun handleToolCalls(response: MessageResponse) {
        // Use the explicit tool_calls from top-level response if isClientToolCall is true
        val toolCalls = if (response.isClientToolCall && !response.toolCalls.isNullOrEmpty()) {
            response.toolCalls
        } else {
            response.messages?.lastOrNull()?.toolCalls ?: response.response?.toolCalls
        }
        
        if (!toolCalls.isNullOrEmpty()) {
            Log.d(TAG, "Found ${toolCalls.size} tool calls")
            toolCalls.forEach { toolCall ->
                when (toolCall.function.name) {
                    "app-shopping_list_add" -> executeShoppingListAdd(toolCall.function.arguments)
                    "app-shopping_list_search" -> executeShoppingListSearch(toolCall.function.arguments)
                    // "app-products_search" -> executeProductsSearch(toolCall.function.arguments)
                    else -> Log.w(TAG, "Unknown tool: ${toolCall.function.name}")
                }
            }
        }
    }
    
    // ... manual parsing ...

    private fun executeShoppingListSearch(argumentsJson: String) {
        viewModelScope.launch {
            try {
                // We'll search for the *current* shopping list items plus any query in arguments
                // Arguments might have "query" or be empty.
                // For now, let's grab current list items basically.
                
                val listId = com.example.omiri.data.repository.ShoppingListRepository.currentListId.value
                val list = com.example.omiri.data.repository.ShoppingListRepository.shoppingLists.value.find { it.id == listId }
                
                if (list == null || list.items.isEmpty()) {
                     // Nothing to search
                     submitToolOutput("Current shopping list is empty.")
                     return@launch
                }
                
                val items = list.items.filter { !it.isDone }.map { it.name }.joinToString(",")
                if (items.isBlank()) {
                    submitToolOutput("No active items in shopping list.")
                    return@launch
                }
                
                // Get filters
                val storesSet: Set<String> = userPreferences.selectedStores.firstOrNull() ?: emptySet()
                val stores: String? = if (storesSet.isNotEmpty()) storesSet.joinToString(",") else null
                
                // Search
                val result = productRepository.searchShoppingList(
                    items = items,
                    stores = stores,
                    country = "DE",
                    limit = 3
                )
                
                result.onSuccess { response ->
                    // Format results for AI
                    val sb = StringBuilder()
                    sb.append("Search Results for Shopping List items ($items):\n")
                    
                    val categories = response.categories ?: emptyMap()
                    val totalFound = response.itemsFound
                    val notFound = response.itemsNotFound
                    
                    sb.append("Summary: Found $totalFound items, ${notFound.size} not found.\n")
                    
                    if (notFound.isNotEmpty()) {
                        sb.append("Items not found: ${notFound.joinToString(", ")}\n")
                    }

                    if (categories.isEmpty()) {
                        sb.append("No deals found.")
                    } else {
                        categories.forEach { (categoryName, categoryResult) ->
                            if (categoryResult.products.isNotEmpty()) {
                                 // Group by item name for clarity
                                 val byItem = categoryResult.products.groupBy { it.searchTerm ?: "Other" }
                                 
                                 byItem.forEach { (itemName, products) ->
                                     sb.append("- Item '$itemName' (Category: $categoryName):\n")
                                     products.take(3).forEach { product ->
                                        val validFrom = product.availableFrom ?: "?"
                                        val validTo = product.availableUntil ?: "?"
                                        sb.append("  * ${product.retailer}: ${product.title} - ${product.priceAmount} ${product.priceCurrency} (Valid: $validFrom to $validTo)\n")
                                     }
                                 }
                            }
                        }
                    }
                    
                    submitToolOutput(sb.toString())
                    
                }.onFailure { e ->
                    submitToolOutput("Tool execution failed: ${e.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Search execution failed", e)
                submitToolOutput("Error executing search: ${e.message}")
            }
        }
    }

    private suspend fun submitToolOutput(output: String) {
        val convId = conversationId.value ?: return
        Log.d(TAG, "Submitting tool output: $output")
        
        // Send as system/tool message to Mistral
        // We assume sending a message will trigger the AI to continue generation
        val messageWithContext = "[System Tool Output]: $output"
        
        val result = repository.sendMessage(convId, messageWithContext)
        
        result.onSuccess { response ->
            val aiText = extractAssistantTextFromMessage(response)
            if (!aiText.isNullOrBlank()) {
                 _messages.value = _messages.value + ChatMessage(
                    text = aiText,
                    isUser = false
                )
            }
        }.onFailure { error ->
             Log.e(TAG, "Failed to submit tool output", error)
        }
    }

    // Helper for manual parsing since we normalized start/continue
    private fun handleToolCallsFromContent(message: com.example.omiri.data.api.models.MistralMessage?) {
        val toolCalls = message?.toolCalls
        if (!toolCalls.isNullOrEmpty()) {
             Log.d(TAG, "Found ${toolCalls.size} tool calls in message")
             toolCalls.forEach { toolCall ->
                if (toolCall.function.name == "app-shopping_list_add") {
                    executeShoppingListAdd(toolCall.function.arguments)
                }
            }
        }
    }

    private fun executeShoppingListAdd(argumentsJson: String) {
        try {
            val gson = com.google.gson.Gson()
            val args = gson.fromJson(argumentsJson, ShoppingListAddArgs::class.java)
            
            val count = args.items.size
            if (count > 0) {
                args.items.forEach { item ->
                    // Map category string to ID if possible
                    val catId = mapCategoryToId(item.category)
                    com.example.omiri.data.repository.ShoppingListRepository.addItem(
                        name = item.name,
                        categoryId = catId
                    )
                }
                
                // Calculate stats for the card
                val dealCount = args.items.count { 
                    // Simple heuristic: if we mapped it to a known category, check deals?
                    // Actually, the tool call doesn't say if it's a deal. 
                    // But in our mock, we can pretend or assume based on category for now.
                    // Or we can count how many added items ended up being "in deals"
                    // The repository.addItem default is isInDeals=false unless specified.
                    // Let's assume the AI might have known. For now, hardcode 0 or check Repo state.
                    false // Valid improvements for later
                }
                
                // Add system message with CARD attachment
                _messages.value = _messages.value + ChatMessage(
                    text = "Added $count items to your shopping list.",
                    isUser = false,
                    attachmentType = AttachmentType.SHOPPING_LIST_SUMMARY,
                    attachmentData = mapOf(
                        "count" to count,
                        "deals" to dealCount // We can fetch real deal count from Repo later
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse shopping list arguments", e)
        }
    }
    
    private fun mapCategoryToId(categoryName: String?): String {
        return when(categoryName?.lowercase()) {
            "dairy", "eggs", "cheese" -> com.example.omiri.data.models.PredefinedCategories.DAIRY_EGGS.id
            "bakery", "bread" -> com.example.omiri.data.models.PredefinedCategories.BREAD_BAKERY.id
            "produce", "fruit", "vegetables" -> com.example.omiri.data.models.PredefinedCategories.FRUITS_VEGETABLES.id
            "meat", "poultry", "fish" -> com.example.omiri.data.models.PredefinedCategories.MEAT_POULTRY.id
            "beverages", "drinks" -> com.example.omiri.data.models.PredefinedCategories.BEVERAGES.id
            "pantry", "spices", "sauces" -> com.example.omiri.data.models.PredefinedCategories.PANTRY_STAPLES.id
            else -> com.example.omiri.data.models.PredefinedCategories.OTHER.id
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetConversation() {
        _conversationId.value = null
        _messages.value = emptyList()
        _error.value = null
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}

// Data classes for matching the JSON arguments
data class ShoppingListAddArgs(
    val items: List<ShoppingItemArg>
)

data class ShoppingItemArg(
    val name: String,
    val category: String?
)

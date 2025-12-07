package com.example.omiri.data.api.models

import com.google.gson.annotations.SerializedName


/**
 * Request to send a message to an existing conversation
 */
data class MessageRequest(
    @SerializedName("message") val message: String
)

/**
 * Response containing conversation data (actual API structure)
 */
data class ConversationResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("conversation_id") val conversationId: String? = null,
    @SerializedName("agent_id") val agentId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("messages") val messages: List<Any>? = null,
    @SerializedName("response") val response: MistralResponseContent? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("is_client_tool_call") val isClientToolCall: Boolean = false,
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>? = null
)

/**
 * Response for individual message sending (actual API structure)
 */
data class MessageResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("conversation_id") val conversationId: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("response") val response: MistralResponseContent?,  // Nested response object
    @SerializedName("messages") val messages: List<MistralMessage>?,
    @SerializedName("metadata") val metadata: Map<String, Any>?,
    @SerializedName("is_client_tool_call") val isClientToolCall: Boolean = false,
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>? = null
)

/**
 * Mistral response content object
 */
data class MistralResponseContent(
    @SerializedName("content") val content: String? = null,
    @SerializedName("object") val objectType: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("completed_at") val completedAt: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("agent_id") val agentId: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>? = null
)

/**
 * Individual message in a conversation
 */
data class MistralMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: Any?, // Can be string or complex object
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class ToolCall(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String = "function",
    @SerializedName("function") val function: FunctionCall
)

data class FunctionCall(
    @SerializedName("name") val name: String,
    @SerializedName("arguments") val arguments: String // JSON string
)

/**
 * Message content (can be text or structured data)
 */
data class MistralMessageContent(
    @SerializedName("text") val text: String?,
    @SerializedName("type") val type: String?
)

/**
 * Error response from Mistral AI API
 */
data class MistralErrorResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("error") val error: String,
    @SerializedName("detail") val detail: String?
)

/**
 * Response from the health check endpoint
 */
data class HealthResponse(
    @SerializedName("status") val status: String
)

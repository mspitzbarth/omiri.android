package com.example.omiri.data.api.models

import com.google.gson.annotations.SerializedName

data class ConversationStartRequest(
    @SerializedName("inputs")
    val inputs: List<ChatInputMessage>,

    @SerializedName("agent_id")
    val agentId: String? = null,

    @SerializedName("model")
    val model: String? = null,

    @SerializedName("instructions")
    val instructions: String? = null,

    @SerializedName("tools")
    val tools: List<Map<String, Any>>? = null,

    @SerializedName("completion_args")
    val completionArgs: Map<String, Any>? = null
)

data class ChatInputMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

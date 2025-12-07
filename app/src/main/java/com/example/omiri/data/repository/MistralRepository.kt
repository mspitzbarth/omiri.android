package com.example.omiri.data.repository

import com.example.omiri.data.api.RetrofitClient
import com.example.omiri.data.api.models.ChatInputMessage
import com.example.omiri.data.api.models.ConversationResponse
import com.example.omiri.data.api.models.ConversationStartRequest
import com.example.omiri.data.api.models.MessageRequest
import com.example.omiri.data.api.models.MessageResponse
import com.example.omiri.data.api.services.MistralApiService

/**
 * Repository for Mistral AI chat operations
 */
class MistralRepository {

    private val apiService: MistralApiService = RetrofitClient.createService()

    /**
     * Start a new conversation with Mistral AI
     *
     * Backend expects:
     * {
     *   "inputs": [{"role":"user","content":"Hello!"}],
     *   ...
     * }
     */
    suspend fun startConversation(
        initialMessage: String,
        agentId: String? = null,
        systemContext: String? = null
    ): Result<ConversationResponse> {
        return try {
            val userMessage = ChatInputMessage(role = "user", content = initialMessage)
            
            val inputs = if (systemContext != null) {
                listOf(
                    ChatInputMessage(role = "system", content = systemContext),
                    userMessage
                )
            } else {
                listOf(userMessage)
            }

            val request = ConversationStartRequest(
                inputs = inputs,
                agentId = agentId
                // model/instructions/tools/completionArgs optional
            )

            val response = apiService.startConversation(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send a message to an existing conversation
     *
     * IMPORTANT:
     * conversationId must be "conv_..."
     */
    suspend fun sendMessage(
        conversationId: String,
        message: String
    ): Result<MessageResponse> {
        return try {
            val request = MessageRequest(message = message)
            val response = apiService.sendMessage(conversationId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConversation(
        conversationId: String
    ): Result<ConversationResponse> {
        return try {
            val response = apiService.getConversation(conversationId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkHealth(): Result<Boolean> {
        return try {
            apiService.healthCheck()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

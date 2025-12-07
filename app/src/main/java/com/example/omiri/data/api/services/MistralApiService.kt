package com.example.omiri.data.api.services

import com.example.omiri.data.api.models.ConversationResponse
import com.example.omiri.data.api.models.ConversationStartRequest
import com.example.omiri.data.api.models.MessageRequest
import com.example.omiri.data.api.models.MessageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API service for Mistral AI conversation endpoints
 */
interface MistralApiService {

    /**
     * Start a new conversation with Mistral AI agent
     * POST /api/v1/mistral/conversation/start
     */
    @POST("api/v1/mistral/conversation/start")
    suspend fun startConversation(
        @Body request: ConversationStartRequest
    ): ConversationResponse

    /**
     * Send a message to an existing conversation
     * POST /api/v1/mistral/conversation/{conversation_id}/message
     */
    @POST("api/v1/mistral/conversation/{conversation_id}/message")
    suspend fun sendMessage(
        @Path("conversation_id") conversationId: String,
        @Body request: MessageRequest
    ): MessageResponse

    /**
     * Get conversation details and history
     * GET /api/v1/mistral/conversation/{conversation_id}
     */
    @GET("api/v1/mistral/conversation/{conversation_id}")
    suspend fun getConversation(
        @Path("conversation_id") conversationId: String
    ): ConversationResponse

    /**
     * Health check for Mistral AI service
     * GET /api/v1/mistral/health
     */
    @GET("api/v1/mistral/health")
    suspend fun healthCheck(): Map<String, Any>
}

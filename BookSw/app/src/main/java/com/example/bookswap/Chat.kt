package com.example.bookswap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val id: Long? = null,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    @SerialName("book_id")
    val bookId: Long,
    val type: String, // "swap" or "rent"
    val status: String = "pending", // "pending", "accepted", "rejected"
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class Message(
    val id: Long? = null,
    @SerialName("chat_request_id")
    val chatRequestId: Long,
    @SerialName("sender_id")
    val senderId: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

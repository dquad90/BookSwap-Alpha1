package com.example.bookswap

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val postgrest = supabase.postgrest
    private val auth = supabase.auth

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _chatRequests = mutableStateListOf<ChatRequest>()
    val chatRequests: List<ChatRequest> = _chatRequests

    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    fun sendChatRequest(receiverId: String, bookId: Long, type: String, onSuccess: () -> Unit) {
        val currentUser = auth.currentUserOrNull() ?: return
        
        _loading.value = true
        viewModelScope.launch {
            try {
                val request = ChatRequest(
                    senderId = currentUser.id,
                    receiverId = receiverId,
                    bookId = bookId,
                    type = type,
                    status = "pending"
                )
                postgrest["chat_requests"].insert(request)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to send request: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchChatRequests() {
        val currentUser = auth.currentUserOrNull() ?: return
        _loading.value = true
        viewModelScope.launch {
            try {
                val userId = currentUser.id
                // Fetch requests where user is either sender or receiver
                val sent = postgrest["chat_requests"].select {
                    filter {
                        ChatRequest::senderId eq userId
                    }
                }.decodeList<ChatRequest>()
                
                val received = postgrest["chat_requests"].select {
                    filter {
                        ChatRequest::receiverId eq userId
                    }
                }.decodeList<ChatRequest>()
                
                _chatRequests.clear()
                _chatRequests.addAll(sent + received)
            } catch (e: Exception) {
                _error.value = "Failed to fetch chats: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateRequestStatus(requestId: Long, status: String) {
        viewModelScope.launch {
            try {
                postgrest["chat_requests"].update({
                    ChatRequest::status setTo status
                }) {
                    filter {
                        ChatRequest::id eq requestId
                    }
                }
                fetchChatRequests()
            } catch (e: Exception) {
                _error.value = "Failed to update status: ${e.message}"
            }
        }
    }

    fun sendMessage(chatRequestId: Long, content: String) {
        val currentUser = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val message = Message(
                    chatRequestId = chatRequestId,
                    senderId = currentUser.id,
                    content = content
                )
                postgrest["messages"].insert(message)
                fetchMessages(chatRequestId)
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }

    fun fetchMessages(chatRequestId: Long) {
        viewModelScope.launch {
            try {
                val results = postgrest["messages"].select {
                    filter {
                        Message::chatRequestId eq chatRequestId
                    }
                    order("created_at", Order.ASCENDING)
                }.decodeList<Message>()
                _messages.clear()
                _messages.addAll(results)
            } catch (e: Exception) {
                _error.value = "Failed to fetch messages: ${e.message}"
            }
        }
    }
}

package com.example.bookswap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel,
    currentUserId: String,
    onChatClick: (Long) -> Unit,
    onProfileClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val chatRequests = viewModel.chatRequests

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (chatRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No messages yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatRequests) { request ->
                    val isReceiver = request.receiverId == currentUserId
                    val otherPartyName = if (isReceiver) request.senderName else request.receiverName
                    val otherPartyUsername = if (isReceiver) request.senderUsername else request.receiverUsername
                    val otherPartyId = if (isReceiver) request.senderId else request.receiverId
                    val label = if (isReceiver) "From: " else "To: "

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { 
                            if (request.status == "accepted" || !isReceiver) {
                                request.id?.let { onChatClick(it) }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD))
                                    .clickable { otherPartyId?.let { onProfileClick(it) } },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1976D2))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(label, fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = otherPartyName ?: "User",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { otherPartyId?.let { onProfileClick(it) } }
                                    )
                                    if (otherPartyUsername != null) {
                                        Text(
                                            text = " @$otherPartyUsername",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 4.dp).clickable { otherPartyId?.let { onProfileClick(it) } }
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(request.bookTitle ?: "Book", fontSize = 13.sp, color = Color.DarkGray)
                                }
                                Text(
                                    text = "Status: ${request.status.replaceFirstChar { it.uppercase() }}",
                                    fontSize = 12.sp,
                                    color = if (request.status == "accepted") Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            if (isReceiver && request.status == "pending") {
                                Row {
                                    IconButton(onClick = { request.id?.let { viewModel.updateRequestStatus(it, "accepted") } }) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Accept", tint = Color(0xFF4CAF50))
                                    }
                                    IconButton(onClick = { request.id?.let { viewModel.updateRequestStatus(it, "rejected") } }) {
                                        Icon(Icons.Default.Cancel, contentDescription = "Reject", tint = Color(0xFFF44336))
                                    }
                                }
                            } else {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessagesScreen(
    viewModel: ChatViewModel,
    requestId: Long,
    currentUserId: String,
    onProfileClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages = viewModel.messages
    val request = viewModel.chatRequests.find { it.id == requestId }

    val otherPartyName = remember(request) {
        if (request?.receiverId == currentUserId) request.senderName else request?.receiverName
    }
    val otherPartyUsername = remember(request) {
        if (request?.receiverId == currentUserId) request.senderUsername else request?.receiverUsername
    }
    val otherPartyId = remember(request) {
        if (request?.receiverId == currentUserId) request.senderId else request?.receiverId
    }

    LaunchedEffect(requestId) {
        viewModel.fetchMessages(requestId)
        viewModel.startListeningToMessages(requestId)
    }

    DisposableEffect(requestId) {
        onDispose {
            viewModel.stopListeningToMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(
                        modifier = Modifier
                            .clickable { otherPartyId?.let { onProfileClick(it) } }
                            .padding(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(otherPartyName ?: "Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (otherPartyUsername != null) {
                                Text(
                                    text = " @$otherPartyUsername",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                        request?.bookTitle?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(requestId, messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            reverseLayout = false,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val isMe = message.senderId == currentUserId
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isMe) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        )
                    ) {
                        Text(
                            text = message.content,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (isMe) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}

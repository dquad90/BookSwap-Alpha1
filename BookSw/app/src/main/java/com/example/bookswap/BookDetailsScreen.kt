package com.example.bookswap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookswap.ui.theme.CyanMain
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookId: Long, // Use ID instead of the object for better reactivity
    currentUserId: String,
    chatViewModel: ChatViewModel,
    bookViewModel: BookViewModel,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onRequestSent: () -> Unit
) {
    // Reactive lookup: always get the latest version of this book from the list
    val book = bookViewModel.books.find { it.id == bookId } ?: return
    
    val loading by chatViewModel.loading
    val bookLoading by bookViewModel.loading
    val error by bookViewModel.error
    val windowSize = rememberWindowSize()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val isOwner = book.ownerId == currentUserId
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = if (isOwner) listOf("Overview", "Details", "Edit Info") else listOf("Overview", "Details")

    // Error Dialog
    if (error != null) {
        AlertDialog(
            onDismissRequest = { bookViewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Database Issue", fontWeight = FontWeight.Bold) },
            text = { Text(error ?: "An unexpected error occurred.") },
            confirmButton = {
                TextButton(onClick = { bookViewModel.clearError() }) {
                    Text("OK", color = CyanMain, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!bookLoading) showDeleteDialog = false },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete this book? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        book.id?.let { id ->
                            bookViewModel.deleteBook(id) {} // Optimistic delete happens inside
                            showDeleteDialog = false
                            onBack() // Go back INSTANTLY
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (isOwner) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Availability Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = book.isAvailable,
                                onCheckedChange = { 
                                    book.id?.let { id -> bookViewModel.updateBookAvailability(id, it) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanMain)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (book.isAvailable) "Available" else "Busy",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Edit Button
                        Button(
                            onClick = onEditClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Info")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 24.dp, vertical = 16.dp)
                ) {
                    if (book.isAvailable) {
                        Button(
                            onClick = { 
                                scope.launch {
                                    chatViewModel.sendChatRequest(
                                        receiverId = book.ownerId ?: "",
                                        bookId = book.id ?: 0,
                                        type = "swap",
                                        onSuccess = onRequestSent
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !loading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Swap Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    if (book.isForRent && book.rentalPricePerDay != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { 
                                scope.launch {
                                    chatViewModel.sendChatRequest(
                                        receiverId = book.ownerId ?: "",
                                        bookId = book.id ?: 0,
                                        type = "rent",
                                        onSuccess = onRequestSent
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !loading,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE57373))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Rent for $${book.rentalPricePerDay}/day",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE57373)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFE57373))
                            }
                        }
                    }
                    
                    if (!book.isAvailable && !book.isForRent) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.LightGray.copy(alpha = 0.3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Book Not Available", color = Color.Gray, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 0.dp)
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (windowSize.heightSizeClass == WindowSizeClass.COMPACT) 300.dp else 420.dp)
                    .padding(16.dp)
            ) {
                // Book Cover
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    color = if (book.imageUrl == null) (if (book.id == 1L) Color(0xFFE57373) else Color(0xFFFFB74D)) else Color.Transparent
                ) {
                    if (book.imageUrl != null) {
                        AsyncImage(
                            model = book.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (!book.isAvailable) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                                Text("BUSY", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Top Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White)
                    }
                    
                    IconButton(
                        onClick = { if (isOwner) showDeleteDialog = true },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                    ) {
                        if (isOwner) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        } else {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark", tint = Color.White)
                        }
                    }
                }

                // Book Info Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = book.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4FC3F7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOwner) "You (Owner)" else book.owner,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Swaps", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            Text(
                                text = book.swaps.toString(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                // Tab Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Column(
                            modifier = Modifier
                                .padding(end = 24.dp)
                                .clickable { selectedTabIndex = index },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color.Black else Color.Gray
                            )
                            if (selectedTabIndex == index) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .width(20.dp)
                                        .height(3.dp)
                                        .background(CyanMain, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tab Content
                when (selectedTabIndex) {
                    0 -> { // Overview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoChip(
                                icon = if (book.isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                text = if (book.isAvailable) "Available" else "Busy",
                                bgColor = if (book.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            )
                            InfoChip(
                                icon = Icons.Default.Sell,
                                text = if (book.isForRent) "Rentable" else "Swap Only",
                                bgColor = if (book.isForRent) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                            )
                            InfoChip(Icons.Default.Star, book.rating.toString(), Color(0xFFFFF8E1))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = book.description,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 22.sp
                        )
                    }
                    1 -> { // Details
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailRow(label = "Category", value = book.category)
                            DetailRow(label = "Rental Price", value = if (book.isForRent) "$${book.rentalPricePerDay}/day" else "Not for rent")
                            DetailRow(label = "Book ID", value = "#${book.id}")
                        }
                    }
                    2 -> { // Edit Info Tab (Owner only)
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Management Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            
                            Button(
                                onClick = onEditClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Modify Listing Details")
                            }

                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete This Book")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        }
    }
}

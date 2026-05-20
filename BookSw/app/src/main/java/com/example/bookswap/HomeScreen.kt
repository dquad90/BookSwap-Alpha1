package com.example.bookswap

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    userPhotoUrl: String?,
    onBookClick: (Book) -> Unit,
    onExploreClick: () -> Unit,
    onAddClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: BookViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Fiction", "Science", "Business", "History", "Arts")
    var selectedCategory by remember { mutableStateOf("All") }
    val windowSize = rememberWindowSize()
    
    val books = viewModel.books
    val favoriteBookIds = viewModel.favorites

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onExploreClick,
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onAddClick,
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(32.dp)) },
                    label = { Text("Add") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onChatClick,
                    icon = { Icon(Icons.Default.Message, contentDescription = null) },
                    label = { Text("Chats") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { paddingValues ->
        val filteredBooks = books.filter { book ->
            val matchesCategory = selectedCategory == "All" || book.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() || 
                book.title.contains(searchQuery, ignoreCase = true) || 
                book.description.contains(searchQuery, ignoreCase = true) ||
                book.owner.contains(searchQuery, ignoreCase = true)
            
            matchesCategory && matchesSearch
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 48.dp else 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Hello there!", color = Color.Gray, fontSize = 14.sp)
                    Text(text = "$userName!", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFE3F2FD),
                    onClick = onProfileClick
                ) {
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = Color(0xFF1976D2))
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                placeholder = { Text("Search for books, authors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE3F2FD),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Categories
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (searchQuery.isEmpty()) "Featured Books" else "Search Results",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredBooks.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text("No books found", color = Color.Gray)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(filteredBooks) { book ->
                        BookCard(
                            book = book,
                            isFavorite = favoriteBookIds.contains(book.id),
                            onFavoriteToggle = { book.id?.let { viewModel.toggleFavorite(it) } },
                            onClick = { onBookClick(book) },
                            backgroundColor = if (book.id == 1L) Color(0xFFFFE5E5) else Color(0xFFFFF3E0),
                            windowSize = windowSize
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (searchQuery.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Recently Added", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onExploreClick) { Text("See All") }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    books.take(3).forEach { book ->
                        RecentBookRow(book = book, onClick = { onBookClick(book) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

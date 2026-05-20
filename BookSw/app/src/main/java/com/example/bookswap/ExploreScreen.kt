package com.example.bookswap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: BookViewModel,
    onBookClick: (Book) -> Unit,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Fiction", "Science", "Business", "History", "Arts")
    var selectedCategory by remember { mutableStateOf("All") }
    var availableOnly by remember { mutableStateOf(false) }

    val books = viewModel.books
    val favoriteBookIds = viewModel.favorites
    val windowSize = rememberWindowSize()

    val filteredBooks = books.filter { book ->
        val matchesSearch = searchQuery.isBlank() || 
            book.title.contains(searchQuery, ignoreCase = true) || 
            book.author.contains(searchQuery, ignoreCase = true) ||
            book.location.contains(searchQuery, ignoreCase = true)
        
        val matchesCategory = selectedCategory == "All" || book.category == selectedCategory
        val matchesAvailability = !availableOnly || book.isAvailable
        
        matchesSearch && matchesCategory && matchesAvailability
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            // Header with Count
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explore",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                if (filteredBooks.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "${filteredBooks.size} books",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                placeholder = { Text("Search title, author, or city...") },
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

            // Category Selection
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
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

            // Filters Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Available only", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = availableOnly,
                    onCheckedChange = { availableOnly = it },
                    modifier = Modifier.scale(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredBooks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No results found", color = Color.Gray, fontWeight = FontWeight.Medium)
                        TextButton(onClick = { 
                            searchQuery = ""
                            selectedCategory = "All"
                            availableOnly = false
                        }) {
                            Text("Clear all filters")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 4 else 2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredBooks) { book ->
                        BookCard(
                            book = book,
                            isFavorite = favoriteBookIds.contains(book.id),
                            onFavoriteToggle = { book.id?.let { viewModel.toggleFavorite(it) } },
                            onClick = { onBookClick(book) },
                            backgroundColor = Color.White,
                            windowSize = windowSize,
                            width = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 150.dp else 220.dp,
                            height = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 230.dp else 320.dp
                        )
                    }
                }
            }
        }
    }
}

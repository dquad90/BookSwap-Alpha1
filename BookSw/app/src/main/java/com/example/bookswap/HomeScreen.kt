package com.example.bookswap

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    userPhotoUrl: String?,
    onBookClick: (Book) -> Unit,
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
                    Text(
                        text = "Hello there!",
                        color = Color.Gray,
                        fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 14.sp else 18.sp
                    )
                    Text(
                        text = "$userName!",
                        color = Color.Black,
                        fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 24.sp else 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    modifier = Modifier.size(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 48.dp else 64.dp),
                    shape = CircleShape,
                    color = Color(0xFFE3F2FD),
                    onClick = onProfileClick
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color(0xFF1976D2)
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 24.dp),
                placeholder = { Text("Search for books, authors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    } else {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.FilterList, contentDescription = null)
                        }
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
                contentPadding = PaddingValues(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1A1A1A),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Featured Books
            val titleText = if (searchQuery.isEmpty()) "Featured Books" else "Search Results"
            Text(
                text = titleText,
                fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 20.sp else 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No books found matching your criteria", color = Color.Gray)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(filteredBooks) { book ->
                        BookCard(
                            book = book,
                            isFavorite = favoriteBookIds.contains(book.id),
                            onFavoriteToggle = {
                                book.id?.let { viewModel.toggleFavorite(it) }
                            },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Added",
                        fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 20.sp else 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { }) {
                        Text("See All", color = Color(0xFF1976D2))
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) 120.dp else 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Take the first 3 items (since we sort by DESC id, these are the newest)
                    books.take(3).forEach { book ->
                        RecentBookRow(book = book, onClick = { onBookClick(book) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RecentBookRow(book: Book, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                if (book.imageUrl != null) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1.0f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(book.owner, color = Color.Gray, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
                    Text(book.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${book.swaps} swaps", color = Color.Gray, fontSize = 12.sp)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun BookCard(
    book: Book, 
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onClick: () -> Unit, 
    backgroundColor: Color, 
    windowSize: WindowSize = WindowSize(WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM, 360.dp, 800.dp)
) {
    Card(
        modifier = Modifier
            .width(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 220.dp else 300.dp)
            .height(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 320.dp else 420.dp)
            .clickable { onClick() }
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Book Cover
            if (book.imageUrl != null) {
                AsyncImage(
                    model = book.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (book.id == 1L) Color(0xFFE57373) else Color(0xFFFFB74D))
                )
            }

            // Favorite Icon
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .size(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 36.dp else 48.dp)
                    .align(Alignment.TopEnd)
                    .clickable { onFavoriteToggle() },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.padding(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 8.dp else 12.dp)
                )
            }

            // Bottom Info Card
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = book.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 16.sp else 20.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF4FC3F7),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = book.owner,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = book.rating.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.bookswap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    userName: String,
    profile: Profile?,
    booksCount: Int,
    swapsCount: Int,
    favoritesCount: Int,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAddBookClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val windowSize = rememberWindowSize()
    var showPersonalInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding() // Boundary adjustment inside header
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Logout", tint = Color.Red)
                    }
                }

                // Profile Image
                Box(
                    modifier = Modifier
                        .size(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 100.dp else 140.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 60.dp else 80.dp),
                        tint = Color(0xFF1976D2)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    fontSize = if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 24.sp else 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Book Enthusiast",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // Stats Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                count = swapsCount.toString(),
                label = "Swaps",
                icon = Icons.Default.SwapHoriz,
                color = Color(0xFFE3F2FD),
                contentColor = Color(0xFF1976D2)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                count = booksCount.toString(),
                label = "Books",
                icon = Icons.Default.MenuBook,
                color = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                count = favoritesCount.toString(),
                label = "Favorites",
                icon = Icons.Default.Favorite,
                color = Color(0xFFFFEBEE),
                contentColor = Color(0xFFD32F2F)
            )
        }

        // Menu Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Menu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Personal Information Item
            ProfileActionItem(
                icon = Icons.Default.Badge,
                title = "Personal Information",
                subtitle = if (showPersonalInfo) "Hide details" else "View your contact details",
                onClick = { showPersonalInfo = !showPersonalInfo },
                containerColor = Color(0xFFE1F5FE),
                iconColor = Color(0xFF0288D1),
                trailingIcon = if (showPersonalInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
            )

            AnimatedVisibility(
                visible = showPersonalInfo,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoItem(icon = Icons.Default.Email, label = "Email", value = profile?.email ?: "N/A")
                    InfoItem(icon = Icons.Default.Phone, label = "Phone", value = profile?.phone ?: "N/A")
                    InfoItem(icon = Icons.Default.Home, label = "Address", value = profile?.address ?: "N/A")
                }
            }

            ProfileActionItem(
                icon = Icons.Default.Add,
                title = "List a New Book",
                subtitle = "Share your books with others",
                onClick = onAddBookClick,
                containerColor = Color(0xFFF3E5F5),
                iconColor = Color(0xFF7B1FA2)
            )

            ProfileActionItem(
                icon = Icons.Default.History,
                title = "Swap History",
                subtitle = "Track your previous transactions",
                onClick = { },
                containerColor = Color(0xFFE0F2F1),
                iconColor = Color(0xFF00796B)
            )

            ProfileActionItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Configure your alerts",
                onClick = { },
                containerColor = Color(0xFFFFF3E0),
                iconColor = Color(0xFFE65100)
            )
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontSize = 12.sp, color = Color.Gray)
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    count: String,
    label: String,
    icon: ImageVector,
    color: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    containerColor: Color,
    iconColor: Color,
    trailingIcon: ImageVector = Icons.Default.ChevronRight
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(trailingIcon, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        userName = "John Doe",
        profile = Profile(fullName = "John Doe", email = "john@example.com", phone = "+1 234 567 890", address = "123 Book St, Library City"),
        booksCount = 5,
        swapsCount = 12,
        favoritesCount = 8,
        onBack = {},
        onLogout = {},
        onAddBookClick = {}
    )
}

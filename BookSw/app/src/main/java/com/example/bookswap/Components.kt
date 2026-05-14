package com.example.bookswap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookswap.ui.theme.CyanMain
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun RatingChip(
    rating: Double,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF8E1),
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFFFD54F)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.1f", rating),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                maxLines = 1
            )
        }
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
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun RatingSliderDialog(
    currentRating: Double,
    onDismiss: () -> Unit,
    onSubmit: (Double) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(currentRating.toFloat()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this Book", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", sliderValue),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = CyanMain
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = (it * 10).roundToInt() / 10f },
                    valueRange = 0f..5f,
                    steps = 49,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanMain,
                        activeTrackColor = CyanMain
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0.0", color = Color.Gray, fontSize = 12.sp)
                    Text("5.0", color = Color.Gray, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(sliderValue.toDouble()) }) {
                Text("Submit", color = CyanMain, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BookCard(
    book: Book,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onClick: () -> Unit,
    backgroundColor: Color,
    windowSize: WindowSize = WindowSize(WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM, 360.dp, 800.dp),
    width: Dp? = null,
    height: Dp? = null
) {
    val cardWidth = width ?: if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 220.dp else 300.dp
    val cardHeight = height ?: if (windowSize.widthSizeClass == WindowSizeClass.COMPACT) 320.dp else 420.dp
    
    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .clickable { onClick() }
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (book.imageUrl != null) {
                AsyncImage(model = book.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxSize().background(backgroundColor))
            }

            Surface(
                modifier = Modifier.padding(12.dp).size(if (cardWidth < 200.dp) 32.dp else 36.dp).align(Alignment.TopEnd).clickable { onFavoriteToggle() },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.padding(if (cardWidth < 200.dp) 6.dp else 8.dp)
                )
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = book.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (cardWidth < 200.dp) 12.sp else 16.sp, maxLines = 1)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = book.owner, color = Color.White.copy(alpha = 0.8f), fontSize = if (cardWidth < 200.dp) 9.sp else 11.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(if (cardWidth < 200.dp) 10.dp else 14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = String.format(Locale.getDefault(), "%.1f", book.rating), color = Color.White, fontSize = if (cardWidth < 200.dp) 10.sp else 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentBookRow(book: Book, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                if (book.imageUrl != null) {
                    AsyncImage(model = book.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(book.owner, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = String.format(Locale.getDefault(), "%.1f", book.rating), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${book.swaps} swaps", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

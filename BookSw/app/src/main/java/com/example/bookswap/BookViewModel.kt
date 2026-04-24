package com.example.bookswap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

class BookViewModel : ViewModel() {
    private val postgrest = supabase.postgrest
    private val auth = supabase.auth
    private val storage = supabase.storage

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _books = mutableStateListOf<Book>()
    val books: List<Book> = _books

    fun fetchBooks() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val results = postgrest["books"]
                    .select() {
                        order("id", Order.DESCENDING)
                    }
                    .decodeList<Book>()
                _books.clear()
                _books.addAll(results)
            } catch (e: Exception) {
                _error.value = "Failed to fetch books: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addBook(
        title: String,
        description: String,
        category: String,
        isForRent: Boolean,
        rentalPrice: Double?,
        imageBytes: ByteArray?,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || description.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }

        val currentUser = auth.currentUserOrNull()
        if (currentUser == null) {
            _error.value = "User not logged in"
            return
        }

        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                var finalImageBytes = imageBytes
                
                // Ensure image is JPG and doesn't exceed 300KB
                if (finalImageBytes != null) {
                    var quality = 80
                    var bitmap = BitmapFactory.decodeByteArray(finalImageBytes, 0, finalImageBytes.size)
                    
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    finalImageBytes = outputStream.toByteArray()
                    
                    // Further compress if still over 300KB
                    while (finalImageBytes!!.size > 300 * 1024 && quality > 10) {
                        quality -= 10
                        val loopStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, loopStream)
                        finalImageBytes = loopStream.toByteArray()
                    }
                    
                    if (finalImageBytes.size > 300 * 1024) {
                        // If still too large, resize it
                        val scale = 0.8f
                        val resizedBitmap = Bitmap.createScaledBitmap(
                            bitmap, 
                            (bitmap.width * scale).toInt(), 
                            (bitmap.height * scale).toInt(), 
                            true
                        )
                        val resizeStream = ByteArrayOutputStream()
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, resizeStream)
                        finalImageBytes = resizeStream.toByteArray()
                    }
                }

                var imageUrl: String? = null
                if (finalImageBytes != null) {
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val bucket = storage.from("book-images")
                    bucket.upload(fileName, finalImageBytes)
                    imageUrl = bucket.publicUrl(fileName)
                }

                val userName = currentUser.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "Unknown"
                
                val book = Book(
                    title = title,
                    description = description,
                    owner = userName,
                    ownerId = currentUser.id,
                    rating = 0.0,
                    swaps = 0,
                    isAvailable = true,
                    isForRent = isForRent,
                    rentalPricePerDay = if (isForRent) rentalPrice else null,
                    imageUrl = imageUrl,
                    category = category
                )

                postgrest["books"].insert(book)
                fetchBooks() // Refresh the list
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add book"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

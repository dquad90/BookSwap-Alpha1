package com.example.bookswap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: Long? = null,
    val title: String,
    val owner: String,
    @SerialName("owner_id")
    val ownerId: String? = null,
    val rating: Double = 0.0,
    val swaps: Int = 0,
    val description: String,
    @SerialName("is_available")
    val isAvailable: Boolean = true,
    @SerialName("is_for_rent")
    val isForRent: Boolean = false,
    @SerialName("rental_price_per_day")
    val rentalPricePerDay: Double? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val category: String = "Fiction",
    @SerialName("created_at")
    val createdAt: String? = null // Added to track when the book was published
)

@Serializable
data class Favorite(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("book_id")
    val bookId: Long
)

val sampleBooks = listOf(
    Book(
        id = 1,
        title = "The Alchemist",
        owner = "John Doe",
        rating = 4.8,
        swaps = 12,
        description = "A global phenomenon...",
        isAvailable = true,
        isForRent = true,
        rentalPricePerDay = 2.5,
        category = "Fiction"
    ),
    Book(
        id = 2,
        title = "Atomic Habits",
        owner = "Jane Smith",
        rating = 4.9,
        swaps = 8,
        description = "No matter your goals...",
        isAvailable = false,
        isForRent = false,
        category = "Business"
    )
)

package com.example.bookswap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: Long? = null,
    val title: String,
    val author: String = "Unknown", // Added for Explore
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
    val location: String = "Nearby", // Added for Explore
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class Favorite(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("book_id")
    val bookId: Long
)

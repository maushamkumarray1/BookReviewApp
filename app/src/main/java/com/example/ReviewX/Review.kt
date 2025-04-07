package com.example.myapplication
data class Review(
    var reviewId: String = "",
    val bookId: String = "",
    val userId: String = "",
    val reviewText: String = "",
    val rating: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

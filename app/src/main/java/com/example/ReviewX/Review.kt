package com.example.myapplication

data class Review(
    var id: String = "",
    var userId: String = "",
    var bookId: String = "",
    var reviewerName: String = "",
    var reviewText: String = "",
    var rating: Float = 0.0f,
    var timestamp: Long = System.currentTimeMillis()
)

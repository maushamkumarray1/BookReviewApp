package com.example.myapplication
data class Book(
    var id: String = "",
    var title: String = "",
    var author: String = "",
    var genre: String = "",
    var adminId: String = "",
    var latestReview: String = "No reviews yet",
    var averageRating: Float = 0f // ⭐ New field for storing average rating
)

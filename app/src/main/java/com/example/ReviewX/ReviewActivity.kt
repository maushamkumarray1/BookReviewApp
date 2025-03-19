package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var adapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var bookId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId") ?: return

        adapter = ReviewAdapter(reviewList,
            onEditClick = { review -> editReview(review) },
            onDeleteClick = { review -> deleteReview(review) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadReviews()

        binding.btnSubmitReview.setOnClickListener {
            submitReview()
        }
    }

    private fun loadReviews() {
        db.collection("books").document(bookId)
            .collection("reviews").get()
            .addOnSuccessListener { snapshot ->
                reviewList.clear()
                for (doc in snapshot.documents) {
                    val review = doc.toObject(Review::class.java)
                    review?.let {
                        it.id = doc.id
                        reviewList.add(it)
                    }
                }
                adapter.updateList(reviewList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editReview(review: Review) {
        binding.etReview.setText(review.reviewText)
        binding.ratingBar.rating = review.rating

        binding.btnSubmitReview.setOnClickListener {
            val updatedReviewText = binding.etReview.text.toString().trim()
            val updatedRating = binding.ratingBar.rating

            if (updatedReviewText.isEmpty() || updatedRating == 0f) {
                Toast.makeText(this, "Please enter a review and rating", Toast.LENGTH_SHORT).show()

            }

            db.collection("books").document(bookId)
                .collection("reviews").document(review.id)
                .update("reviewText", updatedReviewText, "rating", updatedRating)
                .addOnSuccessListener {
                    Toast.makeText(this, "Review updated!", Toast.LENGTH_SHORT).show()
                    loadReviews()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update review", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteReview(review: Review) {
        db.collection("books").document(bookId)
            .collection("reviews").document(review.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Review deleted!", Toast.LENGTH_SHORT).show()
                loadReviews()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show()
            }
    }
    private fun submitReview() {
        val reviewText = binding.etReview.text.toString().trim()
        val rating = binding.ratingBar.rating

        if (reviewText.isEmpty() || rating == 0f) {
            Toast.makeText(this, "Please enter a review and rating", Toast.LENGTH_SHORT).show()
            return
        }

        val reviewRef = db.collection("books").document(bookId)
            .collection("reviews").document()

        val review = hashMapOf(
            "id" to reviewRef.id,
            "userId" to userId,
            "bookId" to bookId,
            "reviewText" to reviewText,
            "rating" to rating
        )

        reviewRef.set(review)
            .addOnSuccessListener {
                Toast.makeText(this, "Review added successfully!", Toast.LENGTH_SHORT).show()
                binding.etReview.text.clear()
                binding.ratingBar.rating = 0f
                loadReviews()  // Refresh the reviews
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add review: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()  // Print error details in Logcat
            }
    }



}

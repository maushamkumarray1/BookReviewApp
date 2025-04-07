package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar


class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var adapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var bookId: String
    private var isEditing = false
    private var editingReviewId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve bookId from intent
        bookId = intent.getStringExtra("BOOK_ID") ?: ""

        if (bookId.isEmpty()) {
            Toast.makeText(this, "Error: Book ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupListeners()
        loadReviews()
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(
            reviewList,
            userId,
            onEditClick = { review -> prepareEditReview(review) },
            onDeleteClick = { review -> deleteReview(review) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun deleteReview(review: Review) {
        db.collection("books").document(bookId)
            .collection("reviews").document(review.reviewId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Review deleted successfully!", Toast.LENGTH_SHORT).show()
                loadReviews()  // Refresh reviews after deletion
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete review: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun setupListeners() {
        binding.btnSubmitReview.setOnClickListener {
            if (isEditing) {
                updateReview()
            } else {
                submitReview()
            }
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            Log.d("ReviewActivity", "Rating selected: $rating")
        }
    }

    private fun loadReviews() {
        if (bookId.isBlank()) {
            Log.e("ReviewActivity", "Book ID is missing, cannot load reviews")
            return
        }

        val reviewsRef = db.collection("books").document(bookId).collection("reviews")

        // ✅ Real-time listener to update reviews instantly
        reviewsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FirestoreDebug", "Failed to fetch reviews: ${e.message}", e)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                Log.w("ReviewActivity", "No reviews found for bookId: $bookId")
                adapter.updateList(mutableListOf())
            } else {
                val reviews = snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
                adapter.updateList(reviews.toMutableList())
            }
        }
    }

    private fun submitReview() {
        val reviewText = binding.etReview.text.toString().trim()
        val rating = binding.ratingBar.rating

        if (reviewText.isEmpty() || rating == 0f) {
            Toast.makeText(this, "Please enter a review and rating", Toast.LENGTH_SHORT).show()
            return
        }

        val reviewRef = db.collection("books").document(bookId).collection("reviews").document()

        val review = hashMapOf(
            "reviewId" to reviewRef.id,
            "userId" to userId,
            "bookId" to bookId,
            "reviewText" to reviewText,
            "rating" to rating,
            "timestamp" to System.currentTimeMillis()
        )

        reviewRef.set(review)
            .addOnSuccessListener {
                // ✅ Show success message
                Toast.makeText(this, "Review added successfully!", Toast.LENGTH_SHORT).show()

                // ✅ Display Snackbar confirmation
                Snackbar.make(binding.root, "Review is added!", Snackbar.LENGTH_LONG).show()

                // ✅ Clear input fields
                resetInputFields()

                // ✅ Refresh the review list
                loadReviews()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, " Failed to add review: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ReviewActivity", "Error adding review", e)
            }
    }


    private fun prepareEditReview(review: Review) {
        binding.etReview.setText(review.reviewText)
        binding.ratingBar.rating = review.rating
        isEditing = true
        editingReviewId = review.reviewId
        binding.btnSubmitReview.text = "Update Review"
    }

    private fun updateReview() {
        val updatedReviewText = binding.etReview.text.toString().trim()
        val updatedRating = binding.ratingBar.rating

        if (updatedReviewText.isEmpty() || updatedRating == 0f) {
            Toast.makeText(this, "Please enter a review and rating", Toast.LENGTH_SHORT).show()
            return
        }

        val reviewId = editingReviewId ?: return

        db.collection("books").document(bookId)
            .collection("reviews").document(reviewId)
            .update("reviewText", updatedReviewText, "rating", updatedRating)
            .addOnSuccessListener {
                Toast.makeText(this, "Review updated!", Toast.LENGTH_SHORT).show()
                resetInputFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update review", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetInputFields() {
        binding.etReview.text.clear()
        binding.ratingBar.rating = 0f
        binding.btnSubmitReview.text = "Submit Review"
        isEditing = false
        editingReviewId = null
    }
}

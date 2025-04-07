package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddBookBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveBook.setOnClickListener {
            addBookToFirestore()
        }
    }

    private fun addBookToFirestore() {
        val title = binding.etBookTitle.text.toString().trim()
        val author = binding.etBookAuthor.text.toString().trim()
        val genre = binding.etBookGenre.text.toString().trim()
        val description = binding.etBookDescription.text.toString().trim()

        // Validation: Ensure required fields are filled
        if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Title, Author, and Genre are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val bookId = UUID.randomUUID().toString()
        val bookData = mapOf(
            "bookId" to bookId,
            "title" to title,
            "author" to author,
            "genre" to genre,
            "description" to description,
            "adminId" to currentUser.uid // Store uploader's ID
        )

        firestore.collection("books").document(bookId)
            .set(bookData)
            .addOnSuccessListener {
                Toast.makeText(this, "Book added successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close activity after success
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add book: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditBookActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etGenre: EditText
    private lateinit var btnUpdate: Button

    private var bookId: String? = null
    private var bookAdminId: String? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)

        etTitle = findViewById(R.id.etTitle)
        etAuthor = findViewById(R.id.etAuthor)
        etGenre = findViewById(R.id.etGenre)
        btnUpdate = findViewById(R.id.btnUpdate)

        bookId = intent.getStringExtra("bookId")
        etTitle.setText(intent.getStringExtra("title"))
        etAuthor.setText(intent.getStringExtra("author"))
        etGenre.setText(intent.getStringExtra("genre"))

        bookId?.let { id ->
            db.collection("books").document(id).get().addOnSuccessListener { document ->
                bookAdminId = document.getString("adminId")
            }
        }

        btnUpdate.setOnClickListener {
            updateBook()
        }
    }

    private fun updateBook() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val genre = etGenre.text.toString().trim()

        if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (bookAdminId == userId) {
            bookId?.let { id ->
                val bookData = mapOf(
                    "title" to title,
                    "author" to author,
                    "genre" to genre
                )

                db.collection("books").document(id)
                    .update(bookData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Book updated successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Close activity after update
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Only the book admin can update this book.", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivitySearchBooksBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBooksBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var bookList = mutableListOf<Book>()
    private lateinit var adapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize adapter with edit and delete functionality
        adapter = BookAdapter(bookList,
            onEditClick = { book -> editBook(book) },
            onDeleteClick = { book -> deleteBook(book) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Load all books initially
        loadBooks()

        // Search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchBooks(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadBooks() {
        firestore.collection("books").get()
            .addOnSuccessListener { result ->
                bookList.clear()
                for (document in result) {
                    val book = document.toObject(Book::class.java).apply {
                        id = document.id // Set the document ID as book ID
                    }
                    bookList.add(book)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this@SearchBooksActivity, "Failed to load books!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchBooks(query: String) {
        val filteredList = bookList.filter {
            it.title.contains(query, true) || it.author.contains(query, true) || it.genre.contains(query, true)
        }.toMutableList()

        adapter.updateBooks(filteredList) // Update adapter with filtered list
    }

    private fun editBook(book: Book) {
        val intent = Intent(this, EditBookActivity::class.java).apply {
            putExtra("bookId", book.id) // Ensure 'book.id' is not null
            putExtra("title", book.title)
            putExtra("author", book.author)
            putExtra("genre", book.genre)
        }
        startActivity(intent)
    }

    private fun deleteBook(book: Book) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Ensure only the admin (book uploader) can delete the book
        if (book.adminId == userId) {
            firestore.collection("books").document(book.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Book deleted!", Toast.LENGTH_SHORT).show()
                    loadBooks() // Refresh list
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete book!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "You can only delete books you uploaded!", Toast.LENGTH_SHORT).show()
        }
    }
}

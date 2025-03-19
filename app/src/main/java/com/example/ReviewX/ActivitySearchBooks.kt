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
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class SearchBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBooksBinding
    private val database = FirebaseDatabase.getInstance().getReference("books")
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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookList.clear()
                for (bookSnapshot in snapshot.children) {
                    val book = bookSnapshot.getValue(Book::class.java)
                    book?.let {
                        it.id = bookSnapshot.key ?: ""  // Ensure book has an ID
                        bookList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SearchBooksActivity, "Failed to load books!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchBooks(query: String) {
        val filteredList = bookList.filter {
            it.title.contains(query, true) || it.author.contains(query, true) || it.genre.contains(query, true)
        }.toMutableList()

        adapter = BookAdapter(filteredList,
            onEditClick = { book -> editBook(book) },
            onDeleteClick = { book -> deleteBook(book) }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun editBook(book: Book) {
        val intent = Intent(this, EditBookActivity::class.java).apply {
            putExtra("bookId", book.id) // Make sure 'book.id' is not null
            putExtra("title", book.title)
            putExtra("author", book.author)
            putExtra("genre", book.genre)
        }
        startActivity(intent)
    }


    private fun deleteBook(book: Book) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("books").document(book.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Book deleted!", Toast.LENGTH_SHORT).show()
                loadBooks() // Refresh list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete book!", Toast.LENGTH_SHORT).show()
            }
    }
}
